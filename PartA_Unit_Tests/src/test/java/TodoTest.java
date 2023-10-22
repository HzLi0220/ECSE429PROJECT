import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
// For ramdom order tests
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.Random;

@TestMethodOrder(Random.class)
public class TodoTest {

    private int todoId;
    private int todoIdExtra;
    private boolean todoDeleted = false;

    private final String TODO_TITLE = "Test Todo";
    private final String TODO_DESCRIPTION = "Description of the test todo";

    private final String UPDATED_TITLE = "New title";
    private final String UPDATED_DESCRIPTION = "New description";

    private final int UNLINKED_ID = Integer.MAX_VALUE;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:4567";
    }

    @Test
    void testServerIsRunning() {
        todoDeleted = false;  // Reset the flag during setup
        try {
            URL url = new URL("http://localhost:4567");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            assertEquals(200, responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void setUp() {
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

    @Test
    void testGetAllTodos() {
        Response response = given()
                .when()
                .get("/todos");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testGetTodoById() {
        Response response = given()
                .pathParam("id", todoId)
                .when()
                .get("/todos/{id}");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testCreateTodoWithNonBooleanStatusFail() {
        Map<String, String> wrongTodoData = new HashMap<>();
        wrongTodoData.put("title", TODO_TITLE);
        wrongTodoData.put("description", TODO_DESCRIPTION);
        wrongTodoData.put("doneStatus", "Wrong");
        Response response = given()
                .when()
                .contentType("application/json")
                .body(wrongTodoData)
                .post("/todos");
        assertEquals(400, response.getStatusCode());
    }

    @Test
    void testGetTodoWithDoneStatusFalse() {
        Response response = given()
                .when()
                .get("/todos?doneStatus=false");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testUpdateTodoUsingIdWithPost() {
        Map<String, String> todoDataUpdated = new HashMap<>();
        todoDataUpdated.put("title", UPDATED_TITLE);
        todoDataUpdated.put("description", UPDATED_DESCRIPTION);

        Response response = given()
                .pathParam("id", todoId)
                .contentType("application/json")
                .body(todoDataUpdated)
                .when()
                .post("/todos/{id}");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testDeleteTodo() {
        Response response = given()
                .pathParam("id", todoId)
                .when()
                .delete("/todos/{id}");
        assertEquals(200, response.getStatusCode());
        todoDeleted = true;
    }

    @Test
    void testGetAllTodosAsXml() {
        Response response = given()
                .contentType("application/xml")
                .when()
                .get("/todos");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testCreateTodoWithXmlBody() {
        String xmlBody = "<todo>"
                + "<title> " + TODO_TITLE + " </title>"
                + "<description> " + TODO_DESCRIPTION + " </description>"
                + "</todo>";

        Response response = given()
                .when()
                .contentType("application/xml")
                .body(xmlBody)
                .post("/todos");

        assertEquals(201, response.getStatusCode());

        todoIdExtra = response.jsonPath().getInt("id");
    }
    @After
    void deleteTodoExtra() {
        Response response = given()
                .pathParam("id", todoIdExtra)
                .when()
                .delete("/todos/{id}");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testGetAllTodosHeaders() {
        Response response = given()
                .when()
                .head("/todos");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testGetTodoHeader() {
        Response response = given()
                .pathParam("id", todoId)
                .when()
                .head("/todos/{id}");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testGetTodoWithNonExistingIdFail() {
        Response response = given()
                .pathParam("id", UNLINKED_ID)
                .when()
                .head("/todos/{id}");
        assertEquals(404, response.getStatusCode());
    }
}
