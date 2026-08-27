🏆 VU Sports Society

A modern Android application developed for managing and connecting the sports community of Virtual University of Pakistan.

The VU Sports Society app provides a centralized platform for students and sports society members to access sports-related information, manage activities, and interact with the society through a modern mobile interface.

📱 Overview

VU Sports Society is an Android application built with Kotlin and Jetpack Compose.

The application uses Firebase as its backend infrastructure for authentication, cloud data storage, and file storage.

The project follows a modular structure with separate components for:

- UI screens
- Reusable components
- Data models
- Navigation
- ViewModels
- Theme and UI configuration

✨ Features

- 🔐 Firebase Authentication
- 🏅 Sports society management
- 👤 User-oriented functionality
- 📋 Sports-related information and activities
- ☁️ Cloud-based data storage with Firebase Firestore
- 📁 Firebase Storage integration
- 🧭 Navigation between application screens
- 🎨 Modern Material 3 interface
- ✨ Lottie animations
- 🖼️ Image loading with Coil
- 📱 Responsive Jetpack Compose UI
- 🧩 Reusable Compose components
- 🌓 Centralized application theme

🛠️ Technology Stack

Technology| Usage
Kotlin| Primary programming language
Jetpack Compose| UI development
Material 3| UI components and design
Firebase Authentication| User authentication
Firebase Firestore| Cloud database
Firebase Storage| File/image storage
Navigation Compose| Application navigation
Coil| Image loading
Lottie Compose| Animations
Android Gradle Plugin| Android build system
Ktlint| Kotlin code formatting/linting

The current Gradle configuration uses compileSdk 36, targetSdk 36, and minSdk 26.

🏗️ Project Architecture

The application source code is organized into dedicated packages:

app/
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── vusports/
        │           └── bc220200768/
        │               ├── components/
        │               ├── models/
        │               ├── navigation/
        │               ├── screens/
        │               ├── ui/
        │               │   └── theme/
        │               ├── viewmodels/
        │               └── MainActivity.kt
        │
        ├── assets/
        └── res/

The repository currently contains dedicated "components", "models", "navigation", "screens", "ui/theme", and "viewmodels" packages.

Components

Contains reusable Jetpack Compose UI components used throughout the application.

Models

Contains application data models used to represent and work with application data.

Navigation

Contains navigation configuration and routes used to move between application screens.

Screens

Contains the application's individual UI screens and their associated functionality.

ViewModels

Contains ViewModels responsible for managing UI state and application logic.

UI / Theme

Contains the application's Compose theme and UI styling.

🔥 Firebase

The application is integrated with Firebase services.

Firebase Authentication

Firebase Authentication is used to handle user authentication.

Cloud Firestore

Cloud Firestore provides cloud-based data storage for application information.

Firebase Storage

Firebase Storage is used for storing files and images.

The Android module includes Firebase Authentication, Firestore, and Storage dependencies and the Google Services Gradle plugin.

⚙️ Requirements

Before running the project, make sure you have:

- Android Studio
- JDK 11 or compatible Android Studio Java configuration
- Android SDK 36
- Android device or emulator running Android 8.0 (API 26) or higher
- A Firebase project configured for the application

The project is configured for Java 11 and Kotlin JVM target 11.

🚀 Getting Started

1. Clone the repository

git clone https://github.com/syedfahadmuzafar/vu-sports-society.git

2. Open the project

Open the cloned project in Android Studio.

Allow Android Studio to complete Gradle synchronization and download the required dependencies.

3. Configure Firebase

Create or use a Firebase project and configure the Android application using the package name:

com.vusports.bc220200768

Download the Firebase "google-services.json" file and place it inside:

app/google-services.json

«Security note: Do not publish private Firebase credentials, API keys with unrestricted access, service-account credentials, or other sensitive configuration in a public repository.»

4. Build the project

From Android Studio:

Build → Make Project

Or using Gradle:

./gradlew assembleDebug

On Windows:

gradlew.bat assembleDebug

5. Run the application

Connect an Android device or start an Android emulator and select:

Run → Run 'app'

📦 Main Dependencies

The project currently uses AndroidX and Jetpack Compose libraries together with Firebase services.

Notable dependencies include:

- AndroidX Core KTX
- AndroidX Lifecycle Runtime
- Activity Compose
- Jetpack Compose BOM
- Compose UI
- Compose Material
- Material 3
- Navigation Compose
- Firebase Authentication KTX
- Firebase Firestore KTX
- Firebase Storage KTX
- Lottie Compose
- Coil Compose
- Material Icons Extended

These dependencies are defined in the application's Gradle configuration.

🎨 UI

The application is built using Jetpack Compose, allowing the interface to be implemented declaratively and maintained through reusable composable components.

Material 3 is used for the application's UI components and design system, while Lottie is available for animations and Coil is used for image loading.

🧪 Code Quality

The project uses Ktlint for Kotlin code formatting and linting.

Ktlint is configured for Android and is set to fail when linting requirements are not satisfied.

Run the formatting/check task with Gradle as required by the project configuration.

📂 Repository Structure

vu-sports-society/
│
├── .idea/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── assets/
│   │       ├── java/
│   │       │   └── com/vusports/bc220200768/
│   │       │       ├── components/
│   │       │       ├── models/
│   │       │       ├── navigation/
│   │       │       ├── screens/
│   │       │       ├── ui/
│   │       │       ├── viewmodels/
│   │       │       └── MainActivity.kt
│   │       └── res/
│   │
│   ├── build.gradle.kts
│   ├── google-services.json
│   └── proguard-rules.pro
│
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts

The repository's top-level Android project contains the "app" and "gradle" directories along with the Gradle configuration files.

🎓 Academic Project

Project: VU Sports Society
Platform: Android
University: Virtual University of Pakistan
Student ID / Package Identifier: "BC220200768"

👨‍💻 Developer

Syed Fahad Muzafar

GitHub:

https://github.com/syedfahadmuzafar

Project repository:

https://github.com/syedfahadmuzafar/vu-sports-society

📄 License

This project is intended for academic and educational purposes.

If you intend to reuse, modify, or distribute this project, please contact the author and follow the applicable project/university requirements.

---

<p align="center">
  Made with ❤️ using Kotlin & Jetpack Compose
</p>
