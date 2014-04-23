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
import com.google.api.ads.dfp.jaxws.v201306.ApiException_Exception;
import com.google.api.ads.dfp.jaxws.v201306.LineItem;
import com.google.api.ads.dfp.jaxws.v201306.LineItemPage;
import com.google.api.ads.dfp.jaxws.v201306.LineItemServiceInterface;
import com.google.api.ads.dfp.jaxws.v201306.Order;
import com.google.api.ads.dfp.jaxws.v201306.OrderPage;
import com.google.api.ads.dfp.jaxws.v201306.OrderServiceInterface;
import com.google.api.ads.dfp.jaxws.v201306.Statement;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

/**
 * Tests for OrderLineItemFetcher implementation.
 *
 * @author Jeff Sham
 */
@RunWith(JUnit4.class)
public class OrderLineItemFetcherTest {

  private static final String CHANNEL_KEY = "channel-key";
  private static final String ERROR_STRING = "error";
  private static final String FILTER_STATEMENT = "";
  private static final String LINE_ITEM_TAG = "order-li";
  private static final String ORDER_TAG = "order";
  private static final String REQUEST_ID = "reqId";

  @Mock private Channels channels;
  private OrderLineItemFetcher orderLineItemFetcher;
  @Mock private LineItemServiceInterface lineItemService;
  @Mock private OrderServiceInterface orderService;
  private OrderPage orderPage;
  private LineItemPage lineItemPage;
  List<Order> orders;
  List<LineItem> lineItems;

  /**
   * Constructor.
   */
  public OrderLineItemFetcherTest() {}

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    orderLineItemFetcher = new OrderLineItemFetcher(null, channels);

    orderPage = new OrderPage();
    orders = Lists.newArrayList();
    Order order = new Order();
    orders.add(order);
    lineItemPage = new LineItemPage();
    lineItems = Lists.newArrayList();
    LineItem lineItem = new LineItem();
    lineItems.add(lineItem);
  }

  /**
   * Test method for {@link OrderLineItemFetcher#fetch}.
   */
  @Test
  public void testFetch_success() throws ApiException_Exception {
    orderPage.setTotalResultSetSize(1);
    orderPage.getResults().addAll(orders);
    lineItemPage.setTotalResultSetSize(1);
    lineItemPage.getResults().addAll(lineItems);
    when(orderService.getOrdersByStatement(any(Statement.class))).thenReturn(orderPage);
    when(lineItemService.getLineItemsByStatement(any(Statement.class))).thenReturn(lineItemPage);

    orderLineItemFetcher.fetch(FILTER_STATEMENT, CHANNEL_KEY, ORDER_TAG, REQUEST_ID,
        orderService, lineItemService);

    verify(channels).sendSingleObject(CHANNEL_KEY, orders.get(0), ORDER_TAG, REQUEST_ID);
    verify(channels).sendObjects(CHANNEL_KEY, lineItems, LINE_ITEM_TAG, REQUEST_ID);
  }

  /**
   * Test method for {@link OrderLineItemFetcher#fetch}.
   */
  @Test
  public void testFetch_throwsApiException() throws ApiException_Exception {
    when(orderService.getOrdersByStatement(any(Statement.class)))
        .thenThrow(new ApiException_Exception(ERROR_STRING, null));

    orderLineItemFetcher.fetch(FILTER_STATEMENT, CHANNEL_KEY, ORDER_TAG, REQUEST_ID,
        orderService, lineItemService);

    verify(channels).sendErrorChannelMessage(CHANNEL_KEY, ORDER_TAG, REQUEST_ID, ERROR_STRING);
  }
}
