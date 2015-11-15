# Event-Serializability Checker

- Instruments an Android application to keep and log execution event traces. An event trace keeps the event id and name (e.g. onClick) of each field access in an application. 
- Using the event trace file, checks the event-serializability of an execution.

## Requirements

- Android system image instrumented for recording event traces
 
  (Link for the instrumented image will be provided soon)  
- Android SDK: http://developer.android.com/sdk/index.html
- Android SDK Tools
- Android SDK Platform-tools
- Android SDK Platform (we use Android 4.4.4 / API 19)
- Java (SE 7)
- Apache Ant (1.9.4)
- Python (2.7.5)
- Ruby


## Usage

Ensure the `ANDROID_HOME` environment variable points to a valid Android
platform SDK containing an Android platform library, e.g.,
`$ANDROID_HOME/platforms/android-19/android.jar`. Also, be sure you have
started the emulator.

Instrument your application `.apk` file to record its event trace. For instance, try the example app in `example/com.vlille.checker.apk`:

    ant -Dapk=example/com.vlille.checker.apk -Dandroid.api.version=19 -Dpackage.name=com.vlille.checker

(Note that the original `.apk` file will remain unmodified.)

Then, start an Android device and install the instrumented app into the device:

	adb install build/com.vlille.checker.apk
	
(Optional)You can read the event keeping logs:

    adb logcat ROB:I *:S
    
You can collect event traces of an application using the test scripts. The script runs a specified app for the specified number of times using a monkeyrunner test script. Then, it copies the generated trace files into an output folder. The following command runs the test script 1 of com.vlille.checker app for 5 times.

    cd bin
    ./runTests.sh com.vlille.checker 1 5
    
Then, you can check for cycles in the collected traces: 

	ruby conflicts.rb testOutputs/com.vlille.checker/<TRACE_NAME>.trc
