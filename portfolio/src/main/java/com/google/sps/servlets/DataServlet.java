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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.lang.Math;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.sps.data.EntityProperties;
import com.google.sps.data.RequestParameters;
import com.google.sps.servlets.AuthenticationServlet;
import java.util.stream.Collectors;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.util.HashMap;

/** 
  * Servlet that uploads and retrieves persistent comment data using datastore.
  */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  /** Represents a single comment */
  private static class Comment {
      String text;
      String username;
      long time;
      long id;
      String userId;
      String email;

      Comment(String text, String username, long time, long id, 
        String userId, String email) {
        this.text = text;
        this.username = username;
        this.time = time;
        this.id = id;
        this.userId = userId;
        this.email = email;
      }

      /** Returns an entity representing this comment. */
      Entity toEntity() {
        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty(EntityProperties.COMMENT_TEXT, text);
        commentEntity.setProperty(EntityProperties.COMMENT_TIMESTAMP, time);
        commentEntity.setProperty(EntityProperties.USER_ID_OF_AUTHOR, userId);
        commentEntity.setProperty(EntityProperties.USER_EMAIL, email);
        return commentEntity;
      }

      /** Returns a comment representing this entity */
      static Comment fromEntity(Entity e) {
        return new Comment(
          (String) e.getProperty(EntityProperties.COMMENT_TEXT),
          AuthenticationServlet.getUserName(
            (String) e.getProperty(EntityProperties.USER_ID_OF_AUTHOR)),
          (long) e.getProperty(EntityProperties.COMMENT_TIMESTAMP), 
          e.getKey().getId(),
          (String) e.getProperty(EntityProperties.USER_ID_OF_AUTHOR),
          (String) e.getProperty(EntityProperties.USER_EMAIL));
      }
  }

  /** Extracts user comment from form and stores it via datastore. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String userComment = request.getParameter(
      RequestParameters.INPUTTED_TEXT).trim();
    String userName = request.getParameter(
      RequestParameters.INPUTTED_AUTHOR_NAME).trim();
    if (Strings.isNullOrEmpty(userName)) {
      userName = "Anonymous";
    }
    long timestamp = System.currentTimeMillis();
    String userId = userService.getCurrentUser().getUserId();
    String email = userService.getCurrentUser().getEmail();
    AuthenticationServlet.updateUserName(userName, userId);

    if (!Strings.isNullOrEmpty(userComment)) {    
      DatastoreService datastore = 
        DatastoreServiceFactory.getDatastoreService();
      Key key = datastore.put(
        (new Comment(userComment, "", timestamp, 0, userId, email))
        .toEntity());
      try {
        Entity commentJustAdded = datastore.get(key);
        commentJustAdded.setProperty(EntityProperties.COMMENT_ID, key.getId());
        datastore.put(commentJustAdded);
      } catch (com.google.appengine.api.datastore.EntityNotFoundException e) {
        //we have just added this entity
      }
    }
    response.sendRedirect("/index.html");
  }

  /** 
    * Loads all user comments from datastore and returns JSON list of n 
    * comments, where n is the number of comments the user has requested, 
    * filtered by any search query the user may have entered.
    */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    String currentUserId;
    if (!userService.isUserLoggedIn()) {
      currentUserId = "";
    } else if (userService.isUserAdmin()) {
      currentUserId = "ADMIN";
    } else {
      currentUserId = userService.getCurrentUser().getUserId();
    }

    int numberToDisplay = getNumberToDisplay(request); 
    String paginationInstruction = (String) request.getParameter(
      RequestParameters.PAGE_ACTION);
    String searchQuery = (String) request.getParameter(
      RequestParameters.SEARCH_QUERY);
    searchQuery = searchQuery.substring(1,searchQuery.length() - 1);
    int pageToken = Integer.parseInt(
      (String) request.getParameter(RequestParameters.PAGE_TOKEN));

    PreparedQuery results = getAllComments();
    ArrayList<Comment> unfilteredComments = new ArrayList<Comment>();
    results.asIterable().forEach(commentEntity -> {
      unfilteredComments.add(Comment.fromEntity(commentEntity));
    });
    List<Comment> comments = getFilteredComments(unfilteredComments, 
      searchQuery);

    Range<Integer> commentRange = getRangeOfCommentsToDisplay(
      paginationInstruction, numberToDisplay, comments.size(), pageToken);
    List<Comment> commentsToDisplay = 
      comments.subList(commentRange.lowerEndpoint(), 
      commentRange.upperEndpoint());
    int newPageToken = commentRange.lowerEndpoint();

    String json = convertToJson(commentsToDisplay, newPageToken, 
      currentUserId); 
    response.setContentType("application/json;");
    response.getWriter().println(json);
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
    String parameterString = (String) request.getParameter(
      RequestParameters.NUMBER_PER_PAGE);
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
      .addSort(EntityProperties.COMMENT_TIMESTAMP, SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    return datastore.prepare(query);
  }

  /** 
    * Returns all user comments that contain `search` in either
    * the comment text or author name. 
    * @param comments List containing all comments in database.
    * @param search A string to filter the comments by. 
    */
  private List<Comment> getFilteredComments( 
    ArrayList<Comment> comments, String search) {
    List<Comment> filteredComments = comments.stream()
      .filter(c -> satisfiesSearch(c, search)).collect(Collectors.toList());
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
      return comment.text.contains(search) || comment.username.contains(search);
    }
  }

  /** 
    * Returns JSON string representation of `data`, `pageToken`, 
    * and `currentUserId`. 
    */
  private String convertToJson(List<Comment> data, int pageToken, 
    String currentUserId) {
    HashMap<String, Object> combineData = new HashMap<String, Object>();
    combineData.put("pageToken", pageToken);
    combineData.put("commentData", data);
    combineData.put("currentUserId", currentUserId);

    Gson gson = new Gson(); 
    String json = gson.toJson(combineData);
    return json;
  }
}
