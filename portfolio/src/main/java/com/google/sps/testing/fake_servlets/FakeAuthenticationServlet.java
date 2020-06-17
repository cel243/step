
package com.google.sps.testing.fake_servlets;

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
import com.google.sps.configuration.Flags;
import com.google.sps.testing.fake_data.FakeUserService;

/** 
  * Fake servlet that uploads and retrieves user data from fake database. 
  */
@WebServlet(Flags.IS_REAL_SERVER ? "/fakeauthenticate" : "/authenticate")
public class AuthenticationServlet extends HttpServlet {

  /** 
    * Checks whether the user is currently logged in and provides
    * a log-out link and username if so, and a log-in link if not. 
    */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FakeUserService userService = FakeUserService.FAKE_USER_SERVICE_INSTANCE;
    HashMap<String, Object> authenticationStatusInfo = new HashMap<String, Object>();

    if (userService.isUserLoggedIn()) {
      authenticationStatusInfo.put("isLoggedIn", true);
      String logOutLink = userService.createLogoutURL("/index.html");
      authenticationStatusInfo.put("logOutLink", logOutLink);
      authenticationStatusInfo.put("username", getUserName(
        userService.getUserId()));
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
    if (!FakeUserDatabase.contains(userId)) {
      return "";
    } else {
      return FakeUserDatabase.get(userId);
    }
  }

  /** 
    * Updates the username in the fake database associated with this 
    * user id to be `newUserName`. 
    */
  public static void updateUserName(String newUserName, String userId) {
    FakeUserDatabase.put(userId, newUserName);
  }

}
