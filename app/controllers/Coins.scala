package controllers

import javax.inject.Inject

import models.db.CoinDAO
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

import scala.concurrent.ExecutionContext

class Coins @Inject()(cc: ControllerComponents, coinsDao: CoinDAO)(implicit executionContext: ExecutionContext) extends AbstractController(cc) {

  def findAll() = Action.async { implicit request: Request[AnyContent] =>
    coinsDao.findAll().map(seq => Ok(seq.mkString(",")))
  }
}
