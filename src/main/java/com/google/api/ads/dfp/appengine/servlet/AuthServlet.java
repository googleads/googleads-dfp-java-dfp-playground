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

package com.google.api.ads.dfp.appengine.servlet;

import com.google.api.ads.dfp.appengine.oauth.AuthorizationCodeFlowFactory;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.client.http.GenericUrl;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServletRequest;

/**
 * Servlet that handles authorization. Subclass this to make a servlet that walks the user through
 * the OAuth2 dance before serving.
 *
 * @author Jeff Sham
 */
@Singleton
public class AuthServlet extends AbstractAppEngineAuthorizationCodeServlet {

  private final AuthorizationCodeFlowFactory authorizationCodeFlowFactory;
  private final String redirectUrl;

  @Inject
  public AuthServlet(
      AuthorizationCodeFlowFactory authorizationCodeFlowFactory, String redirectUrl) {
    this.authorizationCodeFlowFactory = authorizationCodeFlowFactory;
    this.redirectUrl = redirectUrl;
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() {
    return authorizationCodeFlowFactory.getInstance();
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath(redirectUrl);
    return url.build();
  }
}
