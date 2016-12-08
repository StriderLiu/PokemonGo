package controllers

import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import scala.concurrent.{ ExecutionContext, Future }
import javax.inject._

import models._
import services._

class PokeController @Inject() (predictor: Predictor, val messagesApi: MessagesApi)
                     (implicit ec: ExecutionContext) extends Controller with I18nSupport {
  /**
    * The mapping for the address form.
    */
  val addressForm: Form[Address] = Form {
    mapping(
      "street" -> nonEmptyText,
      "city" -> nonEmptyText,
      "state"  -> nonEmptyText,
      "zipcode" -> nonEmptyText,
      "country" -> nonEmptyText
    )(Address.apply)(Address.unapply)
  }

  /**
    * The index action.
    */
  def index = Action {
    Ok(views.html.index(addressForm))
  }

  def add = Action.async {
    addressForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.index(errorForm)))
      },
      address => {
          Future(Ok(views.html.result(predictor.predict(address))))
      }
    )
  }

}