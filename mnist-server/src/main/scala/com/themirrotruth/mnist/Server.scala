package com.themirrotruth.mnist

import com.themirrortruth.mnist.mnist_server.MnistServerGrpc.MnistServer
import com.themirrortruth.mnist.mnist_server.{Features, PredictedDigit}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.factory.Nd4j

import scala.concurrent.{ExecutionContext, Future}

class Server(network: MultiLayerNetwork) extends MnistServer {
  override def classify(request: Features): Future[PredictedDigit] = {
    Future {
      val array = request.pixels.map(n => {
        (0x00 << 24 | n.toByte & 0xff).toDouble / 255.0
      }).toArray
      val iNDArray = Nd4j.create(array)
      val results = network.predict(iNDArray)
      println("Predicted: " + results.mkString(", "))
      PredictedDigit(results.head)
    }(ExecutionContext.global)
  }
}
