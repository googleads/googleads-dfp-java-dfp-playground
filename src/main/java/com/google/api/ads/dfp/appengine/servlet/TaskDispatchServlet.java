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

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles all entity fetch requests and creates tasks for API requests. If a task is running to
 * fetch a type of entity and another request is made, the older task is deleted before adding the
 * new task.
 *
 * @author Jeff Sham
 */
@Singleton
@SuppressWarnings("serial")
public class TaskDispatchServlet extends HttpServlet {

  private final UserService userService;

  /**
   * Constructor.
   *
   * @param userService service to get the App Engine user entity
   */
  @Inject
  public TaskDispatchServlet(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String channelKey = getChannelKey();
    String filterText = req.getParameter("filterText");
    filterText = (filterText == null) ? "" : filterText;
    String tag = req.getParameter("tag");
    String requestId = req.getParameter("reqId");
    String userId = userService.getCurrentUser().getUserId();

    Queue queue = QueueFactory.getDefaultQueue();
    TaskOptions taskOptions = withUrl("/fetch")
        .param("displayStyle", req.getParameter("displayStyle"))
        .param("filterText", filterText)
        .param("key", channelKey)
        .param("networkCode", req.getParameter("networkCode"))
        .param("userId", userId)
        .param("tag", tag)
        .param("reqId", requestId)
        .param("typeOverride", req.getParameter("typeOverride"));

    queue.add(taskOptions.method(Method.GET));
    resp.getWriter().print(channelKey);
  }

  /**
   * Gets the channel key.
   */
  private String getChannelKey() {
    return userService.getCurrentUser().getUserId();
  }
}
