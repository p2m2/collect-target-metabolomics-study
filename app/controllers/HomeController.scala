package controllers

import daos.MassSpectrometryFileDAO
import fr.inrae.metabolomics.p2m2.format.{GenericP2M2, MassSpectrometryResultSet, MassSpectrometryResultSetFactory}
import fr.inrae.metabolomics.p2m2.parser._
import models.MassSpectrometryFile
import play.api.mvc._

import java.nio.file.Paths
import javax.inject._
import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
                                val msfiles : MassSpectrometryFileDAO,
                                val controllerComponents: ControllerComponents
                              ) (implicit ec : ExecutionContext) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def importMassSpectrometryFile() = Action {
      Ok(views.html.importMassSpectrometryFile())
  }

  def upload() = Action(parse.multipartFormData) {
    request => {
      /*
      println(request.body.asFormUrlEncoded)
      val formData = request.body.asMultipartFormData
      println(formData)
      Ok(views.html.importMassSpectrometryFile())*/
      request.body
        .file("massSpectrometryFile")
        .map { msfile =>
          // only get the last part of the filename
          // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
          val filename    = Paths.get(msfile.filename).getFileName
          val fileSize    = msfile.fileSize
          val contentType = msfile.contentType
          msfile.ref.copyTo(Paths.get(s"/tmp/$filename"), replace = true)
          val information = s"($filename,$fileSize,$contentType)" +
          "<br/>\\n \n nGCMSParser:"+GCMSParser.sniffFile(s"/tmp/$filename") +
          "<br/>OpenLabCDSParser:"+OpenLabCDSParser.sniffFile(s"/tmp/$filename") +
          "<br/>QuantifyCompoundSummaryReportMassLynxParser:"+QuantifyCompoundSummaryReportMassLynxParser.sniffFile(s"/tmp/$filename") +
          "<br/>XcaliburXlsParser:"+XcaliburXlsParser.sniffFile(s"/tmp/$filename")

          val ms : Option[MassSpectrometryResultSet] = ParserManager.buildMassSpectrometryObject(s"/tmp/$filename")
          ms.map {
            x => {
            val stringifyMs = MassSpectrometryResultSetFactory.stringify(x)
             msfiles.insert(MassSpectrometryFile(name=filename.toString,fileContent=stringifyMs,
               className = x.getClass.getSimpleName))
            }

          }

          Ok(views.html.importMassSpectrometryFile())
        }
        .getOrElse {
          println("================  ERREUR ==================")
          Redirect(routes.HomeController.index()).flashing("error" -> "Missing file")
        }
      }
    }

  def preview(): Action[AnyContent] = Action.async {
    msfiles.all().map {
      msfiles: Seq[MassSpectrometryFile] =>
        val obj: GenericP2M2 =
          msfiles
            .flatMap((msf: MassSpectrometryFile) => {
              MassSpectrometryResultSetFactory.build(msf.fileContent)
            })
            .foldLeft(GenericP2M2(Seq()))((accumulator, v) => accumulator + v)

        Ok(views.html.preview(obj))
    } recover {
      case e => Ok(e.getMessage)
    }
  }
  def displayTableWithMassSpectrometryFiles(): Action[AnyContent] = Action.async {
        msfiles.all().map {
          msfiles: Seq[MassSpectrometryFile] => Ok(views.html.msfiles(msfiles))
        } recover {
          case e => Ok(e.getMessage)
        }
      }


}
