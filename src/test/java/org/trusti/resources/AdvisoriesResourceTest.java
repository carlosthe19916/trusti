package org.trusti.resources;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.InputStream;
import java.net.URISyntaxException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@TestHTTPEndpoint(AdvisoriesResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class AdvisoriesResourceTest {

    @Test
    @Order(1)
    public void importCSAF() throws URISyntaxException {
        InputStream is = AdvisoriesResourceTest.class.getClassLoader().getResourceAsStream("csaf/cve-2023-33201.json");

        given()
                .contentType(ContentType.JSON)
                .when().body(is).post("/csaf")
                .then()
                .statusCode(201)
                .body(
                        "identifier", is("CVE-2023-33201"),
                        "title", is("potential  blind LDAP injection attack using a self-signed certificate"),
                        "severity", is("moderate")
                );
    }
}