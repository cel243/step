package com.google.sps.testing.fake_data;

import java.util.LinkedList;
import java.util.List;
import com.google.sps.testing.fake_data.FakeComment;
import com.google.sps.testing.fake_servlets.FakeAuthenticationServlet;
import java.util.stream.Collectors;

/** A fake database that stored comments for testing purposes. */
public class FakeCommentDatabase {
  private static List<FakeComment> comments = new LinkedList<FakeComment>();
  private static int next_id = 0;

  /** 
    * Puts a copy of comment in the fake database and assigns it an id, 
    * sentiment, and topic. 
    */
  public static void put(FakeComment comment) {
    comments.add(0, new FakeComment(comment.text, "", comment.time, next_id, 
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
      .collect(Collectors.toList());
  }

  /** 
    * Deletes all comments from the fake database that were written by this 
    * user, or all the comments if userId = "ADMIN". 
    */
  public static void deleteAllCommentsByUser(String userId) {
    if (userId.equals("ADMIN")) {
      comments = new LinkedList<FakeComment>();
    } else {
      comments = comments.stream()
        .filter(comment -> !userId.equals(comment.userId))
        .collect(Collectors.toList());
    }
  }

  /** Deletes the comment with id commentId from the fake database. */
  public static void deleteThisComment(String commentId) {
    comments = comments.stream()
      .filter(comment -> !commentId.equals(comment.id+""))
      .collect(Collectors.toList());
  }
}
