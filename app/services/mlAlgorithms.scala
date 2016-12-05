package services

import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by vincentliu on 05/12/2016.
  */
trait mlAlgorithms[T] {
  def getModel(sc: SparkContext = new SparkContext(
    new SparkConf().
      setMaster("local").
      setAppName("PokemonGo")
  ), file: String): T
}
