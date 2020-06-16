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
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;


public final class FindMeetingQuery {
  /** 
    * If possible, returns all possible time ranges in which the mandatory and 
    * optional attendees of the requested meeting could meet for the requested 
    * duration, without conflicting with any `events` that are already scheduled 
    * for these attendees. If no times are possible that work for all the attendees,
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

  /** 
    * Returns all possible time ranges in which the `requestedAttendees`
    * could meet for the requested duration without conflicting with the 
    * `events` that are already scheduled for these attendees. Returns
    * possible time ranges in order of start time. 
    */
  private List<TimeRange> getPossibleTimes(Collection<Event> events, 
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
    Map<String, List<TimeRange>> mapOfPeopleToTimes = mapPeopleToUnavailabeTImes(events);
    List<TimeRange> mandatoryAttendeeAvailability = 
      getPossibleTimes(events, mandatoryAttendees, request);
    Set<String> mostOptionalAttendeesCanAttend = new HashSet<String>();

    for (TimeRange availableTime : mandatoryAttendeeAvailability) {
      int start = availableTime.start();
      int end = start + (int) request.getDuration();
      while (end <= availableTime.end()) {
        Set<String> availableOptionalAttendees = getAvailableAttendees(
          mapOfPeopleToTimes, start, end, optionalAttendees);
        if (availableOptionalAttendees.size() > mostOptionalAttendeesCanAttend.size()) {
          mostOptionalAttendeesCanAttend = availableOptionalAttendees;
        }
        start += 1;
        end += 1;
      }
    }

    if (mandatoryAttendees.isEmpty() && mostOptionalAttendeesCanAttend.isEmpty()) {
      return new ArrayList();
    } else {
      return getPossibleTimes(
        events, Sets.union(mandatoryAttendees, mostOptionalAttendeesCanAttend), request);
    }
  }

  /** 
    * Returns the attendees that are available for the entirety of the time 
    * range from start to end. Mutates mapOfPeopleToTimes by removing times 
    * that finish before start. 
    */
  private Set<String> getAvailableAttendees(
    Map<String, List<TimeRange>> mapOfPeopletoTimes, int start, int end, 
    Set<String> attendees) {
    Set<String> availableAttendees = new HashSet<String>();
    attendees.stream().forEach(attendee -> {
      List<TimeRange> unavailableTimes = mapOfPeopletoTimes.get(attendee);
      while (!unavailableTimes.isEmpty() && 
        unavailableTimes.get(0).end() < start) {
        unavailableTimes.remove(0);
      }
      if (unavailableTimes.isEmpty() || unavailableTimes.get(0).start() >= end) {
        availableAttendees.add(attendee);
      }
    });
    return availableAttendees;
  }


  /**
   * A comparator for sorting events by their start time in ascending order.
   */
  private static final Comparator<Event> ORDER_EVENT_BY_START = new Comparator<Event>() {
    @Override
    public int compare(Event a, Event b) {
      return Long.compare(a.getWhen().start(), b.getWhen().start());
    }
  };

  /** 
    * Returns a map of people to time ranges of events they are scheduled to 
    * attend, where the events they are mapped to are ordered by event start 
    * time, ascending. 
    */
  private Map<String, List<TimeRange>> mapPeopleToUnavailabeTImes(Collection<Event> events) {
    Map<String, List<TimeRange>> peopleToEvents = new HashMap<String, List<TimeRange>>();
    events.stream().sorted(ORDER_EVENT_BY_START).forEachOrdered(event -> {
      event.getAttendees().stream().forEach(attendee -> {
        if (peopleToEvents.containsKey(attendee)) {
          peopleToEvents.get(attendee).add(event.getWhen());
        } else {
          List<TimeRange> eventsAttending = new LinkedList<TimeRange>();
          eventsAttending.add(event.getWhen());
          peopleToEvents.put(attendee, eventsAttending);
        }
      });
    });
    return peopleToEvents;
  }
}
