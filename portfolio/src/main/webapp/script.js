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
  * Generate a random image url from the available pictures of Penny
  */
function generateImageUrl() {
    const imageIndex = Math.floor(Math.random() * NUM_AVAILABLE_IMAGES) + 1;
    return 'images/Penny-' + imageIndex + '.JPG';
}

/**
 * Replaces the previous image button with a new image button that has the same 
 * functionality but with a new, random image. 
 */
function newImageButton() {

    const pennyButton = document.getElementById('penny-button');
    const currentImgUrl = pennyButton.getAttribute("src");
    let newImgUrl = currentImgUrl;

    //ensure new image is not the same as old one 
    while (newImgUrl === currentImgUrl) {
        newImgUrl = generateImageUrl();
    }

    const pennyContainer = document.getElementById('penny-container');
    let newHTML = `<input id="penny-button" type="image" src="${newImgUrl}"\
     onClick="newImageButton()"/>`;
    pennyContainer.innerHTML = newHTML;
}

/**
  * Automatically fill gallery page with all available images
  */
function fillGallery() {
    const imgsContainer = document.getElementById('penny-gallery-images');

    let htmlToAdd = '';
    for (let i = 1; i <= NUM_AVAILABLE_IMAGES; i++) {
        htmlToAdd += `<a href="images/Penny-${i}.JPG">\
        <img src="images/Penny-${i}.JPG"/></a> `
    }

    imgsContainer.innerHTML = htmlToAdd;
}
