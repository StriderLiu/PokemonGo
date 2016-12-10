import models.{Address, Coordinate}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{FlatSpec, Matchers}
import services.Predictor._



/**
  * Created by Shuxian on 12/8/16.
  */
class PredictorSpec extends FlatSpec with Matchers{

  behavior of "parsedData"
  // There are two kinds of vectors we used in project based on different models we used
  // Here we test on org.apache.spark.mllib.linalg.Vectors

  it should "work for other data" in {
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local")
        .setAppName("PokemonGo")
    )
    val dataGen=sc.textFile("resources/test.csv")
      .map(_.split(","))
      .filter(line => line(0) != "y_3")
      .map(_ map (_.toDouble))
    val data= parseData(dataGen,6)
    val firstLabel= data.first().label

    firstLabel should be (12.0)

  }

  behavior of "getCoordinate"

  it should "work for input like 5th avenue, new york, NY 10011, USA, decisionTree" in {
    val input = "5th avenue, new york, NY 10011, USA, decisionTree"
    val splits=input.split(", ")
    val address ={
      val street=splits(0)
      val city = splits(1).replace(" ","_")
      val state = splits(2).split(" ")(0)
      val zipCode = splits(2).split(" ")(1)
      val country = splits(3)
      val selectedAlgo = splits(4)
      Address(street,city,state,zipCode,country,selectedAlgo)
    }
    val coord=getCoordinate(address)

    coord should be (Coordinate(40.7356684,-73.99388230000001))

  }

  behavior of "getPopDensity"

  it should "work with Coordinate(40.7356684,-73.99388230000001)" in {
    val coord = Coordinate(40.7356684,-73.99388230000001)
    val pop = getPopDensity(coord)
    pop should be >=0.0
  }

  behavior of "getWeatherJson"

  it should "work with Coordinate(40.7356684,-73.99388230000001)" in {
    val coord = Coordinate(40.7356684,-73.99388230000001)
    val weatherJson=getWeatherJson(coord)
    val test=weatherJson.toString()
    val pattern = """(icon)""".r

    pattern.findAllIn(test).groupCount should be > 0
  }
}
