
const languages = ["Afrikaans", "Albanian", "Arabic", "Armenian", "Basque", "Bengali", 
  "Bulgarian", "Catalan", "Cambodian", "Chinese (Mandarin)", "Croatian", 
  "Czech", "Danish", "Dutch", "English", "Estonian", "Fiji", "Finnish", 
  "French", "Georgian", "German", "Greek", "Gujarati", "Hebrew", "Hindi", 
  "Hungarian", "Icelandic", "Indonesian", "Irish", "Italian", "Japanese", 
  "Javanese", "Korean", "Latin", "Latvian", "Lithuanian", "Macedonian", "Malay",
  "Malayalam", "Maltese", "Maori", "Marathi", "Mongolian", "Nepali", 
  "Norwegian", "Persian", "Polish", "Portuguese", "Punjabi", "Quechua", 
  "Romanian", "Russian", "Samoan", "Serbian", "Slovak", "Slovenian", "Spanish", 
  "Swahili", "Swedish", "Tamil", "Tatar", "Telugu", "Thai", "Tibetan", "Tonga", 
  "Turkish", "Ukrainian", "Urdu", "Uzbek", "Vietnamese", "Welsh", "Xhosa"];
const languageCodes = ["AF", "SQ", "AR", "HY", "EU", "BN", "BG", "CA", "KM", 
  "ZH", "HR", "CS", "DA", "NL", "EN", "ET", "FJ", "FI", "FR","KA", "DE", "EL", 
  "GU", "HE", "HI", "HU", "IS", "ID", "GA", "IT", "JA", "JW", "KO", "LA", "LV", 
  "LT", "MK", "MS", "ML", "MT", "MI", "MR", "MN", "NE", "NO", "FA", "PL", "PT", 
  "PA", "QU", "RO", "RU", "SM", "SR", "SK", "SL", "ES", "SW", "SV", "TA", "TT", 
  "TE", "TH", "BO", "TO", "TR", "UK", "UR", "UZ", "VI", "CY", "XH"];
let pageToken = 0;

/** 
  * Fetches comment data from the server and displays it on the page.
  * @param {String} pageAction Either "none", "next", or "previous", indicating
      whether the site should display the next page of comments, the previous, 
      or stay on the same page.
  */
function displayCommentSection(pageAction) {
  const selectNumberInput = document.getElementById('number-to-display');
  const numberToDisplay = selectNumberInput
    .options[selectNumberInput.selectedIndex].value;
  const languageInput = document.getElementById('select-language');
  const language = languageInput
    .options[languageInput.selectedIndex].value;
  let searchQuery = document.getElementById('search').value;
  if (!searchQuery || !searchQuery.trim()) {
    searchQuery = "";
  }

  fetch(`/data?numberToDisplay=${numberToDisplay}` + 
    `&pageAction="${pageAction}"&search="${searchQuery}"` +
    `&pageToken=${pageToken}&language="${language}"`)
    .then(response => response.json())
    .then(displayJSON);
}

/** 
  * Outputs formatted comment section, where each comment 
  * has a name and date to the left, text to the right, 
  * and all comments are aligned vertically and stacked on top
  * of one another. 
  * @param {JSON} json The JSON representing the list of comment objects. 
  */
function displayJSON(json) {
  pageToken = json.pageToken;
  const currentUserId = json.currentUserId;
  json = json.commentData;

  const dataContainer = document.getElementById('comment-section');
  htmlToAdd =`<table> <tr><td></td><td></td><td></td>` +
    `<td></td><td></td><td></td><td></td></tr>`;

  let searchQuery = document.getElementById('search').value;
  let noResultsMessage = "Sorry, we couldn't find anything!";
  if (!searchQuery || !searchQuery.trim()) {
    noResultsMessage = "Be the first to leave a comment!";
  }

  if (json.length === 0) {
    htmlToAdd += 
      `<tr rowspan="5">` +
      ` <td colspan="7" id="be-the-first">` +
          `${noResultsMessage}` +
      ` </td>` +
      `</tr>`;
  } else {
    let allCommentHTML = [...Array(json.length).keys()]
      .map(i => getCommentHTML(currentUserId, json, i));
    allCommentHTML.forEach(commentHTML => htmlToAdd += commentHTML);
  }

  htmlToAdd += `</table>`; 
  dataContainer.innerHTML = htmlToAdd; 
}

/**
  * Returns the HTML representation of `json[i]`
  * as a row in the comment section table.
  * @param {JSON} json The JSON representing the list of all comments.
  * @param {int} i The index of the desired comment.
  * @return {String} The HTML representation. */
function getCommentHTML(currentUserId, json, i) {
  html =
    `<tr id="comment-${json[i].id}" class="comment">` +
    ` <td class="comment-button">` +
    `   ${getCommentButtonHTML(currentUserId, json[i])}` + 
    ` </td>` +
    ` <td colspan = 2 class="user-info-box">` +
    `   <b><abbr title="${json[i].email}">${json[i].username}</abbr>:</b>` +
        ` ${displaySentiment(json[i].sentiment)}<br>` +
    `   <i class="comment-date">${prettyPrintTime(json[i].time)}</i>` +
    ` </td>` +
    ` <td colspan="4" class="comment-text">` +
        `${json[i].text}` +
        `${displayTopic(json[i].topic)}` +
    ` </td>` +
    `</tr>`;
  return html;
}

/** 
  * If `topic` is not empty, returns an HTML paragraph
  * of the form "Topic: <topic>", otherwise returns 
  * the empty string.  
  */
function displayTopic(topic) {
  if (topic.trim()) {
    return `<br><i class="topic">Topic: ${topic}</i>`;
  } else {
    return ``;
  }
}

/** 
  * Returns an emoticon reflecting whether the sentiement
  * of this comment is "Positive", "Negative",
  * or "Neutral". 
  */
function displaySentiment(sentiment){
  if (sentiment === "POSITIVE") {
    return `😊`;
  } else if (sentiment === "NEGATIVE") {
    return `☹`;
  } else {
    return `😐`;
  }
} 

/** 
  * Returns the HTML for a button that will delete this comment if
  * this comment was written by the current user. Otherwise it
  * returns no button. 
  * @param {String} currentUserId the id of the current user.
  * @param {JSON} commentObject the json object representing the comment 
      in question. 
  */
function getCommentButtonHTML(currentUserId, commentObject) {
  if (currentUserId === commentObject.userId || currentUserId === "ADMIN") {
    return `<button onclick="deleteThisComment(${commentObject.id})"` +
      ` id="delete-${commentObject.id}">` +
      `X</button>`;
  } else {
    return ``;
  }
}

/**
  * Returns a time of the form: Jun 16, 5:30.
  * @param {number} timeInmilliseconds The time in milliseconds since the 
      epoch. 
  * @return {String} The nicely-formatted string representing that time. 
  */
function prettyPrintTime(timeInMilliseconds) {
  let time = new Date(0);
  time.setUTCMilliseconds(timeInMilliseconds);

  const options = {month: 'short', day: 'numeric', hour: 'numeric', 
    minute: 'numeric', hour12: true };

  return time.toLocaleDateString(undefined, options);
}

/** Deletes all comments from the datastore. */
function deleteAllComments() {
  fetch(new Request('/delete-data?whichData="all"', {method: 'POST'}))
    .then(response => {
      displayCommentSection('none');
    });
}

/** Deletes the comment with id `commentId` from the datastore. */
function deleteThisComment(commentId) {
  fetch(new Request(`/delete-data?whichData=${commentId}`, 
    {method: 'POST'}))
    .then(response => {
      displayCommentSection('none');
    })
}

/** Clears the current search of the comment section. */
function onClearSearch() {
  let searchBar = document.getElementById("search");
  searchBar.value = "";
  pageToken = 0;
  displayCommentSection('none');
}

/** 
  * Displays the dropdown menu that decides the language to display the comment 
  * section in.  
  */
function displayLanguageSelect() {
  let selectLanguageBox = document.getElementById("select-language-box");

  let htmlToAdd =`<select name="select-language" id="select-language"` +
    ` onchange="displayCommentSection('none')">`;
  htmlToAdd += `<option value="none">Select Language</option>`;

  let alllanguageOptionHTML = [...Array(languages.length).keys()]
    .map(i => `<option value="${languageCodes[i]}">${languages[i]}</option>`);
  alllanguageOptionHTML.forEach(optionHTML => htmlToAdd += optionHTML);

  htmlToAdd += `</select>`;

  selectLanguageBox.innerHTML = htmlToAdd;
}


/** Determines whether the current user is signed in, and if so, 
  * displays the form where the user could leave a comment. If
  * not, it displays a link to sign in and gets rid of the clear
  * all button.
  * @param json The json returned by the GET request to /authenticate  */
function displayCommentForm(json) {
  let commentFormBox = document.getElementById("comment-form");
  let clearAllButtonBox = document.getElementById("clear-all-button");

  if (json.isLoggedIn) {
    const username = !json.username.trim() ? 
      `placeholder="Please enter a username."` : `value="${json.username}"`;
    commentFormBox.innerHTML = 
      `<table>` +
          `<tr>` +
              `<td id="leave-a-comment">` +
                  `Leave a comment:` +
              `</td>` +
          `</tr>` + 
      `</table>` +
      `<div id="comment-form-box">` +
        `<form action="/data" method="POST" id="comment-form">` +
            `<input type="text" name="author" id="author" ` +
                `tabindex="1" required="required" ${username}>` +
            `<textarea name="text-input" id="text-input" rows="10"` + 
                `tabindex="4" required="required"></textarea>` +
            `<input id="submit-comment" type="submit" />` +
        `</form>` +
      `</div>` +
      `<div id="log-out"><a href="${json.logOutLink}">Log out</a> here.`;
  } else {
    clearAllButtonBox.innerHTML = ``;
    commentFormBox.innerHTML = `<div id="please-sign-in">Please` +
      ` <a href="${json.logInLink}">sign in</a> to leave a comment.</div>`
  }
}

/** 
  * Sets up event listener for search bar such that enter key
  * triggers action. 
  */
function initializeSearchBar() {
  let searchBar = document.getElementById("search");
  searchBar.addEventListener("keydown", function (e) {
    if(e.keyCode == 13) {
      pageToken = 0;
      displayCommentSection('none');
    }
  });
}


