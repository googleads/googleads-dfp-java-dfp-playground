<!-- Copyright 2012 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. -->

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="java.util.Map"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>DFP API Playground</title>
<script type="text/javascript" src="/_ah/channel/jsapi"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.5.1/jquery.min.js"></script>
<script
  src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.13/jquery-ui.min.js"></script>
<script type="text/javascript" src="js/main.js"></script>
<link rel="stylesheet" type="text/css" href="css/common.css" />
<link rel="stylesheet" type="text/css" href="css/main.css" />
<link rel="stylesheet" type="text/css" href="css/jquery-ui-1.8.13.custom.css" />
<script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-9603638-4']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

</script>
</head>
<body>
  <div class="dfp-header">
    <!-- Account Information -->
    <div id="dfp-account">
      <%
      String networkCode = (String) request.getAttribute("network_code");
      networkCode = (networkCode == null) ? "" : networkCode;
      %>
        <span>Network: </span>
        <select id="network">
          <%
            Map<String, String> networkCodeDisplayNameMap =
                (Map<String, String>) request.getAttribute("networks");
          %>
          <%
            if (!networkCodeDisplayNameMap.containsKey(networkCode)) {
          %>
          <option value="no_network" DISABLED SELECTED>Select a network property</option>
          <%
            }
          %>
          <%
            if (networkCodeDisplayNameMap != null) {
              for (Map.Entry<String, String> entry : networkCodeDisplayNameMap.entrySet()) {
          %>
          <option value="<%=entry.getKey()%>"
            <%if (entry.getKey().equals(networkCode)) {
            %>
            SELECTED <%}%>><%=entry.getValue()%>
            (<%=entry.getKey()%>)
          </option>
          <%
            }
          }
          %>
        </select>
        <span>|</span>
        <div id="dfp-account-user" class="dfp-menu-header">
          <span><%=request.getAttribute("user")%></span><span class="dropdown-arrow">▼</span>
          <div id="dfp-account-user-options" class="menu">
            <a href="<%=request.getAttribute("logout_url")%>">Sign out</a>
          </div>
        </div>
      <span>|</span>
      <a id="dfp-view-in-ui" href="http://www.google.com/dfp/main?networkCode=<%=networkCode%>" target="_blank">View network in DFP UI</a>
      <span>|</span>
      <div id="dfp-get-help" class="dfp-menu-header">
        <span>Get Help</span><span class="dropdown-arrow">▼</span>
        <div id="dfp-get-help-options" class="menu">
          <a href="https://developers.google.com/doubleclick-publishers/docs/start" target="_blank">Documentation</a>
          <a href="http://groups.google.com/group/google-doubleclick-for-publishers-api/topics" target="_blank">Product help center</a>
          <a href="https://code.google.com/p/google-api-ads-java/issues/list" target="_blank">Report issues</a>
          <a href="http://goo.gl/DaIAx" target="_blank">View source for playground</a>
        </div>
      </div>

    </div>
    <h1 id="dfp-logo">DFP API Playground</h1>
  </div>
  <hr />
  <div class="dfp-content">
    <!-- Panels container -->
    <div id="dfp-panels">
      <!-- Users -->
      <div id="dfp-panel-users" class="dfp-panel">
        <a class="dfp-reload" href="#">Load</a>
        <div class="dfp-rc-header">
          <h2>Users</h2>
        </div>
        <div class="dfp-panel-content"></div>
        <div class="dfp-loading"><a href="#" class="dfp-cancel">Cancel</a></div>
        <div class="dfp-panel-filter">
          <a class="dfp-reference-link" target="_blank"
            href="https://developers.google.com/doubleclick-publishers/docs/reference/latest/UserService.html#getUsersByStatement">Reference</a>
          <h3>Filter</h3>
          <div class="dfp-panel-filter-content">
            <textarea class="dfp-filter-textarea">WHERE id != 0 LIMIT 50 OFFSET 0</textarea>
            <div class="dfp-filter-footer">
              <button class="dfp-filter-button">Filter</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Ad Units -->
      <div id="dfp-panel-ad-units" class="dfp-panel">
        <a class="dfp-reload" href="#">Load</a>
        <div class="dfp-rc-header">
          <h2>Ad Units</h2>
        </div>
        <div class="dfp-panel-content"></div>
        <div class="dfp-loading"><a href="#" class="dfp-cancel">Cancel</a></div>
        <div class="dfp-panel-filter">
          <a class="dfp-reference-link" target="_blank"
            href="https://developers.google.com/doubleclick-publishers/docs/reference/latest/InventoryService.html#getAdUnitsByStatement">Reference</a>
          <h3>Filter</h3>
          <div class="dfp-panel-filter-content">
            <textarea class="dfp-filter-textarea">WHERE id != 0 LIMIT 50 OFFSET 0</textarea>
            <div class="dfp-filter-footer">
              <button class="dfp-filter-button">Filter</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Companies -->
      <div id="dfp-panel-companies" class="dfp-panel">
        <a class="dfp-reload" href="#">Load</a>
        <div class="dfp-rc-header">
          <h2>Companies</h2>
        </div>
        <div class="dfp-panel-content"></div>
        <div class="dfp-loading"><a href="#" class="dfp-cancel">Cancel</a></div>
        <div class="dfp-panel-filter">
          <a class="dfp-reference-link" target="_blank"
            href="https://developers.google.com/doubleclick-publishers/docs/reference/latest/CompanyService.html#getCompaniesByStatement">Reference</a>
          <h3>Filter</h3>
          <div class="dfp-panel-filter-content">
            <textarea class="dfp-filter-textarea">WHERE id != 0 LIMIT 50 OFFSET 0</textarea>
            <div class="dfp-filter-footer">
              <button class="dfp-filter-button">Filter</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Creatives -->
      <div id="dfp-panel-creatives" class="dfp-panel">
        <a class="dfp-reload" href="#">Load</a>
        <div class="dfp-rc-header">
          <h2>Creatives</h2>
        </div>
        <div class="dfp-panel-content"></div>
        <div class="dfp-loading"><a href="#" class="dfp-cancel">Cancel</a></div>
        <div class="dfp-panel-filter">
          <a class="dfp-reference-link" target="_blank"
            href="https://developers.google.com/doubleclick-publishers/docs/reference/latest/CreativeService.html#getCreativesByStatement">Reference</a>
          <h3>Filter</h3>
          <div class="dfp-panel-filter-content">
            <textarea class="dfp-filter-textarea">WHERE id != 0 LIMIT 50 OFFSET 0</textarea>
            <div class="dfp-filter-footer">
              <button class="dfp-filter-button">Filter</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Creative Templates -->
      <div id="dfp-panel-creativetemplates" class="dfp-panel">
        <a class="dfp-reload" href="#">Load</a>
        <div class="dfp-rc-header">
          <h2>Creative Templates</h2>
        </div>
        <div class="dfp-panel-content"></div>
        <div class="dfp-loading"><a href="#" class="dfp-cancel">Cancel</a></div>
        <div class="dfp-panel-filter">
          <a class="dfp-reference-link" target="_blank"
            href="https://developers.google.com/doubleclick-publishers/docs/reference/latest/CreativeTemplateService.html#getCreativeTemplatesByStatement">Reference</a>
          <h3>Filter</h3>
          <div class="dfp-panel-filter-content">
            <textarea class="dfp-filter-textarea">WHERE id != 0 LIMIT 50 OFFSET 0</textarea>
            <div class="dfp-filter-footer">
              <button class="dfp-filter-button">Filter</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Custom Targeting -->
      <div id="dfp-panel-custom-targeting" class="dfp-panel">
        <a class="dfp-reload" href="#">Load</a>
        <div class="dfp-rc-header">
          <h2>Custom Targeting</h2>
        </div>
        <div class="dfp-panel-content"></div>
        <div class="dfp-loading"><a href="#" class="dfp-cancel">Cancel</a></div>
        <div class="dfp-panel-filter">
          <a class="dfp-reference-link" target="_blank"
            href="https://developers.google.com/doubleclick-publishers/docs/reference/latest/CustomTargetingService.html#getCustomTargetingKeysByStatement">Reference</a>
          <h3>Filter</h3>
          <div class="dfp-panel-filter-content">
            <label id="dfp-customtargeting-warning" class="invisible">Note:
              You must specify <strong><a
                href="https://developers.google.com/doubleclick-publishers/docs/reference/latest/CustomTargetingService.html#getCustomTargetingValuesByStatement">"customTargetingKeyId
                  IN (...)"</a></strong><br />
            </label>
            <textarea class="dfp-filter-textarea">WHERE id != 0 LIMIT 50 OFFSET 0</textarea>
            <div class="dfp-filter-footer">
              <label>Filter on</label> <select id="dfp-customtargeting-filter"
                class="dfp-filter-select">
                <option value="key">Keys</option>
                <option value="value">Values</option>
              </select>
              <button class="dfp-filter-button">Filter</button>
            </div>
          </div>
        </div>
      </div>

      <!-- LICAs -->
      <div id="dfp-panel-licas" class="dfp-panel">
        <a class="dfp-reload" href="#">Load</a>
        <div class="dfp-rc-header">
          <h2>LICAs</h2>
        </div>
        <div class="dfp-panel-content"></div>
        <div class="dfp-loading"><a href="#" class="dfp-cancel">Cancel</a></div>
        <div class="dfp-panel-filter">
          <a class="dfp-reference-link" target="_blank"
            href="https://developers.google.com/doubleclick-publishers/docs/reference/latest/LineItemCreativeAssociationService.html#getLineItemCreativeAssociationsByStatement">Reference</a>
          <h3>Filter</h3>
          <div class="dfp-panel-filter-content">
            <textarea class="dfp-filter-textarea">WHERE lineitemid != 0 LIMIT 50 OFFSET 0</textarea>
            <div class="dfp-filter-footer">
              <button class="dfp-filter-button">Filter</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Networks -->
      <div id="dfp-panel-networks" class="dfp-panel">
        <a class="dfp-reload" href="#">Load</a>
        <div class="dfp-rc-header">
          <h2>Networks</h2>
        </div>
        <div class="dfp-panel-content"></div>
        <div class="dfp-loading"><a href="#" class="dfp-cancel">Cancel</a></div>
      </div>

      <!-- Orders and Line Items -->
      <div id="dfp-panel-orders" class="dfp-panel">
        <a class="dfp-reload" href="#">Load</a>
        <div class="dfp-rc-header">
          <h2>Orders and Line Items</h2>
        </div>
        <div class="dfp-panel-content"></div>
        <div class="dfp-loading"><a href="#" class="dfp-cancel">Cancel</a></div>
        <div class="dfp-panel-filter">
          <span class="dfp-reference-link">Reference: <a target="_blank"
            href="https://developers.google.com/doubleclick-publishers/docs/reference/latest/LineItemService.html#getLineItemsByStatement">LineItem</a>
            <a target="_blank"
            href="https://developers.google.com/doubleclick-publishers/docs/reference/latest/OrderService.html#getOrdersByStatement">Order</a>
          </span>
          <h3>Filter</h3>
          <div class="dfp-panel-filter-content">
            <textarea class="dfp-filter-textarea">WHERE name != '' LIMIT 50 OFFSET 0</textarea>
            <div class="dfp-filter-footer">
              <label>Filter on</label> <select class="dfp-filter-select">
                <option value="lineitem">Line Items</option>
                <option value="order">Orders</option>
              </select>
              <button class="dfp-filter-button">Filter</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Placements -->
      <div id="dfp-panel-placements" class="dfp-panel">
        <a class="dfp-reload" href="#">Load</a>
        <div class="dfp-rc-header">
          <h2>Placements</h2>
        </div>
        <div class="dfp-panel-content"></div>
        <div class="dfp-loading"><a href="#" class="dfp-cancel">Cancel</a></div>
        <div class="dfp-panel-filter">
          <a class="dfp-reference-link" target="_blank"
            href="https://developers.google.com/doubleclick-publishers/docs/reference/latest/PlacementService.html#getPlacementsByStatement">Reference</a>
          <h3>Filter</h3>
          <div class="dfp-panel-filter-content">
            <textarea class="dfp-filter-textarea">WHERE id != 0 LIMIT 50 OFFSET 0</textarea>
            <div class="dfp-filter-footer">
              <button class="dfp-filter-button">Filter</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Roles -->
      <div id="dfp-panel-roles" class="dfp-panel">
        <a class="dfp-reload" href="#">Load</a>
        <div class="dfp-rc-header">
          <h2>Roles</h2>
        </div>
        <div class="dfp-panel-content"></div>
        <div class="dfp-loading"><a href="#" class="dfp-cancel">Cancel</a></div>
      </div>

      <!-- Publisher Query Language -->
      <div id="dfp-panel-pql" class="dfp-panel">
        <a class="dfp-expand" href="#">Expand</a>
        <div class="dfp-rc-header">
          <h2>Publisher Query Language</h2>
        </div>
        <div class="dfp-panel-content"></div>
        <div class="dfp-loading"><a href="#" class="dfp-cancel">Cancel</a></div>
        <div class="dfp-panel-filter">
          <a class="dfp-reference-link" target="_blank"
            href="https://developers.google.com/doubleclick-publishers/docs/reference/latest/PublisherQueryLanguageService.html">Reference</a>
          <h3>Select Statement</h3>
          <div class="dfp-panel-filter-content">
            <textarea class="dfp-filter-textarea">SELECT * FROM Browser</textarea>
            <div class="dfp-filter-footer">
              <button class="dfp-filter-button">Select</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div id="dfp-footer">Created using Google"s DoubleClick for Publishers
    API. &#169;2012 Google Inc. All rights reserved. Google and DoubleClick are
    trademarks of Google Inc.</div>
  <div id="dfp-signin-tooltip">Click "Sign in" to get started.</div>
  <script type="text/javascript">
    var channel = new goog.appengine.Channel('<%=request.getAttribute("channel_token")%>');
    var socket = channel.open();
    socket.onmessage = dfpwebapp.handleChannelMessage;
    </script>
  <script type="text/javascript">
      // Determine if the user is logged in.
      // Code to run when page loads.
      $(document).ready(function() {
        panelManager = new dfpwebapp.PanelManager({});
        panelManager.init();
        panelManager.registerPanel('dfp-panel-ad-units', '/get?tag=ad-units');
        panelManager.registerPanel('dfp-panel-companies', '/get?tag=companies');
        panelManager.registerPanel('dfp-panel-creatives', '/get?tag=creatives');
        panelManager.registerPanel('dfp-panel-creativetemplates', '/get?tag=creativetemplates');
        panelManager.registerPanel('dfp-panel-custom-targeting', '/get?tag=custom-targeting');
        panelManager.registerPanel('dfp-panel-licas', '/get?tag=licas');
        panelManager.registerPanel('dfp-panel-networks', '/get?tag=networks');
        panelManager.registerPanel('dfp-panel-orders', '/get?tag=orders');
        panelManager.registerPanel('dfp-panel-placements', '/get?tag=placements');
        panelManager.registerPanel('dfp-panel-pql', '/get?tag=pql');
        panelManager.registerPanel('dfp-panel-roles', '/get?tag=roles');
        panelManager.registerPanel('dfp-panel-users', '/get?tag=users');

        // Find the custom targeting filter drop-down
        var dfpFilterSelect = $('#dfp-customtargeting-filter');
        dfpFilterSelect.change(
          // Add a listener to the drop-down box
          function () {
            var state = dfpFilterSelect.val();
            var warning = $('#dfp-customtargeting-warning');
            if (state == 'value') {
              // Make the warning visible
              warning.removeClass('invisible');
              warning.addClass('visible');
            } else {
              // Make the warning invisible
              warning.removeClass('visible');
              warning.addClass('invisible');
            }
          }
        );
        // Attach listener to change view in UI link when network drop-down changes.
        var dfpNetworkSelect = $('#network');
        dfpNetworkSelect.change(
          function() {
            if (dfpNetworkSelect.val() !== 'no_network') {
              window.location.href = "/" + dfpNetworkSelect.val();
              return;
            }
          }
        );

        <%String newNetworkCode = (String) request.getAttribute("new_network_code");
      String error = (String) request.getAttribute("error");%>
        <%if (newNetworkCode != null) {%>
          dfpwebapp.showDialog('#new-network-welcome-dialog', 'Welcome to DFP API');
        <%}%>
        <%if (error != null) {%>
          dfpwebapp.showDialog('#error-dialog', 'API Error');
        <%}%>
      });
    </script>
  <!-- New Network Welcome Dialog -->
  <div id="new-network-welcome-dialog" class="dfp-dialog">
    <h3>
      Your new test network (<%=newNetworkCode%>) has been created.
    </h3>
    <p>
      Please go to the <a
        href="http://www.google.com/dfp/main?networkCode=<%=newNetworkCode%>"
        target="_blank">DFP user interface</a> to configure your new <a
        href="https://developers.google.com/doubleclick-publishers/docs/environments"
        target="_blank">test network</a>.
    </p>
  </div>
  <!-- Error Dialog -->
  <div id="error-dialog" class="dfp-dialog">
    <h3>Error occurred while making API call.</h3>
    <p>
      <%=error%>
    </p>
  </div>
</body>
</html>
