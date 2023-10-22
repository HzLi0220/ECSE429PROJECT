import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.MethodOrderer.Random;

/**
 * @author Moncef Amchech
 */
@TestMethodOrder(Random.class)
public class InteroperabilityTest {
    HashMap<String,Object> testProject;
    HashMap<String,Object> testTodo;
    HashMap<String,Object> testCategory;

    void updateTestVariables()
    {
        if (testTodo != null)
        {
            Response response = given()
                    .pathParam("id", testTodo.get("id"))
                    .when()
                    .get("/todos/{id}");
            testTodo.putAll(response.jsonPath().get("todos[0]"));
        }
        if (testProject != null)
        {
            Response response = given()
                    .pathParam("id", testProject.get("id"))
                    .when()
                    .get("/projects/{id}");
            testProject.putAll(response.jsonPath().getMap("projects[0]"));
        }
        if (testCategory != null)
        {
            Response response = given()
                    .pathParam("id", testCategory.get("id"))
                    .when()
                    .get("/projects/{id}");
            testCategory.putAll(response.jsonPath().getMap(""));
        }
    }
    @BeforeAll
    public static void initialSetup(){
        RestAssured.baseURI = "http://localhost:4567";
    }
    @BeforeEach
    public void createTestVariables()
    {
        // creating a test project instance
        testProject = new HashMap<>();
        testProject.put("title", "testProject");
        Response response = given()
                .contentType("application/json")
                .body(testProject)
                .when()
                .post("/projects");
        assertEquals(201, response.getStatusCode());
        testProject.putAll(response.jsonPath().getMap(""));

        //creating a test todo instance
        testTodo = new HashMap<>();
        testTodo.put("title", "testTodo");
        response = given()
                .contentType("application/json")
                .body(testTodo)
                .when()
                .post("/todos");
        assertEquals(201, response.getStatusCode());
        testTodo.putAll(response.jsonPath().getMap(""));
        //creating a test category
        testCategory = new HashMap<>();
        testCategory.put("title", "testCategory");
        response = given()
                .contentType("application/json")
                .body(testCategory)
                .when()
                .post("/categories");
        assertEquals(201, response.getStatusCode());
        testCategory.putAll(response.jsonPath().getMap(""));
    }
    @AfterEach
    public void cleanup()
    {
        if (testTodo != null)
        {
            Response response = given()
                    .pathParam("id", testTodo.get("id"))
                    .when()
                    .delete("/todos/{id}");
            assertEquals(200, response.getStatusCode());
            testTodo = null;
        }
        if (testProject != null)
        {
            Response response = given()
                    .pathParam("id", testProject.get("id"))
                    .when()
                    .delete("/projects/{id}");
            assertEquals(200, response.getStatusCode());
            testProject = null;
        }
        if (testCategory != null)
        {
            Response response = given()
                    .pathParam("id", testCategory.get("id"))
                    .when()
                    .delete("/categories/{id}");
            assertEquals(200, response.getStatusCode());
            testCategory = null;
        }
    }
    @Test
    void testServerIsRunning() {
        try {
            URL url = new URL("http://localhost:4567");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            assertEquals(200, responseCode);
        } catch (Exception e) {
            fail(e);
        }
    }
    @Test
    void testCreateBidirectionalRelationshipTodoProject()
    {
        //creating todo-project relationship
        Response response = given()
                .contentType("application/json")
                .body(testProject)
                .pathParam("id", testTodo.get("id"))
                .when()
                .post("/todos/{id}/tasksof");
        assertEquals(201,response.getStatusCode());
        //validating
        updateTestVariables();
        // we check on both sides since its a bidirectional relationship
        assertEquals(((ArrayList<Map>)testTodo.get("tasksof")).get(0).get("id"),testProject.get("id")); // check on todo's side
        assertEquals(((ArrayList<Map>)testProject.get("tasks")).get(0).get("id"),testTodo.get("id")); // check on project's side
    }
    @Test
    void testCreateBidirectionalRelationshipProjectTodo()
    {
        //creating project-todo relationship
        Response response = given()
                .contentType("application/json")
                .body(testTodo)
                .pathParam("id", testProject.get("id"))
                .when()
                .post("/projects/{id}/tasks");
        assertEquals(201,response.getStatusCode());
        //validating
        updateTestVariables();
        // we check on both sides since its a bidirectional relationship
        assertEquals(((ArrayList<Map>)testTodo.get("tasksof")).get(0).get("id"),testProject.get("id")); // check on todo's side
        assertEquals(((ArrayList<Map>)testProject.get("tasks")).get(0).get("id"),testTodo.get("id")); // check on project's side
    }

    @Test
    void testDeleteBidirectionalRelationshipTodoProject()
    {
        //creating project-todo relationship
        Response response = given()
                .contentType("application/json")
                .body(testTodo)
                .pathParam("id", testProject.get("id"))
                .when()
                .post("/projects/{id}/tasks");
        assertEquals(201,response.getStatusCode());

        //deleting it
        response = given()
                .contentType("application/json")
                .pathParam("id", testTodo.get("id"))
                .pathParam("id2", testProject.get("id"))
                .when()
                .delete("/todos/{id}/tasksof/{id2}");
        assertEquals(200,response.getStatusCode());
        //validating
        updateTestVariables();
        // we check on both sides since its a bidirectional relationship
        assertNull(testTodo.get("tasksof")); // check on todo's side
        assertNull(testProject.get("tasks")); // check on project's side
    }
    @Test
    void testDeleteBidirectionalRelationshipProjectTodo()
    {
        //creating project-todo relationship
        Response response = given()
                .contentType("application/json")
                .body(testTodo)
                .pathParam("id", testProject.get("id"))
                .when()
                .post("/projects/{id}/tasks");
        assertEquals(201,response.getStatusCode());

        //deleting it
        response = given()
                .contentType("application/json")
                .pathParam("id", testProject.get("id"))
                .pathParam("id2", testTodo.get("id"))
                .when()
                .delete("/projects/{id}/tasks/{id2}");
        assertEquals(200,response.getStatusCode());
        //validating
        updateTestVariables();
        // we check on both sides since its a bidirectional relationship
        assertNull(testTodo.get("tasksof")); // check on todo's side
        assertNull(testProject.get("tasks")); // check on project's side
    }



}
