[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## RoboTransfer

### Steps for generating APK

1. Clone into a local repo.
2. If desired, import project into Android Studio

4. If you do not already have one, follow the steps [here](https://stackoverflow.com/questions/3997748/how-can-i-create-a-keystore) to generate a keystore.

5. Add a file named "keystore.properties" to your root project directory, and give it the following contents. The values should be based on the values you used to generate the keystore.
```
storePassword=<your_store_password>
keyPassword=<your_key_password>
keyAlias=<your_key_alias>
storeFile=<path_to_location_of_keystore>
```

6. Use Android Studio or gradlew to generate a signed APK with the flavor "xprize" or "local". This APK can be transferred to your local SystemBuild directory.



*private* see documentation [here](https://docs.google.com/document/d/1t3cLAXfo6T8Rw-G8SCvToC8Di2TR6O1z7mYXxCj71fc/edit#heading=h.2y6rwsquwh24)


## **Usage**

<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />The RoboTutor Global Learning Xprize Submission</span> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.
