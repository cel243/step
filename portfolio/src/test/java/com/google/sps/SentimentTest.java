package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.*;

import com.google.sps.functionality.SentimentAnalyzer;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.ClassificationCategory;
import com.google.api.gax.rpc.InvalidArgumentException;
import java.lang.Throwable;
import com.google.api.gax.grpc.GrpcStatusCode;
import io.grpc.Status;

@RunWith(JUnit4.class)
public final class SentimentTest {

  private static LanguageServiceClient mockLanguageServiceClient;
  private static AnalyzeSentimentResponse mockSentimentResponse;
  private static Sentiment mockSentiment;
  private static AnalyzeEntitiesResponse mockAnalyzeEntitiesResponse;
  private static Entity mockEntityOne;
  private static Entity mockEntityTwo;
  private static ClassifyTextResponse mockClassifyTextResponse;
  private static ClassificationCategory mockClassificationCategoryOne;
  private static ClassificationCategory mockClassificationCategoryTwo;


  private static final String TEST_STRING = "test";

  private static final String STRING_WITH_ENTITIES = "Entity One and Entity Two";
  private static final String HAS_LINK_FOR_ENTITY_ONE = 
    "<a target=\"_blank\" href=\"entity_one.com\">Entity One</a> and Entity Two";
  private static final String HAS_LINK_FOR_ENTITY_TWO = 
    "Entity One and <a target=\"_blank\" href=\"entity_two.com\">Entity Two</a>";
  private static final String HAS_LINKS_FOR_BOTH_ENTITIES = 
    "<a target=\"_blank\" href=\"entity_one.com\">Entity One</a>" +
    " and <a target=\"_blank\" href=\"entity_two.com\">Entity Two</a>";
  private static final String ENTITY_ONE_NAME = "Entity One";
  private static final String ENTITY_ONE_LINK = "entity_one.com";
  private static final String ENTITY_TWO_NAME = "Entity Two"; 
  private static final String ENTITY_TWO_LINK = "entity_two.com";

  private static Map<String, String> ENTITY_ONE_METADATA_MAP = new HashMap<String, String>();
  private static Map<String, String> ENTITY_TWO_METADATA_MAP = new HashMap<String, String>();

  private static final String CATEGORY_NAME_ONE = "Category One";
  private static final String CATEGORY_NAME_TWO = "Category Two";

  private static boolean isDocument(Document document) {
    return true;
  }
  private static boolean isAnalyzeEntitiesRequest(
    AnalyzeEntitiesRequest analyzeEntitiesRequest) {
    return true;
  }
  private static boolean isClassifyTextRequest(
    ClassifyTextRequest classifyTextRequest) {
    return true;
  }

  @Before
  public void setUp() {
    mockLanguageServiceClient = mock(LanguageServiceClient.class);
    mockSentimentResponse = mock(AnalyzeSentimentResponse.class);
    mockSentiment = mock(Sentiment.class);

    mockAnalyzeEntitiesResponse = mock(AnalyzeEntitiesResponse.class);
    mockEntityOne = mock(Entity.class);
    mockEntityTwo = mock(Entity.class);

    mockClassifyTextResponse = mock(ClassifyTextResponse.class);
    mockClassificationCategoryOne = mock(ClassificationCategory.class);
    mockClassificationCategoryTwo = mock(ClassificationCategory.class);

    when(mockLanguageServiceClient.analyzeSentiment(argThat(SentimentTest::isDocument)))
      .thenReturn(mockSentimentResponse);
    when(mockSentimentResponse.getDocumentSentiment())
      .thenReturn(mockSentiment);

    when(mockLanguageServiceClient
      .analyzeEntities(argThat(SentimentTest::isAnalyzeEntitiesRequest)))
      .thenReturn(mockAnalyzeEntitiesResponse);
    when(mockEntityOne.getName()).thenReturn(ENTITY_ONE_NAME);
    when(mockEntityTwo.getName()).thenReturn(ENTITY_TWO_NAME);

    when(mockClassificationCategoryOne.getName()).thenReturn(CATEGORY_NAME_ONE);
    when(mockClassificationCategoryTwo.getName()).thenReturn(CATEGORY_NAME_TWO);

    ENTITY_ONE_METADATA_MAP.put("wikipedia_url", ENTITY_ONE_LINK);
    ENTITY_TWO_METADATA_MAP.put("wikipedia_url", ENTITY_TWO_LINK);
  }

  @Test 
  public void negativeSentiment() {
    // when the sentiment analyzer returns -1, then the SentimentAnalyzer class
    // classifies the text as NEGATIVE.

    when(mockSentiment.getScore()).thenReturn((float) -1);

    SentimentAnalyzer.SentimentType actual = SentimentAnalyzer.getSentiment(
      TEST_STRING, mockLanguageServiceClient);
    SentimentAnalyzer.SentimentType expected = SentimentAnalyzer
      .SentimentType.NEGATIVE;

    Assert.assertEquals(expected, actual);
  }

  @Test 
  public void positiveSentiment() {
    // when the sentiment analyzer returns 1, then the SentimentAnalyzer class
    // classifies the text as POSITIVE.
    
    when(mockSentiment.getScore()).thenReturn((float) 1);

    SentimentAnalyzer.SentimentType actual = SentimentAnalyzer.getSentiment(
      TEST_STRING, mockLanguageServiceClient);
    SentimentAnalyzer.SentimentType expected = SentimentAnalyzer
      .SentimentType.POSITIVE;

    Assert.assertEquals(expected, actual);
  }

  @Test 
  public void neutralSentiment() {
    // when the sentiment analyzer returns 0, then the SentimentAnalyzer class
    // classifies the text as NEUTRAL.
    
    when(mockSentiment.getScore()).thenReturn((float) 0);

    SentimentAnalyzer.SentimentType actual = SentimentAnalyzer.getSentiment(
      TEST_STRING, mockLanguageServiceClient);
    SentimentAnalyzer.SentimentType expected = SentimentAnalyzer
      .SentimentType.NEUTRAL;

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noAvailableEntityLinks() {
    // When entities are found but neither has any link, the string is
    // unchanged. 

    when(mockAnalyzeEntitiesResponse.getEntitiesList())
      .thenReturn(Arrays.asList(mockEntityOne, mockEntityTwo));
    when(mockEntityOne.containsMetadata("wikipedia_url"))
      .thenReturn(false);
    when(mockEntityTwo.containsMetadata("wikipedia_url"))
      .thenReturn(false);
    
    String actual = SentimentAnalyzer.getHTMLWithNamedEntityLinks(STRING_WITH_ENTITIES, mockLanguageServiceClient);
    String expected = STRING_WITH_ENTITIES;

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noEntitiesFound() {
    // When there are no entities, the string remains unchanged.

    when(mockAnalyzeEntitiesResponse.getEntitiesList())
      .thenReturn(Arrays.asList());

    String actual = SentimentAnalyzer.getHTMLWithNamedEntityLinks(STRING_WITH_ENTITIES, mockLanguageServiceClient);
    String expected = STRING_WITH_ENTITIES;

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void onlyEntityOneHasLink() {
    // When only the first entity has a link, only that link is added
    // to the string.

    when(mockAnalyzeEntitiesResponse.getEntitiesList())
      .thenReturn(Arrays.asList(mockEntityOne, mockEntityTwo));
    when(mockEntityOne.containsMetadata("wikipedia_url"))
      .thenReturn(true);
    when(mockEntityOne.getMetadataMap()).thenReturn(ENTITY_ONE_METADATA_MAP);
    when(mockEntityTwo.containsMetadata("wikipedia_url"))
      .thenReturn(false);
    
    String actual = SentimentAnalyzer.getHTMLWithNamedEntityLinks(STRING_WITH_ENTITIES, mockLanguageServiceClient);
    String expected = HAS_LINK_FOR_ENTITY_ONE;

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void onlyEntityTwoHasLink() {
    // When only the second entity has a link, only that link is added
    // to the string.

    when(mockAnalyzeEntitiesResponse.getEntitiesList())
      .thenReturn(Arrays.asList(mockEntityOne, mockEntityTwo));
    when(mockEntityOne.containsMetadata("wikipedia_url"))
      .thenReturn(false);
    when(mockEntityTwo.containsMetadata("wikipedia_url"))
      .thenReturn(true);
    when(mockEntityTwo.getMetadataMap()).thenReturn(ENTITY_TWO_METADATA_MAP);
    
    String actual = SentimentAnalyzer.getHTMLWithNamedEntityLinks(STRING_WITH_ENTITIES, mockLanguageServiceClient);
    String expected = HAS_LINK_FOR_ENTITY_TWO;

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void bothEntitiesHaveLinks() {
    // When both entities have links, both links are inserted
    // into the string.

    when(mockAnalyzeEntitiesResponse.getEntitiesList())
      .thenReturn(Arrays.asList(mockEntityOne, mockEntityTwo));
    when(mockEntityOne.containsMetadata("wikipedia_url"))
      .thenReturn(true);
    when(mockEntityOne.getMetadataMap()).thenReturn(ENTITY_ONE_METADATA_MAP);
    when(mockEntityTwo.containsMetadata("wikipedia_url"))
      .thenReturn(true);
    when(mockEntityTwo.getMetadataMap()).thenReturn(ENTITY_TWO_METADATA_MAP);
    
    String actual = SentimentAnalyzer.getHTMLWithNamedEntityLinks(STRING_WITH_ENTITIES, mockLanguageServiceClient);
    String expected = HAS_LINKS_FOR_BOTH_ENTITIES;

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noTopicFound() {
    // When no topic can be found (the function throws an exception)
    // then the topic is simply the empty string.

    when(mockLanguageServiceClient.classifyText(argThat(SentimentTest::isClassifyTextRequest)))
      .thenThrow(new com.google.api.gax.rpc.InvalidArgumentException(
        new Throwable(), GrpcStatusCode.of(Status.INVALID_ARGUMENT.getCode()), false));
    
    String actual = SentimentAnalyzer.getTopic(TEST_STRING, mockLanguageServiceClient);
    String expected = "";

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void returnTopicIfFound() {
    // When there is one topic, the topic is the name of that topic
    // category.

    when(mockLanguageServiceClient.classifyText(argThat(SentimentTest::isClassifyTextRequest)))
      .thenReturn(mockClassifyTextResponse);
    when(mockClassifyTextResponse.getCategoriesList())
      .thenReturn(Arrays.asList(mockClassificationCategoryOne));
    when(mockClassificationCategoryOne.getConfidence()).thenReturn((float) 1);
    
    String actual = SentimentAnalyzer.getTopic(TEST_STRING, mockLanguageServiceClient);
    String expected = CATEGORY_NAME_ONE;

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void returnHigherConfidenceTopic() {
    // When there are two topics detected, the one with higher confidence 
    // is returned.

    when(mockLanguageServiceClient.classifyText(argThat(SentimentTest::isClassifyTextRequest)))
      .thenReturn(mockClassifyTextResponse);
    when(mockClassifyTextResponse.getCategoriesList())
      .thenReturn(Arrays.asList(mockClassificationCategoryOne, mockClassificationCategoryTwo));
    when(mockClassificationCategoryOne.getConfidence()).thenReturn((float) 1);
    when(mockClassificationCategoryTwo.getConfidence()).thenReturn((float) 2);
    
    String actual = SentimentAnalyzer.getTopic(TEST_STRING, mockLanguageServiceClient);
    String expected = CATEGORY_NAME_TWO;

    Assert.assertEquals(expected, actual);
  }
}
