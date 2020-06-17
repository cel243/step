package com.google.sps.testing.fake_data;

import java.util.ArrayList;
import java.util.List;
import com.google.sps.testing.fake_data.FakeComment;
import com.google.sps.testing.fake_servlets.FakeAuthenticationServlet;
import java.util.stream.Collectors;

public class FakeCommentDatabase {
  private static ArrayList<FakeComment> comments = new ArrayList<FakeComment>();
  private static int next_id = 0;

  /** 
    * Puts a copy of comment in the fake database and assigns it an id, 
    * sentiment, and topic. 
    */
  public static void put(FakeComment comment) {
    comments.add(new FakeComment(comment.text, "", comment.time, next_id, 
      comment.userId, comment.email, "POSITIVE", ""));
    next_id++;
  }

  /** 
    * Gets copies of all comments from the fake database and retreives the 
    * appropriate usernames. 
    */
  public static List<FakeComment> getAllComments() {
    return comments.stream()
      .map(comment -> 
        new FakeComment(comment.text, 
        FakeAuthenticationServlet.getUserName(comment.userId), comment.time, 
        comment.id, comment.userId, comment.email, comment.sentiment, 
        comment.topic))
      .collect(Collectors.asList());
  }
}
