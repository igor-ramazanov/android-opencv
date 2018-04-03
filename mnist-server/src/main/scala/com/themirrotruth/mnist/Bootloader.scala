package com.themirrotruth.mnist

import java.nio.file.{Files, Paths}

import com.themirrortruth.mnist.mnist_server.MnistServerGrpc
import io.grpc.ServerBuilder
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.{NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

object Bootloader {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    if (!isModelExists) {
      retrainModel()
    }
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
    logger.info("Server is listening on 0.0.0.0:50051")
    server.awaitTermination()
  }

  def isModelExists: Boolean = {
    Files.exists(Paths.get("res"))
  }

  def retrainModel(): Unit = {
    Files.createDirectory(Paths.get("res"))
    logger.info("Model is not exists, retraining")
    val numRows = 28
    val numColumns = 28
    val outputNum = 10
    val batchSize = 128
    val rngSeed = 123
    val numEpochs = 15

    MnistDownloader.download()
    val mnistTrain = new MnistDataSetIterator(batchSize, true, rngSeed)

    val conf = new NeuralNetConfiguration.Builder()
      .seed(rngSeed)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .iterations(1)
      .learningRate(0.006)
      .updater(Updater.NESTEROVS)
      .regularization(true)
      .l2(1e-4)
      .list
      .layer(0,
             new DenseLayer.Builder()
               .nIn(numRows * numColumns)
               .nOut(1000)
               .activation(Activation.RELU)
               .weightInit(WeightInit.XAVIER)
               .build)
      .layer(
        1,
        new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
          .nIn(1000)
          .nOut(outputNum)
          .activation(Activation.SOFTMAX)
          .weightInit(WeightInit.XAVIER)
          .build
      )
      .pretrain(false)
      .backprop(true)
      .build

    val model = new MultiLayerNetwork(conf)
    model.init()

    model.setListeners(new ScoreIterationListener(1))

    (0 until numEpochs).foreach({ i =>
      println(s"Epoch - $i")
      model.fit(mnistTrain)
    })
    ModelSerializer.writeModel(model, "res/model", true)
  }
}
