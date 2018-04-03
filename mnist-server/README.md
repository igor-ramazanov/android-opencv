# Mnist Apache Spark server
Server on Apache Spark for receiving handwritten digits vector features from [Android application](../android-app) 
and responding with a predicted answer.

Uses gRPC as a communication protocol.

Automatically retrain the model if it does not exist.

## Dependencies
[Scala Built Tool (sbt)](https://www.scala-sbt.org)

## Building
Not working currently.

TODO: solve `.jar` file deduplication problem
```
sbt assembly
```

## Running
```
sbt run
```