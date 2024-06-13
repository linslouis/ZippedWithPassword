# Password-Protected Zip File Creator

This Android application allows users to create password-protected zip files from a selected directory on their device. Users can select a directory using a directory picker and input a password, and the app will create a zip file in the parent directory of the selected directory.

## Features

- Select a directory to zip using a directory picker
- Input a password to protect the zip file
- Save the zip file in the parent directory of the selected directory
- Simple and easy-to-use interface

## Screenshots

*Include screenshots of your app here if possible.*

## Getting Started

### Prerequisites

- Android Studio 4.0 or higher
- Android device or emulator running Android 6.0 (API level 23) or higher

### Installation

1. Clone this repository to your local machine:
    ```bash
    git clone https://github.com/yourusername/PasswordProtectedZipFileCreator.git
    ```

2. Open the project in Android Studio.

3. Build and run the project on an Android device or emulator.

## Usage

1. **Open the App**: Launch the app on your Android device.

2. **Select Directory**: Click the "Pick Directory" button to open the directory picker and select the directory you want to zip.

3. **Enter Password**: Input the password you want to use to protect the zip file.

4. **Create Zip**: Click the "Create Zip" button to create the password-protected zip file. The zip file will be saved in the parent directory of the selected directory with the same name as the selected directory.

## Code Overview

### MainActivity.java

The `MainActivity` handles the user interface and the core functionality of selecting a directory, inputting a password, and creating the password-protected zip file.

#### Key Methods

- `openDirectoryPicker()`: Launches the directory picker intent.
- `checkPermissions()`: Checks if the necessary permissions are granted.
- `requestPermissions()`: Requests the necessary permissions if not already granted.
- `createZip()`: Creates the password-protected zip file.
- `createPasswordProtectedZip()`: Implements the logic for creating a password-protected zip file.
- `getPathFromUri()`: Converts the selected directory URI to a file path.

### Permissions

The app requires the following permissions:
- `READ_EXTERNAL_STORAGE`
- `WRITE_EXTERNAL_STORAGE`
- `MANAGE_EXTERNAL_STORAGE` (for Android 11 and above)

### Dependencies

The project uses the following dependency for handling zip files:
- [zip4j](https://github.com/srikanth-lingala/zip4j): A Java library for handling zip files.

Add the following dependency to your `build.gradle` file:

```groovy
dependencies {
    implementation 'net.lingala.zip4j:zip4j:2.9.1'
}
