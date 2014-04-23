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

import com.google.api.ads.dfp.lib.client.DfpSession;

/**
 * Implementations of this interface will fetches API objects filtered by the PQL filter statement
 * for the network and returns them via the channel API.
 *
 * @author Jeff Sham
 */
public interface Fetcher {

  /**
   * Fetch API objects filtered by the filterText with the given session and outputs to the channel.
   *
   * @param filterText the PQL syntax filter text to filter objects by
   * @param channelKey the key to send a message via the Channel API
   * @param tag identifies that objects that are sent via the channel
   * @param requestId identifies the incoming data request
   * @param session a DFP session to make API calls with
   */
  void fetch(String filterText, String channelKey, String tag, String requestId,
      DfpSession session);
}
