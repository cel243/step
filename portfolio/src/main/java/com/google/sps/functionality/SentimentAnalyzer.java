package com.google.sps.functionality;

import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.ClassificationCategory;
import com.google.api.gax.rpc.InvalidArgumentException;
import java.util.List;
import java.util.Map;
import java.lang.StringBuilder;
import java.util.Comparator;


/** Class that analyzes the sentiment and content of text.  */
public class SentimentAnalyzer { 

  /** The three "moods" a comment can have. */
  public static enum SentimentType {
    POSITIVE,
    NEGATIVE,
    NEUTRAL,
  }

  /** 
    * Analyzes the sentiment of `text` and returns the appropriate
    * sentiment type depending on the detected sentiment.
    */
  public static SentimentType getSentiment(String text) {
    Document document = Document.newBuilder().setContent(text).setType
      (Document.Type.PLAIN_TEXT).build();
    float score = 0;

    try (LanguageServiceClient languageService = 
      LanguageServiceClient.create()) {
      Sentiment sentiment = languageService.analyzeSentiment(document)
        .getDocumentSentiment();
      score = sentiment.getScore();

      languageService.close();
    } catch (java.io.IOException e) {
      System.err.println("Failed to create LanguageServiceClient");
    }

    if (score < -0.5) {
      return SentimentType.NEGATIVE;
    } else if (score < 0.5) {
      return SentimentType.NEUTRAL;
    } else {
      return SentimentType.POSITIVE;
    } 
  }

  /** 
    * Returns an HTML String that contains `text`, but with wikipedia links
    * embedded in the string that give more information about the named 
    * entities that were detected in `text`, if the entity recognition
    * analysis of the text yields these wikipedia links.
    */
  public static String getHTMLWithNamedEntityLinks(String text) {
    Document document = Document.newBuilder().setContent(text).setType
      (Document.Type.PLAIN_TEXT).build();
    AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder().setDocument(document).build();
    StringBuilder textWithLinks = new StringBuilder(text);

    try (LanguageServiceClient languageService = 
      LanguageServiceClient.create()) {
      List<Entity> entities = languageService.analyzeEntities(request)
        .getEntitiesList();
      entities.forEach(entity -> insertLink(entity, textWithLinks));
      
      languageService.close();
    } catch (java.io.IOException e) {
      System.err.println("Failed to create LanguageServiceClient");
    }

    return textWithLinks.toString(); 
  }

  /** 
    * Inserts HTML into `textWithLinks` such that the first occurence
    * of the name of `entity` becomes a link to a wikipedia page
    * about that entity, if such a wikipedia link was found by the 
    * entity analysis. 
    */
  private static void insertLink(Entity entity, StringBuilder textWithLinks) {
    if (entity.containsMetadata("wikipedia_url")) {
        textWithLinks.insert(
          textWithLinks.indexOf(entity.getName()) + entity.getName().length(),
            "</a>");
        textWithLinks.insert(textWithLinks.indexOf(entity.getName()), 
          "<a target=\"_blank\" href=\""+
            entity.getMetadataMap().get("wikipedia_url")+"\">");
    }
  }

  /** 
    * Returns the highest-confidence topic if a topic could be 
    * determined from the text, and otherwise returns the 
    * empty string. 
    */
  public static String getTopic(String text) {
    Document document = Document.newBuilder().setContent(text).setType
      (Document.Type.PLAIN_TEXT).build();
    ClassifyTextRequest request = ClassifyTextRequest.newBuilder().setDocument(document).build();
    String topic = "";

    try (LanguageServiceClient languageService = 
      LanguageServiceClient.create()) {
      List<ClassificationCategory> categories = 
      languageService.classifyText(request).getCategoriesList();
      ClassificationCategory bestCategory = categories.stream()
        .max(Comparator.comparing(category -> category.getConfidence()))
        .get();
      topic = bestCategory.getName();

      languageService.close();
    } catch (java.io.IOException e) {
      System.err.println("Failed to create LanguageServiceClient");
    } catch (com.google.api.gax.rpc.InvalidArgumentException e) {
      //No action needed; no topic was detected
    }

    return topic;
  }
}
