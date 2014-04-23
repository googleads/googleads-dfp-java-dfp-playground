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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.IOException;

/**
 * Factory for creating OAuth2 credentials.
 *
 * @author Jeff Sham
 */
public class CredentialFactory {

  private final String clientId;
  private final String clientSecret;
  private final CredentialStore credentialStore;
  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;

  @Inject
  public CredentialFactory(CredentialStore credentialStore, HttpTransport httpTransport,
      JsonFactory jsonFactory, @Named("clientId") String clientId,
      @Named("clientSecret") String clientSecret) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.credentialStore = credentialStore;
    this.httpTransport = httpTransport;
    this.jsonFactory = jsonFactory;
  }

  /**
   * Fetch the OAuth2 credential from the App Engine Datastore.
   *
   * @param userId the App Engine assigned user ID
   * @return the oAuth2 credential
   * @throws CredentialException if the credential cannot be obtained
   */
  public Credential getInstance(String userId) throws CredentialException {
    Preconditions.checkNotNull(clientId);
    Preconditions.checkNotNull(clientSecret);
    GoogleCredential credential = new GoogleCredential.Builder()
        .setJsonFactory(jsonFactory)
        .setTransport(httpTransport)
        .setClientSecrets(clientId, clientSecret)
        .build();
    try {
      credentialStore.load(userId, credential);
      try {
        credential.refreshToken();
      } catch (IOException e) {
        credentialStore.delete(userId, credential);
        throw new CredentialException("Credential cannot be refreshed.", e);
      }
    } catch (IOException e) {
      throw new CredentialException("Credential cannot be loaded.", e);
    }
    return credential;
  }
}
