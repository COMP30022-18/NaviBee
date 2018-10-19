# NaviBee
[![AUR](https://img.shields.io/aur/license/yaourt.svg)](https://github.com/COMP30022-18/NaviBee/blob/master/LICENSE)
![GitHub repo size in bytes](https://img.shields.io/github/repo-size/badges/shields.svg)

[description]

### Screenshot
[screenshot]

### Key features
[key features]

## Technical details

### Folder Structure
```js
/
├── Android/    // Android project root
│   ├── app/src/main/java/au/edu/unimelb/eng/navibee/
│   │           // Main code
│   └── app/src/test/java/au/edu/unimelb/eng/navibee/
│               // Unit test
├── Backend/
|               // Code for Firebase Cloud Function
└── Tool/
                // Code for migrating db during development
```

### Setup
NaviBee uses multiple online services and please follow the instruction below in order to build your own version.

#### Firebase
> [https://firebase.google.com/](https://firebase.google.com/)  

1. Register and create a new project.
2. Enable Google Sign-in provider in Authentication.
3. Upload code in '\Backend\' to Firebase Cloud Function following [this guide](https://firebase.google.com/docs/functions/get-started).
4. Open the NaviBee Android project using Android Studio and connect it to the Firebase project following [this guide](https://firebase.google.com/docs/android/setup).

#### Agora
> [https://www.agora.io/en/](https://www.agora.io/en/)

1. Register and add the AppId to gradle.properties:
```
agoraAppId=f******4
```
