import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
// For ramdom order tests
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.Random;

// Note all the tests in this class will be failing due to bugs/risks found in the application under the task.
@TestMethodOrder(Random.class)
public class BugFound {

    private int projectId;
    private int todoId;

    private final String TODO_TITLE = "Test Todo";
    private final String TODO_DESCRIPTION = "Description of the test todo";

    private final String UPDATED_TITLE = "New title";
    private final String UPDATED_DESCRIPTION = "New description";

    private boolean todoDeleted = false;

    @BeforeAll
    public static void initialSetup() {
        RestAssured.baseURI = "http://localhost:4567";
    }

    public void createProject() {
        // Create a project before each test
        Map<String, String> projectData = new HashMap<>();
        projectData.put("title", "Test Project");
        projectData.put("description", "Description of the test project");

        Response response = given()
                .contentType("application/json")
                .body(projectData)
                .when()
                .post("/projects");
        assertEquals(201, response.getStatusCode());

        // Extract the project ID for further operations
        projectId = response.jsonPath().getInt("id");
        System.out.println("Set Up project with ID projectId");
    }


    @BeforeEach
    public void createTodo() {
        // Create a todo before each test
        Map<String, String> todoData = new HashMap<>();
        todoData.put("title", TODO_TITLE);
        todoData.put("description", TODO_DESCRIPTION);

        Response response = given()
                .contentType("application/json")
                .body(todoData)
                .when()
                .post("/todos");
        assertEquals(201, response.getStatusCode());

        // Extract the project ID for further operations
        todoId = response.jsonPath().getInt("id");
        System.out.println("Set Up todo with ID " + todoId);
    }

    @Test
    void testUpdateProjectWithStringValues() {
        createProject();
        Map<String, String> projectData = new HashMap<>();
        projectData.put("completed", "true");
        projectData.put("active", "true");

        Response updateResponse = given()
                .pathParam("id", projectId)
                .contentType("application/json")
                .body(projectData)
                .when()
                .post("/projects/{id}");

        if (updateResponse.getStatusCode() == 400) {
            String errorMessage = updateResponse.getBody().asString();
            System.out.println("Error from following the doc with 'completed' and 'active' set as string values: " + errorMessage);
        }
        assertEquals(200, updateResponse.getStatusCode());
    }

    @Test
    void testServerHeartbeat() {
        createProject();
        Response response = given()
                .when()
                .get("/heartbeat");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testUpdateTodoUsingIdWithPut() {
        Map<String, String> todoDataUpdated = new HashMap<>();
        todoDataUpdated.put("description", UPDATED_DESCRIPTION);

        Response response = given()
                .pathParam("id", todoId)
                .contentType("application/json")
                .body(todoDataUpdated)
                .when()
                .put("/todos/{id}");
        assertEquals(200, response.getStatusCode());
    }

    @AfterEach
    public void tearDown() {
        if (!todoDeleted) {
            // Delete the todo after each test
            Response response = given()
                    .pathParam("id", todoId)
                    .when()
                    .delete("/todos/{id}");
            assertEquals(200, response.getStatusCode());
        }
        todoDeleted = false;
    }

}
