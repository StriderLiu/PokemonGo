package models

//import javax.inject.Singleton
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

/**
  * Created by vincentliu on 05/12/2016.
  */
object LogisticRegressionGen {

  def getModel(sc: SparkContext, file: String): LogisticRegressionModel = {
    // if the model already exists, then retrieve the model from directory
    // if the model does not exist, then train the data set and get a model
    val modelOption = Option(LogisticRegressionModel.load(sc, "target/tmp/LogisticRegressionModel"))

    modelOption match {
      case Some(model) => model
      case _ => train(sc, file)
    }
  }

  private def train(sc: SparkContext, file: String): LogisticRegressionModel = {
    // Data cleansing
    lazy val data = sc.textFile(file)
      .map(_.split(","))
      .filter(line => line(0) != "latitude")
      .map(_ map (_.toDouble))

    val parsedData = for{
      vals <- data
    } yield LabeledPoint(vals(41), Vectors.dense(vals.slice(0, 41)))

    // Normalization
    lazy val scaler = new StandardScaler().fit(parsedData map (_.features))
    val normalizedData = parsedData.map(p => LabeledPoint(p.label, scaler.transform(p.features)))

    // Split data into training (70%) and test (30%).
    val splits = normalizedData.randomSplit(Array(0.7, 0.3), seed = 11L)
    val (training, test) = (splits(0), splits(1))

    // Logistic Regression
    // Run training algorithm to build the model
    lazy val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(3)
      .run(training)

    // Compute raw scores on the test set.
    lazy val predictionAndLabels = test.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    // Get evaluation metrics.
    val metrics = new MulticlassMetrics(predictionAndLabels)
    val accuracy = metrics.accuracy
    println(s"Accuracy = $accuracy")

    // Save model
    model.save(sc, "target/tmp/LogisticRegressionModel")

    model
  }
}
