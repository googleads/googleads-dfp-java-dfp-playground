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

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.inject.Inject;

import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for sending API entities through the Channel API.
 *
 * @author Jeff Sham
 */
public class Channels {

  static final Logger log = Logger.getLogger(Channels.class.getName());

  /** Key for the tag field in a message. */
  public static final String TAG = "tag";

  /** Key for the request ID field in a message. */
  public static final String REQUEST_ID = "reqId";

  /** Key for the message string field in a message. */
  public static final String MESSAGE = "message";

  /** Tag to use for an info message. */
  public static final String INFO_TAG = "info";

  /** Tag to use for an error message. */
  public static final String ERROR_TAG = "error";

  /** Controls how many entities are returned per message. */
  @VisibleForTesting
  static final int BATCH_SIZE = 5;

  private final ChannelService channelService;
  private final Gson gson;

  @Inject
  public Channels(ChannelService channelService, Gson gson) {
    this.channelService = channelService;
    this.gson = gson;
  }

  /**
   * Sends a list of objects via the channel API. Objects are broken up into batches because the
   * size of each message is capped.
   *
   * @param channelKey the key to send a message via the Channel API
   * @param objects a list of objects to send via the channel
   * @param tag the name of the content panel to send objects
   * @param requestId the ID of the incoming data request to respond to
   */
  public void sendObjects(String channelKey, List<?> objects, String tag, String requestId) {
    checkPreconditions(channelKey, tag, requestId);
    List<Object> list = Lists.newArrayList();
    int count = 0;
    for (Object object : objects) {
      count += 1;
      list.add(object);
      if (count % BATCH_SIZE == 0) {
        sendMessage(channelKey, ImmutableList.copyOf(list), tag, requestId);
        list.clear();
      }
    }
    if (!list.isEmpty()) {
      sendMessage(channelKey, list, tag, requestId);
    }
    if (count == 0) {
      sendNoResultMessage(channelKey, tag, requestId);
    }
  }

  /**
   * Sends a single message through the channel.
   *
   * @param channelKey the key to send a message via the Channel API
   * @param list objects to send back
   * @param tag the name of the content panel to send message to
   * @param requestId the ID of the incoming data request to respond to
   */
  private void sendMessage(String channelKey, List<Object> list, String tag, String requestId) {
    try {
      channelService.sendMessage(
          createMessage(channelKey, ImmutableMap.of(tag, list, REQUEST_ID, requestId)));
    } catch (IllegalArgumentException e) {
      log.log(Level.SEVERE, "Error sending channel message.", e);
    }
  }

  /**
   * Sends a message for no results found through the channel.
   *
   * @param channelKey the key to send a message via the Channel API
   * @param tag the name of the content panel to send message to
   * @param requestId the ID of the incoming data request to respond to
   */
  public void sendNoResultMessage(String channelKey, String tag, String requestId) {
    checkPreconditions(channelKey, tag, requestId);
    sendInfoMessage(channelKey, tag, requestId, "No results found.");
  }

  /**
   * Sends an info message through the channel.
   *
   * @param channelKey the key to send a message via the Channel API
   * @param tag the name of the content panel to send message to
   * @param requestId the ID of the incoming data request to respond to
   * @param message the message to display to the user
   */
  public void sendInfoMessage(String channelKey, String tag, String requestId, String message) {
    checkPreconditions(channelKey, tag, message);
    Map<String, String> info = ImmutableMap.of(TAG, tag, MESSAGE, message);
    channelService.sendMessage(createMessage(channelKey,
        ImmutableMap.of(INFO_TAG, info, REQUEST_ID, requestId)));
  }

  /**
   * Sends an error message through the channel.
   *
   * @param channelKey the key to send a message via the Channel API
   * @param tag the name of the content panel that threw the error
   * @param requestId the incoming data request that threw the error
   * @param message the error message details
   */
  public void sendErrorChannelMessage(String channelKey, String tag, String requestId,
      String message) {
    checkPreconditions(channelKey, tag, requestId, message);
    Map<String, String> info = ImmutableMap.of(TAG, tag, MESSAGE, message);
    channelService.sendMessage(createMessage(channelKey,
        ImmutableMap.of(ERROR_TAG, info, REQUEST_ID, requestId)));
  }


  /**
   * Creates a {@link ChannelMessage} with the JSON representation of the data map.
   *
   * @param channelKey the key to send a message via the Channel API
   * @param data the data map
   * @return a {@link ChannelMessage}
   */
  private ChannelMessage createMessage(String channelKey, Map<String, ?> data) {
    return new ChannelMessage(channelKey, gson.toJson(data));
  }

  /**
   * Sends a page of results through the channel.
   *
   * @param channelKey the key to send a message via the Channel API
   * @param page the page of objects to send
   * @param tag the name of the content panel to send objects to
   * @param requestId the ID of the incoming data request to respond to
   * @throws NoSuchMethodException if the method 'results' does not exist on page
   * @throws InvocationTargetException if the property accessor method throws an exception
   * @throws IllegalAccessException if the caller does not have access to the property accessor
   *         method
   */
  public void sendPage(String channelKey, Object page, String tag, String requestId)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    checkPreconditions(channelKey, tag, requestId);
    sendObjects(channelKey, (List<?>) PropertyUtils.getProperty(page, "results"), tag, requestId);
  }

  /**
   * Sends a single object through the channel.
   *
   * @param channelKey the key to send a message via the Channel API
   * @param object object to send back
   * @param tag the name of the content panel to send the object to
   * @param requestId the ID of the incoming data request to respond to
   */
  public void sendSingleObject(String channelKey, Object object, String tag, String requestId) {
    checkPreconditions(channelKey, tag, requestId);
    sendMessage(channelKey, ImmutableList.of(object), tag, requestId);
  }

  /**
   * Performs some up front checks before sending channel messages.
   *
   * @param channelKey the key to send a message via the Channel API
   * @param tag the name of the content panel
   * @param requestId the ID of the incoming data request
   * @param message the message to send
   */
  private void checkPreconditions(String channelKey, String tag, String requestId, String message) {
    checkPreconditions(channelKey, tag, requestId);
    Preconditions.checkNotNull(message, "Cannot send null message.");
  }

  /**
   * Performs some up front checks before sending channel messages.
   *
   * @param channelKey the key to send a message via the Channel API
   * @param tag the name of the content panel
   * @param requestId the ID of the incoming data request
   */
  private void checkPreconditions(String channelKey, String tag, String requestId) {
    Preconditions.checkNotNull(channelKey, "Cannot send message with null channel key.");
    Preconditions.checkNotNull(tag, "Cannot send message with null tag.");
    Preconditions.checkNotNull(requestId, "Cannot send message with null requestId.");
  }
}
