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
import com.google.api.ads.dfp.jaxws.v201306.CustomTargetingServiceInterface;
import com.google.api.ads.dfp.jaxws.v201306.Statement;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.inject.Inject;

/**
 * A class to make API requests to fetch custom targeting key and value entities.
 *
 * @author Jeff Sham
 */
public class CustomTargetingKeyAndValueFetcher extends AbstractFetcher {

  @Inject
  public CustomTargetingKeyAndValueFetcher(
      DfpServices dfpServices, Channels channels) {
    super(dfpServices, channels);
  }

  public void fetch(String filterText, String channelKey, String tag, String requestId,
      DfpSession session) {
    final CustomTargetingServiceInterface customTargetingService =
        dfpServices.get(session, CustomTargetingServiceInterface.class);

    // Create the custom targeting key page fetcher.
    PageFetcher customTargetingKeyPageFetcher = new PageFetcher() {

      public Object getByStatement(Statement statement) throws ApiException_Exception {
        return customTargetingService.getCustomTargetingKeysByStatement(statement);
      }
    };

    // Create the custom targeting value page fetcher.
    PageFetcher customTargetingValuePageFetcher = new PageFetcher() {

      public Object getByStatement(Statement statement) throws ApiException_Exception {
        return customTargetingService.getCustomTargetingValuesByStatement(statement);
      }
    };

    // Create all fetcher that gets all custom targeting values.
    AllSingleEntityFetcher customTargetingValueAllFetcher = new AllSingleEntityFetcher(
        customTargetingValuePageFetcher, channels, channelKey, tag + "-value", requestId);

    // Fetch all custom targeting keys and nested values.
    new AllNestedFetcher(
        customTargetingKeyPageFetcher, customTargetingValueAllFetcher, channels, channelKey, tag,
        requestId).fetchAll("WHERE customTargetingKeyId = ");
  }
}
