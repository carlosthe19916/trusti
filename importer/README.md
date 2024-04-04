# code-with-quarkus

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev -Dquarkus.args="git --target-url=http://localhost:8080/advisories --working-directory=src/test/resources https://github.com/carlosthe19916/trusti.git"
```

```shell
./mvnw compile quarkus:dev -Dquarkus.args="http --target-url=http://localhost:8080/advisories https://access.redhat.com/security/data/csaf/v2/advisories/"
```
