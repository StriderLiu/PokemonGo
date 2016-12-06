package models

import org.apache.spark.SparkContext

/**
  * Created by vincentliu on 05/12/2016.
  */
trait ModelGenerator[T] {
  def getModel(sc: SparkContext, file: String): T
}
