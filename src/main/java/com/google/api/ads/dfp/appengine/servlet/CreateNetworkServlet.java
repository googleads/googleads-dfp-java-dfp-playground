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

import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.ads.dfp.appengine.oauth.AuthorizationCodeFlowFactory;
import com.google.api.ads.dfp.appengine.oauth.CredentialException;
import com.google.api.ads.dfp.appengine.util.Networks;
import com.google.api.ads.dfp.jaxws.v201306.ApiException_Exception;
import com.google.api.ads.dfp.jaxws.v201306.Network;
import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that allows users to create a new DFP test network.
 *
 * @author Jeff Sham
 */
@Singleton
@SuppressWarnings("serial")
public class CreateNetworkServlet extends AuthServlet {

  private final Networks networks;
  private final UserService userService;

  /**
   * Constructor.
   *
   * @param networks used to create networks
   * @param userService the App Engine service for user management
   * @param authorizationCodeFlowFactory used to get an OAuth2 flow
   * @param redirectUrl where to direct the user once authorization is done
   */
  @Inject
  public CreateNetworkServlet(Networks networks, UserService userService,
      AuthorizationCodeFlowFactory authorizationCodeFlowFactory,
      @Named("redirectUrl") String redirectUrl) {
    super(authorizationCodeFlowFactory, redirectUrl);
    this.networks = networks;
    this.userService = userService;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    ServletContext servletContext = getServletContext();
    RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher("/newNetwork.jsp");
    requestDispatcher.forward(req, resp);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    ServletContext servletContext = getServletContext();
    RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher("/newNetwork.jsp");
    if (req.getParameter("createNetwork") != null) {
      try {
        Network newNetwork = networks.make(userService.getCurrentUser().getUserId());
        resp.sendRedirect("/index?newNetworkCode=" + newNetwork.getNetworkCode());
        return;
      } catch (ApiException_Exception e) {
        req.setAttribute("error", true);
      } catch (ValidationException e) {
        req.setAttribute("error", true);
      } catch (CredentialException e) {
        resp.sendRedirect("/");
        return;
      }
    }
    requestDispatcher.forward(req, resp);
  }
}
