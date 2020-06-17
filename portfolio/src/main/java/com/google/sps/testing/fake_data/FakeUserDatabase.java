package com.google.sps.testing.fake_data;

import java.util.HashMap;

/** A fake database that stores user data for testing purposes. */
public class FakeUserDatabase {
  private static HashMap<String, String> users = new HashMap<String, String>();

  /** 
    * Puts the pairing of (userId, username) into the database. If (userId, __) 
    * already exists the value is updated. 
    */
  public static void put(String userId, String username) {
    users.put(userId, username);
  }

  /** Returns the value userId is paired with in the database. */
  public static String get(String userId) {
    users.get(userId);
  }

  /** Returns true if userId is paired with some username in the database. */
  public static boolean contains(String userId) {
    return users.containsKey(userId);
  }
}
