# Comapi Android SDK Samples

This repository contains all the sample code associated with [comapi-sdk-android](https://github.com/comapi/comapi-sdk-android).

For the Chat Sample app to initialise Foundation SDK you will need to provide a value of `apiSpaceId` variable in `com.comapi.sample.SampleApplication` class.
```java
private void initComapi() {
        ...
        // PUT YOUR API KEY HERE
        final String apiSpaceId = "";
        ...
}
```
You can obtain this ID from API Space configuration. For the details please read Comapi [documentation](http://docs.comapi.com/docs/getting-started).

The Chat Sample app is creating [JWT](https://jwt.io/introduction/) token locally. The correct [Authentication Setup](http://docs.comapi.com/docs/channel-setup-app-messaging#section-authentication-setup) on Comapi portal for this sample is the following: issuer - `local`; audience - `local`; shared secret - `secret`; ID Claim - `sub`.

To avoid error log while initialising SDK (it doesn't affect sample app functionality in its current form) you will need to replace mocked [google-services.json](https://support.google.com/firebase/answer/7015592?hl=en) file in `foundation/sample/chat_sample` folder.