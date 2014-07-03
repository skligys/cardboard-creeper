cardboard-creeper
=================

A simple clone of Minecraft to run on Android with Google Cardboard.

This was inspired by a nice Minecraft-like demo written in Pyglet:

https://github.com/fogleman/Minecraft

I thought it would be nice to make it run with Google Cardboard.  I rewrote everything in Java and
OpenGL ES 2.0 to make that happen.

## Building the project
### In Android Studio
Create a new project from CardboardCreeper directory, build and run on your Android device.

### From the command line on Windows
Go to the CardboardCreeper directory on the command line.  Run this command:

```
gradlew.bat assembleDebug
```

The `assembleDebug` build task builds the debug version of the app and signs it with the default
local certificate, so that you can install it on your device for debugging.

After you successfully build the project, the output APK for the app is located in `app/build/apk/`.

### From the command line on Mac OS or Linux
Go to the CardboardCreeper directory in your terminal.  The first time only, run this command:

```
chmod +x gradlew
```

It adds the execution permission to the Gradle wrapper script.  Then run:

```
./gradlew assembleDebug
```

After you successfully build the project, the output APK for the app is located in `app/build/apk/`.

## Running the demo
Launch the app, you will see a Minecraft-like landscape.  Swiping left/right/up/down moves your
viewpoint.  "Volume up" button walks forward.
