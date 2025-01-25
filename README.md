# Giphy App

Test app

---


### Project overview

// TODO: MODIFY an overview!!!

This application is used to download a list of GIF images from
the [Giphy](https://developers.giphy.com/) web API and show them to the user.
If desired, the user can click on any GIF and it will open on the next screen in full-screen mode.

**Images are uploaded in two different ways:**

1. [Trending GIF images](https://developers.giphy.com/docs/api/endpoint/#trending) that are loaded
   every time you enter the application either from the database or from
   the Internet if the database is empty. If you perform a “swipe to refresh” action on the screen
   with
   a list of GIFs, a new list with trending GIFs will be downloaded directly from the Internet,
   saved to the database,
   and displayed on the screen.
2. [Searched GIF images](https://developers.giphy.com/docs/api/endpoint/#search) that are relevant
   to the query entered in the search field (there is a check for entering certain
   characters). These images are downloaded from the Internet and immediately displayed on the
   search screen
   without saving them to the database

When opening the application, the user is greeted with a splash screen on which the Internet
connection is checked.
If there is no connection, the corresponding screen is displayed, which informs about this and
offers to go
to the device settings and turn on the Internet


#### Minimum Android Version:

- Mobile app: Android 8.0 (API level 26)


#### Project Launch:

// TODO: add a description how to launch the app and it's possible or now


### Basic Architecture

- Jetpack Compose
- Multi-module project
- MVI (Model-View-Intent) pattern
- Offline-first app


### Project Configuration

- Android Splash Screen API
- Type-safe navigation
- Internet check during whole app lifecycle
- Landscape/Portrait screen orientation


### Libraries and Frameworks

- Koin for dependency injection
- Room Database for offline storage
- Retrofit for the network calling
- Coil for loading and previewing images


### Features and Functionality

- Custom Result class to process Http requests and errors
- Kotlin Coroutines
- Kotlin Flows to transfer data
- Work with focusable states
