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
import com.google.api.ads.dfp.jaxws.utils.v201306.Pql;
import com.google.api.ads.dfp.jaxws.v201306.ApiException_Exception;
import com.google.api.ads.dfp.jaxws.v201306.PublisherQueryLanguageServiceInterface;
import com.google.api.ads.dfp.jaxws.v201306.ResultSet;
import com.google.api.ads.dfp.jaxws.v201306.Statement;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

import java.util.List;
import java.util.logging.Level;

/**
 * A class to make API requests to fetch publisher query language table results.
 *
 * @author Jeff Sham
 */
public class PublisherQueryLanguageFetcher extends AbstractFetcher {

  @Inject
  public PublisherQueryLanguageFetcher(
      DfpServices dfpServices, Channels channels) {
    super(dfpServices, channels);
  }

  public void fetch(String filterText, String channelKey, String tag, String requestId,
      DfpSession session) {
    fetch(filterText, channelKey, tag, requestId,
        dfpServices.get(session, PublisherQueryLanguageServiceInterface.class));
  }

  /**
   * Fetch publisher query language table results.
   *
   * @param filterText the PQL syntax filter text to filter objects by
   * @param channelKey the key to send a message via the Channel API
   * @param tag identifies that objects that are sent via the channel
   * @param requestId identifies the incoming data request
   * @param pqlService used to fetch tables
   */
  @VisibleForTesting
  void fetch(String filterText, String channelKey, String tag, String requestId,
      final PublisherQueryLanguageServiceInterface pqlService) {
    try {
      // Create statement with filter.
      Statement filterStatement = new Statement();
      filterStatement.setQuery(filterText);

      // Make select request.
      ResultSet resultSet = pqlService.select(filterStatement);
      List<String[]> stringResult = Pql.resultSetToStringArrayList(resultSet);

      channels.sendObjects(channelKey, stringResult, tag, requestId);
    } catch (ApiException_Exception e) {
      channels.sendErrorChannelMessage(channelKey, tag, requestId, e.getMessage());
    } catch (Exception e) {
      channels.sendErrorChannelMessage(channelKey, tag, requestId, e.getMessage());
      log.log(Level.SEVERE, "Error fetching objects.", e);
    }
  }
}
