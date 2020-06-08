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
import java.util.stream.Collectors;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.util.HashMap;

/** 
  * Servlet that uploads and retrieves persistent comment data using datastore.
  */
@WebServlet("/authenticate")
public class AuthenticationServlet extends HttpServlet {

  /** 
    * Checks whether the user is currently logged in and provides
    * a log-in link if not, and a log-out link if not. 
    */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    HashMap<String, Object> authenticationStatusInfo = 
      new HashMap<String, Object>();

    if (userService.isUserLoggedIn()) {
      authenticationStatusInfo.put("isLoggedIn", true);
      String logOutLink = userService.createLogoutURL("/index.html");
      authenticationStatusInfo.put("logOutLink", logOutLink);
      authenticationStatusInfo.put("username", getUserName(
        userService.getCurrentUser().getUserId()));
    } else {
      authenticationStatusInfo.put("isLoggedIn", false);
      String logInLink = userService.createLoginURL("/index.html");
      authenticationStatusInfo.put("logInLink", logInLink);
    }

    String json = (new Gson()).toJson(authenticationStatusInfo);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /** 
    * Returns the username currently associated with this user id.
    * If no username is currently associated with this user, returns
    * the empty string.
    */
  public static String getUserName(String userId) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("User").setFilter(
      new Query.FilterPredicate(
        EntityProperties.USER_ID, Query.FilterOperator.EQUAL, userId));
    Entity user = datastore.prepare(query).asSingleEntity();

    if (user == null) {
      return "";
    } else {
      return (String) user.getProperty(EntityProperties.USERNAME);
    }
  }

  /** 
    * Updates the username in the datastore associated with this 
    * user id to be `newUserName`. 
    */
  public static void updateUserName(String newUserName, String userId) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("User").setFilter(
      new Query.FilterPredicate(
        EntityProperties.USER_ID, Query.FilterOperator.EQUAL, userId));
    Entity user = datastore.prepare(query).asSingleEntity();

    if (user == null) {
      Entity newUser = new Entity("User");
      newUser.setProperty(EntityProperties.USER_ID, userId);
      newUser.setProperty(EntityProperties.USERNAME, newUserName);
      datastore.put(newUser);
    } else if (!((String) user.getProperty(EntityProperties.USERNAME)).equals(newUserName)) {
      user.setProperty(EntityProperties.USERNAME, newUserName);
      datastore.put(user);
    }
  }

}
