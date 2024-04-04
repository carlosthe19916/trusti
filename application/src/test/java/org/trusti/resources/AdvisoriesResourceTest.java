package org.trusti.resources;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

@TestHTTPEndpoint(AdvisoriesResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class AdvisoriesResourceTest {

    @Test
    public void importCSAF() {
        InputStream is = AdvisoriesResourceTest.class.getClassLoader().getResourceAsStream("csaf/cve-2023-33201.json");

        given()
                .contentType(ContentType.JSON)
                .when().body(is).post("/")
                .then()
                .statusCode(201)
                .body(
                        "identifier", is("CVE-2023-33201"),
                        "title", is("potential  blind LDAP injection attack using a self-signed certificate"),
                        "severity", is("moderate")
                );
    }

    @Test
    public void importOSV1() {
        InputStream is = AdvisoriesResourceTest.class.getClassLoader().getResourceAsStream("osv/RUSTSEC-2021-0079.json");
        given()
                .contentType(ContentType.JSON)
                .when().body(is).post("/")
                .then()
                .statusCode(201)
                .body(
                        "identifier", is("RUSTSEC-2021-0079"),
                        "title", is("Integer overflow in `hyper`'s parsing of the `Transfer-Encoding` header leads to data loss"),
                        "severity", is("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:H/A:H")
                );
    }

    @Test
    public void importOSV2() {
        InputStream is = AdvisoriesResourceTest.class.getClassLoader().getResourceAsStream("osv/RUSTSEC-2022-0022.json");
        given()
                .contentType(ContentType.JSON)
                .when().body(is).post("/")
                .then()
                .statusCode(201)
                .body(
                        "identifier", is("RUSTSEC-2022-0022"),
                        "title", is("Parser creates invalid uninitialized value"),
                        "severity", is(nullValue())
                );
    }
}