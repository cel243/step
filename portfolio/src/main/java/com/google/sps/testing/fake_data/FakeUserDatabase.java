package com.google.sps.testing.fake_data;

import java.util.HashMap;

public class FakeUserDatabase {
  private static HashMap<String, String> users = new HashMap<String, String>();


  public static void put(String userId, String username) {
    users.put(userId, username);
  }

  public static String get(String userId) {
    users.get(userId);
  }

  public static boolean contains(String userId) {
    return users.containsKey(userId);
  }

}
