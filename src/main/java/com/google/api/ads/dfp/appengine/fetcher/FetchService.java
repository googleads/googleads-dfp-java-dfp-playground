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

import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.ads.dfp.appengine.oauth.CredentialException;
import com.google.api.ads.dfp.appengine.util.Channels;
import com.google.api.ads.dfp.appengine.util.Sessions;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import java.util.logging.Logger;


/**
 * Service to handle API object fetching.
 *
 * @author Jeff Sham
 */
public final class FetchService {

  protected final Logger log = Logger.getLogger(this.getClass().getName());
  protected final Channels channels;
  protected final Sessions sessions;

  /**
   * Construct the fetch service.
   * @param channels used for Channel API communication
   * @param sessions used to create a DFP session
   */
  @Inject
  public FetchService(Channels channels, Sessions sessions) {
    this.channels = channels;
    this.sessions = sessions;
  }

  /**
   * Fetch API objects on the user's network.
   *
   * @param fetcher handles API object fetching
   * @param filterText the PQL syntax filter text to filter objects by
   * @param channelKey the key to send a message via the Channel AP
   * @param networkCode the user's network code
   * @param tag identifies that objects that are sent via the channel
   * @param requestId identifies the incoming data request
   * @param userId App Engine assigned user ID
   * @throws ServiceException if session object cannot be obtained
   */
  public void fetch(Fetcher fetcher, String filterText, String channelKey,
      String networkCode, String tag, String requestId, String userId) throws ServiceException {
    Preconditions.checkNotNull(fetcher);
    DfpSession session = getSession(channelKey, networkCode, tag, requestId, userId);
    fetcher.fetch(filterText, channelKey, tag, requestId, session);
  }

  /**
   * Gets a DFP session or sends a channel error message if it cannot be done.
   *
   * @param channelKey the key to send a message via the Channel API
   * @param networkCode the user's network code
   * @param tag identifies that objects that are sent via the channel
   * @param requestId identifies the incoming data request
   * @param userId App Engine assigned user ID
   * @return a DFP session
   * @throws ServiceException if session object cannot be obtained
   */
  private DfpSession getSession(String channelKey, String networkCode, String tag, String requestId,
      String userId) throws ServiceException {
    try {
      return sessions.get(networkCode, userId);
    } catch (ValidationException e) {
      channels.sendErrorChannelMessage(channelKey, tag, requestId, e.getMessage());
      throw new ServiceException("Unable to acquire session object", e);
    } catch (CredentialException e) {
      channels.sendErrorChannelMessage(channelKey, tag, requestId, e.getMessage());
      throw new ServiceException("Unable to acquire credential", e);
    }
  }

}
