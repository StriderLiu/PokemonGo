package services

import org.apache.spark.SparkContext
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.classification.{MultilayerPerceptronClassificationModel, MultilayerPerceptronClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator


/**
  * Created by vincentliu on 05/12/2016.
  */
object neuralNetwork extends mlAlgorithms[MultilayerPerceptronClassificationModel]{

  def getModel(sc: SparkContext, file: String) = {
    // if the model already exists, then retrieve the model from directory
    // if the model does not exist, then train the data set and get a model
    val modelOption = Option(MultilayerPerceptronClassificationModel.load("target/tmp/NeuralNetworkModel"))

    modelOption match {
      case Some(model) => model
      case _ => train(sc, file)
    }
  }

  private def train(sc: SparkContext, file: String) = {
    // Data cleansing
    lazy val data = sc.textFile(file)
      .map(_.split(","))
      .filter(line => line(0) != "class")
      .map(_ map (_.toDouble))

    lazy val parsedData = for{
      vals <- data
    } yield LabeledPoint(vals(196), Vectors.dense(vals.slice(0, 196)))

    // Split data into training (70%) and test (30%).
    val splits = parsedData.randomSplit(Array(0.7, 0.3), seed = 11L)
    val (training, test) = (splits(0), splits(1)))

    // Transform training and test set into Dataframe
    val trainFrame = training toDF
    val testFrame = test toDF

    // NN
    // specify layers for the neural network:
    // input layer of size 196 (features), two intermediate of size 50 and 50
    // and output of size 15 (classes)
    val layers = Array[Int](196, 80, 15)
    // create the trainer and set its parameters
    val trainer = new MultilayerPerceptronClassifier()
      .setLayers(layers)
      .setBlockSize(128)
      .setSeed(1234L)
      .setMaxIter(100)

    // train the model
    val model = trainer.fit(trainFrame)

    // compute accuracy on the test set
    val result = model.transform(testFrame)
    val predictionAndLabels = result.select("prediction", "label")
    val evaluator = new MulticlassClassificationEvaluator()
      .setMetricName("accuracy")
    println("Accuracy: " + evaluator.evaluate(predictionAndLabels))

    // Save model
    model.save("target/tmp/NeuralNetworkModel")

    model
  }
}
