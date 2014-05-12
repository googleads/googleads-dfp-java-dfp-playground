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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.ads.dfp.appengine.util.Channels;
import com.google.api.ads.dfp.jaxws.v201403.ApiException_Exception;
import com.google.api.ads.dfp.jaxws.v201403.Order;
import com.google.api.ads.dfp.jaxws.v201403.OrderPage;
import com.google.api.ads.dfp.jaxws.v201403.Statement;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Tests for {@link AbstractFetcher} implementation.
 *
 * @author Jeff Sham
 */
@RunWith(JUnit4.class)
public class AbstractFetcherTest {

  private static final String CHANNEL_KEY = "channel-key";
  private static final String ERROR_STRING = "error";
  private static final String EMPTY_FILTER_TEXT = "";
  private static final String LIMIT_FILTER_TEXT = "LIMIT 1";
  private static final String TAG = "tag";
  private static final String REQUEST_ID = "reqId";

  @Mock private Channels channels;
  private AbstractFetcher fetcher;
  @Mock private PageFetcher pageFetcher;
  private OrderPage page;
  private List<Order> orders;

  /**
   * Constructor.
   */
  public AbstractFetcherTest() {}

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    fetcher = new AbstractFetcher(null, channels) {
      public void fetch(String filterText, String channelKey, String tag, String requestId,
          DfpSession session) {}
    };

    page = new OrderPage();
    page.setTotalResultSetSize(1);
    orders = Lists.newArrayList();
    orders.add(new Order());
    page.getResults().addAll(orders);
  }

  /**
   * Test method for {@link AbstractFetcher#fetchObjects}.
   */
  @Test
  public void testFetchObjects_withEmptyFilterText() throws ApiException_Exception {
    when(pageFetcher.getByStatement(any(Statement.class))).thenReturn(page);

    fetcher.fetchObjects(EMPTY_FILTER_TEXT, CHANNEL_KEY, TAG, REQUEST_ID, pageFetcher);

    verify(channels).sendObjects(CHANNEL_KEY, orders, TAG, REQUEST_ID);
  }

  /**
   * Test method for {@link AbstractFetcher#fetchObjects}.
   */
  @Test
  public void testFetchObjects_withLimitFilterText() throws ApiException_Exception,
      IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    when(pageFetcher.getByStatement(any(Statement.class))).thenReturn(page);

    fetcher.fetchObjects(LIMIT_FILTER_TEXT, CHANNEL_KEY, TAG, REQUEST_ID, pageFetcher);

    verify(channels).sendPage(CHANNEL_KEY, page, TAG, REQUEST_ID);
  }

  /**
   * Test method for {@link AbstractFetcher#fetchObjects}.
   */
  @Test
  public void testFetchObjects_throwsApiException() throws ApiException_Exception {
    when(pageFetcher.getByStatement(any(Statement.class)))
        .thenThrow(new ApiException_Exception(ERROR_STRING, null));

    fetcher.fetchObjects(LIMIT_FILTER_TEXT, CHANNEL_KEY, TAG, REQUEST_ID, pageFetcher);

    verify(channels).sendErrorChannelMessage(CHANNEL_KEY, TAG, REQUEST_ID, ERROR_STRING);
  }
}
