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

package com.google.api.ads.dfp.appengine.util;

import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.ads.dfp.appengine.oauth.CredentialException;
import com.google.api.ads.dfp.appengine.oauth.CredentialFactory;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.client.auth.oauth2.Credential;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * {@code Sessions} is used to get DFP sessions for an App Engine application.
 *
 * Typical usage is: <code>DfpSession session = Sessions.get(12345, 'APP_ENGINE_USER_ID');</code>
 *
 * Only use the get method without a network code if you know the API call can accept the call
 * without one (i.e. MakeTestNetwork).
 *
 * <code>DfpSession session = Sessions.get('APP_ENGINE_USER_ID');</code>
 *
 * @author Jeff Sham
 */
public class Sessions {

  private final String applicationName;
  private final CredentialFactory credentialFactory;

  @Inject
  public Sessions(CredentialFactory credentialFactory,
      @Named("applicationName") String applicationName) {
    this.credentialFactory = credentialFactory;
    this.applicationName = applicationName;
  }


  /**
   * Creates a {@link DfpSession} from the user's stored credentials and specified network.
   *
   * @param networkCode the user's network code
   * @param userId the App Engine assigned user ID
   * @return session the DFP session
   * @throws ValidationException if the DFP session could not be validated and created
   * @throws CredentialException if credential cannot be obtained
   */
  public DfpSession get(String networkCode, String userId) throws ValidationException,
      CredentialException {
    return getSessionBuilder(userId).withNetworkCode(networkCode).build();
  }

  /**
   * Creates a {@link DfpSession} from the user's stored credential but without specifying a
   * network. This session should only be used to discover the networks available for that user.
   *
   * @param userId the App Engine assigned user ID
   * @return session the DFP session
   * @throws ValidationException if the DFP session could not be validated and created
   * @throws CredentialException if credential cannot be obtained
   */
  public DfpSession get(String userId) throws ValidationException, CredentialException {
    return getSessionBuilder(userId).build();
  }

  /**
   * Create a session builder with base configuration.
   *
   * @param userId the App Engine assigned user ID
   * @return DfpSession.Builder the DFP session
   * @throws CredentialException if credential cannot be obtained
   */
  private DfpSession.Builder getSessionBuilder(String userId) throws CredentialException {
    Credential credential = credentialFactory.getInstance(userId);
    return new DfpSession.Builder()
        .withOAuth2Credential(credential)
        .withApplicationName(applicationName);
  }
}
