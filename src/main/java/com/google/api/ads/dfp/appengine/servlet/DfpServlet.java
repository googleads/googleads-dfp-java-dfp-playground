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

import com.google.api.ads.dfp.appengine.fetcher.FetchService;
import com.google.api.ads.dfp.appengine.fetcher.Fetcher;
import com.google.api.ads.dfp.appengine.fetcher.FetcherFactory;
import com.google.api.ads.dfp.appengine.fetcher.ServiceException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base servlet for all DFP entity requests.
 *
 * @author Jeff Sham
 */
@Singleton
public class DfpServlet extends HttpServlet {

  static final Logger log = Logger.getLogger(DfpServlet.class.getName());
  private final FetcherFactory fetcherFactory;
  private final FetchService fetchService;

  @Inject
  public DfpServlet(FetcherFactory fetcherFactory, FetchService fetchService) {
    this.fetcherFactory = fetcherFactory;
    this.fetchService = fetchService;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
    String filterText = req.getParameter("filterText");
    String channelKey = req.getParameter("key");
    String networkCode = req.getParameter("networkCode");
    String tag = req.getParameter("tag");
    String typeOverride = req.getParameter("typeOverride");
    String userId = req.getParameter("userId");
    String requestId = req.getParameter("reqId");
    try {
      Fetcher fetcher = fetcherFactory.getInstance(tag, typeOverride);
        fetchService.fetch(fetcher, filterText, channelKey, networkCode, tag, requestId, userId);
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "Fetcher cannot be found.", e);
    } catch (ServiceException e) {
      log.log(Level.SEVERE, "Error fetching with service.", e);
    }
  }
}
