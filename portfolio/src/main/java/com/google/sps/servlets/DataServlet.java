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

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime; 
import java.lang.Math;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.base.Predicates;
import com.google.common.base.Predicate;
import com.google.common.collect.Range;

/** 
  * Servlet that uploads and retrieves persistent comment data using datastore.
  */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  /** Represents a single comment */
  private static class Comment {
      String text;
      String name;
      long time;
      long id;

      Comment(String text, String name, long time, long id) {
        this.text = text;
        this.name = name;
        this.time = time;
        this.id = id;
      }

      /** Returns an entity representing this comment. */
      Entity toEntity() {
        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("text", text);
        commentEntity.setProperty("name", name);
        commentEntity.setProperty("time", time);
        return commentEntity;
      }

      /** Returns a comment representing this entity */
      static Comment fromEntity(Entity e) {
        return new Comment(
          (String) e.getProperty("text"),
          (String) e.getProperty("name"),
          (long) e.getProperty("time"), 
          e.getKey().getId());
      }
  }

  /** Extracts user comment from form and stores it via datastore. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userComment = request.getParameter("text-input").trim();
    String userName = request.getParameter("author").trim();
    if (Strings.isNullOrEmpty(userName)) {
      userName = "Anonymous";
    }
    long timestamp = System.currentTimeMillis();

    if (!Strings.isNullOrEmpty(userComment)) {    
      DatastoreService datastore = 
        DatastoreServiceFactory.getDatastoreService();
      datastore.put((new Comment(userComment, userName, timestamp, 0))
        .toEntity());
    }
    response.sendRedirect("/index.html");
  }

  /** 
    * Loads all user comments from datastore and returns JSON list of n 
    * comments, where n is the number of comments the user has requested. 
    */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PreparedQuery results = getAllComments();
    ArrayList<Comment> comments = new ArrayList<Comment>();

    int numberToDisplay = getNumberToDisplay(request); 
    String paginationInstruction = (String) request.getParameter("pageAction");
    String searchQuery = (String) request.getParameter("search");
    searchQuery = searchQuery.substring(1,searchQuery.length() - 1);
    int pageToken = Integer.parseInt(
      (String) request.getParameter("pageToken"));;

    comments = getFilteredComments(results, comments, searchQuery);
    Range<Integer> commentRange = getRangeOfCommentsToDisplay(
      paginationInstruction, numberToDisplay, comments.size(), pageToken);
    List<Comment> commentsToDisplay = 
      comments.subList(commentRange.lowerEndpoint(), 
      commentRange.upperEndpoint());
    int newPageToken = commentRange.lowerEndpoint();

    String json = convertToJson(commentsToDisplay, newPageToken); 
    response.setContentType("application/json;");
    response.getWriter().println(json);

    System.out.println(json);
  }

  /** Returns the range of comments that should be displayed on
    * the site, given the user's instructions (reflecting
    * whether to jump forward a page, jump backwards, or
    * stay on the same page of comments).
    * @param instruction Of the form ""next"", ""previous"", 
        or ""none"".
    * @param numberToDisplay Indicates the desired number of comments
        on the page.
    * @param totalNumberComments Indicates the number of comments currently
        in the database.
    * @param pageToken The current index of the first comment being displayed 
        on the page. 
    * @return A range from `startIndex` to `stopIndex + 1`, where
        startIndex is the index of the first comment that should be displayed
        and stopIndex is the index of the last comment that should be
        displayed. */
  private Range<Integer> getRangeOfCommentsToDisplay(String instruction, 
    int numberToDisplay, int totalNumberComments, int pageToken) {
      int startIndex;
      int stopIndex;

      if (instruction.equals("\"previous\"")) {
        startIndex = Math.max(0, pageToken - numberToDisplay);
      } else if (instruction.equals("\"next\"")) {
        if (pageToken + numberToDisplay >= totalNumberComments) {
          startIndex = pageToken;
        } else {
          startIndex = pageToken + numberToDisplay;
        }
      } else {
        startIndex = pageToken;
      }
      stopIndex = Math.min(totalNumberComments, startIndex + numberToDisplay);

      if (stopIndex <= startIndex) {
        startIndex = Math.max(0, stopIndex - numberToDisplay);
      }

      return Range.closed(startIndex, stopIndex);
  }

  /** 
    * Returns number of comments to display, based on user's 
    * selection. 
    * @throws IllegalArgumentException if value returned by 
        `request.getParameter("numberToDisplay")` cannot be converted to 
        an integer. 
    */
  private int getNumberToDisplay(HttpServletRequest request)
    throws IllegalArgumentException {
    String parameterString = (String) request.getParameter("numberToDisplay");
    int numberToDisplay;
    try {
      numberToDisplay = Integer.parseInt(parameterString);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Cannot convert to integer"); 
    }
    return numberToDisplay;
  }

  /** Returns all user comments stored in datastore. */
  private PreparedQuery getAllComments() {
    Query query = new Query("Comment")
      .addSort("time", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    return datastore.prepare(query);
  }

  /** 
    * Returns all user comments that contain `search` in either
    * the comment text or author name. 
    * @param results All available comment entities in the database.
    * @param comments An empty list to be filled. 
    * @param search A string to filter the comments by. 
    */
  private ArrayList<Comment> getFilteredComments(PreparedQuery results, 
    ArrayList<Comment> comments, String search) {
    results.asIterable().forEach(commentEntity -> {
      comments.add(Comment.fromEntity(commentEntity));
    });

    ArrayList<Comment> filteredComments = new ArrayList<Comment>();
    Iterables.filter(comments, c -> satisfiesSearch(c, search))
      .forEach(c -> filteredComments.add(c));
    return filteredComments;
  }

  /** 
    * Returns true if this comment contains the search string in the
    * comment text or author name. 
    */
  private boolean satisfiesSearch(Comment comment, String search) {
    if (Strings.isNullOrEmpty(search)) {
      return true;
    } else {
      return comment.text.contains(search) || comment.name.contains(search);
    }
  }

  /** Returns JSON string representation of `data`. */
  private String convertToJson(List<Comment> data, int pageToken) {
    Gson gson = new Gson(); 
    String json = gson.toJson(data);
    return "[{\"pageToken\" : "+pageToken+" }, "+json+"]";
  }
}
