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
import com.google.api.ads.dfp.jaxws.utils.v201306.StatementBuilder;
import com.google.api.ads.dfp.jaxws.v201306.ApiException_Exception;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import org.apache.commons.beanutils.PropertyUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to fetch all of an entity with multiple API calls.
 *
 * @author Jeff Sham
 */
class AllSingleEntityFetcher {

  private final Logger log = Logger.getLogger(this.getClass().getName());

  /** Fetcher to get a page of entity results with the API. */
  private final PageFetcher pageFetcher;

  /** Provides communication via the Channel API. */
  private final Channels channels;

  /** Specifies a client to send messages to via the Channel API. */
  private final String channelKey;

  /** Specifies the objects that are sent via the channel. */
  private final String tag;

  /** Specifies the request to respond to. */
  private final String requestId;

  /**
   * Constructor.
   *
   * @param pageFetcher fetcher used to make a single API call
   * @param channels provides communication via Channel API
   * @param channelKey the key to send a message via the Channel API
   * @param tag identifies that objects that are sent via the channel
   * @param requestId identifies the incoming data request
   */
  public AllSingleEntityFetcher(
      PageFetcher pageFetcher, Channels channels, String channelKey, String tag, String requestId) {
    this.pageFetcher = Preconditions.checkNotNull(pageFetcher);
    this.channels = Preconditions.checkNotNull(channels);
    this.channelKey = channelKey;
    this.tag = tag;
    this.requestId = requestId;
  }

  /**
   * Method used to fetch all of an entity possibly with multiple API calls. Exceptions are handled
   * by logging and sending error messages back to the user through the channel.
   *
   * @param filterText the text to filter results by
   */
  @VisibleForTesting
  void fetchAll(String filterText) {
    try {
      StatementBuilder statementBuilder =
          new StatementBuilder().where(filterText).limit(StatementBuilder.SUGGESTED_PAGE_LIMIT);
      int totalResultSetSize = 0;
      do {
        Object page = pageFetcher.getByStatement(statementBuilder.toStatement());
        channels.sendObjects(channelKey, (List<?>) PropertyUtils.getProperty(page, "results"), tag,
            requestId);
        statementBuilder.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
        totalResultSetSize = (Integer) PropertyUtils.getProperty(page, "totalResultSetSize");
      } while (statementBuilder.getOffset() < totalResultSetSize);
    } catch (ApiException_Exception e) {
      channels.sendErrorChannelMessage(channelKey, tag, requestId, e.getMessage());
    } catch (Exception e) {
      channels.sendErrorChannelMessage(channelKey, tag, requestId, e.getMessage());
      log.log(Level.SEVERE, "Error fetching objects.", e);
    }
  }
}
