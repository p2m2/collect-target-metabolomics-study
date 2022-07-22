# --- !Ups

CREATE TABLE "MassSpectrometryFile" (
	`ID` BIGINT NOT NULL AUTO_INCREMENT,
	`NAME` VARCHAR NOT NULL,
	`FILECONTENT` VARCHAR NOT NULL,
	`CLASSNAME` VARCHAR NOT NULL,
	PRIMARY KEY (`ID`)
);

# --- !Downs

DROP TABLE "MassSpectrometryFile";
