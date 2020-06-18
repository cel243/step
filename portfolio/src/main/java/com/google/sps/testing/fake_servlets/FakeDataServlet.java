
package com.google.sps.testing.fake_servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import java.lang.Math;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.sps.data.EntityProperties;
import com.google.sps.data.RequestParameters;
import java.util.stream.Collectors;
import java.util.HashMap;
import com.google.sps.configuration.Flags;
import com.google.sps.testing.fake_data.FakeComment;
import com.google.sps.testing.fake_data.FakeCommentDatabase;
import com.google.sps.testing.fake_data.FakeUserService;

/** 
  * Fake Servlet that uploads and retrieves comments from fake database.
  */
@WebServlet(Flags.IS_REAL_SERVER ? "/fakedata" : "/data")
public class FakeDataServlet extends HttpServlet {

  @Override
  public void init() {
    FakeCommentDatabase.put(  
      new FakeComment("Test Comment One", "", Long.parseLong("1592401704803"), 
        0, "1", "test1@example.com", "POSITIVE", "TOPIC"));
    FakeCommentDatabase.put(  
      new FakeComment("Test Comment Two", "", Long.parseLong("1592401704803"), 
        0, "1", "test1@example.com", "NEGATIVE", ""));
    FakeCommentDatabase.put(  
      new FakeComment("Test Comment Three", "", Long.parseLong("1592401704803"), 
        0, "1", "test1@example.com", "NEUTRAL", ""));
  }

 
  /** Extracts user comment from form and stores it in a fake database. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FakeUserService userService = FakeUserService.FAKE_USER_SERVICE_INSTANCE;
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String userComment = request.getParameter(RequestParameters.INPUTTED_TEXT).trim();
    String userName = request.getParameter(RequestParameters.INPUTTED_AUTHOR_NAME).trim();
    if (Strings.isNullOrEmpty(userName)) {
      userName = "Anonymous";
    }
    long timestamp = Long.parseLong("1592401704803"); // Wed Jun 17 2020 09:48:24
    String userId = userService.getUserId();
    String email = userService.getEmail();
    FakeAuthenticationServlet.updateUserName(userName, userId);

    if (!Strings.isNullOrEmpty(userComment)) {    
      FakeCommentDatabase.put(
        new FakeComment(userComment, "", timestamp, 0, userId, email, "", ""));
    }
    response.sendRedirect("/index.html");
  }

  /** 
    * Loads all user comments from the fake database and returns JSON list of 
    * the comments, with a page token and the current user id. 
    */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FakeUserService userService = FakeUserService.FAKE_USER_SERVICE_INSTANCE;
    String currentUserId;
    if (!userService.isUserLoggedIn()) {
      currentUserId = "";
    } else if (userService.isUserAdmin()) {
      currentUserId = "ADMIN";
    } else {
      currentUserId = userService.getUserId();
    }

    int numberToDisplay = getNumberToDisplay(request); 
    String paginationInstruction = request.getParameter(RequestParameters.PAGE_ACTION);
    String searchQuery = request.getParameter(RequestParameters.SEARCH_QUERY);
    searchQuery = searchQuery.substring(1,searchQuery.length() - 1);
    int pageToken = Integer.parseInt(request.getParameter(RequestParameters.PAGE_TOKEN));
    String languageCodeWithQuotes = request.getParameter(RequestParameters.LANGUAGE);
    String languageCode = languageCodeWithQuotes.substring(1,languageCodeWithQuotes.length() - 1);

    List<FakeComment> unfilteredComments = FakeCommentDatabase.getAllComments()
      .stream()
      .map(comment -> comment.translateComment(languageCode))
      .collect(Collectors.toList());

    List<FakeComment> comments = getFilteredComments(unfilteredComments, 
      searchQuery);

    Range<Integer> commentRange = getRangeOfCommentsToDisplay(
      paginationInstruction, numberToDisplay, comments.size(), pageToken);
    List<FakeComment> commentsToDisplay = 
      comments.subList(commentRange.lowerEndpoint(), 
      commentRange.upperEndpoint());
    int newPageToken = commentRange.lowerEndpoint();

    String json = convertToJson(commentsToDisplay, newPageToken, 
      currentUserId); 
    response.setCharacterEncoding("UTF-8");
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

  /** 
    * Returns all user comments that contain `search` in either
    * the comment text or author name. 
    * @param comments List containing all comments in database.
    * @param search A string to filter the comments by. 
    */
  private List<FakeComment> getFilteredComments( 
    List<FakeComment> comments, String search) {
    List<FakeComment> filteredComments = comments.stream()
      .filter(c -> satisfiesSearch(c, search)).collect(Collectors.toList());
    return filteredComments;
  }

  /** 
    * Returns true if this comment contains the search string in the
    * comment text or author name. 
    */
  private boolean satisfiesSearch(FakeComment comment, String search) {
    if (Strings.isNullOrEmpty(search)) {
      return true;
    } else {
      return comment.text.contains(search) || 
        comment.username.contains(search) || comment.email.contains(search) ||
        comment.topic.contains(search);
    }
  }

  /** 
    * Returns JSON string representation of `data`, `pageToken`, 
    * and `currentUserId`. 
    */
  private String convertToJson(List<FakeComment> data, int pageToken, 
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
