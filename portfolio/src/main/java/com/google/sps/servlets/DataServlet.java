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

/** 
  * Servlet that uploads and retrieves persistent comment data using datastore.
  */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  /** Represents a single comment */
  private static class Comment {
      String text;
      String name;
      String time;

      Comment(String text, String name, String time) {
        this.text = text;
        this.name = name;
        this.time = time;
      }
  }

  /** Extracts user comment from form and stores it via datastore. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userComment = request.getParameter("text-input");
    String userName = request.getParameter("author");
    System.out.println(userName);
    if (userName == null || userName.trim() == "") {
      userName = "Anonymous";
    }
    String timestamp = Long.toString(System.currentTimeMillis());

    if (userComment != null || userComment.trim() == "") {
      storeComment(userComment, userName, timestamp);
    }
    response.sendRedirect("/index.html");
  }

  /** Stores user comment as entity in datastore. */
  private void storeComment(String userComment, String userName, 
    String timestamp) {
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("text", userComment);
    commentEntity.setProperty("name", userName);
    commentEntity.setProperty("time", timestamp);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
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
    int numberDisplayed = 0;

    results.asIterable().forEach(comment -> {
      comments.add(new Comment(
          (String) comment.getProperty("text"),
          (String) comment.getProperty("name"),
          (String) comment.getProperty("time")));
    });
    List<Comment> commentsToDisplay = 
      comments.subList(0, Math.min(numberToDisplay - 1, comments.size()));

    String json = convertToJson(commentsToDisplay); 
    response.setContentType("text/html;");
    response.getWriter().println(json);
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

  /** Returns JSON string representation of `data`. */
  private String convertToJson(List<Comment> data) {
    Gson gson = new Gson(); 
    String json = gson.toJson(data);
    return json;
  }
}
