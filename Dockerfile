FROM inraep2m2/scala-sbt:latest
ADD . /webapp
WORKDIR /webapp
RUN sbt compile
CMD ["sbt run"]