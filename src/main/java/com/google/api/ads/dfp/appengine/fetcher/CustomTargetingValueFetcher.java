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
 * A class to make API requests to fetch custom targeting values.
 *
 * @author Jeff Sham
 */
public class CustomTargetingValueFetcher extends AbstractFetcher {

  @Inject
  public CustomTargetingValueFetcher(
      DfpServices dfpServices, Channels channels) {
    super(dfpServices, channels);
  }

  public void fetch(String filterText, String channelKey, String tag, String requestId,
      DfpSession session) {
    // Create the service.
    final CustomTargetingServiceInterface customTargetingService =
        dfpServices.get(session, CustomTargetingServiceInterface.class);

    // Create the page fetcher.
    PageFetcher pageFetcher = new PageFetcher() {

      public Object getByStatement(Statement statement) throws ApiException_Exception {
        return customTargetingService.getCustomTargetingValuesByStatement(statement);
      }
    };
    // Fetch the objects.
    fetchObjects(filterText, channelKey, tag, requestId, pageFetcher);
  }
}
