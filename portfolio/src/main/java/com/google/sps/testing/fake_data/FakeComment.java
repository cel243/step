package com.google.sps.testing.fake_data;

/** Represents a single comment */
public class FakeComment {
  public String text;
  public String username;
  public long time;
  public long id;
  public String userId;
  public String email;
  public String sentiment;
  public String topic;

  public Comment(String text, String username, long time, long id, 
    String userId, String email, String sentiment, String topic) {
    this.text = text;
    this.username = username;
    this.time = time;
    this.id = id;
    this.userId = userId;
    this.email = email;
    this.sentiment = sentiment;
    this.topic = topic;
  }

  /** 
    * Returns the "translated" comment, where the text simply becomes
    * "<languageCode>: text"
    */
  public Comment translateComment(String languageCode) {
    text = languageCode+": "+text;
    return this;
  }
}

