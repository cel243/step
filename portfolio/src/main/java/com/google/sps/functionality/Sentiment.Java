package com.google.sps.functionality;

import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import java.util.List;
import java.util.Map;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.Sentiment;

/** Class that analyzes the sentiment and content of text.  */
@WebServlet("/data")
public class Sentiment { 
  /** 
    * Analyzes the sentiment of `text` and returns "Negative",
    * "Positive", or "Neutral" depending on the detected sentiment.
    */
  public static String getSentiment(String text) {
    Document document = Document.newBuilder().setContent(text).setType
      (Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = new LanguageServiceClient.create();

    Sentiment sentiment = languageService.analyzeSentiment(document)
      .getDocumentSentiment();
    float score = sentiment.getScore();

    languageService.close();

    if (score < -0.1) {
      return "Negative"
    } else if (score < 0.1) {
      return "Neutral";
    } else {
      return "Positive";
    }
  }

  /** 
    * Returns an HTML String that contains `text`, but with wikipedia links
    * embedded in the string that give more information about the named 
    * entities that were detected in `text`, if the entity recognition
    * analysis of the text yields these wikipedia links.
    */
  public static String getHTMLWithNamedEnttiyLinks(String text) {
    Document document = Document.newBuilder().setContent(text).setType
      (Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = new LanguageServiceClient.create();

    List<Entity> entities = languageService.analyzeEntities(document)
      .getEntitiesList();
    

    langaugeService.close();
  }
}