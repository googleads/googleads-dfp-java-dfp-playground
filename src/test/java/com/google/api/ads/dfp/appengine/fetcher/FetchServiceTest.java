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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.ads.dfp.appengine.oauth.CredentialException;
import com.google.api.ads.dfp.appengine.util.Channels;
import com.google.api.ads.dfp.appengine.util.Sessions;
import com.google.api.ads.dfp.lib.client.DfpSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link FetchService} implementation.
 *
 * @author Jeff Sham
 */
@RunWith(JUnit4.class)
public class FetchServiceTest {

  private static final String CHANNEL_KEY = "channel-key";
  private static final String ERROR_STRING = "error";
  private static final String FILTER_TEXT = "some filter";
  private static final String NETWORK_CODE = "1234";
  private static final String TAG = "tag";
  private static final String REQUEST_ID = "reqId";
  private static final String USER_ID = "567";

  @Mock private Channels channels;
  @Mock private DfpSession dfpSession;
  @Mock private Fetcher fetcher;
  @Mock private Sessions sessions;
  private FetchService fetchService;

  /**
   * Constructor.
   */
  public FetchServiceTest() {}

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    fetchService = new FetchService(channels, sessions);
  }

  /**
   * Test method for {@link FetchService#fetch}.
   */
  @Test
  public void testFetch_success() throws ValidationException, ServiceException,
      CredentialException {
    when(sessions.get(NETWORK_CODE, USER_ID)).thenReturn(dfpSession);

    fetchService.fetch(fetcher, FILTER_TEXT, CHANNEL_KEY, NETWORK_CODE, TAG, REQUEST_ID,
        USER_ID);

    verify(fetcher).fetch(FILTER_TEXT, CHANNEL_KEY, TAG, REQUEST_ID, dfpSession);
  }

  /**
   * Test method for {@link FetchService#fetch}.
   */
  @Test
  public void testFetch_throwsServiceException() throws ValidationException, CredentialException {
    when(sessions.get(NETWORK_CODE, USER_ID))
        .thenThrow(new ValidationException(ERROR_STRING, null));

    try {
      fetchService.fetch(fetcher, FILTER_TEXT, CHANNEL_KEY, NETWORK_CODE, TAG, REQUEST_ID,
          USER_ID);
      fail("ServiceException expected.");
    } catch (ServiceException e) {
      // Pass.
    }

    verify(channels).sendErrorChannelMessage(CHANNEL_KEY, TAG, REQUEST_ID, ERROR_STRING);
  }

}
