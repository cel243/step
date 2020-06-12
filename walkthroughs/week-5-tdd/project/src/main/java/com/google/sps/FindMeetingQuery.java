// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import com.google.common.collect.Sets;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableSet;
import java.lang.Math;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

public final class FindMeetingQuery {
  /** 
    * Returns all possible time ranges in which the `requestedAttendees`
    * could meet for the requested duration without conflicting with the 
    * `events` that are already scheduled for these attendees. 
    */
  private Collection<TimeRange> getPossibleTimes(Collection<Event> events, 
    Set<String> requestedAttendees, MeetingRequest request) {
    List<TimeRange> sortedEvents = events.stream()
      .filter(event -> 
        !Sets.intersection(event.getAttendees(), requestedAttendees).isEmpty())
      .map(event -> event.getWhen())
      .sorted(TimeRange.ORDER_BY_START)
      .collect(Collectors.toList());

    List<TimeRange> validTimes = new ArrayList<TimeRange>();
    int start = TimeRange.START_OF_DAY;
    for (TimeRange event : sortedEvents) {
      if (event.start() - start >= request.getDuration()) {
        validTimes.add(TimeRange.fromStartEnd(start, event.start(), false));
      }
      start = Math.max(start, event.end());
    }
    if (TimeRange.END_OF_DAY - start >= request.getDuration()) {
      validTimes.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));
    }

    return validTimes;
  }

  /** 
    * Returns a collection of times that work for an optimal number of optional 
    * people, as well as all the mandatory people.  
    */
  private Collection<TimeRange> getOptimalSubsetTimes(
    Set<String> mandatoryAttendees, Set<String> optionalAttendees, 
    Collection<Event> events, MeetingRequest request) {
    
    Set<Set<String>> subsets = Sets.powerSet(optionalAttendees);
    Set<Set<String>> infeasibleGroups = new HashSet<Set<String>>();
    Collection<TimeRange> optimalTimes = new ArrayList<TimeRange>();

    for (int i = 1; i < optionalAttendees.size(); i++) {
      int currentSize = i;
      Set<Set<String>> subsetsToCheck = Sets.filter(subsets, subset -> 
        subset.size() == currentSize && !containsInfeasibleGroup(subset, infeasibleGroups));
      if (subsetsToCheck.isEmpty()) {
        // all subsets larger than this will be infeasible 
        break;
      }

      for (Set<String> subset : subsetsToCheck) {
        Collection<TimeRange> possibleTimes = 
          getPossibleTimes(events, Sets.union(mandatoryAttendees, subset), request);
        if (possibleTimes.isEmpty()) {
          // for any group of optional people containing this group, no meeting 
          // can be scheduled.
          infeasibleGroups.add(subset);
        } else {
          optimalTimes = possibleTimes;
        }
      }
    }

    if (optimalTimes.isEmpty() && !mandatoryAttendees.isEmpty()) {
      return getPossibleTimes(events, mandatoryAttendees, request);
    } else {
      return optimalTimes;
    }

  }

  /** 
    * Returns true if `set` contains a group of people that has already been 
    * determined to have no mutually open timeslots between the group members 
    * and the mandatory meeting participants. 
    */
  private boolean containsInfeasibleGroup(Set<String> set, 
    Set<Set<String>> infeasibleGroups) {
    for (Set<String> infeasibleGroup : infeasibleGroups) {
      if (set.containsAll(infeasibleGroup)) {
        return true;
      }
    }
    return false;
  }

  /** 
    * If possible, returns all possible time ranges in which the mandatory and 
    * optional attendees of the requested meeting could meet for the requested 
    * duration, without conflicting with any `events` that are already scheduled 
    * for these attendees.If no times are possible that work for all the attendees,
    * returns possible times such that all mandatory attendees and as many 
    * optional antendees as possible can attend the meeting at these times. 
    */
  public Collection<TimeRange> query(Collection<Event> events, 
    MeetingRequest request) {
    Set<String> mandatoryAttendees = request.getAttendees().stream()
      .collect(Collectors.toSet()); 
    Set<String> optionalAttendees = request.getOptionalAttendees().stream()
      .collect(Collectors.toSet());
    Set<String> allAttendees = Sets.union(mandatoryAttendees, optionalAttendees);
    
    Collection<TimeRange> timesIncludingAllAttendees = getPossibleTimes(
      events, allAttendees, request);
    if (timesIncludingAllAttendees.isEmpty()) {
      return getOptimalSubsetTimes(
          mandatoryAttendees, optionalAttendees, events,request);
    } else {
      return timesIncludingAllAttendees; 
    }
  }
}
