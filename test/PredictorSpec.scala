import models.Coordinate
import org.scalatest.{FlatSpec, Matchers}
import services.Predictor._

import scala.util._
import scala.math._

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

//    coord should matchPattern {
//      case Success(Coordinate(_,_)) =>
//    }
    coord should be (Coordinate(40.7314123,-73.99698479999999))

  }


}
