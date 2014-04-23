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

package com.google.api.ads.dfp.appengine;

import com.google.api.ads.dfp.appengine.fetcher.AdUnitFetcher;
import com.google.api.ads.dfp.appengine.fetcher.CompanyFetcher;
import com.google.api.ads.dfp.appengine.fetcher.CreativeFetcher;
import com.google.api.ads.dfp.appengine.fetcher.CreativeTemplateFetcher;
import com.google.api.ads.dfp.appengine.fetcher.CustomTargetingKeyAndValueFetcher;
import com.google.api.ads.dfp.appengine.fetcher.CustomTargetingKeyFetcher;
import com.google.api.ads.dfp.appengine.fetcher.CustomTargetingValueFetcher;
import com.google.api.ads.dfp.appengine.fetcher.FetchService;
import com.google.api.ads.dfp.appengine.fetcher.Fetcher;
import com.google.api.ads.dfp.appengine.fetcher.FetcherFactory;
import com.google.api.ads.dfp.appengine.fetcher.LineItemCreativeAssociationFetcher;
import com.google.api.ads.dfp.appengine.fetcher.LineItemFetcher;
import com.google.api.ads.dfp.appengine.fetcher.NetworkFetcher;
import com.google.api.ads.dfp.appengine.fetcher.OrderFetcher;
import com.google.api.ads.dfp.appengine.fetcher.OrderLineItemFetcher;
import com.google.api.ads.dfp.appengine.fetcher.PlacementFetcher;
import com.google.api.ads.dfp.appengine.fetcher.PublisherQueryLanguageFetcher;
import com.google.api.ads.dfp.appengine.fetcher.RoleFetcher;
import com.google.api.ads.dfp.appengine.fetcher.UserFetcher;
import com.google.api.ads.dfp.appengine.json.GsonModule;
import com.google.api.ads.dfp.appengine.oauth.AuthorizationCodeFlowFactory;
import com.google.api.ads.dfp.appengine.oauth.CredentialFactory;
import com.google.api.ads.dfp.appengine.servlet.CreateNetworkServlet;
import com.google.api.ads.dfp.appengine.servlet.DeniedServlet;
import com.google.api.ads.dfp.appengine.servlet.DfpServlet;
import com.google.api.ads.dfp.appengine.servlet.IndexServlet;
import com.google.api.ads.dfp.appengine.servlet.OAuth2CallbackServlet;
import com.google.api.ads.dfp.appengine.servlet.TaskDispatchServlet;
import com.google.api.ads.dfp.jaxws.factory.DfpServices;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.extensions.appengine.auth.oauth2.AppEngineCredentialStore;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.apphosting.api.ApiProxy;
import com.google.inject.Provider;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configures the servlet bindings.
 *
 * @author Jeff Sham
 */
public class PlaygroundServletModule extends ServletModule {

  static final Logger log = Logger.getLogger(PlaygroundServletModule.class.getName());

  /**
   * If you do not have a client ID or secret, please create one in the API console:
   * https://code.google.com/apis/console#access
   */
  private static final String CLIENT_ID = System.getProperty("dfp.appengine.clientId");
  private static final String CLIENT_SECRET = System.getProperty("dfp.appengine.clientSecret");
  private static final String SCOPE = "https://www.google.com/apis/ads/publisher";

  /**
   * The redirect URL must be registered in the API console: https://code.google.com/apis/console/
   * This is so that once the user has authorized your app, they are redirected back to your app.
   */
  private static final String REDIRECT_URL = "/oauth2callback";

  /**
   * The absolute path to the playground properties file.
   */
  private static final String PROPERTIES_PATH = "/WEB-INF/dfp-playground.properties";

  /**
   * The property name for the playground version.
   */
  private static final String VERSION_PROPERTY = "version";

  /**
   * String for an unknown version.
   */
  private static final String UNKNOWN_VERSION = "unknown";

  /**
   * The number of seconds to keep a user's networks in memcache.
   */
  private static final int EXPIRATION_DELTA = 60 * 60;

  @Override
  protected void configureServlets() {
    super.configureServlets();

    serve("/").with(IndexServlet.class);
    serve("/denied").with(DeniedServlet.class);
    serve("/dfp").with(IndexServlet.class);
    serveRegex("/[0-9]+").with(IndexServlet.class);
    serve("/fetch").with(DfpServlet.class);
    serve("/get").with(TaskDispatchServlet.class);
    serve("/newnetwork").with(CreateNetworkServlet.class);
    serve(REDIRECT_URL).with(OAuth2CallbackServlet.class);

    MapBinder<String, Fetcher> mapbinder =
        MapBinder.newMapBinder(binder(), String.class, Fetcher.class);
    mapbinder.addBinding(DfpTags.AD_UNITS).to(AdUnitFetcher.class);
    mapbinder.addBinding(DfpTags.COMPANIES).to(CompanyFetcher.class);
    mapbinder.addBinding(DfpTags.CREATIVES).to(CreativeFetcher.class);
    mapbinder.addBinding(DfpTags.CREATIVE_TEMPLATES).to(CreativeTemplateFetcher.class);
    mapbinder.addBinding(DfpTags.CUSTOM_TARGETING_KEYS).to(CustomTargetingKeyFetcher.class);
    mapbinder.addBinding(DfpTags.CUSTOM_TARGETING).to(CustomTargetingKeyAndValueFetcher.class);
    mapbinder.addBinding(DfpTags.CUSTOM_TARGETING_VALUES).to(CustomTargetingValueFetcher.class);
    mapbinder.addBinding(DfpTags.LINE_ITEM_CREATIVE_ASSOCIATIONS)
        .to(LineItemCreativeAssociationFetcher.class);
    mapbinder.addBinding(DfpTags.LINE_ITEMS).to(LineItemFetcher.class);
    mapbinder.addBinding(DfpTags.NETWORKS).to(NetworkFetcher.class);
    mapbinder.addBinding(DfpTags.ORDERS).to(OrderFetcher.class);
    mapbinder.addBinding(DfpTags.ORDERS_LINE_ITEMS).to(OrderLineItemFetcher.class);
    mapbinder.addBinding(DfpTags.PLACEMENTS).to(PlacementFetcher.class);
    mapbinder.addBinding(DfpTags.PUBLISHER_QUERY_LANGUAGE).to(PublisherQueryLanguageFetcher.class);
    mapbinder.addBinding(DfpTags.ROLES).to(RoleFetcher.class);
    mapbinder.addBinding(DfpTags.USERS).to(UserFetcher.class);

    // The application name reported to the DFP API. Defaulted to the App Engine Unique ID.
    bind(String.class)
        .annotatedWith(Names.named("applicationName")).toProvider(new Provider<String>() {
          public String get() {
            String version = UNKNOWN_VERSION;
            Properties properties = new Properties();
            try {
              properties.load(getServletContext().getResourceAsStream(PROPERTIES_PATH));
              version = properties.getProperty(VERSION_PROPERTY);
            } catch (IOException e) {
              log.log(Level.SEVERE, "Cannot fetch file from " + PROPERTIES_PATH, e);
            }
            return ApiProxy.getCurrentEnvironment().getAppId() + "/" + version;
          }
        });

    if (CLIENT_ID.equals("INSERT_CLIENT_ID_HERE")
        || CLIENT_SECRET.equals("INSERT_CLIENT_SECRET_HERE")) {
      throw new IllegalArgumentException(
          "Please input your client IDs or secret in your WEB-INF/appengine-web.xml file.");
    }

    bindConstant().annotatedWith(Names.named("clientId")).to(CLIENT_ID);
    bindConstant().annotatedWith(Names.named("clientSecret")).to(CLIENT_SECRET);
    bindConstant().annotatedWith(Names.named("scope")).to(SCOPE);
    bindConstant().annotatedWith(Names.named("redirectUrl")).to(REDIRECT_URL);
    bindConstant().annotatedWith(Names.named("expirationDelta")).to(EXPIRATION_DELTA);

    bind(FetchService.class);
    bind(AuthorizationCodeFlowFactory.class);
    bind(CredentialFactory.class);
    bind(CredentialStore.class).to(AppEngineCredentialStore.class);
    bind(DfpServices.class);
    bind(FetcherFactory.class);
    bind(GoogleCredential.class);
    bind(HttpTransport.class).to(NetHttpTransport.class);
    bind(JsonFactory.class).to(JacksonFactory.class);

    bind(ChannelService.class).toProvider(ChannelServiceProvider.class);
    bind(MemcacheService.class).toProvider(MemcacheServiceProvider.class);
    bind(UserService.class).toProvider(UserServiceProvider.class);

    install(new GsonModule());

    install(new ExternalModule());
  }

  private static class ChannelServiceProvider implements Provider<ChannelService> {

    public ChannelService get() {
      return ChannelServiceFactory.getChannelService();
    }
  }

  private static class MemcacheServiceProvider implements Provider<MemcacheService> {

    public MemcacheService get() {
      return MemcacheServiceFactory.getMemcacheService();
    }
  }

  private static class UserServiceProvider implements Provider<UserService> {

    public UserService get() {
      return UserServiceFactory.getUserService();
    }
  }
}
