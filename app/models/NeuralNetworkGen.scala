package models

import org.apache.spark.SparkContext
import org.apache.spark.ml.classification.{MultilayerPerceptronClassificationModel, MultilayerPerceptronClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * Created by vincentliu on 05/12/2016.
  */
object NeuralNetworkGen {

  private val spark = SparkSession.builder()
    .master("local")
    .appName("PokemonGo")
    .config("spark.some.config.option", "some-value")
    .getOrCreate()

  def getModel(sc: SparkContext, file: String): MultilayerPerceptronClassificationModel = {
    // If the model already exists, then retrieve the model from directory.
    // If the model does not exist, then train the data set and get a model.
    // Option is a very handy way to deal with this case.
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

  // Manually define predict function (It's trickier than it seems like!)
  // There is no official "predict" for neural network.
  def predict(sc: SparkContext, model: MultilayerPerceptronClassificationModel,
                      input: org.apache.spark.mllib.linalg.Vector): Double = {

    val point = LabeledPoint(1.0, Vectors.dense(input.toArray))
    val pointRDD = sc.parallelize(List(point))
    import spark.implicits._
    val inputFrame = pointRDD toDF

    model.transform(inputFrame).select("prediction").first().getDouble(0)
  }

  // Implement of NN in MLlib is quite different than other algorithms.
  // Vectors and LabeledPoint class comes from ml package (not mllib) and the input format must be Dataset[_].
  // We tried Dataframe but the Row type in it cannot recognize org.apache.spark.ml.linalg.Vectors.
  private def train(sc: SparkContext, file: String):MultilayerPerceptronClassificationModel = {
    // Data cleansing
    val data = sc.textFile(file)
      .map(_.split(","))
      .filter(line => line(0) != "latitude") // Get rid of the name row
      .map(_ map (_.toDouble)) // MLlib only recognize double type

    val parsedData = parseData(data, 41)

    // Split data into training (70%) and test (30%).
    val splits = parsedData.randomSplit(Array(0.7, 0.3), seed = 11L)
    val (training, test) = (splits(0), splits(1))

    // Transform training and test set into Dataframe
    import spark.implicits._
    val trainDS = training toDF
    val testDS = test toDF

    // NN
    // specify layers for the neural network:
    // input layer of size 41 (features), two intermediate of size 20
    // and output of size 3 (classes)
    val layers = Array[Int](41, 20, 3)
    // create the trainer and set its parameters
    val trainer = new MultilayerPerceptronClassifier()
      .setLayers(layers)
      .setBlockSize(128)
      .setSeed(1234L)
      .setMaxIter(100)

    // train the model
    val model = trainer.fit(trainDS)

    // compute accuracy on the test set
    val result = model.transform(testDS)
    val predictionAndLabels = result.select("prediction", "label")
    val evaluator = new MulticlassClassificationEvaluator()
      .setMetricName("accuracy")
    println("Accuracy: " + evaluator.evaluate(predictionAndLabels))

    // Save model
    model.save("resources/models/NeuralNetworkModel")

    model
  }

  def parseData(data: RDD[Array[Double]], colNums: Int): RDD[LabeledPoint] = for{
    vals <- data
  } yield LabeledPoint(vals(colNums), Vectors.dense(vals.slice(0, colNums)))
}
