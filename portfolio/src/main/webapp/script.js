// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

const NUM_AVAILABLE_IMAGES = 13;

/** 
  * Generates a random image url from the available pictures of Penny.
  * @returns {String} This returns new random valid image url.
  */
function generateImageUrl() {
  const imageIndex = Math.floor(Math.random() * NUM_AVAILABLE_IMAGES) + 1;
  return 'images/Penny-' + imageIndex + '.JPG';
}

/**
  * Generates the HTML for the image corresponding to `imageIndex + 1`,
  * where the HTML element is an input with this image that calls 
  * `onEnlargeThisImage(imageIndex + 1)` when clicked. 
  * @param {int} imageIndex The index of the desired image - 1.
  * @returns {String} Returns HTML for image with index `imageIndex + 1`
  */
function getImageHTML(imageIndex) {
  return `<input type="image" src="images/Penny-${imageIndex + 1}.JPG"` + 
      ` onClick="onEnlargeThisImage(${imageIndex + 1})"/> `;
}

/**
  * Generates the HTML for image corresponding to `imageIndex`, where the 
  * HTML element is a link to this image. 
  * @param {int} imageIndex The index of the desired image.
  * @returns {String} Returns HTML for image with index `imageIndex`
  */
function enlargedImageHTML(imageIndex) {
  return `<a href="images/Penny-${imageIndex}.JPG">` +
      `<img src="images/Penny-${imageIndex}.JPG"/></a> `;
}

/**
  * Generates the HTML for image corresponding to `imageUrl`, where the 
  * HTML element is an input with this image that calls `onClickPennyButton()`
  * when clicked. 
  * @param {String} imageUrl The url of the desired image.
  * @returns {String} Returns HTML for image at url `imageUrl`
  */
function createImageButtonHTML(imageUrl) {
  return `<input id="penny-button" type="image" src="${imageUrl}"` +
      ` onClick="onClickPennyButton()"/>`;
}

/**
 * Replaces the previous image button with a new image button that has the same 
 * functionality but with a new, random image. 
 */
function onClickPennyButton() {
  const pennyButton = document.getElementById('penny-button');
  const currentImageUrl = pennyButton.getAttribute('src');
  let newImageUrl = currentImageUrl;

  /* ensure new image is not the same as old one */ 
  while (newImageUrl === currentImageUrl) {
    newImageUrl = generateImageUrl();
  }

  const pennyContainer = document.getElementById('penny-container');
  let newHTML = createImageButtonHTML(newImageUrl);
  pennyContainer.innerHTML = newHTML;
}

/**
  * Fills gallery page with all available images.
  */
function fillGallery() {
  const imagesContainer = document.getElementById('penny-gallery-images');

  let htmlToAdd = '';
  let allImageHTML = [...Array(NUM_AVAILABLE_IMAGES).keys()].map(getImageHTML);
  allImageHTML.forEach(imageHTML => htmlToAdd += imageHTML);

  imagesContainer.innerHTML = htmlToAdd;
}

/**
  * Displays `thisImage` enlarged above a grid of the other available images. 
  * `thisImage` becomes a link to the image, while all of the other images
  * are buttons that call this function. 
  * @param {int} thisImage The image to be enlarged.
  */
function onEnlargeThisImage(thisImage) {
  const imagesContainer = document.getElementById('penny-gallery-images');
  imagesContainer.innerHTML = ''; 

  const enlargedContainer = document.getElementById('enlarged-image');
  enlargedContainer.innerHTML = enlargedImageHTML(thisImage);

  const smallContainer = document.getElementById('small-images');
  let htmlToAdd = '';
  let allImageHTML = [...Array(NUM_AVAILABLE_IMAGES).keys()]
    .filter(imageIndex => imageIndex + 1 !== thisImage)
    .map(getImageHTML);
  allImageHTML.forEach(imageHTML => htmlToAdd += imageHTML);
  smallContainer.innerHTML = htmlToAdd;
}

/** 
  * Fetches comment data from the server and displays it on the page.
  */
function displayCommentSection() {
  const selectNumberInput = document.getElementById('number-to-display');
  const numberToDisplay = selectNumberInput
    .options[selectNumberInput.selectedIndex].value;

  fetch(`/data?numberToDisplay=${numberToDisplay}`)
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
  const dataContainer = document.getElementById('comment-section');
  htmlToAdd =`<table> <tr><td></td><td></td><td></td>` +
    `<td></td><td></td><td></td><td></td></tr>`;

  if (json.length === 0) {
    htmlToAdd += 
      `<tr rowspan="5">` +
      ` <td colspan="7" id="be-the-first">` +
          `Be the first to leave a comment!` +
      ` </td>` +
      `</tr>`;
  }
  else {
    let allCommentHTML = [...Array(json.length).keys()]
      .map(i => getCommentHTML(json, i));
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
function getCommentHTML(json, i) {
  html =
    `<tr>` +
    ` <td class="comment-button">` + 
    `   <button onclick="deleteThisComment(${json[i].id})">X</button></td>` +
    ` <td colspan = 2 class="user-info-box">` +
    `   <b><u>${json[i].name}: </b></u><br>` +
    `   <i class="comment-date">${prettyPrintTime(json[i].time)}</i>` +
    ` </td>` +
    ` <td colspan="4">` +
        `${json[i].text}` +
    ` </td>` +
    `</tr>`;
  return html;
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
  let dateInformation = time.toString().split(" ");
  let timeInformation = dateInformation[4].split(":");

  // Example output of toString(): 
  // Tue Aug 19 1975 23:15:30 GMT+0200 (CEST)
  let dateInformation = time.toString().split(" ");
  let timeInformation = dateInformation[4].split(":");

  return `${dateInformation[1]} ${dateInformation[2]}, ${timeInformation[0]}:${timeInformation[1]}`;
}

/** Deletes all comments from the datastore. */
function deleteAllComments() {
  fetch(new Request('/delete-data?whichData="all"', {method: 'POST'}))
    .then(response => {
      displayCommentSection();
    });
}

/** Deletes the comment with id `commentId` from the datastore. */
function deleteThisComment(commentId) {
  fetch(new Request(`/delete-data?whichData=${commentId}`, 
    {method: 'POST'}))
    .then(response => {
      displayCommentSection();
    })
}
