# Trusti

## Running the application in dev mode

```shell script
./mvnw compile quarkus:dev
```

## Schemas

- CSAF Schema: https://github.com/oasis-tcs/csaf/blob/master/csaf_2.0/json_schema/csaf_json_schema.json
- OSV Schema https://github.com/ossf/osv-schema/blob/main/validation/schema.json
- CVE
    - Schema: https://github.com/CVEProject/cve-schema/blob/master/schema/v5.0/CVE_JSON_5.0_schema.json
    - Source: https://github.com/CVEProject/cvelistV5
    - Schema: https://cveproject.github.io/cve-schema/schema/v5.0/docs/

## Deploy to Minikube

```shell
eval $(minikube docker-env)
mvn clean package \
-Dquarkus.container-image.build=true \
-Dquarkus.kubernetes.deploy=true \
-DskipTests
```

```shell
kubectl port-forward svc/trusti 8080:80

curl --location 'http://localhost:8080/sources' \
--header 'Accept: application/json' \
--header 'Content-Type: application/json' \
--data '{
    "type": "http",
    "url": "https://access.redhat.com/security/data/csaf/v2/advisories/"
}'

curl --location 'http://localhost:8080/tasks' \
--header 'Content-Type: application/json' \
--data '{
    "source": {
        "id": 1
    }
}'
```
