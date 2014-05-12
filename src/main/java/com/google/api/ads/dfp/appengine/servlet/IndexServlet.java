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
import com.google.api.ads.dfp.jaxws.v201403.ApiException_Exception;
import com.google.api.ads.dfp.jaxws.v201403.Network;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.users.UserService;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that presents the web application. Users will have to authorize use of the API to view
 * the application page. A new channel token is assigned to the user on every invocation of this
 * servlet.
 *
 * @author Jeff Sham
 */
@Singleton
@SuppressWarnings("serial")
public class IndexServlet extends AuthServlet {

  // TODO(user): Look for ways to use server filter to parse for network code.
  private static final Pattern NETWORK_CODE_PATTERN = Pattern.compile("/(\\d+)");

  static final Logger log = Logger.getLogger(IndexServlet.class.getName());
  private final Networks networks;
  private final ChannelService channelService;
  private final MemcacheService memcacheService;
  private final UserService userService;
  private final int expirationDelta;

  /**
   * Constructor.
   *
   * @param channelService service for handling channel communication
   * @param networks used to get a list of networks
   * @param userService the App Engine service for user management
   * @param memcacheService the App Engine caching service
   * @param expirationDelta the cache expiration delta in seconds
   * @param authorizationCodeFlowFactory used to get an OAuth2 flow
   * @param redirectUrl where to direct the user once authorization is done
   */
  @Inject
  public IndexServlet(ChannelService channelService,
      Networks networks,
      UserService userService,
      MemcacheService memcacheService,
      @Named("expirationDelta") int expirationDelta,
      AuthorizationCodeFlowFactory authorizationCodeFlowFactory,
      @Named("redirectUrl") String redirectUrl) {
    super(authorizationCodeFlowFactory, redirectUrl);
    this.channelService = channelService;
    this.networks = networks;
    this.memcacheService = memcacheService;
    this.userService = userService;
    this.expirationDelta = expirationDelta;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String userId = userService.getCurrentUser().getUserId();
    ServletContext servletContext = getServletContext();
    RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher("/index.jsp");
    @SuppressWarnings("unchecked") // Casting cached data into map.
    Map<String, String> networkMap = (Map<String, String>) memcacheService.get(userId);
    if (networkMap != null) {
      req.setAttribute("networks", networkMap);
    } else {
      try {
        List<Network> testNetworks = networks.get(userId);
        if (!testNetworks.isEmpty()) {
          Map<String, String> fetchedNetworkMap = getNetworkCodeDisplayNameMap(testNetworks);
          req.setAttribute("networks", fetchedNetworkMap);
          memcacheService.put(
              userId, fetchedNetworkMap, Expiration.byDeltaSeconds(expirationDelta));
          networkMap = fetchedNetworkMap;
        } else {
          resp.sendRedirect("/newnetwork");
          return;
        }
      } catch (ApiException_Exception e) {
        req.setAttribute("error", e.getMessage());
        requestDispatcher.forward(req, resp);
        return;
      } catch (ValidationException e) {
        req.setAttribute("error", e.getMessage());
        requestDispatcher.forward(req, resp);
        return;
      } catch (CredentialException e) {
        resp.sendRedirect("/");
        return;
      }
    }

    String networkCode = getNetworkCode(req);

      // Redirect if the user only belongs to one network.
      if (networkCode == null && networkMap.size() == 1) {
        resp.sendRedirect(getNetworkRedirectUrl(req, networkMap));
        return;
      }

    req.setAttribute("network_code", networkCode);


    req.setAttribute("logout_url", userService.createLogoutURL("/"));
    req.setAttribute("user", userService.getCurrentUser().getEmail());
    req.setAttribute("new_network_code", req.getParameter("newNetworkCode"));
    String token = channelService.createChannel(userId);
    req.setAttribute("channel_token", token);
    requestDispatcher.forward(req, resp);
  }

  /**
   * @param req The servlet request
   * @param networkMap a map of network codes to network names
   * @return the redirect url
   */
  private String getNetworkRedirectUrl(HttpServletRequest req, Map<String, String> networkMap) {
    String redirectNetworkCode = networkMap.entrySet().iterator().next().getKey();
    StringBuilder builder = new StringBuilder().append("/").append(redirectNetworkCode);
    if (req.getParameter("newNetworkCode") != null) {
      builder.append("?newNetworkCode=").append(req.getParameter("newNetworkCode"));
    }
    return builder.toString();
  }

  /**
   * Gets a network code from the request URI. Returns null if not found.
   *
   * @param req The servlet request
   * @return the network code or null if not found
   */
  private String getNetworkCode(HttpServletRequest req) {
    String uri = req.getRequestURI();
    Matcher matcher = NETWORK_CODE_PATTERN.matcher(uri);
    return matcher.matches() ? matcher.group(1) : null;
  }

  /**
   * Produce a map of network code to display name.
   *
   * @param networks List of networks.
   * @return a map of network codes to network names.
   */
  private Map<String, String> getNetworkCodeDisplayNameMap(List<Network> networks) {
    Map<String, String> networkCodeDisplayNameMap = Maps.newHashMap();
    for (Network network : networks) {
      networkCodeDisplayNameMap.put(network.getNetworkCode(), network.getDisplayName());
    }
    return networkCodeDisplayNameMap;
  }
}
