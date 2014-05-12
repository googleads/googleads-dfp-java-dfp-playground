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
import com.google.api.ads.dfp.jaxws.v201403.ApiException_Exception;
import com.google.api.ads.dfp.jaxws.v201403.Statement;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Base class for all the fetchers.
 *
 * @author Jeff Sham
 */
abstract class AbstractFetcher implements Fetcher {

  private static final Pattern NO_LIMIT_OR_OFFSET =
      Pattern.compile("(?i)(?<=^|\\s)(LIMIT|OFFSET)(?=$|\\s)");

  protected final Logger log = Logger.getLogger(this.getClass().getName());
  protected final Channels channels;
  protected final DfpServices dfpServices;

  /**
   * Construct a fetcher.
   *
   * @param dfpServices provides DFP services
   * @param channels used for Channel API communication
   */
  public AbstractFetcher(DfpServices dfpServices, Channels channels) {
    Preconditions.checkNotNull(channels);
    this.dfpServices = dfpServices;
    this.channels = channels;
  }

  /**
   * Fetch API objects with a single call or multiple calls. Exceptions are handled by logging and
   * senting error messages back to the user through the channel.
   *
   * @param filterText the PQL syntax filter text to filter objects by
   * @param channelKey the key to send a message via the Channel API
   * @param tag identifies that objects that are sent via the channel
   * @param requestId identifies the incoming data request
   * @param pageFetcher used to fetch a page of results
   */
  @VisibleForTesting
  void fetchObjects(String filterText, String channelKey, String tag, String requestId,
      PageFetcher pageFetcher) {
    Preconditions.checkNotNull(pageFetcher);
    // Only make a single call if there is a limit or offset.
    if (filterTextHasLimitOrOffset(filterText)) {
      Statement filterStatement = new Statement();
      filterStatement.setQuery(filterText);
      try {
        Object page = pageFetcher.getByStatement(filterStatement);
        channels.sendPage(channelKey, page, tag, requestId);
      } catch (ApiException_Exception e) {
        channels.sendErrorChannelMessage(channelKey, tag, requestId, e.getMessage());
      } catch (Exception e) {
        channels.sendErrorChannelMessage(channelKey, tag, requestId, e.getMessage());
        log.log(Level.SEVERE, "Error fetching objects.", e);
      }
    } else {
      new AllSingleEntityFetcher(pageFetcher, channels, channelKey, tag, requestId)
          .fetchAll(filterText);
    }
  }

  /**
   * Checks if the filter text contains a limit or offset clause.
   *
   * @param filterText the filter text to check
   * @return whether the filter text contains a limit or offset clause
   */
  private boolean filterTextHasLimitOrOffset(String filterText) {
    return NO_LIMIT_OR_OFFSET.matcher(filterText).find();
  }
}
