package com.google.sps.testing.fake_data;

/** Simulates a UserService Object, but only has hardcoded responses. */
public class FakeUserService {
  public static FakeUserService FAKE_USER_SERVICE_INSTANCE = new FakeUserService();

  private FakeUserService(){
  }

  /** Returns whether the current user is logged in. */
  public boolean isUserLoggedIn() {
    return true;
  }

  /** Returns whether the current user is an admin. */
  public boolean isUserAdmin() {
    return false;
  }

  /** Returns the unique id of this user. */
  public String getUserId() {
    return "0";
  }

  /** Returns the user's email. */
  public String getEmail() {
    return "test@example.com";
  }

  /** Returns url. */
  public String createLogoutURL(String url) {
    return url;
  }

  /** Returns url. */
  public String createLoginURL(String url) {
    return url;
  }
}
