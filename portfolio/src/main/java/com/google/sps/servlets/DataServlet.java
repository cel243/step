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
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

/** Servlet that handles comment data. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userComment = request.getParameter("text-input");
    if (userComment != null) {
      storeComment(userComment);
    }
    response.sendRedirect("/index.html");
  }

  /**
    * Stores user comment as entity in datastore.
    */
  private void storeComment(String userComment) {
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("text", userComment);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PreparedQuery results = getAllComments();
    
    ArrayList<String> comments = new ArrayList<String>();
    int numberToDisplay = getNumberToDisplay(request); 
    int numberDisplayed = 0;

    for(Entity comment : results.asIterable()) {
      String commentText = (String) comment.getProperty("text");
      comments.add(commentText);
      numberDisplayed += 1;
      if (numberDisplayed == numberToDisplay) {
        break;
      }
    }

    String json = convertToJson(comments); 
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /** 
    * Returns number of comments to display, based on user's 
    * selection. 
    */
  private int getNumberToDisplay(HttpServletRequest request) {
    String parameterString = (String) request.getParameter("numberToDisplay");
    int numberToDisplay;
    try {
      numberToDisplay = Integer.parseInt(parameterString);
    } catch (Exception e) {
      System.err.println("Could not convert to int: " + parameterString +
          "\nSetting value to default: 5");
      numberToDisplay = 5;
    }
    return numberToDisplay;
  }

  /** 
    * Returns all user comments stored in datastore. 
    */
  private PreparedQuery getAllComments() {
    Query query = new Query("Comment");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    return datastore.prepare(query);
  }

  /** 
    * Returns JSON string representation of `data`.
    */
  private String convertToJson(ArrayList<String> data) {
    Gson gson = new Gson(); 
    String json = gson.toJson(data);
    return json;
  }
}
