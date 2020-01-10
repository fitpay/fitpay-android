# FitPay Android SDK

[![GitHub license](https://img.shields.io/github/license/fitpay/fitpay-ios-sdk.svg)](https://github.com/fitpay/fitpay-ios-sdk/blob/develop/LICENSE)
[![Build Status](https://travis-ci.com/fitpay/fitpay-android-sdk.svg?branch=develop)](https://travis-ci.com/fitpay/fitpay-android-sdk)
[![Bintray](https://api.bintray.com/packages/fit-pay/maven/com.fitpay/images/download.svg)](https://bintray.com/fit-pay/maven/com.fitpay)
[![codecov](https://codecov.io/gh/fitpay/fitpay-android-sdk/branch/develop/graph/badge.svg)](https://codecov.io/gh/fitpay/fitpay-android-sdk)

## Version Strategy
Versioned releases follow the common major.minor.maintenance naming convention. Only major versions are allowed to introduce breaking changes (including removal of deprecated services). Minor versions typically introduce a new feature or functionality. Maintenance versions are commonly bug fixes and minor improvements like refactors and inline commenting.

## Building the library

### Building using Android Studio

```
mkdir fitpay  
cd fitpay
git clone git@github.com:fitpay/fitpay-android-sdk.git  
cd fitpay-android-sdk
studio.sh (or double click your android studio)
Open an existing Android Studio project
/home/yourname/fitpay/fitpay-android-sdk
Click on Gradle (topright), select fitpay-android->Tasks->build, build - right click and select "Run fitpay-android[build]"
```

### Building from the command line

Ensure you have the Android SDK installed on your machine. http://developer.android.com/sdk/index.html

Gradle also automatically runs the tests when you build.

```
export ANDROID_HOME=/home/myname/Android/Sdk
cd ~
mkdir fitpay  
cd fitpay
git clone git@github.com:fitpay/fitpay-android-sdk.git  
cd fitpay-android-sdk  
./gradlew clean build  
```

### Implementation

Before the start it's better to check the references [PebblePagarePaymentDeviceConnector](https://github.com/fitpay/Pagare_Android_WV/blob/develop/app/src/main/java/fitpay/pagare/paymentdevice/pebble/PebblePagarePaymentDeviceConnector.java) and [BaseWVActivity](https://github.com/fitpay/Pagare_Android_WV/blob/develop/app/src/wvUI/java/fitpay.pagare/activities/BaseWvActivity.java).

There are some main things that you need to implement.

* Start the service:
```
DeviceService.run(context);
```
* Create your connector:
```
MockPaymentDeviceConnector deviceConnector = new MockPaymentDeviceConnector();
deviceConnector.setContext(this);
PaymentDeviceConnectorManager.getInstance().addConnector("put_your_id_here", deviceConnector);
```
* Wait for ```Device``` info:
```
class DeviceListener extends ConnectionListener {
	private WebViewOpeningPaymentDeviceListener() {
		super();
		mCommands.put(Device.class, data -> onDeviceInfoReceived((Device) data));
	}
}
NotificationManager.getInstance().addListener(new DeviceListener());
```

- ##### Continue with WebView:

* Assign the connector:
```
WebViewCommunicatorImpl communicator = new WebViewCommunicatorImpl(activity, deviceConnector, webViewId);
```
* Open url link using ```WvConfig``` data:
```
WvPaymentDeviceInfoSecure paymentDeviceModel = new WvPaymentDeviceInfoSecure(device);
Prefs prefs = Prefs.with(this);

WvConfig wvConfig = new WvConfig.Builder()
	.version("0.0.1")
	.clientId(config.get(ApiManager.PROPERTY_CLIENT_ID))
	.setCSSUrl(config.get(Constants.PROPERTY_CUSTOM_CSS_URL))
	.email(prefs.getString(Constants.PROPERTY_EMAIL, ""))
	.accountExist(prefs.getBoolean(Constants.PROPERTY_EXISTING_ACCOUNT, false))
	.demoMode(prefs.getBoolean(Constants.PROPERTY_DEMO_MODE, false))
	.demoCardGroup(prefs.getString(Constants.PROPERTY_DEMO_CARD_GROUP_SELECTED, ""))
	.useWebCardScanner(false) //we are going to use CardIO card scanner
	.paymentDevice(paymentDeviceModel)
	.build();

wv.loadUrl(config.get(Constants.PROPERTY_WV_URL) + "?config=" + wvConfig.getEncodedString());
```

- ##### Continue with native UI:

You can have a full control on your app. In a such case it's your own responsibility to sign up/sign in user, manage devices, connectors and credit cards from code.
Use [WalletActivity](https://github.com/fitpay/Pagare_Android_WV/blob/develop/app/src/nativeUI/java/fitpay/pagare/activities/WalletActivity.java) as a reference.

#### Connector filters and listeners
Each connector has unique ```paymentDeviceConnector.id()``` that you can use as a filter for your events.

Send event with a filter through ```paymentDeviceConnector.postData(data)``` or ```RxBus.post(connectorId, data)```.

Receive data for specific event:
```
class CustomEventListener extends Listener {
	public CustomEventListener(String connectorId) {
	    super(connectorId);
	    mCommands.put(CustomData.class, data -> onEvent((CustomData) data));
	}

	private void onEvent(CustomData request) {
	    //do something
	}
}

NotificationManager.getInstance()
	.addListener(new CustomEventListener(paymentDeviceConnector.id()));
```

### Running tests using Android Studio

```
Open an existing Android Studio project
/home/yourname/fitpay/fitpay-android-sdk
Click on Gradle (topright), select fitpay-android->Tasks->verification, test - right click and select "Run fitpay-android[test]"
```

### Running tests from the command line

```
cd fitpay-android-sdk
./gradlew clean test
```

### Running code coverage

You can run code coverage in the android SDK (to highlight individual file results) or manually. To run manually, run:
```
gradlew testDebugUnitTestCoverage
```
(Filtered) results can be found in fitpay/build/reports/jacoco/testDebugUnitTestCoverage/html. You can also see per line information in fitpay/build/reports/jacoco/testDebugUnitTestCoverage/testDebugUnitTestCoverage.xml.  

## Using a pre-built version of the SDK as a dependency for your build:

Pre-built versions of the Android SDK are hosted on jcenter(). To use in your project:

* make sure jcenter() is listed in the ```repositories``` closure of your application's build.gradle file.

    ```
    buildscript {
        repositories {
            jcenter()
        }
    }
    ```
* add the pre-built SDK dependency, for example to use v0.5.0 of the SDK: ```compile 'com.fitpay.android:android_sdk:0.5.0'``` dependency to the ```dependencies``` closure of the build.gradle file.
    * Grab via Maven:

        ```xml
        <dependency>
        <groupId>com.fitpay.android</groupId>
        <artifactId>android_sdk</artifactId>
        <version>0.5.0</version>
        </dependency>
        ```
    * or Gradle:

        ```groovy
        compile 'com.fitpay.android:android_sdk:0.5.0'
        ```

## Using local version of the SDK as a dependency for your build:
In order to use a local version of the SDK in your project, you need to first build the local repository by using the Gradle task ```uploadArchives``` that's included in the FitPay Android SDK. After running this task, the compiled SDK will be outputted to a local folder on your computer ("LocalRepository") and will be available for use in your project. Note that you may have to clear gradles cached version of the SDK, usually found in $HOME/.gradle/caches/.

You can run the Gradle task and build the repository from Android Studio or from the commandline.

* Build using Android Studio

    ```
    Open the FitPay Android SDK in Android Studio

    /home/yourname/fitpay/fitpay-android-sdk

    Click on Gradle (topright), select fitpay-android->Tasks->publishing, publishToMavenLocal - right click and select "Run fitpay-android"
    ```

* Build from the commandline
    ```
    Open the FitPay Android SDK in your commandline

    cd fitpay-android-sdk
    ./gradlew clean uploadArchives
    ```

Now that you've built the repository, you need to tell your Android project where it is located and that it needs to be included in your project. Open your Android project, and do the following:

1. Add the local repository's location to the top-level build.gradle file
    ```
    def localMavenRepository = 'file://' + new File(System.getProperty('user.home'), 'LocalRepository').absolutePath
    def String pbwURL = 'http://'
    def String metaDataURL = 'http://artifactory.fpctrl.com:8080/artifactory/repo/fitpay/pagare/maven-metadata.xml'
    ```

2. Include ```localMavenRepository()``` in the ```repositories``` closure of the same top-level build.gradle file.
    ```
    allprojects {
        repositories {
            maven { url localMavenRepository }
        }
    }
     ```
3. Add the repository as a dependency to the module-level build.gradle file of your project.
    ```
    dependencies {
        implementation fileTree(dir: 'libs', include: ['*.jar'])
        implementation 'com.fitpay.android:android_sdk:1.0.0'
    }
    ```

That's it! You are now able to build from your local repository.

## Contributing to the SDK
We welcome contributions to the SDK. For your first few contributions please fork the repo, make your changes and submit a pull request. Internally we branch off of develop, test, and PR-review the branch before merging to develop (moderately stable). Releases to Master happen less frequently, undergo more testing, and can be considered stable. For more information, please read:  [http://nvie.com/posts/a-successful-git-branching-model/](http://nvie.com/posts/a-successful-git-branching-model/)

## Progaurd Setup

For those building your application using progaurd, FitPay has created the following suggestions:

```
-dontwarn

#When not preverifing in a case-insensitive filing system, such as Windows. Because this tool unpacks your processed jars, you should then use:
-dontusemixedcaseclassnames

#Specifies not to ignore non-public library classes. As of version 4.5, this is the default setting
-dontskipnonpubliclibraryclasses

#Preverification is irrelevant for the dex compiler and the Dalvik VM, so we can switch it off with the -dontpreverify option.
-dontpreverify

#Specifies to write out some more information during processing. If the program terminates with an exception, this option will print out the entire stack trace, instead of just the exception message.
-verbose

#The -optimizations option disables some arithmetic simplifications that Dalvik 1.0 and 1.5 can't handle. Note that the Dalvik VM also can't handle aggressive overloading (of static fields).
#To understand or change this check http://proguard.sourceforge.net/index.html#/manual/optimizations.html
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

#Annotations for greendao and LineNumber for reports in play store
-keepattributes *Annotation*, LineNumberTable

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

-keepattributes SetJavaScriptEnabled
-keepattributes JavascriptInterface
-keepattributes InlinedApi

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.app.Fragment
-keep public class com.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}
-keep class **.R$*

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepclassmembers class com.fitpay.android.webview.WebViewCommunicator {
    public *;
}

-keepclassmembers class com.fitpay.android.webview.impl.WebViewCommunicatorImpl{
    public *;
}


# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

# Keep all FitPay SDK
-keep class com.fitpay.android.**  {*;}
-keep interface com.fitpay.android.** { *; }

# AppCompat
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Retrofit 2.X
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# RxJava
-dontwarn sun.misc.**

-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

# Many of the Conscrypt classes are referenced indirectly via JNI or
# reflection.
# This could probably be tightened up, but this will get it building for now.
# TODO(kroot): Need anything special to prevent obfuscation?
-keep class org.conscrypt.** { *; }

# Backward compatibility code.
-dontnote libcore.io.Libcore
-dontnote org.apache.harmony.xnet.provider.jsse.OpenSSLRSAPrivateKey
-dontnote org.apache.harmony.security.utils.AlgNameMapper
-dontnote sun.security.x509.AlgorithmId

-dontwarn dalvik.system.BlockGuard
-dontwarn dalvik.system.BlockGuard$Policy
-dontwarn dalvik.system.CloseGuard
-dontwarn com.android.org.conscrypt.AbstractConscryptSocket
-dontwarn com.android.org.conscrypt.ConscryptFileDescriptorSocket
-dontwarn com.android.org.conscrypt.OpenSSLSocketImpl
-dontwarn org.apache.harmony.xnet.provider.jsse.OpenSSLSocketImpl
```

## License
This code is licensed under the MIT license. More information can be found in the [LICENSE](LICENSE) file contained in this repository.

## Questions? Comments? Concerns?
Please contact the team via support@fit-pay.com

# Fit Pay Internal Instructions
## Release Steps
Note: You must have bintray permissions set up to deploy. Once permissions are set, create a `bintray.properties` file in the root project folder with `bintray_user` and `bintray_key` properties.  [Fitpay bintray](https://bintray.com/fit-pay). 

```
bintray_user=USERNAME
bintray_key=API_KEY
```

1. Run deploy script with old and new version numbers (maintain 3 digit semantic versioning)
	* Example: `sh deploy.sh 1.2.0 1.2.1`
	* You should be on develop branch
	* The script will exit early if you don't supply two arguments or have uncommitted changed
* Release app in bintray
	* Click into Maven section
	* There will be a banner asking if you would like to publish the latest version
	* If the upload failed, you can retry it with `./gradlew bintrayUpload` once the problem has been fixed.
* Create a release in Github
	* Use the following convention for name: `FitPay SDK for Android vX.X.X`
	* Include notes using proper markdown about each major PR in the release.
		* notes can be gathered from commit messages and from github releases page (commits since this release)
* Confirm release was successful by updating the gradle file and syncing in Pagare
* Don't forget to switch your local branch back

The test mocked responses for tests are in the resources/tests.zip file.  If you change any values in the tests, you will need to update that file.
