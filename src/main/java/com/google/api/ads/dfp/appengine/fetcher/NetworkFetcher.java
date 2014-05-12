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
import com.google.api.ads.dfp.jaxws.v201403.Network;
import com.google.api.ads.dfp.jaxws.v201403.NetworkServiceInterface;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.inject.Inject;

import java.util.List;

/**
 * A class to make API requests to fetch network entities.
 *
 * @author Jeff Sham
 */
public class NetworkFetcher extends AbstractFetcher {

  @Inject
  public NetworkFetcher(DfpServices dfpServices, Channels channels) {
    super(dfpServices, channels);
  }

  public void fetch(String filterText, String channelKey, String tag, String requestId,
      DfpSession session) {
    try {
      // Create the service.
      NetworkServiceInterface networkService =
          dfpServices.get(session, NetworkServiceInterface.class);

      // Make the request to get networks.
      List<Network> networks = networkService.getAllNetworks();

      // Return results through the channel.
      channels.sendObjects(channelKey, networks, tag, requestId);
    } catch (ApiException_Exception e) {
      channels.sendErrorChannelMessage(channelKey, tag, requestId, e.getMessage());
    } catch (Exception e) {
      channels.sendErrorChannelMessage(channelKey, tag, requestId, e.getMessage());
      log.warning("Error:" + e.getMessage());
    }
  }
}
