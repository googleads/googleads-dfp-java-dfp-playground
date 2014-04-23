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

import com.google.api.ads.dfp.appengine.util.Channels;
import com.google.api.ads.dfp.jaxws.factory.DfpServices;
import com.google.api.ads.dfp.jaxws.v201306.ApiException_Exception;
import com.google.api.ads.dfp.jaxws.v201306.LineItemServiceInterface;
import com.google.api.ads.dfp.jaxws.v201306.OrderServiceInterface;
import com.google.api.ads.dfp.jaxws.v201306.Statement;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

/**
 * A class to make API requests to fetch order and line item entities.
 *
 * @author Jeff Sham
 */
public final class OrderLineItemFetcher extends AbstractFetcher {

  @Inject
  public OrderLineItemFetcher(DfpServices dfpServices, Channels channels) {
    super(dfpServices, channels);
  }

  public void fetch(String filterText, String channelKey, String tag, String requestId,
      DfpSession session) {
    OrderServiceInterface orderService =
        dfpServices.get(session, OrderServiceInterface.class);
    LineItemServiceInterface lineItemService =
        dfpServices.get(session, LineItemServiceInterface.class);
    fetch(filterText, channelKey, tag, requestId, orderService, lineItemService);
  }

  /**
   * Fetch orders and the nested line items.
   *
   * @param filterText the PQL syntax filter text to filter objects by
   * @param channelKey the key to send a message via the Channel API
   * @param tag identifies that objects that are sent via the channel
   * @param requestId identifies the incoming data request
   * @param orderService used to fetch orders
   * @param lineItemService used to fetch line items
   */
  @VisibleForTesting
  void fetch(String filterText, String channelKey, String tag, String requestId,
      final OrderServiceInterface orderService, final LineItemServiceInterface lineItemService) {
    // Create the order page fetcher.
    PageFetcher orderPageFetcher = new PageFetcher() {
      public Object getByStatement(Statement statement) throws ApiException_Exception {
        return orderService.getOrdersByStatement(statement);
      }
    };

    // Create the line item page fetcher.
    PageFetcher lineItemPageFetcher = new PageFetcher() {
      public Object getByStatement(Statement statement) throws ApiException_Exception {
        return lineItemService.getLineItemsByStatement(statement);
      }
    };

    // Create all fetcher that gets all line items.
    AllSingleEntityFetcher lineItemAllFetcher = new AllSingleEntityFetcher(lineItemPageFetcher,
        channels, channelKey, tag + "-li", requestId);

    // Fetch all orders and nested line items.
    new AllNestedFetcher(orderPageFetcher, lineItemAllFetcher, channels, channelKey, tag, requestId)
        .fetchAll("WHERE orderId = ");
  }
}
