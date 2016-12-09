import models.Coordinate
import org.scalatest.{FlatSpec, Matchers}
import services.Predictor._

/**
  * Created by Shuxian on 12/8/16.
  */
class PredictorSpec extends FlatSpec with Matchers{

  behavior of "getCoordianate"

  it should "work for incomplete address" in {
    val input = "5th avenue, new york"
    val address=for{
      s<-input.replace(" ","+")
    } yield s
    val coord=getCoordinate(address)

    coord should be (Coordinate(40.7314123,-73.99698479999999))

  }

  behavior of "getPopDensity"

  it should "work with Coordinate(40.7314123,-73.99698479999999)" in {
    val coord = Coordinate(40.7314123,-73.99698479999999)
    val pop = getPopDensity(coord)
    pop should be >=0.0
  }

  behavior of "getWeatherJson"

  it should "work with Coordinate(40.7314123,-73.99698479999999)" in {
    val coord = Coordinate(40.7314123,-73.99698479999999)
    val weatherJson=getWeatherJson(coord)
    val test=weatherJson.toString()
    val pattern = """(icon)""".r

    pattern.findAllIn(test).groupCount should be > 0
  }
}
