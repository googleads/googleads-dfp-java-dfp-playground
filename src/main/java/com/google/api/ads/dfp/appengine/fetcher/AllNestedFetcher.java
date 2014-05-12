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
import com.google.api.ads.dfp.jaxws.utils.v201403.StatementBuilder;
import com.google.api.ads.dfp.jaxws.v201403.ApiException_Exception;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import org.apache.commons.beanutils.PropertyUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to fetch all objects of one type and one level of nested objects.
 *
 * @author Jeff Sham
 */
class AllNestedFetcher {

  private final Logger log = Logger.getLogger(this.getClass().getName());

  /** Fetcher to get a page of parent entities with the API. */
  private final PageFetcher parentPageFetcher;

  /** Fetcher to get all the entities that belong to the parent. */
  private final AllSingleEntityFetcher childAllFetcher;

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
   * @param parentPageFetcher fetcher used to make API calls for parent object
   * @param childAllFetcher fetcher used to fetch all of child objects
   * @param channels provides communication via Channel API
   * @param channelKey the key to send a message via the Channel API
   * @param tag identifies that objects that are sent via the channel
   * @param requestId identifies the incoming data request
   */
  public AllNestedFetcher(PageFetcher parentPageFetcher, AllSingleEntityFetcher childAllFetcher,
      Channels channels, String channelKey, String tag, String requestId) {
    this.parentPageFetcher = Preconditions.checkNotNull(parentPageFetcher);
    this.childAllFetcher = Preconditions.checkNotNull(childAllFetcher);
    this.channels = Preconditions.checkNotNull(channels);
    this.channelKey = channelKey;
    this.tag = tag;
    this.requestId = requestId;
  }

  /**
   * Method used to fetch all of an entity and it's nested child. Exceptions are handled by logging
   * and sending error messages back to the user through the channel.
   *
   * @param filterText the filter that should be used to filter for the parent ID in the child
   */
  @VisibleForTesting
  void fetchAll(String filterText) {
    try {
      StatementBuilder statementBuilder =
          new StatementBuilder().limit(StatementBuilder.SUGGESTED_PAGE_LIMIT);
      int totalResultSetSize = 0;
      do {
        Object page = parentPageFetcher.getByStatement(statementBuilder.toStatement());
        totalResultSetSize = (Integer) PropertyUtils.getProperty(page, "totalResultSetSize");
        if (totalResultSetSize == 0) {
          channels.sendNoResultMessage(channelKey, tag, requestId);
        }
        List<?> objects = (List<?>) PropertyUtils.getProperty(page, "results");
        for (Object object : objects) {
          channels.sendSingleObject(channelKey, object, tag, requestId);
          childAllFetcher.fetchAll(filterText + PropertyUtils.getProperty(object, "id"));
        }
        statementBuilder.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
      } while (statementBuilder.getOffset() < totalResultSetSize);
    } catch (ApiException_Exception e) {
      channels.sendErrorChannelMessage(channelKey, tag, requestId, e.getMessage());
    } catch (Exception e) {
      channels.sendErrorChannelMessage(channelKey, tag, requestId, e.getMessage());
      log.log(Level.SEVERE, "Error fetching objects.", e);
    }
  }
}
