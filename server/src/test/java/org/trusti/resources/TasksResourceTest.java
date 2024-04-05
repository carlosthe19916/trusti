package org.trusti.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.trusti.dto.GitDetailsDto;
import org.trusti.dto.SourceDto;
import org.trusti.dto.TaskDto;
import org.trusti.models.SourceType;
import org.trusti.models.TaskState;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(OrderAnnotation.class)
@QuarkusTest
public class TasksResourceTest {

    static SourceDto sourceDto = new SourceDto(
            null,
            SourceType.git,
            "https://github.com/actions/checkout.git",
            new GitDetailsDto(
                    "1.0.0",
                    null
            )
    );

    static TaskDto taskDto;

    @Test
    @Order(1)
    public void createSource() {
        sourceDto = given()
                .contentType(ContentType.JSON)
                .when().body(sourceDto).post("/api/sources")
                .then()
                .statusCode(201)
                .body(
                        "id", is(notNullValue()),
                        "url", is(sourceDto.url())
                ).extract().body().as(SourceDto.class);
    }

    @Test
    @Order(2)
    public void createTask() {
        taskDto = new TaskDto(
                null,
                null,
                null,
                sourceDto,
                null,
                null,
                null,
                null,
                null,
                null
        );

        taskDto = given()
                .contentType(ContentType.JSON)
                .when().body(taskDto).post("/api/tasks")
                .then()
                .statusCode(201)
                .body(
                        "id", is(notNullValue()),
                        "name", startsWith("task-"),
                        "state", is(TaskState.Created.toString()),
                        "source.id", is(notNullValue())
                ).extract().body().as(TaskDto.class);

        await()
                .atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    TaskDto updatedTaskDto = given()
                            .contentType(ContentType.JSON)
                            .when().get("/api/tasks/" + taskDto.id())
                            .then()
                            .statusCode(200)
                            .extract().body().as(TaskDto.class);

                    assertEquals(TaskState.Ready, updatedTaskDto.state());
                });

    }

}