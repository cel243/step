package com.google.sps.data;

/** File containing constants used throughout project. */
public final class EntityProperties {

  private EntityProperties() {}

  /* Comment Properties: */

  /** The property representing the text of a comment on the webpage. */
  public static final String COMMENT_TEXT = "text";

  /** The property representing the timestamp when the comment was posted (in 
    * milliseconds since the epoch). */
  public static final String COMMENT_TIMESTAMP = "time";

  /** The property representing the email of the author of this comment */
  public static final String USER_EMAIL = "email";

  /** The property representing the sentiment of this comment. */
  public static final String COMMENT_SENTIMENT = "sentiment";

  /** 
    * The property representing the topic of the comment, if one is available.
    */
  public static final String COMMENT_TOPIC = "topic";

  /* User Properties: */

  /** The property representing the username of this user */
  public static final String USERNAME = "username";

  /* Both User and Comment properties: */

  /** 
    * The property representing the user id of a user or an author 
    * of a comment. 
    */
  public static final String USER_ID = "userId";
}
