package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

@RunWith(JUnit4.class)
public final class TestSentiment {

  private static LanguageServiceClient mockLanguageServiceClient;
  private static AnalyzeSentimentResponse mockSentimentResponse;
  private static Sentiment mockSentiment;

  private static final String EXAMPLE_STRING = "test";
  private static boolean isDocument(Document document) {
    return true;
  }

  @Before
  public void setUp() {
    mockLanguageServiceClient = mock(LanguageServiceClient.class);
    mockSentimentResponse = mock(AnalyzeSentimentResponse.class);
    mockSentiment = mock(Sentiment.class);

    when(mockLanguageServiceClient.analyzeSentiment(argThat(TestSentiment::isDocument)))
      .thenReturn(mockSentimentResponse);
    when(mockSentimentResponse.getDocumentSentiment())
      .thenReturn(mockSentiment);
  }

  @Test 
  public void negativeSentiment() {
    // when the sentiemnt analyzer returns -1, then the SentimentAnalyzer class
    // classifies the text as NEGATIVE

    when(mockSentiment.getScore()).thenReturn((float) -1);

    SentimentAnalyzer.SentimentType actual = SentimentAnalyzer.getSentiment(
      EXAMPLE_STRING, mockLanguageServiceClient);
    SentimentAnalyzer.SentimentType expected = SentimentAnalyzer
      .SentimentType.NEGATIVE;

    Assert.assertEquals(expected, actual);
  }

  @Test 
  public void positiveSentiment() {
    // when the sentiemnt analyzer returns 1, then the SentimentAnalyzer class
    // classifies the text as POSITIVE
    
    when(mockSentiment.getScore()).thenReturn((float) 1);

    SentimentAnalyzer.SentimentType actual = SentimentAnalyzer.getSentiment(
      EXAMPLE_STRING, mockLanguageServiceClient);
    SentimentAnalyzer.SentimentType expected = SentimentAnalyzer
      .SentimentType.POSITIVE;

    Assert.assertEquals(expected, actual);
  }

  @Test 
  public void neutralSentiment() {
    // when the sentiemnt analyzer returns 0, then the SentimentAnalyzer class
    // classifies the text as NEUTRAL
    
    when(mockSentiment.getScore()).thenReturn((float) 0);

    SentimentAnalyzer.SentimentType actual = SentimentAnalyzer.getSentiment(
      EXAMPLE_STRING, mockLanguageServiceClient);
    SentimentAnalyzer.SentimentType expected = SentimentAnalyzer
      .SentimentType.NEUTRAL;

    Assert.assertEquals(expected, actual);
  }
}
