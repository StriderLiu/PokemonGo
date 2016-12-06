package services

import scala.concurrent.{ExecutionContext, Future}
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.{SparkConf, SparkContext}
import javax.inject.{Inject, Singleton}

import models._
/**
  * Created by vincentliu on 05/12/2016.
  */
@Singleton
class Predictor @Inject()(modelGenerator: ModelGenerator[DecisionTreeModel])
                         (implicit ec: ExecutionContext) {

  def predict(address: Address): Future[String] = {
    val sc = new SparkContext(
      new SparkConf()
        .setMaster("local")
        .setAppName("PokemonGo")
    )
    // Get the model
    val model = modelGenerator.getModel(sc, "/Users/vincentliu/Desktop/Courses_2016Fall/CSYE7200_Scala/Final Project/poke_46.csv")

    // Geocoding
    // Get all the other data
    // Combine these data into a LabeledPoint
    // Pass the point as argument to model.predict()
    // return the prediction result (type)

  }
}
