# Unwired Android App

This is an Android application developed using Kotlin and Java, and built with Gradle. The app provides a platform for users to create and join private or public groups for chat.

## Features

- User authentication
- Create private or public groups
- Add users to groups
- Real-time chat functionality (without Push)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Android Studio Jellyfish | 2023.3.1 Patch 1 or later
- JDK 8 or later
- Android SDK

### Installation

1. Clone the repo: `git clone https://github.com/harmdhast/unwired-android.git`
2. Open the project in Android Studio
3. Change API_ULR and API_PORT as per your server configuration in `app/src/main/java/com/example/unwired/app/api/APIClient.kt`
4. Build the project and run on an emulator or real device

Default user is `test` and password is `test`

[demo.webm](https://github.com/harmdhast/unwired-android/assets/5670689/80f940c2-ae0c-48b0-9fb7-96e7b277ff99)

## Not implemented yet

- Push notifications
- Dynamic updates to chat messages
