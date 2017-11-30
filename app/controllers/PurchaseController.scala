package controllers

import javax.inject.{Inject, Singleton}

import models.db.{Purchase, PurchaseDAO}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class PurchaseController @Inject()(cc: ControllerComponents, purchaseDAO: PurchaseDAO)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def create: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val json = request.body.asJson.get
    val result = Json.fromJson[Purchase](json)

    result.fold(errors => {
      var errorMsg = ""
      errors.foreach(tuple => {
        errorMsg += "Error while parsing: " + tuple._1.toString() + "\nFailed Paths: "
        tuple._2.foreach(error => errorMsg += "\t" + error.message)
        errorMsg += "\n"
      })
      Future {
        BadRequest(errorMsg)
      }
    }, success => {
      purchaseDAO.insert(success).transform {
        case Success(_) => Success(Ok("Created"))
        case Failure(exception) => Success(BadRequest(exception.toString))
      }
    })
  }
}