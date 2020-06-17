 
import "@babel/polyfill";
import chrome from "selenium-webdriver/chrome";
import { Builder, By, Key, Capabilities } from "selenium-webdriver";
import assert from "assert";
import { path } from "chromedriver";
let driver = null;
//let webdriver = require("selenium-webdriver");
//SeleniumServer = require("selenium-webdriver/remote").SeleniumServer;
//request = require("request");
const chromeOptions = new chrome.Options().headless();
const URL = "https://8080-696f5616-cc74-4b85-a8ad-24ee4476293f.us-east1.cloudshell.dev/?authuser=0";

describe("Startup Webpage Tests", () => {
  beforeAll(async () => {
    driver = await new Builder(path)
      .forBrowser("chrome")
      .setChromeOptions(chromeOptions)
      .build();
    await driver.get(URL);
  });

  afterAll(async () => {
    await driver.quit();
  });

  it("should have 3 comments on the page at startup", async () => {
    const rowsOfTable = await driver.findElement(By.cssSelector("[id='comment-section' tr]"));
    assert.Equal(rowsOfTable.length, 4);
  });
  it("should have correct user info for each comment", async () => {
    const rowsOfTable = await driver.findElement(By.cssSelector("[id='comment-section' tr]"));
    const moods = ["😐", "☹", "😊"];
    let i = 0;
    for (row in rowsOfTable) {
      const userInfoBox = await row.findElement(By.class("user-info-box"));
      if (userInfoBox === undefined) {
        continue;
      }
      assert.Equal(userInfoBox.getText(), `Sally: ${moods[i]}Jun 17, 9:48 AM`);
      i += 1;
    }
  });
  it("should have no option to delete these three comments", async () => {
    const rowsOfTable = await driver.findElement(By.cssSelector("[id='comment-section' tr]"));
    for (row in rowsOfTable) {
      const commentButtonBox = await row.findElement(By.class("comment-button"))
      if (commentButtonBox === undefined) {
        continue;
      }
      assert.Equal(commentButtonBox.getText(), undefined);
    }
  });
  it("should have no username for the current user", async () => {
    const commentForm = await driver.findElement(By.id("comment-form"));
    const nameField = await commentForm.findElement(By.cssSelector("#author"));
    assert.Equal(nameField.getAttribute("value"), undefined);    
  });
});

describe("Submit a Comment Tests", () => {
  beforeAll(async () => {
    driver = await new Builder(path)
      .forBrowser("chrome")
      .setChromeOptions(chromeOptions)
      .build();
    await driver.get(URL);

    const commentForm = await driver.findElement(By.id("comment-form"));
    await commentForm.findElement(By.cssSelector("input[type='text']")).sendKeys("Caroline");
    await commentForm.findElement(By.cssSelector("textarea")).sendKeys("This is a test.");
    await commentForm.findElement(By.cssSelector("input[type='submit']")).submit();
  });

  afterAll(async () => {
    await driver.quit();
  });

  it("should have another comment on the page now", async () => {
    const rowsOfTable = await driver.findElement(By.cssSelector("[id='comment-section' tr]"));
    assert.Equal(rowsOfTable.length, 5);
  });
  it("should have new comment with the info I submitted above", async () => {
    const rowsOfTable = await driver.findElement(By.cssSelector("[id='comment-section' tr]"));
    let commentExists = false;
    for (row in rowsOfTable) {
      const userInfoBox = await row.findElement(By.class("user-info-box"));
      if (userInfoBox === undefined) {
        continue;
      }
      const commentTextBox = await row.findElement(By.class("comment-text"));
      if (userInfoBox.getText() === `Caroline: 😊Jun 17, 9:48 AM` && commentTextBox.getText() === `none: This is a test.`) {
        commentExists = true;
      } 
    }
    assert.Equal(commentExists, true);
  });
  it("should have delete button next to new comment", async () => {
    const deleteButton = await driver.findElement(By.id("delete-3"));
    assert.notEqual(deleteButton, undefined);
  });
});


describe("Delete a Comment Tests", () => {
  beforeAll(async () => {
    driver = await new Builder(path)
      .forBrowser("chrome")
      .setChromeOptions(chromeOptions)
      .build();
    await driver.get(URL);

    const commentForm = await driver.findElement(By.id("comment-form"));
    await commentForm.findElement(By.cssSelector("input[type='text']")).sendKeys("Caroline");
    await commentForm.findElement(By.cssSelector("textarea")).sendKeys("This is a test.");
    await commentForm.findElement(By.cssSelector("input[type='submit']")).submit();

    await commentForm.findElement(By.cssSelector("input[type='text']")).sendKeys("Caroline");
    await commentForm.findElement(By.cssSelector("textarea")).sendKeys("This is a another test.");
    await commentForm.findElement(By.cssSelector("input[type='submit']")).submit();
  });

  afterAll(async () => {
    await driver.quit();
  });

  it("should have no comment 4 after I delete comment 4", async () => {
    await driver.findElement(By.id("delete-4")).click();
    const comment4 = await driver.findElement(By.id("comment-4"));
    const comment3 = await driver.findElement(By.id("comment-3"));
    assert.Equal(comment4, undefined);
    assert.notEqual(comment3, undefined);
  });
});

describe("Delete All Comments Tests", () => {
  beforeAll(async () => {
    driver = await new Builder(path)
      .forBrowser("chrome")
      .setChromeOptions(chromeOptions)
      .build();
    await driver.get(URL);

    const commentForm = await driver.findElement(By.id("comment-form"));
    await commentForm.findElement(By.cssSelector("input[type='text']")).sendKeys("Caroline");
    await commentForm.findElement(By.cssSelector("textarea")).sendKeys("This is a test.");
    await commentForm.findElement(By.cssSelector("input[type='submit']")).submit();

    await commentForm.findElement(By.cssSelector("input[type='text']")).sendKeys("Caroline");
    await commentForm.findElement(By.cssSelector("textarea")).sendKeys("This is a another test.");
    await commentForm.findElement(By.cssSelector("input[type='submit']")).submit();
  });

  afterAll(async () => {
    await driver.quit();
  });

  it("should have 3 comments after hitting clear all", async () => {
    await driver.findElement(By.id("clear-all-button")).click();
    const rowsOfTable = await driver.findElement(By.cssSelector("[id='comment-section' tr]"));
    assert.Equal(rowsOfTable.length, 4);
  });
});

describe("Username Change Tests", () => {
  beforeAll(async () => {
    driver = await new Builder(path)
      .forBrowser("chrome")
      .setChromeOptions(chromeOptions)
      .build();
    await driver.get(URL);

    const commentForm = await driver.findElement(By.id("comment-form"));
    await commentForm.findElement(By.cssSelector("input[type='text']")).sendKeys("Caroline");
    await commentForm.findElement(By.cssSelector("textarea")).sendKeys("This is a test.");
    await commentForm.findElement(By.cssSelector("input[type='submit']")).submit();

    await commentForm.findElement(By.cssSelector("input[type='text']")).sendKeys("Bob");
    await commentForm.findElement(By.cssSelector("textarea")).sendKeys("This is a another test.");
    await commentForm.findElement(By.cssSelector("input[type='submit']")).submit();
  });

  afterAll(async () => {
    await driver.quit();
  });

  it("should have the two new comments labelled with Bob", async () => {
    const comment4 = await driver.findElement(By.id("comment-4"));
    const comment3 = await driver.findElement(By.id("comment-3"));
    const comment4UserInfo = await comment4.findElement(By.class("user-info-box"));
    const comment3UserInfo = await comment3.findElement(By.class("user-info-box"));
    assert.Equal(comment4UserInfo.getText(), `Bob: 😊Jun 17, 9:48 AM`);
    assert.Equal(comment3UserInfo.getText(), `Bob: 😊Jun 17, 9:48 AM`);
  });
  it("should have Bob in the comment form", async () => {
    const commentForm = await driver.findElement(By.id("comment-form"));
    const nameField = await commentForm.findElement(By.cssSelector("#author"));
    assert.Equal(nameField.getAttribute("value"), "Bob");    
  });
});
