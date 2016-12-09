package models

//import javax.inject.Singleton
import java.util

import org.apache.spark.SparkContext
import org.apache.spark.ml.classification.{MultilayerPerceptronClassificationModel, MultilayerPerceptronClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.rdd.RDD

/**
  * Created by vincentliu on 05/12/2016.
  */
object NeuralNetworkGen {

  private val session = SparkSession.builder()
    .master("local")
    .appName("PokemonGo")
    .config("spark.some.config.option", "some-value")
    .getOrCreate()

  private val schema = StructType(
    List(StructField("label", DoubleType),
      StructField("features", ArrayType(DoubleType, false))))


  def getModel(sc: SparkContext, file: String): MultilayerPerceptronClassificationModel = {
    // if the model already exists, then retrieve the model from directory
    // if the model does not exist, then train the data set and get a model
    val modelOption: Option[MultilayerPerceptronClassificationModel] = {
      try {
        Some(MultilayerPerceptronClassificationModel.load("resources/models/NeuralNetworkModel"))
      } catch {
        case ex: Exception => None
      }
    }

    modelOption match {
      case Some(model) => model
      case None => train(sc, file)
    }
  }

  def predict(sc: SparkContext, model: MultilayerPerceptronClassificationModel,
                      input: org.apache.spark.mllib.linalg.Vector): Double = {
    val rdd = sc.parallelize(List[Double](input.toArray))
    val frame: DataFrame = session.createDataFrame(rowList, schema) // RDD[Row]

    model.transform(frame).select("prediction").first().getInt(0)
  }

  private def train(sc: SparkContext, file: String):MultilayerPerceptronClassificationModel = {
    // Data cleansing
    val data = sc.textFile(file)
      .map(_.split(","))
      .filter(line => line(0) != "latitude")
      .map(_ map (_.toDouble))

    val parsedData = for{
      vals <- data
    } yield LabeledPoint(vals(41), Vectors.dense(vals.slice(0, 41)))

    // Split data into training (70%) and test (30%).
    val splits = parsedData.randomSplit(Array(0.7, 0.3), seed = 11L)
    val (training, test) = (splits(0), splits(1))

    // Transform training and test set into Dataframe
    val trainFrame = session.createDataFrame(training.map(p => Row(p.label, p.features)), schema)
    val testFrame = session.createDataFrame(test.map(p => Row(p.label, p.features)), schema)

    // NN
    // specify layers for the neural network:
    // input layer of size 196 (features), two intermediate of size 50 and 50
    // and output of size 15 (classes)
    val layers = Array[Int](41, 20, 3)
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
    model.save("resources/models/NeuralNetworkModel")

    model
  }
}
