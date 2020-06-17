
package com.google.sps.testing.fake_servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.RequestParameters;
import com.google.sps.data.EntityProperties;
import com.google.sps.servlets.AuthenticationServlet;
import com.google.sps.configuration.Flags;
import com.google.sps.testing.fake_data.FakeCommentDatabase;
import com.google.sps.testing.fake_data.FakeUserService;

/** Fake servlet that deletes comments from the fake database. */
@WebServlet(Flags.IS_REAL_SERVER ? "/fakedelete" : "/delete-data")
public class DeleteServlet extends HttpServlet {

  /** Deletes comments from fake database subject to query string. If
    * `whichData="all"` then all comments are deleted. If `whichData`
    * is a specific comment id, that comment is deleted. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FakeUserService userService = FakeUserService.FAKE_USER_SERVICE_INSTANCE;
    if (!userService.isUserLoggedIn()) {
      return;
    } 
    String currentUserId = userService.getUserId();
    String whichCommentToDelete = request.getParameter(RequestParameters.WHICH_COMMENT_TO_DELETE);

    if (whichCommentToDelete.equals("\"all\"")) {
      FakeCommentDatabase.deleteAllCommentsByUser(
        userService.isUserAdmin() ? "ADMIN" : currentUserId);
    } else {
      FakeCommentDatabase.deleteThisComment(whichCommentToDelete);
    }
  }

}
