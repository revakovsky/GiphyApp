# Giphy App

Test task: an application to display GIF animations from the Giphy website

---

### Project overview

This application retrieves a list of GIF images from the [Giphy](https://developers.giphy.com/) web
API and displays them to the user. Users can click on any GIF to view it in full-screen mode on the
next screen

#### Splash Screen

When the application is launched, the user is greeted with a splash screen where the Internet
connection is checked

- If no connection is detected and the local database is empty (e.g., the application is being
  launched for the first time), a dedicated screen informs the user about the issue with the
  Internet
  connection and provides the option to navigate to the device settings to enable it

- If the Internet connection is unavailable but the database already contains previously saved and
  cached GIF images, the application opens a screen displaying the GIFs. At the same time, the user
  is
  notified about the lack of Internet connectivity with an option to quickly navigate from this
  screen
  directly to the device settings

### GIF Images Loading

#### Search Functionality

- If the local database already contains previously requested GIF images, the application displays
  images based on the most recent search query from the database

- If the user navigates to the next page of the current search query, the application fetches and
  displays images for the next page of the current query

- If the user enters a new search query, the application fetches images for the new query and
  displays
  them. All previously saved GIFs in the local database are cleared and replaced with the newly
  retrieved results

#### Search Requests

All user search queries (validated for specific characters) are processed by making requests to the
Giphy Search API. For example:
https://api.giphy.com/v1/gifs/search?api_key=YOUR_API_KEY&q=SEARCH_QUERY&limit=25&offset=OFFSET&rating=g

#### Individual GIF View

If a user selects a GIF from the list, it is displayed in full-screen mode on a new screen. To fetch
this high-quality and detailed GIF, the application makes a separate request to the following
endpoint:
https://api.giphy.com/v1/gifs/{GIF_ID}?api_key=YOUR_API_KEY&rating=g

This high-resolution GIF is not saved to the local database

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
