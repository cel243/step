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

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    HashMap<String, Object> authenticationStatusInfo = 
      new HashMap<String, Object>();

    if (userService.isUserLoggedIn()) {
      authenticationStatusInfo.put("isLoggedIn", true);
      String logOutLink = userService.createLogoutURL("/index.html");
      authenticationStatusInfo.put("logOutLink", logOutLink);
    } else {
      authenticationStatusInfo.put("isLoggedIn", false);
      String logInLink = userService.createLoginURL("/index.html");
      authenticationStatusInfo.put("logInLink", logInLink);
    }

    String json = (new Gson()).toJson(authenticationStatusInfo);
    System.out.println(json);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

}
