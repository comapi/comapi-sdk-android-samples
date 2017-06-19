# Comapi Android SDK Samples

This repository contains all the sample code associated with [comapi-sdk-android](https://github.com/comapi/comapi-sdk-android).

For the chat sample app to initialise foundation sdk you will need to provide a value of apiSpaceId variable in SampleApplication class. You can obtain this id from a configuration created on www.comapi.com portal.

To avoid initialisation error (doesn't affect sample app functionality) you will need to replace mocked [google-services.json](https://support.google.com/firebase/answer/7015592?hl=en) file in foundation/
sample/chat_sample folder (configured for the package 'com.comapi.chat').