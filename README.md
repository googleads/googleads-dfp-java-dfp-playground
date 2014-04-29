DFP API Playground
==================

This is a project using the Ads Java client library in Google App Engine to
create a web application that serves as a playground for DFP API developers.
To use the playground, users need to authenticate using OAuth 2.0 and
authorize the application to make calls to the DFP API on the user's behalf.
For new API developers, if your Google account is not yet associated with any
test networks, the playground will allow you to create one after authorization.
In the application, you can load various DFP entities in the panels and filter
on them using PQL filter statements if you wish.

## Getting started

This project can be used in two ways. You have the option to use Maven as your
build and deploy mechanism or simply as jars coupled with the
[Google Plugin for Eclipse](https://developers.google.com/appengine/docs/java/tools/eclipse)
and/or [appcfg tool](https://developers.google.com/appengine/docs/java/tools/uploadinganapp).


### Before you do anything

This readme assumes you have already registered an AppEngine application. If you have not,
please do so first at https://appengine.google.com.

You will also need to register a web OAuth2 application to get a valid client ID and secret. When
registering the application, be sure to include the following callbacks:

   ```
   http://localhost:8888/oauth2callback
   https://localhost:8888/oauth2callback
   http://test.<your-app-id>.appspot.com/oauth2callback
   https://test.<your-app-id>.appspot.com/oauth2callback
   http://<your-app-id>.appspot.com/oauth2callback
   https://<your-app-id>.appspot.com/oauth2callback
   ```

The first callback is intended for local development, the second is for the test version of your app (the default in the web.xml),
and the third is for your production application.

### For using maven with Eclipse

In the [releases section](https://github.com/googleads/googleads-dfp-java-dfp-playground/releases) download a file like `dfp-playground-maven-project-v.v.v.tar.gz` and extract it.

The rest of the dependencies will be automatically pulled in using Maven, but you
can examine and modify them through the project's pom.xml if you wish.

To build and run the project locally, follow these steps:

1. Modify

   Fill in your client ID and client secret in
   src/main/webapp/WEB-INF/appengine-web.xml You can create the client ID and
   secret in the API console (https://console.developers.google.com) using
   the redirect URI below:

   ```
   http://localhost:8888/oauth2callback
   https://localhost:8888/oauth2callback
   ```

2. Import

   Open Eclipse, and import the project by going to  **File > Import**, then **General > Existing projects into workspace**
   and selecting the extracted folder.

3. Run (using one of the following methods): 

  - Run from Eclipse

   Within the project, open the folder eclipse-launch-profiles. Right-click DevAppServer.launch,
   and then select **Run As > DevAppServer**.

   The playground will be running at: http://localhost:8888/

  - Run from the command line

   From the root of the project directory, run:

   ```
   $ mvn appengine:devserver
   ```

   The playground will be running at: http://localhost:8888/

5. (Optional) Debug
   
   The DevAppServerWithDebugFlag.launch launch profile can be used to launch the server
   with debug flags so that you can connect a remote debugger. 

   To do so, open the folder eclipse-launch-profiles. Right-click DevAppServerWithDebugFlag.launch,
   and then select **Run As > DevAppServerWithDebugFlag**.

   The server will load as usual, but stop with following output:

   ```
   [INFO] Listening for transport dt_socket at address: 1044
   ```

   At this point, right-click RemoteDebugDevAppServer.launch,
   and then select **Debug As > RemoteDebugDevAppServer**. The server will continue to run
   until it reaches your breakpoint.

   If no source is found, you may have to attach the project manually in the source lookup
   dialog.

#### Deploying to App Engine

1. Create an App Engine application at https://appengine.google.com/.

2. Follow step 1 from above. Instead of localhost, you will
   need to add a redirect URI that corresponds to the application ID of your
   newly created App Engine application:

   ```
   http://APP_ID.appspot.com/oauth2callback
   https://APP_ID.appspot.com/oauth2callback
   ```

3. Replace the application ID (dfp-playground) in
   src/main/webapp/WEB-INF/appengine-web.xml with your App Engine application ID

   You may also want to update the version to something other than `test` (the default).

4. Run the following (you will be prompted for your username and password):

   ```
   $ mvn appengine:update
   ```

   Alternatively, you can update the project from within Eclipse by using the
   UpdateApplication.launch profile. Open the eclipse-launch-profiles folder,
   right-click UpdateApplication.launch, and then select **Run As > UpdateApplication**.

### For using the Google Plugin for Eclipse (with jars)

In the [releases section](https://github.com/googleads/googleads-dfp-java-dfp-playground/releases) download a file like `dfp-playground-jars-and-google-eclipse-plugin-project-v.v.v.tar.gz` and extract it.

If you are not familiar with the Google Plugin for Eclipse, please read through this [getting started guide](https://developers.google.com/eclipse/docs/getting_started)

1. Modify

   Fill in your client ID and client secret in
   war/WEB-INF/appengine-web.xml You can create the client ID and
   secret in the API console (https://console.developers.google.com/project) using
   the redirect URI below:

   ```
   http://localhost:8888/oauth2callback
   https://localhost:8888/oauth2callback
   ```

2. Import

   Open Eclipse, and import the project by going to **File > Import**, then **General > Existing projects into workspace**
   and selecting the extracted folder.

   **IMPORTANT:** The project will not compile at this stage. The next step will complete the process.

3. Setup

   Next, add Google AppEngine functionality to the project. Right-click your project and
   select **Google > App Engine Settings**. Check the **Use Google App Engine box** and click **OK**.

   The project should now compile and not have any build errors.

4. Run
   
   Right-click your project and then select **Run As > Web Application**.

   The playground will be running at: http://localhost:8888/
   
#### Deploying to App Engine

1. Create an App Engine application at https://appengine.google.com/.

2. Follow step 1 from above. Instead of localhost, you will
   need to add a redirect URI that corresponds to the application ID of your
   newly created App Engine application:

   ```
   http://APP_ID.appspot.com/oauth2callback
   https://APP_ID.appspot.com/oauth2callback
   ```

3. Replace the application ID (dfp-playground) in
   war/WEB-INF/appengine-web.xml with your App Engine application ID

   You may also want to update the version to something other than `test` (the default).

4. Right-click your project and then select **Google > Deploy to AppEngine**.
 
   For more information about deploying AppEngine projects, see the [developer site](https://developers.google.com/eclipse/docs/appengine_deploy).


## Where do I submit bug reports, feature requests and patches?

All of these items can be submitted at
https://github.com/googleads/googleads-dfp-java-dfp-playground/issues


## How do I get help?

Post a question to the forum for the community and API advisors:
https://groups.google.com/forum/#!forum/google-doubleclick-for-publishers-api

Authors:
    arogal@google.com (Adam Rogal)

Past contributors:
    shamjeff@google.com (Jeff Sham)
