FROM inraep2m2/scala-sbt:jdk_11.0.11_9

ARG SECRET

ADD . /collect-target-metabolomics-study
WORKDIR /collect-target-metabolomics-study
RUN sbt dist
RUN mkdir /webapp && cp target/universal/collect-target-metabolomics-study-1.0-SNAPSHOT.zip /webapp/
WORKDIR /webapp
RUN apt-get update && apt-get -y install unzip
RUN unzip collect-target-metabolomics-study-1.0-SNAPSHOT.zip
WORKDIR /webapp/collect-target-metabolomics-study-1.0-SNAPSHOT
RUN ls ./bin/collect-target-metabolomics-study

ENV SECRET_VALUE 'L0H7;H2pi7YyB`WCpCc<8k9oo5wyXK9;C7MOw;8G0ol6GXbuo7@fcPm]?getl:Uu'
EXPOSE 80

CMD ["./bin/collect-target-metabolomics-study","-Dplay.http.secret.key=$SECRET_VALUE","-Dplay.evolutions.db.metabolomics.autoApply=true","-Dhttp.port=80"]
