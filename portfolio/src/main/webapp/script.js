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
  * @return {String} This returns new random valid image url.
  */
function generateImageUrl() {
    const imageIndex = Math.floor(Math.random() * NUM_AVAILABLE_IMAGES) + 1;
    return 'images/Penny-' + imageIndex + '.JPG';
}

/**
  * Generates the HTML for corresponding to `imageIndex + 1`.
  * @param {int} imageIndex The index of the desired image - 1.
  * @returns {String} Returns HTML image with index `imageIndex + 1`
  */
function getImageHTML(imageIndex) {
    return `<input type="image" src="images/Penny-${imageIndex + 1}.JPG"` + 
            ` onClick="enlargeThisImage(${imageIndex + 1})"/> `
}

/**
 * Replaces the previous image button with a new image button that has the same 
 * functionality but with a new, random image. 
 */
function newImageButton() {
    const pennyButton = document.getElementById('penny-button');
    const currentImageUrl = pennyButton.getAttribute('src');
    let newImageUrl = currentImageUrl;

    /* ensure new image is not the same as old one */ 
    while (newImageUrl === currentImageUrl) {
        newImageUrl = generateImageUrl();
    }

    const pennyContainer = document.getElementById('penny-container');
    let newHTML = `<input id="penny-button" type="image" src="${newImageUrl}"` +
        ` onClick="newImageButton()"/>`;
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
function enlargeThisImage(thisImage) {
    const imagesContainer = document.getElementById('penny-gallery-images');
    imagesContainer.innerHTML = ''; 

    const enlargedContainer = document.getElementById('enlarged-image');
    enlargedContainer.innerHTML = `<a href="images/Penny-${thisImage}.JPG">` +
            `<img src="images/Penny-${thisImage}.JPG"/></a> `

    const smallContainer = document.getElementById('small-images');
    let htmlToAdd = '';
    let allImageHTML = [...Array(NUM_AVAILABLE_IMAGES).keys()]
        .filter(imageIndex => imageIndex !== thisImage)
        .map(getImageHTML);
    allImageHTML.forEach(imageHTML => htmlToAdd += imageHTML);
    smallContainer.innerHTML = htmlToAdd;
}
