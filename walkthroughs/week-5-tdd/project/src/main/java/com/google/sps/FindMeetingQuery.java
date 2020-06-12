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
import java.util.ArrayList;
import java.util.List;

public final class FindMeetingQuery {
  /** 
    * Returns all possible time ranges in which the `requestedAttendees`
    * could meet for the requested duration without conflicting with the 
    * `events` that are already scheduled for these attendees. 
    */
  public Collection<TimeRange> getPossibleTimes(Collection<Event> events, 
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
    * If possible, returns all possible time ranges in which the mandatory and 
    * optional attendees of the requested meeting could meet for the requested 
    * duration, without conflicting with any `events` that are already scheduled 
    * for these attendees.If no times are possible that work for all the attendees,
    * returns the possible times for just the madatory attendees. 
    */
  public Collection<TimeRange> query(Collection<Event> events, 
    MeetingRequest request) {
    Set<String> mandatoryAttendees = request.getAttendees().stream()
      .collect(Collectors.toSet()); 
    Set<String> optionalAttendees = request.getOptionalAttendees().stream()
      .collect(Collectors.toSet());
    Set<String> allAttendees = Sets.union(mandatoryAttendees, optionalAttendees);
    
    Collection<TimeRange> timesIncludingOptionalAttendees = getPossibleTimes(
      events, allAttendees, request);
    if (timesIncludingOptionalAttendees.isEmpty() && !mandatoryAttendees.isEmpty()) {
      return getPossibleTimes(events, mandatoryAttendees, request);
    } else {
      return timesIncludingOptionalAttendees; 
    }
  }
}
