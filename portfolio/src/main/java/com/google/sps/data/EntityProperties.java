package com.google.sps.data;

/** File containing constants used throughout project. */
public final class EntityProperties {

  private EntityProperties() {}

  /* Comment Properties: */

  /** The property representing the text of a comment on the webpage. */
  public static final String COMMENT_TEXT = "text";

  /** The property representing the user id of the author of a comment. */
  public static final String USER_ID_OF_AUTHOR = "userId";

  /** The property representing the timestamp when the comment was posted (in 
    * milliseconds since the epoch). */
  public static final String COMMENT_TIMESTAMP = "time";

  /** The property representing the numeric id of this entiity in the 
    * datastore. Used for deleting comments. */
  public static final String COMMENT_ID = "id";

  /** The property representing the email of the author of this comment */
  public static final String USER_EMAIL = "email";

  /* User Properties */

  /** The property representing the user id */
  public static final String USER_ID = "userId";

  /** The property representing the username of this user */
  public static final String USERNAME = "username";
}
