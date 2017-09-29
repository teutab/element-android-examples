# element-android-examples
Element example apps

_Element SDK Library_ is a standalone Android application that enables the creation of biometrics models that can be used to authenticate users. _Element Orbit_ is the Android library providing interfaces to communicate with _Element SDK Library_. This document contains information to integrate _Element Orbit_ into an Android application by using Android Studio.

### Setup with Android Studio
#### Import _Element Orbit_
1. Download the latest _[element-orbit-release.aar](https://github.com/Element1/element-android-examples/tree/master/sdk-integration-example/element-orbit)_ file.
2. Open your project in Android Studio.
3. On the top menu bar, click _File->New->New Module->Import .arr/.jar libraries->Next_.
4. In the next window, choose the path to _element-orbit.aar_ in the _File Name_ field, and type in _element-orbit_ in the _Subproject name_ field.
5. Click the _Finish_ button and wait for Android Studio to finish building the project.

#### Refer to _Element Orbit_ as a project dependency
1. On the top menu bar, click _File->Project Structure_.
2. Select your app module under _Modules_ on the left pane, switch to the _Dependencies_ tab, and click on the _+_ button at the bottom of the window.
3. Choose the _Module dependency_ option in the popup, and select _element-orbit_.

### Integrating _Element Orbit_ into your application
* In AndroidManifest.xml, declare your api key in a meta-data tag and declare OrbitReceiver inside the _application_ node.
```
<manifest>
    .....
    <application>
        .....
        <meta-data
            android:name="com.element.ApiKey"
            android:value="[YOUR_API_KEY]"/>
        .....
        <receiver
            android:name="com.element.orbit.OrbitReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="com.element.orbit.ACTION_RESPONSE"/>
            </intent-filter>
        </receiver>
        .....
    </application>
</manifest>
```
* In the app level build.gradle, add the gson library as a dependency.
```
dependencies {
    .....
    compile 'com.google.code.gson:gson:2.2.4'
}
```

### Implementation of the key classes
#### _RequestManager_
_RequestManager_ defines a set of handy functions to send out requests to _Element SDK Library_. After sending out requests via _RequestManager_, _OrbitListener_ will get called as soon as hearing back from _Element SDK Library_.
* syncUserData(): request to fully sync user data from the SDK Library
* addNewUser(): request to add a new user
* trainModel(): request to start training all user models
* authenticateUser(): requests to authenticate the user

#### _OrbitService & OrbitListener_
_OrbitService_ is the main gateway to the communicate with _Element SDK Library_. Initialize _OrbitService_ when an Activity starts with a proper _OrbitListener_ to receive data and information receiving back from _Element SDK Library_.
```https://play.google.com/store/apps/details?id=com.element.portal
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ...

    OrbitService.registerCallback(this);
}
```
* onAllUserData(): get called when all user data is received
* onUpdatedUserData(): get called when user data is updated
* onAuthenticated(): get called when the authentication result is received
* onMessage(): get called when any message is returned


#### _Authentication & UserData_
Both are data class as packets that are delivered between _Element SDK Library_ & _OrbitService_.
* _Authentication_: contains the authentication information, status and results
* _UserData_: contains userId, names, etc.


### _Update Config_
To get Config Update:
1. RequestManager.updateConfig(config): register a Config
2. onUpdateConfig(): receive config updates
3. onMessage(): any message related to configuration 

### Notes
* _Element SDK Library_ is required to be installed on an Android device. It is available on [HockeyApp](https://rink.hockeyapp.net/apps/458abb63bfb442b0afc8989fd0e8b853).
* _Element SDK Library_ is currently in beta release. Please contact Element for access.
