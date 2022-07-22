package controllers

import daos.MassSpectrometryFileDAO
import fr.inrae.metabolomics.p2m2.format.{GenericP2M2, MassSpectrometryResultSet}
import fr.inrae.metabolomics.p2m2.parser._
import models.MassSpectrometryFile
import play.api.mvc._
import upickle.default._

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

  def importMassSpectrometryFile(information: String="") = Action {
      Ok(views.html.importMassSpectrometryFile(information))
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
          /**
           * ================ TODO =====================
           * Utiliser upickle pour serialiser les types GCMS,OpenLabCDS,...
           *
           * GCMS.HeaderFileField.HeaderFileField dit etre egalement seriaiser etc....
           * à intégrer dans p2mtools ?
           *
              val rw1: ReadWriter[GCMS] = macroRW
              val rw2: ReadWriter[OpenLabCDS] = macroRW
              val rw3: ReadWriter[QuantifyCompoundSummaryReportMassLynx] = macroRW
              val rw4: ReadWriter[Xcalibur] = macroRW
              val rw : ReadWriter[MassSpectrometryResultSet]  = ReadWriter.merge(rw1,rw2,rw3,rw4)

              msfiles.insert(MassSpectrometryFile(name=filename.toString,fileContent=write(x)(rw),
                className = x.getClass.getSimpleName))

           */
             println(x.getClass.getSimpleName)
             msfiles.insert(MassSpectrometryFile(name=filename.toString,fileContent=x.toString,
               className = x.getClass.getSimpleName))
            }

          }

          Ok(views.html.importMassSpectrometryFile(information))
        }
        .getOrElse {
          println("================  ERREUR ==================")
          Redirect(routes.HomeController.index()).flashing("error" -> "Missing file")
        }
      }
    }

  def preview() = Action.async {
    msfiles.all().map {
      case msfiles: Seq[MassSpectrometryFile] =>
        val obj : GenericP2M2 =
          msfiles
            .map( (msf : MassSpectrometryFile) => {
              /**
               * TODO Ne fonctionne pas
               *
               * Utiliser la serialisation upicle pour deserialiser un object provenant de la base et creer à nouveau l objet d origine (GCMS, etc...)
               */
              val clazz = Class.forName(msf.className)
              val instance = clazz.getDeclaredConstructor().newInstance()
              instance.asInstanceOf[GenericP2M2]
             // val rw: ReadWriter[instance.type] = macroRW
              //read[instance.type](msf.fileContent)(rw)
              //val stock = ois.readObject.asInstanceOf[Stock]
            })
            /** TODO On doit  la fin faire la conversion GenericP2M2 et faire l affichage du preview !!!! */
          .foldLeft(GenericP2M2(Seq()))( (accumulator,v) => accumulator +v)

        Ok(views.html.preview(obj.toString))
    } recover {
      case e => Ok(e.getMessage)
    }
  }
  def displayTableWithMassSpectrometryFiles() = Action.async {
        msfiles.all().map {
          case msfiles: Seq[MassSpectrometryFile] => Ok(views.html.msfiles(msfiles))
        } recover {
          case e => Ok(e.getMessage)
        }
      }


}
