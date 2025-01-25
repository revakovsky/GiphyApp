# Giphy App

Test task: an application to display GIF animations from the Giphy website

---

### Project overview

This application retrieves a list of GIF images from the [Giphy](https://developers.giphy.com/) web
API and displays them to the user. Users can click on any GIF to view it in full-screen mode on the
next screen

When the application is launched, the user is greeted with a splash screen where the Internet
connection is checked.  
If no connection is detected and the local database is empty (e.g., the application is being
launched for the first time), a dedicated screen appears to inform the user about the issue with the
Internet connection and provides the option to navigate to the device settings to enable it

If the Internet connection is unavailable but the database already contains previously saved and
cached GIF images, the application opens a screen displaying the GIFs. At the same time, the user is
notified about the lack of Internet connectivity with an option to quickly navigate from this screen
directly to the device settings

**GIF Images Are Loaded in Two Ways:**

1. [Trending GIFs](https://developers.giphy.com/docs/api/endpoint/#trending) - trending GIFs are
   loaded every time the application is opened. They are retrieved either from the local database or
   from the Internet if the database is empty. If the user performs a “swipe to refresh” action on
   the screen displaying the GIF list, a new set of trending GIFs will be fetched directly from the
   Internet, saved to the database, and displayed on the screen

2. [Searched GIFs](https://developers.giphy.com/docs/api/endpoint/#search) - these GIFs are fetched
   from the Internet based on the query entered in the search field (with validation for specific
   characters). The retrieved GIFs are displayed immediately on the search screen but are not saved
   to the database

### Minimum Android Version:

- Mobile app: Android 8.0 (API level 26)

### Project Launch:

// TODO: add a description how to launch the app and it's possible or now

### Basic Architecture

- Jetpack Compose
- Multi-module project
- MVI
- Offline-first app: Ensures functionality without an active internet connection

### Features and Functionality

- Support for both landscape and portrait orientations
- Android Splash Screen
- Type-safe compose navigation
- Internet check during the entire app lifecycle with notifications for connectivity issues
- Kotlin Coroutines for background operations
- Kotlin Flows for efficient and reactive data handling and transfer
- Focusable states for advanced accessibility and interactions.

### Libraries and Frameworks

- Koin DI
- Room Database
- Retrofit
- Coil
