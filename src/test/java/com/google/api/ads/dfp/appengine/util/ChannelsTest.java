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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link Channels}.
 *
 * @author Jeff Sham
 */
@RunWith(JUnit4.class)
public class ChannelsTest {

  private static final String CHANNEL_KEY = "channel-key";
  private static final String MY_MESSAGE = "my message";
  private static final String TAG = "tag";
  private static final String REQUEST_ID = "reqId";
  private Channels channels;
  @Mock private ChannelService channelService;
  private Gson gson = new Gson();

  /**
   * Constructor.
   */
  public ChannelsTest() {}

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    channels = new Channels(channelService, gson);
  }

  /**
   * Test method for {@link Channels#sendSingleObject}.
   */
  @Test
  public void testSendObject() {
    Object object = new Object();
    ArgumentCaptor<ChannelMessage> messageCapture = ArgumentCaptor.forClass(ChannelMessage.class);

    channels.sendSingleObject(CHANNEL_KEY, object, TAG, REQUEST_ID);

    verify(channelService).sendMessage(messageCapture.capture());

    assertEquals(CHANNEL_KEY, messageCapture.getValue().getClientId());
    assertEquals(jsonFromList(REQUEST_ID, Lists.newArrayList(object)),
        messageCapture.getValue().getMessage());
  }

  /**
   * Test method for {@link Channels#sendObjects}.
   */
  @Test
  public void testSendObjects_singleBatch() {
    List<Object> list = makeList(Channels.BATCH_SIZE);
    ArgumentCaptor<ChannelMessage> messageCapture = ArgumentCaptor.forClass(ChannelMessage.class);

    channels.sendObjects(CHANNEL_KEY, list, TAG, REQUEST_ID);

    verify(channelService).sendMessage(messageCapture.capture());

    assertEquals(jsonFromList(REQUEST_ID, list), messageCapture.getValue().getMessage());
  }

  /**
   * Test method for {@link Channels#sendObjects}.
   */
  @Test
  public void testSendObjects_multipleBatches() {
    List<Object> list = makeList(Channels.BATCH_SIZE + 1);

    channels.sendObjects(CHANNEL_KEY, list, TAG, REQUEST_ID);

    verify(channelService, times(2)).sendMessage(any(ChannelMessage.class));
  }

  /**
   * Test method for {@link Channels#sendObjects}.
   */
  @Test
  public void testSendObjects_noResults() {
    ArgumentCaptor<ChannelMessage> messageCapture = ArgumentCaptor.forClass(ChannelMessage.class);

    channels.sendObjects(CHANNEL_KEY, Lists.newArrayList(), TAG, REQUEST_ID);

    verify(channelService).sendMessage(messageCapture.capture());

    assertEquals(CHANNEL_KEY, messageCapture.getValue().getClientId());
    @SuppressWarnings("unchecked") // That's the defined message format.
    Map<String, Map<String, String>> message =
        gson.fromJson(messageCapture.getValue().getMessage(), Map.class);
    assertTrue(message.containsKey(Channels.INFO_TAG));
    assertEquals(TAG, message.get(Channels.INFO_TAG).get(Channels.TAG));
    assertTrue(message.get(Channels.INFO_TAG).containsKey(Channels.MESSAGE));
  }

  /**
   * Test method for {@link Channels#sendPage}.
   */
  @Test
  public void testSendPage()
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    Page page = new Page();
    List<Object> list = Lists.newArrayList(new Object());
    page.getResults().addAll(list);
    ArgumentCaptor<ChannelMessage> messageCapture = ArgumentCaptor.forClass(ChannelMessage.class);

    channels.sendPage(CHANNEL_KEY, page, TAG, REQUEST_ID);

    verify(channelService).sendMessage(messageCapture.capture());

    assertEquals(CHANNEL_KEY, messageCapture.getValue().getClientId());
    assertEquals(jsonFromList(REQUEST_ID, list), messageCapture.getValue().getMessage());
  }

  /**
   * Test method for {@link Channels#sendPage}.
   */
  @Test
  public void testSendPage_throwsNoMethodException()
      throws IllegalAccessException, InvocationTargetException {
    try {
      channels.sendPage(CHANNEL_KEY, new Object(), TAG, REQUEST_ID);
      fail("NoSuchMethodException expected.");
    } catch (NoSuchMethodException e) {
      // Pass.
    }
  }

  /**
   * Test method for {@link Channels#sendInfoMessage}.
   */
  @Test
  public void testSendInfoMessage() {
    ArgumentCaptor<ChannelMessage> messageCapture = ArgumentCaptor.forClass(ChannelMessage.class);

    channels.sendInfoMessage(CHANNEL_KEY, TAG, REQUEST_ID, MY_MESSAGE);

    verify(channelService).sendMessage(messageCapture.capture());

    assertEquals(CHANNEL_KEY, messageCapture.getValue().getClientId());
    @SuppressWarnings("unchecked") // That's the defined message format.
    Map<String, Map<String, String>> message =
        gson.fromJson(messageCapture.getValue().getMessage(), Map.class);
    assertTrue(message.containsKey(Channels.INFO_TAG));
    assertEquals(TAG, message.get(Channels.INFO_TAG).get(Channels.TAG));
    assertEquals(MY_MESSAGE, message.get(Channels.INFO_TAG).get(Channels.MESSAGE));
  }

  /**
   * Test method for {@link Channels#sendErrorChannelMessage}.
   */
  @Test
  public void testSendErrorChannelMessage() {
    ArgumentCaptor<ChannelMessage> messageCapture = ArgumentCaptor.forClass(ChannelMessage.class);

    channels.sendErrorChannelMessage(CHANNEL_KEY, TAG, REQUEST_ID, MY_MESSAGE);

    verify(channelService).sendMessage(messageCapture.capture());

    assertEquals(CHANNEL_KEY, messageCapture.getValue().getClientId());
    @SuppressWarnings("unchecked") // That's the defined message format.
    Map<String, Map<String, String>> message =
        gson.fromJson(messageCapture.getValue().getMessage(), Map.class);
    assertTrue(message.containsKey(Channels.ERROR_TAG));
    assertEquals(TAG, message.get(Channels.ERROR_TAG).get(Channels.TAG));
    assertEquals(MY_MESSAGE, message.get(Channels.ERROR_TAG).get(Channels.MESSAGE));
  }

  /**
   * Make a JSON string of a message from a request ID and list.
   *
   * @param requestId the request ID of the message
   * @param list the list of objects in the message
   * @return the JSON string
   */
  private String jsonFromList(String requestId, List<?> list) {
    Map<String, Object> data = ImmutableMap.of(Channels.TAG, list, Channels.REQUEST_ID,
        requestId);
    return gson.toJson(data);
  }

  /**
   * Make a list of objects.
   *
   * @param size the number of elements in the list
   * @return a list of objects
   */
  private List<Object> makeList(int size) {
    List<Object> list = Lists.newArrayList();
    for (int i = 0; i < size; i++) {
      list.add(new Object());
    }
    return list;
  }

  /** A page for testing. */
  public class Page {
    private List<Object> results;

    /** Constructor. */
    public Page(){
      results = Lists.newArrayList();
    }

    /** Returns the results list. */
    public List<Object> getResults() {
      return results;
    }
  }
}
