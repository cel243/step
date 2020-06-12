package com.google.sps.functionality;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

/** Class that translates text.  */
public class TextTranslator { 
  /** Translates `text` from original language to the language
    * specified by `languageCode` and returns the translated text. 
    * If `languageCode` is "none", then returns `text` unchanged.
    */
  public static String translateText(String text, String languageCode) {
   if (languageCode.equals("none")) {
      return text;
    }

    Translate translate = TranslateOptions.getDefaultInstance().getService();
    Translation translation = translate.translate(text, 
      Translate.TranslateOption.targetLanguage(languageCode));
    String translatedText = translation.getTranslatedText();
    return translatedText;
  }
}
