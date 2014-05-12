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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.ads.dfp.appengine.util.Channels;
import com.google.api.ads.dfp.jaxws.utils.v201403.StatementBuilder;
import com.google.api.ads.dfp.jaxws.v201403.ApiException_Exception;
import com.google.api.ads.dfp.jaxws.v201403.Order;
import com.google.api.ads.dfp.jaxws.v201403.OrderPage;
import com.google.api.ads.dfp.jaxws.v201403.Statement;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

/**
 * Tests for AllNestedFetcher implementation.
 *
 * @author Jeff Sham
 */
@RunWith(JUnit4.class)
public class AllNestedFetcherTest {

  private static final String CHANNEL_KEY = "channel-key";
  private static final String FILTER_STATEMENT = "WHERE orderId = ";
  private static final String TAG = "tag";
  private static final String REQUEST_ID = "reqId";

  @Mock private AllSingleEntityFetcher childrenAllFetcher;
  @Mock private Channels channels;
  private OrderPage orderPage;
  @Mock private PageFetcher parentPageFetcher;
  private List<Order> orders;

  /**
   * Constructor.
   */
  public AllNestedFetcherTest() {}

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    orderPage = new OrderPage();
    orders = Lists.newArrayList();
    Order order = new Order();
    orders.add(order);
  }

  /**
   * Test method for {@link AllNestedFetcher#fetchAll}.
   */
  @Test
  public void testFetchOneSetOfNestedResults() throws ApiException_Exception {
    orderPage.setTotalResultSetSize(1);
    orderPage.getResults().addAll(orders);
    when(parentPageFetcher.getByStatement(any(Statement.class))).thenReturn(orderPage);
    ArgumentCaptor<String> statementCapture = ArgumentCaptor.forClass(String.class);

    new AllNestedFetcher(parentPageFetcher, childrenAllFetcher, channels, CHANNEL_KEY, TAG,
        REQUEST_ID).fetchAll(FILTER_STATEMENT);

    verify(childrenAllFetcher).fetchAll(statementCapture.capture());
    verify(channels).sendSingleObject(CHANNEL_KEY, orders.get(0), TAG, REQUEST_ID);

    assertTrue(statementCapture.getValue().contains(FILTER_STATEMENT));
  }

  /**
   * Test method for {@link AllNestedFetcher#fetchAll}.
   */
  @Test
  public void testFetchMoreThanOneParentResults() throws ApiException_Exception {
    orderPage.setTotalResultSetSize(StatementBuilder.SUGGESTED_PAGE_LIMIT + 1);
    orderPage.getResults().addAll(orders);
    when(parentPageFetcher.getByStatement(any(Statement.class))).thenReturn(orderPage);

    new AllNestedFetcher(parentPageFetcher, childrenAllFetcher, channels, CHANNEL_KEY, TAG,
        REQUEST_ID).fetchAll(FILTER_STATEMENT);

    verify(parentPageFetcher, times(2)).getByStatement(any(Statement.class));
    verify(channels, times(2)).sendSingleObject(CHANNEL_KEY, orders.get(0), TAG, REQUEST_ID);
    verify(childrenAllFetcher, times(2)).fetchAll(anyString());
  }

  /**
   * Test method for {@link AllNestedFetcher#fetchAll}.
   */
  @Test
  public void testFetchNoParentResults() throws ApiException_Exception {
    List<Order> orders = Lists.newArrayList();
    orderPage.setTotalResultSetSize(0);
    orderPage.getResults().addAll(orders);
    when(parentPageFetcher.getByStatement(any(Statement.class))).thenReturn(orderPage);

    new AllNestedFetcher(parentPageFetcher, childrenAllFetcher, channels, CHANNEL_KEY, TAG,
        REQUEST_ID).fetchAll(FILTER_STATEMENT);

    verify(channels).sendNoResultMessage(CHANNEL_KEY, TAG, REQUEST_ID);
  }
}
