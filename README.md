# collect-target-metabolomics-study

```shell
sbt run 
``` 

http://localhost:9000/

## notes

h2 database test

```shell
sbt
h2-browser
```

# Docker

- docker run -p 9000:80 -t test

## utilisation Slick/Evolution/H2
- Ajout d'un DAO : MassSpectrometryFile
- Ajout d unc conf pour acceder à H2
- nouvelle vue pour afficher les fichier uploadé