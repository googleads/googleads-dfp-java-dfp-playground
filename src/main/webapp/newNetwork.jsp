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
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>DFP API Playground</title>
<link rel="stylesheet" type="text/css" href="css/common.css" />
<style>
.dfp-content {
  padding-left: 10px;
}

#loader {
  display: none;
}
</style>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.5.1/jquery.min.js"></script>
<script>
function showSpinner() {
  $('#createNetworkSubmit').hide();
  $('#loader').show();
}
</script>
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
    <h1 id="dfp-logo">DFP API Playground</h1>
  </div>
  <hr />
  <div class="dfp-content">
    <%
      if (request.getAttribute("error") != null) {
    %>
    <p>
    <h2 class="error">Error creating test network:</h2>
    <p>
      Please make sure you have created a <a
        href="http://www.google.com/accounts/newaccount">Google account</a> that
      is not already associated with a test network. You can find more
      information about creating test accounts in the <a
        href="https://developers.google.com/doubleclick-publishers/docs/signup">
        documentation</a>.
    </p>

    <%
      } else {
    %>
    <h2>You do not have a test network associated with your account. Would
      you like to create one?</h2>
    <form action="/newnetwork" method="POST" onsubmit="showSpinner()">
      <input type="hidden" name="createNetwork" value="true" />
      <input id="createNetworkSubmit" type="submit" value="Create Test Network">
      <div id="loader">
        <img src="images/ajax-loader-grey.gif" />
        Please wait while your test network is being created.
      </div>
    </form>
    <%
      }
    %>
  </div>
  <div id="dfp-footer">Created using Google"s DoubleClick for Publishers
    API. &#169;2012 Google Inc. All rights reserved. Google and DoubleClick are
    trademarks of Google Inc.</div>
</body>
</html>
