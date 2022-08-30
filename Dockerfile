FROM inraep2m2/scala-sbt:latest
ADD . /collect-target-metabolomics-study
WORKDIR /collect-target-metabolomics-study
RUN sbt dist
RUN mkdir /webapp && cp target/universal/collect-target-metabolomics-study-1.0-SNAPSHOT.zip /webapp/
WORKDIR /webapp
RUN apt-get update && apt-get -y install unzip
RUN unzip collect-target-metabolomics-study-1.0-SNAPSHOT.zip
WORKDIR /webapp/collect-target-metabolomics-study-1.0-SNAPSHOT
RUN ls ./bin/collect-target-metabolomics-study
EXPOSE 80
CMD ["sh ./bin/collect-target-metabolomics-study -Dplay.http.secret.key='L0H7;H2pi7YyB`WCpCc<8k9oo5wyXK9;C7MOw;8G0ol6GXbuo7@fcPm]?getl:Uu' -Dplay.evolutions.db.metabolomics.autoApply=true -Dhttp.port=80"]