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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.ads.dfp.appengine.util.Channels;
import com.google.api.ads.dfp.jaxws.utils.v201403.StatementBuilder;
import com.google.api.ads.dfp.jaxws.v201403.AdUnit;
import com.google.api.ads.dfp.jaxws.v201403.AdUnitPage;
import com.google.api.ads.dfp.jaxws.v201403.ApiException_Exception;
import com.google.api.ads.dfp.jaxws.v201403.Statement;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for AllFetcher implementation.
 *
 * @author Jeff Sham
 */
@RunWith(JUnit4.class)
public class AllSingleEntityFetcherTest {

  private static final String API_EXCEPTION = "API exception.";
  private static final String CHANNEL_KEY = "channel-key";
  private static final String FILTER_STATEMENT = "WHERE id = 123";
  private static final String TAG = "tag";
  private static final String REQUEST_ID = "reqId";

  private AllSingleEntityFetcher allFetcher;
  @Mock private Channels channels;
  private AdUnitPage page;
  @Mock private PageFetcher pageFetcher;

  /**
   * Constructor.
   */
  public AllSingleEntityFetcherTest() {}

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    page = new AdUnitPage();
    allFetcher = new AllSingleEntityFetcher(pageFetcher, channels, CHANNEL_KEY, TAG, REQUEST_ID);
  }

  /**
   * Test method for {@link AllSingleEntityFetcher#fetchAll}.
   */
  @Test
  public void testFetchOnePageOfResults() throws ApiException_Exception {
    page.setTotalResultSetSize(1);
    ArgumentCaptor<Statement> statementCapture = ArgumentCaptor.forClass(Statement.class);
    when(pageFetcher.getByStatement(statementCapture.capture())).thenReturn(page);

    allFetcher.fetchAll(FILTER_STATEMENT);

    verify(channels).sendObjects(CHANNEL_KEY, Lists.<AdUnit>newArrayList(), TAG, REQUEST_ID);

    assertTrue(statementCapture.getValue().getQuery().contains(FILTER_STATEMENT));
  }

  /**
   * Test method for {@link AllSingleEntityFetcher#fetchAll}.
   */
  @Test
  public void testFetchMultiplePagesOfResults() throws ApiException_Exception {
    page.setTotalResultSetSize(StatementBuilder.SUGGESTED_PAGE_LIMIT + 1);
    when(pageFetcher.getByStatement(any(Statement.class))).thenReturn(page);

    allFetcher.fetchAll("");

    verify(pageFetcher, times(2)).getByStatement(any(Statement.class));
    verify(channels, times(2)).sendObjects(CHANNEL_KEY, Lists.newArrayList(), TAG, REQUEST_ID);
  }

  /**
   * Test method for {@link AllSingleEntityFetcher#fetchAll}.
   */
  @Test
  public void testFetchThrowsApiException() throws ApiException_Exception {
    page.setTotalResultSetSize(1);
    when(pageFetcher.getByStatement(any(Statement.class)))
        .thenThrow(new ApiException_Exception(API_EXCEPTION, null));

    allFetcher.fetchAll("");

    verify(channels).sendErrorChannelMessage(CHANNEL_KEY, TAG, REQUEST_ID, API_EXCEPTION);
  }
}
