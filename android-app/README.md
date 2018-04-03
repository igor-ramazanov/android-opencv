# Android app
Android application for recognition handwritten digit. Extracts a feature vector by OpenCV and sends it to [server](/mnist-server) by gRPC protocol and receives a predicted answer.

## Installation
1. Edit the [hostname variable](https://github.com/themirrortruth/android-opencv/blob/master/android-app/app/src/main/java/com/example/igorramazanov/opencvnumbers/MainActivity.java#L41) and point it to your [server](/mnist-server) 
2. Download the [OpenCV Android SDK](https://github.com/opencv/opencv/releases/download/3.4.1/opencv-3.4.1-android-sdk.zip)
3. Install appropriate to your CPU architecture .apk application from SDK: `/apk/<.apk files>`

## Building
```
./gradlew build
```

Output .apk file will be somewhere in the `/app/build/` directory.