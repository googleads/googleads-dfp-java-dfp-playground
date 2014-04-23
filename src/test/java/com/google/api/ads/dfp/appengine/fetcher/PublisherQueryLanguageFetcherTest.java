// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.dfp.appengine.fetcher;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.ads.dfp.appengine.util.Channels;
import com.google.api.ads.dfp.jaxws.v201306.ApiException_Exception;
import com.google.api.ads.dfp.jaxws.v201306.PublisherQueryLanguageServiceInterface;
import com.google.api.ads.dfp.jaxws.v201306.ResultSet;
import com.google.api.ads.dfp.jaxws.v201306.Statement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

/**
 * Tests for {@link PublisherQueryLanguageFetcher} implementation.
 *
 * @author Jeff Sham
 */
@RunWith(JUnit4.class)
public class PublisherQueryLanguageFetcherTest {

  private static final String CHANNEL_KEY = "channel-key";
  private static final String ERROR_STRING = "error";
  private static final String FILTER_TEXT = "filter-text";
  private static final String TAG = "tag";
  private static final String REQUEST_ID = "reqId";

  @Mock private Channels channels;
  private PublisherQueryLanguageFetcher pqlFetcher;
  @Mock private PublisherQueryLanguageServiceInterface pqlService;

  /**
   * Constructor.
   */
  public PublisherQueryLanguageFetcherTest() {}

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    pqlFetcher = new PublisherQueryLanguageFetcher(null, channels);
  }

  /**
   * Test method for {@link PublisherQueryLanguageFetcher#fetch}.
   */
  @Test
  public void testFetchObjects_withResults() throws ApiException_Exception {
    ArgumentCaptor<Statement> statementCapture = ArgumentCaptor.forClass(Statement.class);
    when(pqlService.select(statementCapture.capture())).thenReturn(new ResultSet());

    pqlFetcher.fetch(FILTER_TEXT, CHANNEL_KEY, TAG, REQUEST_ID, pqlService);

    verify(channels).sendObjects(eq(CHANNEL_KEY), Mockito.<List<String[]>>anyObject(),
        eq(TAG), eq(REQUEST_ID));

    assertEquals(statementCapture.getValue().getQuery(), FILTER_TEXT);
  }

  /**
   * Test method for {@link PublisherQueryLanguageFetcher#fetch}.
   */
  @Test
  public void testFetch_throwsApiException() throws ApiException_Exception {
    when(pqlService.select(any(Statement.class)))
        .thenThrow(new ApiException_Exception(ERROR_STRING, null));

    pqlFetcher.fetch(FILTER_TEXT, CHANNEL_KEY, TAG, REQUEST_ID, pqlService);

    verify(channels).sendErrorChannelMessage(CHANNEL_KEY, TAG, REQUEST_ID, ERROR_STRING);
  }

}
