package com.themirrotruth.mnist

import com.themirrortruth.mnist.mnist_server.MnistServerGrpc
import io.grpc.ServerBuilder
import org.deeplearning4j.util.ModelSerializer

import scala.concurrent.ExecutionContext

object Bootloader {
  def main(args: Array[String]): Unit = {
    val network = ModelSerializer.restoreMultiLayerNetwork("res/model")
    val server = ServerBuilder
      .forPort(50051)
      .addService(
        MnistServerGrpc
          .bindService(
            new Server(network),
            ExecutionContext.global
          )
      )
      .build()
    server.start()
    server.awaitTermination()
  }
}
