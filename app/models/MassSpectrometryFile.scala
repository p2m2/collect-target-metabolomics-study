package models

case class MassSpectrometryFile(
                                 id : Long=0,
                                 name  : String,
                                 fileContent: String,
                                 className : String,
                               )
