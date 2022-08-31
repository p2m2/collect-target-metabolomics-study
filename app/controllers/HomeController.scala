package controllers

import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import daos.MassSpectrometryFileDAO
import fr.inrae.metabolomics.p2m2.format.{GenericP2M2, MassSpectrometryResultSet, MassSpectrometryResultSetFactory}
import fr.inrae.metabolomics.p2m2.parser._
import fr.inrae.metabolomics.p2m2.stream.ExportData
import models.MassSpectrometryFile
import play.api.http.HttpEntity
import play.api.mvc._

import java.io.ByteArrayInputStream
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

  def importMassSpectrometryFile() = Action.async {
    msfiles.all().map {
      msFiles: Seq[MassSpectrometryFile] => Ok(views.html.importMassSpectrometryFile(msFiles))
    } recover {
      case e => Ok(e.getMessage)
    }
  }

  def upload() = Action(parse.multipartFormData).async {
    request => {

      /*Ok(views.html.importMassSpectrometryFile())*/
      request
        .body
        .files
        .map { msfile =>
          println(msfile)
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
        }
        msfiles.all().map {
          msFiles: Seq[MassSpectrometryFile] => Ok(views.html.importMassSpectrometryFile(msFiles))
        } recover {
          case e => Ok(e.getMessage)
        }
      }
    }
  def delete(idMsFile : Long): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] => {
      msfiles.delete(idMsFile)
      msfiles.all().map {
        msFiles: Seq[MassSpectrometryFile] => Ok(views.html.importMassSpectrometryFile(msFiles))
      } recover {
        case e => Ok(e.getMessage)
      }
    }
  }

  def clean() : Action[AnyContent] = Action.async {
    msfiles.clean()
    msfiles.all().map {
      msFiles: Seq[MassSpectrometryFile] => Ok(views.html.importMassSpectrometryFile(msFiles))
    } recover {
      case e => Ok(e.getMessage)
    }
  }

  def exportXLS() : Action[AnyContent] = Action.async {
    msfiles.getMergeGenericP2M2().map {
      (obj: GenericP2M2) => {
        val bs = ExportData.xlsP2M2(obj)
        val source: Source[ByteString, _] = StreamConverters.fromInputStream(() => new ByteArrayInputStream(bs.toByteArray))
        val contentLength : Option[Long] = Some(bs.size())
        Result(
          header = ResponseHeader(200, Map.empty),
          body = HttpEntity.Streamed(source, contentLength,Some("application/vnd.ms-excel"))
        )
      }
    } recover {
      case e => Ok(e.getMessage)
    }
  }

  def preview(): Action[AnyContent] = Action.async {
    msfiles.getMergeGenericP2M2().map {
      (obj : GenericP2M2) => Ok(views.html.preview(obj))
    } recover {
      case e => Ok(e.getMessage)
    }
  }

}
