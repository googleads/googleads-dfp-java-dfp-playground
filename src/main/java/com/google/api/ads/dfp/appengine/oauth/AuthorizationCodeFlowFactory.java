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

package com.google.api.ads.dfp.appengine.oauth;

import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Factory for creating OAuth2 authorization code flows.
 *
 * @author Jeff Sham
 */
public class AuthorizationCodeFlowFactory {

  private final CredentialStore credentialStore;
  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;
  private final String clientId;
  private final String clientSecret;
  private final String scope;

  @Inject
  public AuthorizationCodeFlowFactory(CredentialStore credentialStore,
      JsonFactory jsonFactory,
      HttpTransport httpTransport,
      @Named("clientId") String clientId,
      @Named("clientSecret") String clientSecret,
      @Named("scope") String scope) {
    this.credentialStore = credentialStore;
    this.httpTransport = httpTransport;
    this.jsonFactory = jsonFactory;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.scope = scope;
  }

  /**
   * Returns a fetcher for an API object.
   *
   * @return a Google authorization code flow
   */
  public GoogleAuthorizationCodeFlow getInstance() {
    Preconditions.checkNotNull(clientId);
    Preconditions.checkNotNull(clientSecret);
    return new GoogleAuthorizationCodeFlow.Builder(
        httpTransport,
        jsonFactory,
        clientId,
        clientSecret,
        Lists.newArrayList(scope))
        .setCredentialStore(credentialStore)
        .setApprovalPrompt("force")
        .setAccessType("offline")
        .build();
  }

}
