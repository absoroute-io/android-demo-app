# Demo Android App for Vision-AI SDK

This is an example Android application to demonstrate how to integrate with and use [Absoroute.io](https://www.absoroute.io/)'s Vision-AI SDK for face recognition.

## Introduction

The Vision-AI SDK (referred to as the "SDK" from now on) provides face detection and face recognition capabilities to the Android application, using only local device's computing resources without needing to Internet connectivity. Internally it hosts machine learning models trained and optimized for usage on Android devices, sacrificing a bit of accuracy for speed and reduced size of the SDK.

The SDK contains a machine-learning model for detecting the presense of faces within an image or video. To recognize a face once one is found, another machine-learning model optimized to run on device is used to analyze and produce an *embedding* of that face. An *embedding* is a mathematical representation of a face as a series of numbers. This *embedding* is compared against previously registered *embedding*'s of other people for the most similar one. If the most similar person is found and the similarity is within acceptable threshold, the detected face is considered recognized.

This version of the SDK does not share any information of registered faces and people to the Internet. Thus it can operate on a stand-alone device and minimize privacy concerns for end users. However that also means synchronizing a set of registered people across multiple devices is impossible at the moment. This will become available in the later version of the SDK. 

### What's it for?
* Face detection in still images, or continuous video captured from device's camera.
* Face recognition in still images, or continuous video captured from device's camera.
* Non-critical application, such as recognizing people within a household for a smart-home device to interact with. E.g., greeting, playing music or offering product recommendations to their preferences.
* Augment CCTV introder alert capability to only alert user when detecting a face, or an unrecognized face only. 
* Recognizing faces of no more than 10-15 people. 

### What's it NOT meant for?
* Mission-critical applications such as identity verification for financial transaction, or access control.

## Integration with your app
* Contact Absoroute.io to obtain the SDK (the AAR file), a license file and a `google-services.json` file.
* Put the `google-services.json` file at the root folder of the app module. 
* Add the Vision-AI SDK to your project as a dependency. In Android Studio, do this:
  * Click **File -> New -> New Module**.
  * Click **Import .JAR/.AAR Package** then click **Next**.
  * Enter the location of the compiled AAR or JAR file then click **Finish**.
* In the app module's `build.gradle` file, add these lines
```
android {
  ...
  aaptOptions {
    noCompress "tflite"
    noCompress "lite"
  }
	compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
  ...
}

dependencies {
  ...
  implementation project(":visionai")
  implementation "androidx.room:room-runtime:2.2.1"
  implementation 'org.tensorflow:tensorflow-lite:0.0.0-nightly'
  implementation 'org.tensorflow:tensorflow-lite-gpu:0.0.0-nightly'
  implementation 'org.tensorflow:tensorflow-lite-support:0.0.0-nightly'
  implementation 'com.google.firebase:firebase-ml-vision:24.0.0'
  def camerax_version = "1.0.0-alpha05"
  implementation "androidx.camera:camera-core:${camerax_version}"
  implementation "androidx.camera:camera-camera2:${camerax_version}"
  implementation 'com.google.firebase:firebase-analytics:17.2.0'
  ...
}
...  
apply plugin: 'com.google.gms.google-services'
```
* In `settings.gradle` file, ensure this line exists:
```
include ':visionai'
```
* In the project's `build.gradle` file, add these lines:
```
buildscript {
  ...
  dependencies {
    ...
    classpath 'com.google.gms:google-services:4.3.2'
    ...
  }
  ...
}
```

## Face detection API
TODO - Add documentation

## Face recognition API

The class `com.absoroute.io.visionai.FaceRecognizer` provides the main APIs to interact with the SDK for face recognition feature. This class is a Singleton with static methods to initialise, register faces, request image / video processing and stop it from anywhere within your application. You can initialize it to support 2 main use cases, namely an **image processing** and **video processing** use cases. The **image processing** use case allows you to submit an image to the SDK one image at a time to see if there exists registered faces within or not. The **video processing** use case allows you to have the SDK monitoring video stream captured from the device's camera in the background continuously. Once the SDK detects and recognizes faces in the video, it will notify the main UI thread via a registered callback function. You do not need to setup and manage complicated threading mechanics to operate the camera. The SDK does that for you behind the scene. 

### Initialisation
Before using the `FaceRecognizer`, you must initialize it first by calling `FaceRecognizer.initialize()` and providing an `FaceRecognizer.FaceRecognizerConfig` object to it somewhere in your application. For example, you can check if it's been initialized and ready for use in the `onCreate()` of the Activity that needs to interact with it. 

```java
// Initialize FaceRecognizer here, so that it'll be ready to use for face registration
if (!FaceRecognizer.isInitialized()) {
    FaceRecognizer.FaceRecognizerConfig config = new FaceRecognizer.FaceRecognizerConfig();
    config.context = getApplicationContext();
    FaceRecognizer.initialize(config);
}
```

The above initialization is adequate for **image processing** use case (see [FrConfigActivity](https://github.com/absoroute-io/android-demo-app/blob/master/app/src/main/java/com/absoroute/io/demoapp/FrConfigActivity.java)), and for registration of faces. However if you'd like to use it for **video processing** use case, check if it's been properly initialized for video processing and (re)-initialize if necessary in the Activity's `onCreate()` similar to the following (see [FrDemoActivity](https://github.com/absoroute-io/android-demo-app/blob/master/app/src/main/java/com/absoroute/io/demoapp/FrDemoActivity.java)).

```java
// (Re)initialize FaceRecognizer for video processing
if (!FaceRecognizer.isInitializedForVideoProcessing()) {
    FaceRecognizer.FaceRecognizerConfig config = new FaceRecognizer.FaceRecognizerConfig();
    config.context = getApplicationContext();
    config.lensFacing = CameraX.LensFacing.FRONT;
    config.lifecycleOwner = this;
    config.callback = faces -> {
        // Do whatever you want with the returned faces
    };
    FaceRecognizer.initialize(config);
}
```

Note the additional parameters to pass on as follows 
* `lensFaceing` - allows you to set whether you want to use device's front or back camera.
* `lifecycleOwner` - a reference to a `LifecycleOwner`. Typically this is the Activity or anything that implements the Android's `LifecycleOwner` interface. The camera operation will be bound to the life cycle of this object.
* `callback` - an implementation of the `FaceRecognizer.FaceRecognizerCallback` interface, whose `onRecognizeFaces()` will be called when a face is detected and recognized in the video. 

### Face registration

There are APIs to perform CRUD operations on registered faces as follows.

* ```public synchronized Task<String> registerFace(final List<Bitmap> images)``` takes a list of Bitmap images, each containing one and only one face of the person you want to register. The images must be correctly rotated to align with the current display orientation. The registration can take several seconds. If you'd like to be notified on the result, you can add listeners to the returned task. If the registration is successful, a unique face ID is given and returned with the task. There is no requirement on minimum number of images you have to use to register a person. However we recommend using at least 5 images, each depicting the face of the same person from slightly different angles with no digital beauty filter applied. Try to use as natural images of the person as much as possible so face recognition is more accurate under real-life operating condition.

* ```public synchronized List<String> getRegisteredFaceIds()``` returns a list of all registered face IDs.

* ```public synchronized void deleteRegisteredFace(final String id)``` deletes the registered face with the given face ID. 

### Handling of callback

### Stopping the FaceRecognizer
