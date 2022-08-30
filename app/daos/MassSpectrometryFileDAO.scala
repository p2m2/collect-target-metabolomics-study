package daos

import fr.inrae.metabolomics.p2m2.format.{GenericP2M2, MassSpectrometryResultSetFactory}

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject
import models.MassSpectrometryFile
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.H2Profile

class MassSpectrometryFileDAO @Inject()
    (@NamedDatabase("metabolomics")
      protected val dbConfigProvider: DatabaseConfigProvider)
    (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[H2Profile] {

  import profile.api._

  def findAll(): Seq[MassSpectrometryFile] = ???
  def findById(id: String) : MassSpectrometryFile = ???

  def all(): Future[Seq[MassSpectrometryFile]] = db.run(TableQuery[MassSpectrometryFileTable].result)

  def getMergeGenericP2M2() : Future[GenericP2M2] = {
    all().map {
      msfiles: Seq[MassSpectrometryFile] =>
        val obj: GenericP2M2 =
          msfiles
            .flatMap((msf: MassSpectrometryFile) => {
              MassSpectrometryResultSetFactory.build(msf.fileContent)
            }).foldLeft(GenericP2M2(Seq()))((accumulator, v) => accumulator + v)
        obj
    }
  }
  def insert(msfile: MassSpectrometryFile): Future[Unit] = {
    db.run(TableQuery[MassSpectrometryFileTable] += msfile).map { _ => () }
  }
  def update(msfile: MassSpectrometryFile): Future[Unit] = ???

  def delete(idMsFile : Long) : Future[Unit] = {
    val query = TableQuery[MassSpectrometryFileTable].filter( _.id === idMsFile)
    val action = query.delete
    db.run(action).map( _ => ())
  }

  def clean() : Future[Unit] = {
    db.run(TableQuery[MassSpectrometryFileTable].delete).map( _ => ())
  }
  private class MassSpectrometryFileTable(tag: Tag)
    extends Table[MassSpectrometryFile](tag, "MassSpectrometryFile") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def fileContent = column[String]("FILECONTENT")
    def className= column[String]("CLASSNAME")

    def * =
      (id, name, fileContent, className) <> (MassSpectrometryFile.tupled, MassSpectrometryFile.unapply)
  }
}