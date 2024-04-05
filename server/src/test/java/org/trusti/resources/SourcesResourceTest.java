package org.trusti.resources;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.trusti.dto.GitDetailsDto;
import org.trusti.dto.SourceDto;
import org.trusti.models.SourceType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@TestMethodOrder(OrderAnnotation.class)
@QuarkusTest
@TestHTTPEndpoint(SourcesResource.class)
public class SourcesResourceTest {

    static SourceDto sourceDto = new SourceDto(
            null,
            SourceType.git,
            "https://github.com/actions/checkout.git",
            new GitDetailsDto(
                    "1.0.0",
                    null
            )
    );

    @Test
    @Order(1)
    public void createSource() {
        sourceDto = given()
                .contentType(ContentType.JSON)
                .when().body(sourceDto).post("/")
                .then()
                .statusCode(201)
                .body(
                        "id", is(notNullValue()),
                        "url", is(sourceDto.url())
                ).extract().body().as(SourceDto.class);
    }

    @Test
    @Order(2)
    public void createAndGetById() {
        given()
                .contentType(ContentType.JSON)
                .when().get("/" + sourceDto.id())
                .then()
                .body(
                        "url", is(sourceDto.url())
                );
    }

    @Test
    @Order(3)
    public void getSources() {
        given()
                .contentType(ContentType.JSON)
                .when().get("/")
                .then()
                .statusCode(200)
                .body(
                        "size()", is(1),
                        "[0].url", is(notNullValue())
                );
    }

}