// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.api.ads.dfp.appengine.util;

import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.ads.dfp.appengine.oauth.CredentialException;
import com.google.api.ads.dfp.jaxws.factory.DfpServices;
import com.google.api.ads.dfp.jaxws.v201306.ApiException_Exception;
import com.google.api.ads.dfp.jaxws.v201306.Network;
import com.google.api.ads.dfp.jaxws.v201306.NetworkServiceInterface;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.util.List;

/**
 * Utility for making API call to perform operations with network service.
 *
 * @author Jeff Sham
 */
public class Networks {

  private final Sessions sessions;
  private final DfpServices dfpServices;
  private final Predicate<Network> networkPredicate;

  /**
   * Constructor.
   *
   * @param dfpServices provides DFP services
   * @param sessions used to create a DFP session
   * @param networkPredicate predicate used to filter networks
   */
  @Inject
  public Networks(DfpServices dfpServices, Sessions sessions, Predicate<Network> networkPredicate) {
    this.dfpServices = dfpServices;
    this.sessions = sessions;
    this.networkPredicate = networkPredicate;
  }

  /**
   * Gets a list of networks for the user, filtered according to the predicate.
   *
   * @param userId The app engine user ID
   * @return a list of networks
   * @throws ValidationException if the DFP session could not be created for the user
   * @throws ApiException_Exception if API call fails
   * @throws CredentialException if credential cannot be obtained
   */
  public List<Network> get(String userId) throws ValidationException, ApiException_Exception,
      CredentialException {
    DfpSession session = sessions.get(userId);
    NetworkServiceInterface networkService =
        dfpServices.get(session, NetworkServiceInterface.class);
    return Lists.newArrayList(
        Collections2.filter(networkService.getAllNetworks(), networkPredicate));
  }

  /**
   * Make a new test network.
   *
   * @param userId The app engine user ID
   * @return the created network
   * @throws ValidationException if the DFP session could not be created for the user
   * @throws ApiException_Exception if API call fails
   * @throws CredentialException if credential cannot be obtained
   */
  public Network make(String userId) throws ValidationException, ApiException_Exception,
      CredentialException {
    DfpSession session = sessions.get(userId);
    NetworkServiceInterface networkService =
        dfpServices.get(session, NetworkServiceInterface.class);
    return networkService.makeTestNetwork();
  }
}
