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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.RequestParameters;
import com.google.sps.data.EntityProperties;
import com.google.sps.servlets.AuthenticationServlet;

/** Servlet that deletes all persistent comment data from datastore. */
@WebServlet("/delete-data")
public class DeleteServlet extends HttpServlet {

  /** Deletes comments from datastore subject to query string. If
    * `whichData="all"` then all comments are deleted. If `whichData`
    * is a specific comment id, that comment is deleted. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      return;
    } 
    String currentUserId = userService.getCurrentUser().getUserId();

    String whichCommentToDelete = 
      (String) request.getParameter(RequestParameters.WHICH_COMMENT_TO_DELETE);

    if (whichCommentToDelete.equals("\"all\"")) {
      Query query = new Query("Comment");
      PreparedQuery results = datastore.prepare(query);
      results.asIterable().forEach(comment -> {
        if (currentUserId.equals((String) comment.getProperty(
          EntityProperties.USER_ID)) || 
          userService.isUserAdmin()) {
          datastore.delete(comment.getKey());
        }
      });
    } else {
      long id = Long.parseLong(whichCommentToDelete);
      Key key = KeyFactory.createKey("Comment", id);
      datastore.delete(key);
    }
  }

}
