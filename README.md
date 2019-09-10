# Real Time Coin Detection

An Android application which detects coins (circular objects) in real time, using the phone's camera.

Using [OpenCV 2.4](https://opencv.org/) library for image processing.



## Installing

1. [Build](https://docs.opencv.org/2.4/doc/tutorials/introduction/android_binary_package/android_dev_intro.html) the Android project located in [Coin Detection](https://github.com/MTrajK/real-time-coin-detection/tree/master/Coin%20Detection) directory (using [Android Studio](https://developer.android.com/studio), [IntelliJ IDEA](https://www.jetbrains.com/idea/), [Eclipse](https://www.eclipse.org/), etc).

2. Install [OpenCV Manager](https://docs.opencv.org/2.4/platforms/android/service/doc/index.html) on your phone ([Google Play link](https://play.google.com/store/apps/details?id=org.opencv.engine)). 

3. Install the build (from the first step) on your phone.

## Description

The coin detection algorithm is composed of 5 steps:


### Step 1

Get the original frame from the camera.

![alt txt](https://raw.githubusercontent.com/MTrajK/real-time-coin-detection/master/images/step1_original-frame.png "Original frame")

### Step 2

Convert frame to grayscale image (less colors == better object detection).

![alt txt](https://raw.githubusercontent.com/MTrajK/real-time-coin-detection/master/images/step2_gray-frame.png "Grayscale image")

### Step 3

Apply a gaussian blur on the grayscale image (to reduce the noise).

![alt txt](https://raw.githubusercontent.com/MTrajK/real-time-coin-detection/master/images/step3_gaussian-blur.png "Gaussian blur")

### Step 4

The main step, edge detection.

![alt txt](https://raw.githubusercontent.com/MTrajK/real-time-coin-detection/master/images/step4_edge-detection.png "Edge detection")

### Step 5

Final step, mark coins.

![alt txt](https://raw.githubusercontent.com/MTrajK/real-time-coin-detection/master/images/step5_coins-marking.png "Final result")



## Results

A few results with different number of coins, different background and different lighting.

![alt txt](https://raw.githubusercontent.com/MTrajK/real-time-coin-detection/master/images/example1.png "Example 1")

![alt txt](https://raw.githubusercontent.com/MTrajK/real-time-coin-detection/master/images/example2.png "Example 2")

![alt txt](https://raw.githubusercontent.com/MTrajK/real-time-coin-detection/master/images/example3.png "Example 3")

![alt txt](https://raw.githubusercontent.com/MTrajK/real-time-coin-detection/master/images/example4.png "Example 4")

![alt txt](https://raw.githubusercontent.com/MTrajK/real-time-coin-detection/master/images/example5.png "Example 5")

![alt txt](https://raw.githubusercontent.com/MTrajK/real-time-coin-detection/master/images/example6.png "Example 6")

![alt txt](https://raw.githubusercontent.com/MTrajK/real-time-coin-detection/master/images/example7.png "Example 7")
