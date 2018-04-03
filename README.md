# Android Handwritten digit recognition
Android application for recognition of handwritten digits using [MNIST database](http://yann.lecun.com/exdb/mnist/).

Consists of 2 modules:
1) [Android application](/android-app) - android application with OpenCV, extracts features vector and sends it to the server application
2) [Server](/mnist-server) - Scala application with Apache Spark trained on MNIST database. Interacts with the android application by gRPC protocol.

Click on each module for more info.