package com.google.sps.testing.fake_data;

public class FakeUserService {
  public static FakeUserService FAKE_USER_SERVICE_INSTANCE = new FakeUserService();

  public boolean isUserLoggedIn() {
    return true;
  }

  public boolean isUserAdmin() {
    return false;
  }

  public String getUserId() {
    return "0";
  }

  public String getEmail() {
    return "test@example.com"
  }

  public String createLogoutURL(String url) {
    return url;
  }

  public String createLoginURL(String url) {
    return url;
  }

  private FakeUserService(){
  }
}