package models

import org.apache.spark.SparkContext
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel

/**
  * Created by vincentliu on 05/12/2016.
  */
class DecisionTree extends ModelGenerator[DecisionTreeModel]{

  def getModel(sc: SparkContext, file: String) = {
    // if the model already exists, then retrieve the model from directory
    // if the model does not exist, then train the data set and get a model
    val modelOption = Option(DecisionTreeModel.load(sc, "target/tmp/DecisionTreeModel"))

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

    // Normalization
    lazy val scaler = new StandardScaler().fit(parsedData map (_.features))
    lazy val normalizedData = parsedData.map(p => LabeledPoint(p.label, scaler.transform(p.features)))

    // Split data into training (70%) and test (30%).
    val splits = normalizedData.randomSplit(Array(0.7, 0.3), seed = 11L)
    val (training, test) = (splits(0), splits(1))

    // Train a DecisionTree model.
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val numClasses = 15
    val categoricalFeaturesInfo = Map[Int, Int]()
    val impurity = "gini"
    val maxDepth = 15
    val maxBins = 32

    lazy val model = DecisionTree.trainClassifier(training, numClasses, categoricalFeaturesInfo, impurity, maxDepth, maxBins)

    // Evaluate model on test instances and compute test error
    lazy val labelAndPreds = test.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }

    // Get evaluation metrics.
    lazy val metrics = new MulticlassMetrics(labelAndPreds)
    val accuracy = metrics.accuracy
    println(s"Accuracy = $accuracy")

    lazy val testErr = labelAndPreds.filter(r => r._1 != r._2).count().toDouble / test.count()
    println("Test Error = " + testErr)
    // println("Learned classification tree model:\n" + model.toDebugString)

    // Save model
    model.save(sc, "target/tmp/DecisionTreeModel")

    model
  }
}
