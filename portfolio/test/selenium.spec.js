
import "@babel/polyfill";
import chrome from "selenium-webdriver/chrome";
import { Builder, By, Key, Capabilities } from "selenium-webdriver";
import assert from "assert";
import { path } from "chromedriver";
let driver = null;
const chromeOptions = new chrome.Options().headless();
const URL = "http://localhost:8080/";

describe("Startup Webpage Tests", () => {
  before(async () => {
    driver = await new Builder(path)
      .forBrowser("chrome")
      .setChromeOptions(chromeOptions)
      .build();
    await driver.get(URL);
  });

  after(async () => {
    await driver.quit();
  });

  it("will wait before running tests", async () => {
    setTimeout(() => { }, 2000);
  });
  it("should have 3 comments on the page at startup", async () => {
    const commentSection = await driver.findElement(By.id("comment-section"));
    const comments = await commentSection.findElements(By.className("comment"));
    assert.equal(comments.length, 3);
  });
  it("should have correct user info for each comment", async () => {
    const moods = ["😊", "☹", "😐"];
    for (let i = 0; i < 3; i++) {
      const comment = await driver.findElement(By.id(`comment-${i}`));
      const userInfoBox = await comment.findElement(By.className(`user-info-box`));
      const userInfoBoxText = await userInfoBox.getText();
      assert.equal(userInfoBoxText, `Sally: ${moods[i]}\nJun 17, 1:48 PM`)
    }
  });
  it("should have no option to delete these three comments", async () => {
    for (let i = 0; i < 3; i ++) {
      const commentDeleteButton = await driver.findElements(By.id(`delete-${i}`));
      assert.equal(commentDeleteButton.length, 0);
    }
  });
  it("should have no username for the current user", async () => {
    const nameFieldOfCommentForm = await driver.findElement(By.id("author"));
    const nameEntered = await nameFieldOfCommentForm.getAttribute("placeholder");
    assert.equal(nameEntered, "Please enter a username.");    
  });
});

describe("Submit a Comment Tests", () => {
  before(async () => {
    driver = await new Builder(path)
      .forBrowser("chrome")
      .setChromeOptions(chromeOptions)
      .build();
    await driver.get(URL);

    const commentForm = await driver.findElement(By.id("comment-form"));
    await commentForm.findElement(By.id("author")).sendKeys("Caroline");
    await commentForm.findElement(By.id("text-input")).sendKeys("This is a test.");
    await commentForm.findElement(By.id("submit-comment")).click();
  });

  after(async () => {
    await driver.quit();
  }); 

  it("should have another comment on the page now", async () => {
    const commentSection = await driver.findElement(By.id("comment-section"));
    const comments = await commentSection.findElements(By.className("comment"));
    assert.equal(comments.length, 4);
  }); 
  it("should have a new comment with the correct info", async () => {
    const newComment = await driver.findElement(By.id("comment-3"));
    const userInfo = await newComment.findElement(By.className("user-info-box")).getText();
    const commentText = await newComment.findElement(By.className("comment-text")).getText();
    assert.equal(userInfo, `Caroline: 😊\nJun 17, 1:48 PM`);
    assert.equal(commentText, `none: This is a test.`);
  });
  it("should have delete button next to new comment", async () => {
    const deleteButton = await driver.findElements(By.id("delete-3"));
    assert.notEqual(deleteButton.length, 0);
  });
  it("should have a username for the current user", async () => {
    const nameFieldOfCommentForm = await driver.findElement(By.id("author"));
    const nameEntered = await nameFieldOfCommentForm.getAttribute("value");
    assert.equal(nameEntered, "Caroline");    
  });
});

describe("Username Change Tests", () => {
  before(async () => {
    driver = await new Builder(path)
      .forBrowser("chrome")
      .setChromeOptions(chromeOptions)
      .build();
    await driver.get(URL);

    const commentForm = await driver.findElement(By.id("comment-form"));
    await commentForm.findElement(By.id("author")).clear();
    await commentForm.findElement(By.id("author")).sendKeys("Bob");
    await commentForm.findElement(By.id("text-input")).sendKeys("This is another test.");
    await commentForm.findElement(By.id("submit-comment")).click();
  });

  after(async () => {
    await driver.quit();
  });

  it("should have the two comments labelled with Bob", async () => {
    const comment4 = await driver.findElement(By.id("comment-4"));
    const comment3 = await driver.findElement(By.id("comment-3"));
    const comment4UserInfo = await comment4.findElement(By.className("user-info-box")).getText();
    const comment3UserInfo = await comment3.findElement(By.className("user-info-box")).getText();
    assert.equal(comment4UserInfo, `Bob: 😊\nJun 17, 1:48 PM`);
    assert.equal(comment3UserInfo, `Bob: 😊\nJun 17, 1:48 PM`);
  });
  it("should have Bob in the comment form", async () => {
    const nameField = await driver.findElement(By.id("author"))
      .getAttribute("value");
    assert.equal(nameField, "Bob");    
  });
}); 

