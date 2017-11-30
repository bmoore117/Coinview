package controllers

import javax.inject.{Inject, Singleton}

import models.db.{Purchase, PurchaseDAO}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

@Singleton
class PurchaseController @Inject()(cc: ControllerComponents, purchaseDAO: PurchaseDAO) extends AbstractController(cc) {

  def create = Action { implicit request: Request[AnyContent] =>
    val json = request.body.asJson.get
    val result = Json.fromJson[Purchase](json)

    result.fold(errors => {
      var errorMsg = ""
      errors.foreach(tuple => {
        errorMsg += "Error while parsing: " + tuple._1.toString() + "\nFailed Paths: "
        tuple._2.foreach(error => errorMsg += "\t" + error.message)
        errorMsg += "\n"
      })
      BadRequest(errorMsg)
    }, success => {
      purchaseDAO.insert(success)
      Ok
    })
  }
}