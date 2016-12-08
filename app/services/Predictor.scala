package services

import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.{SparkConf, SparkContext}
import play.api.libs.json.{JsValue, Json}
import scala.io.Source.fromURL
import java.util.Calendar
import java.util.Date
import scala.collection.immutable.HashMap
import scala.util.Random
import models._

/**
  * Created by vincentliu on 05/12/2016.
  */
object Predictor {

  def predict(address: Address): String = {
    val sc = new SparkContext(
      new SparkConf()
        .setMaster("local")
        .setAppName("PokemonGo")
    )

    // Get the model
    val model = DecisionTreeGen.getModel(sc, "/Users/vincentliu/Desktop/Courses_2016Fall/CSYE7200_Scala/Final Project/poke_43.csv")

    val input = collectInput(address)

    val prediction = model.predict(input).toInt

    prediction match {
      case 0 => "Bug"
      case 1 => "Dragon"
      case 2 => "Electric"
      case 3 => "Fairy"
      case 4 => "Fighting"
      case 5 => "Fire"
      case 6 => "Ghost"
      case 7 => "Grass"
      case 8 => "Ground"
      case 9 => "Ice"
      case 10 => "Normal"
      case 11 => "Poison"
      case 12 => "Psychic"
      case 13 => "Rock"
      case 14 => "Water"
    }

  }

  private def collectInput(address: Address): Vector = {
    val coord = getCoordianate(address.street + ", " + address.city + ", " +
      address.state + " " + address.zipcode  + ", " + address.country)

    val appearedHour = getCurTime.getHours
    val appearedMinute = getCurTime.getMinutes
    val appearedDayOfWeek = getCurTime.getDay
    val appearedDate = getCurTime.getDate
    val appearedTimeOfDay = appearedHour match {
      case x if x >= 0 && x < 6 => 4
      case x if x >= 6 && x < 12 => 3
      case x if x >=12 && x < 18 => 1
      case x if x >= 18 => 2
    }

    val terrainType = getTerrainType
    val closeToWater = isCloseToWater
    val city = getCity(address.city)

    val json = getWeatherJson(coord)
    val continent = getContinent(json)
    val weather = getWeatherType(json)
    val temperature = getTemperature(json)
    val windSpeed = getWindSpeed(json)
    val windBearing = getWindBearing(json)
    val pressure = getPressure(json)

    val sunriseHour = getSunriseHour(json)
    val sunriseMinute = getSunriseMinute(json)
    val sunriseMinutesMidnight = getSunriseMinutesMidnight(sunriseHour, sunriseMinute)
    val sunsetHour = getSunsetHour(json)
    val sunsetMinute = getSunsetMinute(json)
    val sunsetMinutesMidnight = getSunsetMinutesMidnight(sunsetHour, sunsetMinute)

    val popDensity = getPopDensity(coord)
    val urban = isUrban(popDensity)
    val suburban = isSubUrban(popDensity)
    val midurban = isMidUrban(popDensity)
    val rural = isRural(popDensity)

    val gymDistance = getGymDistance(urban, suburban, midurban, rural)
    val gymIn100m = hasGymIn100m(gymDistance)
    val gymIn250m = hasGymIn250m(gymDistance)
    val gymIn500m = hasGymIn500m(gymDistance)
    val gymIn1000m = hasGymIn1000m(gymDistance)
    val gymIn2500m = hasGymIn2500m(gymDistance)
    val gymIn5000m = hasGymIn5000m(gymDistance)

    val pokestopDistance = getPokestopDistance(urban, suburban, midurban, rural)
    val pokestopIn100m = hasPokestopIn100m(pokestopDistance)
    val pokestopIn250m = hasPokestopIn250m(pokestopDistance)
    val pokestopIn500m = hasPokestopIn500m(pokestopDistance)
    val pokestopIn1000m = hasPokestopIn1000m(pokestopDistance)
    val pokestopIn2500m = hasPokestopIn2500m(pokestopDistance)
    val pokestopIn5000m = hasPokestopIn5000m(pokestopDistance)

    val rarity = 1

    Vectors.dense(
      coord.lat, coord.lng,
      appearedTimeOfDay, appearedHour, appearedMinute, appearedDayOfWeek, appearedDate,
      terrainType, closeToWater, city, continent,
      weather, temperature, windSpeed, windBearing, pressure,
      sunriseMinutesMidnight, sunriseHour, sunriseMinute, sunsetMinutesMidnight, sunsetHour, sunsetMinute,
      popDensity, urban, suburban, midurban, rural,
      gymDistance, gymIn100m, gymIn250m, gymIn500m, gymIn1000m, gymIn2500m, gymIn5000m,
      pokestopDistance, pokestopIn100m, pokestopIn250m, pokestopIn500m, pokestopIn1000m, pokestopIn2500m, pokestopIn5000m,
      rarity
    )
  }

  private def getCoordianate(address: String): Coordinate = {
    val key = "AIzaSyDXxUKKAooWrPYxk09yudhZCKVw5zTWYlw"
    val url = s"https://maps.googleapis.com/maps/api/geocode/json?address=${address}&key=${key}"
    val coord = ((toJson(url) \ "results" )(0) \ "geometry" \ "location")
    Coordinate((coord \ "lat").as[Double], (coord \ "lng").as[Double])
  }

  private def getCurTime = Calendar.getInstance.getTime

  private def getTerrainType: Double = (new Random()).nextInt(17).toDouble

  private def isCloseToWater: Double = (new Random()).nextInt(2).toDouble

  private def getCity(cityName: String): Double = {
    val map = HashMap[String, Int](
      "Adelaide" -> 1, "Amman" -> 2, "Amsterdam" -> 3, "Athens" -> 4, "Auckland" -> 5, "Bahia" -> 6,
      "Bangkok" -> 7, "Belem" -> 8, "Berlin" -> 9, "Bogota" -> 10, "Boise" -> 11, "Bratislava" -> 12,
      "Brisbane" -> 13, "Brunei" -> 14, "Brussels" -> 15, "Bucharest" -> 16, "Buenos_Aires" ->17, "Cairo" -> 18,
      "Casablanca" -> 19, "Chicago" -> 20, "Copenhagen" -> 21, "Cordoba" -> 22, "Costa_Rica" -> 23, "Damascus" -> 24,
      "Denver" -> 25, "Detroit" -> 26, "Dubai" -> 27, "Dublin" -> 29, "Edmonton" -> 30, "Fortaleza" -> 31,
      "Guam" -> 32, "Guayaquil" -> 33, "Guyana" -> 34, "Halifax" -> 35, "Helsinki" -> 36, "Ho_Chi_Minh" -> 37,
      "Hobart" -> 38, "Hong_Kong" -> 39, "Honolulu" -> 40, "Indianapolis" -> 41, "Isle_of_Man" -> 42, "Istanbul" -> 43,
      "Jakarta" -> 44, "Jerusalem" -> 45, "Karachi" -> 46, "Kiev" -> 47, "Kolkata" -> 48, "Kuala_Lumpur" -> 49,
      "Kuching" -> 50, "Lisbon" -> 51, "Ljubljana" -> 52, "London" -> 53, "Los_Angeles" -> 54, "Louisville" -> 55,
      "Luanda" -> 56, "Luxembourg" -> 57, "Madrid" -> 58, "Manila" -> 59, "Melbourne" -> 60, "Mexico_City" -> 61,
      "Monrovia" -> 62, "Monterrey" -> 63, "Montreal" -> 64, "Moscow" -> 65, "Nairobi" -> 66, "New_York" -> 67,
      "Nicosia" -> 68, "Noumea" -> 69, "Oslo" -> 70, "Paris" -> 71, "Perth" -> 72, "Phnom_Penh" -> 73,
      "Phoenix" -> 74, "Prague" -> 75, "Puerto_Rico" -> 76, "Regina" -> 77, "Reunion" -> 78, "Reykjavik" -> 79,
      "Rome" -> 80, "Santiago" -> 81, "Sao_Paulo" -> 81, "Sarajevo" -> 82, "Singapore" -> 83, "Stockholm" -> 84,
      "Sydney" -> 85, "Tahiti" -> 86, "Taipei" -> 87, "Tokyo" -> 88, "Toronto" -> 89, "Tripoli" -> 90,
      "Tunis" -> 91, "Vancouver" -> 92, "Vienna" -> 93, "Vilnius" -> 94, "Warsaw" -> 95, "Winnipeg" -> 96,
      "Zagreb" -> 97, "Zurich" -> 98
    )
    map(cityName).toDouble
  }

  private def getWeatherJson(coord: Coordinate): JsValue = {
    val key = "230d97a0808f8c0bb2c722ea6e9ba251"
    val url = s" https://api.darksky.net/forecast/${key}/${coord.lat},${coord.lng}"
    toJson(url)
  }

  private def toJson(url: String): JsValue = Json.parse(fromURL(url).mkString)

  private def getContinent(jsValue: JsValue): Double = {
    val map = HashMap[String, Int](
      "Africa" -> 1, "America" -> 2, "America/Argentina" -> 3,
      "America/Indiana" -> 4, "America/Kentucky" -> 5, "Asia" -> 6,
      "Atlantic" -> 7, "Australia" -> 8, "Europe" -> 9,
      "Indian" -> 10, "Pacific" -> 11
    )
    map((jsValue \ "timezone").as[String].split("/")(0)).toDouble
  }

  private def getWeatherType(jsValue: JsValue): Double = {
    val map = HashMap[String, Int](
        "Breezy" -> 1, "BreezyandMostlyCloudy" -> 2, "BreezyandOvercast" -> 3, "BreezyandPartlyCloudy" -> 4,
        "Clear" -> 5, "DangerouslyWindy" -> 6, "Drizzle" -> 7, "DrizzleandBreezy" -> 8,
        "Dry" -> 9, "DryandMostlyCloudy" -> 10, "DryandPartlyCloudy" -> 11, "Foggy" -> 12,
        "HeavyRain" -> 13, "Humid" -> 14, "HumidandOvercast" -> 15, "HumidandPartlyCloudy" -> 16,
        "LightRain" ->17, "LightRainandBreezy" -> 18, "MostlyCloudy" -> 19, "Overcast" -> 20,
        "PartlyCloudy" -> 21, "Rain" -> 22, "RainandWindy" -> 23, "Windy" -> 24,
        "WindyandFoggy" -> 25, "WindyandPartlyCloudy" -> 26
    )
    map((jsValue \ "currently" \ "summary").as[String].replace(" ","")).toDouble
  }

  private def getPressure(jsValue: JsValue):Double = (jsValue \ "currently" \ "pressure").as[Double]
  private def getTemperature(jsValue: JsValue):Double = (jsValue \ "currently" \ "temperature").as[Double]
  private def getWindSpeed(jsValue: JsValue):Double = (jsValue \ "currently" \ "windSpeed").as[Double]
  private def getWindBearing(jsValue: JsValue):Double = (jsValue \ "currently" \ "windBearing").as[Double]

  private def getSunriseTime(jsValue: JsValue):Date = new Date(((jsValue \ "daily" \ "data" )(0) \ "sunriseTime").as[Long] * 1000)
  private def getSunsetTime(jsValue: JsValue):Date = new Date(((jsValue \ "daily" \ "data" )(0) \ "sunsetTime").as[Long] * 1000)
  private def getSunriseHour(jsValue: JsValue): Double = getSunriseTime(jsValue).getHours.toDouble
  private def getSunriseMinute(jsValue: JsValue): Double = getSunriseTime(jsValue).getMinutes.toDouble
  private def getSunsetHour(jsValue: JsValue): Double = getSunsetTime(jsValue).getHours.toDouble
  private def getSunsetMinute(jsValue: JsValue): Double = getSunsetTime(jsValue).getMinutes.toDouble
  private def getSunriseMinutesMidnight(sunriseHour: Double, sunriseMinute: Double): Double = sunriseHour *60 +sunriseMinute
//  private def getSunriseMinutesMidnight(jsValue: JsValue):Int = getSunriseTime(jsValue).getHours * 60 + getSunriseTime(jsValue).getMinutes
  private def getSunsetMinutesMidnight(sunsetHour: Double, sunsetMinute:Double): Double = sunsetHour * 60 + sunsetMinute
//  private def getSunsetMinutesMidnight(jsValue: JsValue): Int = getSunsetTime(jsValue).getHours * 60 + getSunsetTime(jsValue).getMinutes

  //  sources: http://www.datasciencetoolkit.org/developerdocs#coordinates2statistics
  private def getPopDensity(coord: Coordinate): Double = {
    val url = s"http://www.datasciencetoolkit.org/coordinates2statistics/${coord.lat}%2c${coord.lng}?statistics=population_density"
    val jsValue = toJson(url)(0)
    (jsValue \ "statistics" \ "population_density" \ "value").as[Double]
  }

  //  <200 for rural, >=200 and <400 for midUrban, >=400 and <800 for subUrban, >800 for urban
  private def isRural(density: Double): Double = if (density < 200) 1.0 else 0.0

  private def isMidUrban(density: Double): Double = density match {
    case x if x >= 200 && x <400 => 1.0
    case _ => 0.0
  }

  private def isSubUrban(density: Double): Double = density match {
    case x if x >= 400 && x < 800 => 1.0
    case _ => 0.0
  }

  private def isUrban(density: Double): Double = if (density > 800) 1.0 else 0.0

  // Get Gym Distance (KMs)
  private def getGymDistance(urban: Double, suburban: Double, midurban: Double, rural: Double): Double = {
    val rnd = new Random()
    // Generate random double between 0 to 6km
    if(urban != 0) 0.5 * rnd.nextDouble() // 0 to 0.5km
    else if(suburban != 0) 0.5 + rnd.nextDouble() // 0.5 to 1.5km
    else if(midurban != 0) 1.5 + 4.0 * rnd.nextDouble() // 1.5 to 5.5km
    else 5.5 + 1000.0 * rnd.nextDouble() // 4.5 to 1004.5km
  }

  private def hasGymIn100m(gymDistance: Double) = if (gymDistance <= 0.1) 1.0 else 0.0
  private def hasGymIn250m(gymDistance: Double) = if (gymDistance <= 0.25) 1.0 else 0.0
  private def hasGymIn500m(gymDistance: Double) = if (gymDistance <= 0.5) 1.0 else 0.0
  private def hasGymIn1000m(gymDistance: Double) = if (gymDistance <= 1.0) 1.0 else 0.0
  private def hasGymIn2500m(gymDistance: Double) = if (gymDistance <= 2.5) 1.0 else 0.0
  private def hasGymIn5000m(gymDistance: Double) = if (gymDistance <= 5.0) 1.0 else 0.0

  // Get Pokestop Distance (KMs)
  private def getPokestopDistance(urban: Double, suburban: Double, midurban: Double, rural: Double): Double = {
    val rnd = new Random()
    // Generate random double between 0 to 6km
    if(urban != 0) 0.3 * rnd.nextDouble() // 0 to 0.3km
    else if(suburban != 0) 0.3 + 0.3 * rnd.nextDouble() // 0.5 to 0.6km
    else if(midurban != 0) 0.6 + 0.4 * rnd.nextDouble() // 0.6 to 1km
    else 1.0 + 450.0 * rnd.nextDouble() // 4.5 to 454.5km
  }

  private def hasPokestopIn100m(gymDistance: Double): Double = if (gymDistance <= 0.1) 1.0 else 0.0
  private def hasPokestopIn250m(gymDistance: Double): Double = if (gymDistance <= 0.25) 1.0 else 0.0
  private def hasPokestopIn500m(gymDistance: Double): Double = if (gymDistance <= 0.5) 1.0 else 0.0
  private def hasPokestopIn1000m(gymDistance: Double): Double = if (gymDistance <= 1.0) 1.0 else 0.0
  private def hasPokestopIn2500m(gymDistance: Double): Double = if (gymDistance <= 2.5) 1.0 else 0.0
  private def hasPokestopIn5000m(gymDistance: Double): Double = if (gymDistance <= 5.0) 1.0 else 0.0
}
