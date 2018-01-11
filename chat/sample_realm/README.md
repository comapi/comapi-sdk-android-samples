# Android Comapi Chat Demo
Sample app using Comapi Chat SDK to provide messaging functionality. This sample stores messaging data internally in db.

## Prerequisites

- Signup for Comapi account [here](http://www.comapi.com).
- Follow quickstart guide [here](http://docs.comapi.com/docs/quick-start) and create an api space.
- Setup [authentication](http://docs.comapi.com/docs/channel-setup-app-messaging) for your apiSpace with the following values.

| Name | Value |
| -----------| ----- |
| `Issuer`   |  `local`| 
| `Audience` |  `local`| 
| `Shared Secret` |  `secret`| 
| `ID Claim` |  `sub`| 

These same will be used in [AuthChallengeHandler.java](https://github.com/comapi/comapi-sdk-android-samples/blob/master/foundation/sample/chat_sample/src/main/java/com/comapi/sample/comapi/AuthChallengeHandler.java) to create a [JWT](https://jwt.io/introduction/) locally.

- Provide a value of `apiSpaceId` variable in [SampleApplication.class](https://github.com/comapi/comapi-sdk-android-samples/blob/master/foundation/sample/chat_sample/src/main/java/com/comapi/sample/SampleApplication.java).
```java
private void initComapi() {
        ...
        // PUT YOUR API KEY HERE
        final String apiSpaceId = "";
        ...
}
```

## Key Features:

### Login dialog / Logout button on Conversation list Activity
In order to create a Comapi session, you will need to create a profile within your apiSpace. Login dialog allows you to specify a profileId that you would like to use. The authentication mechanism is dealt with entirely in the app to simplify this example.

When you launch this app you will see the login dialog. Enter a profileId and you will be redirected to the conversation list view.

### Conversations list Activity

This Activity displays a list of conversations and allows you to drill into a particular one. 
You can create a new conversation by clicking the floating button at the bottom right.

### Conversations detail Activity

This Activity displays a single conversation and allows you to send messages to it. 

### Manage Participants

You can add / remove participants for this conversation by clicking action button at the top right. The corresponding Activity will display current list of participants and allow adding/removing existing users to the conversation participant list.

### Setup a conversation between 2 users

Here is a good way to test out the functionality of this test app with a multi user conversation:

- Install the app on two different devices

- Login to the app using different profileId's on each device.

- Create a conversation on one of the devices.

- On the same device, add the other user as a participant.

- The conversation should appear on the other device conversation list.

- Open the conversation detail Activity on both.

- Start sending massages back and forth.