package controllers

import javax.inject._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.{AbstractController, ControllerComponents}
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext

class Application @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, cc: ControllerComponents)(
  implicit ec: ExecutionContext
) extends AbstractController(cc)
  with HasDatabaseConfigProvider[H2Profile] {


}