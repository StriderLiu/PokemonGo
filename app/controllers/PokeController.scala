package controllers

import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import javax.inject._

import models._
import services._

class PokeController @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {
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

  def predict = Action { implicit request =>
    addressForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.index(formWithErrors))
      },
      address => {
        Ok(views.html.result(Predictor.predict(address)))
      }
    )
  }

}