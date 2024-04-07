FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build
COPY --chown=quarkus:quarkus mvnw /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn
COPY --chown=quarkus:quarkus pom.xml /code/
COPY --chown=quarkus:quarkus server/pom.xml /code/server/
COPY --chown=quarkus:quarkus importer/pom.xml /code/importer/
COPY --chown=quarkus:quarkus importer-cli/pom.xml /code/importer-cli/
COPY --chown=quarkus:quarkus ui/pom.xml /code/ui/
USER quarkus
WORKDIR /code
RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline
COPY server/src/main /code/server/src/main
COPY importer/src/main /code/importer/src/main
COPY importer-cli/src/main /code/importer-cli/src/main
COPY ui/src/main /code/ui/src/main
RUN ./mvnw install -DskipTests && ./mvnw package -Dnative -DskipTests -pl importer

FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /work/
COPY --from=build /code/importer/target/*-runner /work/application

# set up permissions for user `1001`
RUN chmod 775 /work /work/application \
  && chown -R 1001 /work \
  && chmod -R "g+rwX" /work \
  && chown -R 1001:root /work

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application"]
CMD ["--help"]