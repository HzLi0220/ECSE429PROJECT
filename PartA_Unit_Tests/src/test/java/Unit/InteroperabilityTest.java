package Unit;

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
                    .get("/categories/{id}");
            testCategory.putAll(response.jsonPath().getMap("categories[0]"));
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
        //creating todo-project relationship with invalid id
        Response response = given()
                .contentType("application/json")
                .body(testProject)
                .pathParam("id", 4567)
                .when()
                .post("/todos/{id}/tasksof");
        assertEquals(404,response.getStatusCode());
        //creating todo-project relationship
        response = given()
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
    void testDeleteBidirectionalRelationshipTodoProject()
    {
        //deleting project-todo relationship with invalid id
        Response response = given()
                .contentType("application/json")
                .pathParam("id", 2344)
                .pathParam("id2", 4555)
                .when()
                .delete("/todos/{id}/tasksof/{id2}");
        assertEquals(404,response.getStatusCode());
        //creating project-todo relationship
        response = given()
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
    void testGetBidirectionalRelationshipTodoProject()
    {

        //getting relationships when there are none
        Response response = given()
                .contentType("application/json")
                .pathParam("id", testTodo.get("id"))
                .when()
                .get("/todos/{id}/tasksof");
        assertEquals(200,response.getStatusCode());
        //validating the response
        assertEquals(0,response.jsonPath().getList("projects").size()); // no relationships


        //creating project-todo relationship
        response = given()
                .contentType("application/json")
                .body(testTodo)
                .pathParam("id", testProject.get("id"))
                .when()
                .post("/projects/{id}/tasks");
        assertEquals(201,response.getStatusCode());

        //getting it
        response = given()
                .contentType("application/json")
                .pathParam("id", testTodo.get("id"))
                .when()
                .get("/todos/{id}/tasksof");
        assertEquals(200,response.getStatusCode());
        //validating the response
        assertEquals(1,response.jsonPath().getList("projects").size()); // no relationships
    }

    @Test
    void testHeadBidirectionalRelationshipTodoProject()
    {
        // getting relationships (doesnt matter if there are any or not since its just for the headers)
        Response response = given()
                .contentType("application/json")
                .pathParam("id", testTodo.get("id"))
                .when()
                .head("/todos/{id}/tasksof");
        assertEquals(200,response.getStatusCode());
    }
    @Test
    void testCreateBidirectionalRelationshipProjectTodo()
    {
        //creating project-todo relationship with invalid id
        Response response = given()
                .contentType("application/json")
                .body(testTodo)
                .pathParam("id", 3245)
                .when()
                .post("/projects/{id}/tasks");
        assertEquals(404,response.getStatusCode());
        //creating project-todo relationship
        response = given()
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
    void testDeleteBidirectionalRelationshipProjectTodo()
    {
        //deleting project-todo relationship with invalid id
        Response response = given()
                .contentType("application/json")
                .pathParam("id", 2344)
                .pathParam("id2", 2344)
                .when()
                .delete("/projects/{id}/tasks/{id2}");
        assertEquals(404,response.getStatusCode());
        //creating project-todo relationship
        response = given()
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
    @Test
    void testGetBidirectionalRelationshipProjectTodo()
    {
        //getting relationships when there are none
        Response response = given()
                .contentType("application/json")
                .pathParam("id", testProject.get("id"))
                .when()
                .get("/projects/{id}/tasks");
        assertEquals(200,response.getStatusCode());
        //validating the response
        assertEquals(0,response.jsonPath().getList("todos").size()); // no relationships


        //creating project-todo relationship
        response = given()
                .contentType("application/json")
                .body(testTodo)
                .pathParam("id", testProject.get("id"))
                .when()
                .post("/projects/{id}/tasks");
        assertEquals(201,response.getStatusCode());

        //getting it
        response = given()
                .contentType("application/json")
                .pathParam("id", testProject.get("id"))
                .when()
                .get("/projects/{id}/tasks");
        assertEquals(200,response.getStatusCode());
        //validating the response
        assertEquals(1,response.jsonPath().getList("todos").size()); // no relationships
    }

    @Test
    void testHeadBidirectionalRelationshipProjectTodo()
    {
        // getting relationships (doesnt matter if there are any or not since its just for the headers)
        Response response = given()
                .contentType("application/json")
                .pathParam("id", testProject.get("id"))
                .when()
                .head("/projects/{id}/tasks");
        assertEquals(200,response.getStatusCode());
    }
    @Test
    void testCreateUnidirectionalRelationshipTodoCategory()
    {
        //creating todo-category relationship with invalid id
        Response response = given()
                .contentType("application/json")
                .body(testCategory)
                .pathParam("id", 2342)
                .when()
                .post("/todos/{id}/categories");
        assertEquals(404,response.getStatusCode());
        //creating todo-category relationship
        response = given()
                .contentType("application/json")
                .body(testCategory)
                .pathParam("id", testTodo.get("id"))
                .when()
                .post("/todos/{id}/categories");
        assertEquals(201,response.getStatusCode());
        //validating
        updateTestVariables();
        // we check on one side only since its unidirectional
        assertEquals(((ArrayList<Map>)testTodo.get("categories")).get(0).get("id"),testCategory.get("id")); // check on todo's side
    }

    @Test
    void testDeleteUnidirectionalRelationshipTodoCategory()
    {
        //deleting todo-category relationship with invalid id
        Response response = given()
                .contentType("application/json")
                .pathParam("id", 3245)
                .pathParam("id2", 3455)
                .when()
                .delete("/todos/{id}/categories/{id2}");
        assertEquals(404,response.getStatusCode());
        //creating todo-category relationship
        response = given()
                .contentType("application/json")
                .body(testCategory)
                .pathParam("id", testTodo.get("id"))
                .when()
                .post("/todos/{id}/categories");
        assertEquals(201,response.getStatusCode());

        //deleting it
        response = given()
                .contentType("application/json")
                .pathParam("id", testTodo.get("id"))
                .pathParam("id2", testCategory.get("id"))
                .when()
                .delete("/todos/{id}/categories/{id2}");
        assertEquals(200,response.getStatusCode());
        //validating
        updateTestVariables();
        // we check on one side only since its unidirectional
        assertNull(testTodo.get("categories")); // check on todo's side
    }
    @Test
    void testGetUnidirectionalRelationshipTodoCategory()
    {
        //getting relationships when there are none
        Response response = given()
                .contentType("application/json")
                .pathParam("id", testTodo.get("id"))
                .when()
                .get("/todos/{id}/categories");
        assertEquals(200,response.getStatusCode());
        //validating the response
        assertEquals(0,response.jsonPath().getList("categories").size()); // no relationships


        //creating todo-category relationship
        response = given()
                .contentType("application/json")
                .body(testCategory)
                .pathParam("id", testTodo.get("id"))
                .when()
                .post("/todos/{id}/categories");
        assertEquals(201,response.getStatusCode());

        //getting it
        response = given()
                .contentType("application/json")
                .pathParam("id", testTodo.get("id"))
                .when()
                .get("/todos/{id}/categories");
        assertEquals(200,response.getStatusCode());
        //validating the response
        assertEquals(1,response.jsonPath().getList("categories").size()); // no relationships
    }

    @Test
    void testHeadUnidirectionalRelationshipTodoCategory()
    {
        // getting relationships (doesnt matter if there are any or not since its just for the headers)
        Response response = given()
                .contentType("application/json")
                .pathParam("id", testTodo.get("id"))
                .when()
                .head("/todos/{id}/categories");
        assertEquals(200,response.getStatusCode());

    }
    @Test
    void testCreateUnidirectionalRelationshipCategoryTodo()
    {
        //creating category-todo relationship with invalid id
        Response response = given()
                .contentType("application/json")
                .body(testTodo)
                .pathParam("id", 1233)
                .when()
                .post("/categories/{id}/todos");
        assertEquals(404,response.getStatusCode());
        //creating category-todo relationship
        response = given()
                .contentType("application/json")
                .body(testTodo)
                .pathParam("id", testCategory.get("id"))
                .when()
                .post("/categories/{id}/todos");
        assertEquals(201,response.getStatusCode());
        //validating
        updateTestVariables();
        // we check on one side only since its unidirectional
        assertEquals(((ArrayList<Map>)testCategory.get("todos")).get(0).get("id"),testTodo.get("id")); // check on category's side
    }
    @Test
    void testDeleteUnidirectionalRelationshipCategoryTodo()
    {
        //deleting category-todo relationship with invalid id
        Response response = given()
                .contentType("application/json")
                .pathParam("id", 2344)
                .pathParam("id2", 3455)
                .when()
                .delete("/categories/{id}/todos/{id2}");
        assertEquals(404,response.getStatusCode());
        //creating category-todo relationship
        response = given()
                .contentType("application/json")
                .body(testTodo)
                .pathParam("id", testCategory.get("id"))
                .when()
                .post("/categories/{id}/todos");
        assertEquals(201,response.getStatusCode());

        //deleting it
        response = given()
                .contentType("application/json")
                .pathParam("id", testCategory.get("id"))
                .pathParam("id2", testTodo.get("id"))
                .when()
                .delete("/categories/{id}/todos/{id2}");
        assertEquals(200,response.getStatusCode());
        //validating
        updateTestVariables();
        // we check on one side only since its unidirectional
        assertNull(testCategory.get("todos")); // check on category's side
    }
    @Test
    void testGetUnidirectionalRelationshipCategoryTodo()
    {
        //getting relationships when there are none
        Response response = given()
                .contentType("application/json")
                .pathParam("id", testCategory.get("id"))
                .when()
                .get("/categories/{id}/todos");
        assertEquals(200,response.getStatusCode());
        //validating the response
        assertEquals(0,response.jsonPath().getList("todos").size()); // no relationships


        //creating category-todo relationship
        response = given()
                .contentType("application/json")
                .body(testTodo)
                .pathParam("id", testCategory.get("id"))
                .when()
                .post("/categories/{id}/todos");
        assertEquals(201,response.getStatusCode());

        //getting it
        response = given()
                .contentType("application/json")
                .pathParam("id", testCategory.get("id"))
                .when()
                .get("/categories/{id}/todos");
        assertEquals(200,response.getStatusCode());
        //validating the response
        assertEquals(1,response.jsonPath().getList("todos").size()); // no relationships
    }

    @Test
    void testHeadBidirectionalRelationshipCategoryTodo()
    {
        // getting relationships (doesnt matter if there are any or not since its just for the headers)
        Response response = given()
                .contentType("application/json")
                .pathParam("id", testCategory.get("id"))
                .when()
                .head("/categories/{id}/todos");
        assertEquals(200,response.getStatusCode());
    }
    @Test
    void testCreateUnidirectionalRelationshipProjectCategory()
    {
        //creating project-category relationship with invalid id
        Response response = given()
                .contentType("application/json")
                .body(testCategory)
                .pathParam("id", 3455)
                .when()
                .post("/projects/{id}/categories");
        assertEquals(404,response.getStatusCode());
        //creating project-category relationship
        response = given()
                .contentType("application/json")
                .body(testCategory)
                .pathParam("id", testProject.get("id"))
                .when()
                .post("/projects/{id}/categories");
        assertEquals(201,response.getStatusCode());
        //validating
        updateTestVariables();
        // we check on one side only since its unidirectional
        assertEquals(((ArrayList<Map>)testProject.get("categories")).get(0).get("id"),testCategory.get("id")); // check on projects side
    }

    @Test
    void testDeleteUnidirectionalRelationshipProjectCategory()
    {
        //deleting project-category relationship with invalid id
        Response response = given()
                .contentType("application/json")
                .pathParam("id", 3444)
                .pathParam("id2", 4444)
                .when()
                .delete("/projects/{id}/categories/{id2}");
        assertEquals(404,response.getStatusCode());
        //creating project-category relationship
        response = given()
                .contentType("application/json")
                .body(testCategory)
                .pathParam("id", testProject.get("id"))
                .when()
                .post("/projects/{id}/categories");
        assertEquals(201,response.getStatusCode());

        //deleting it
        response = given()
                .contentType("application/json")
                .pathParam("id", testProject.get("id"))
                .pathParam("id2", testCategory.get("id"))
                .when()
                .delete("/projects/{id}/categories/{id2}");
        assertEquals(200,response.getStatusCode());
        //validating
        updateTestVariables();
        // we check on one side only since its unidirectional
        assertNull(testProject.get("categories")); // check on projects side
    }
    @Test
    void testGetUnidirectionalRelationshipProjectCategory()
    {
        //getting relationships when there are none
        Response response = given()
                .contentType("application/json")
                .pathParam("id", testProject.get("id"))
                .when()
                .get("/projects/{id}/categories");
        assertEquals(200,response.getStatusCode());
        //validating the response
        assertEquals(0,response.jsonPath().getList("categories").size()); // no relationships


        //creating project-category relationship
        response = given()
                .contentType("application/json")
                .body(testCategory)
                .pathParam("id", testProject.get("id"))
                .when()
                .post("/projects/{id}/categories");
        assertEquals(201,response.getStatusCode());

        //getting it
        response = given()
                .contentType("application/json")
                .pathParam("id", testProject.get("id"))
                .when()
                .get("/projects/{id}/categories");
        assertEquals(200,response.getStatusCode());
        //validating the response
        assertEquals(1,response.jsonPath().getList("categories").size()); // no relationships
    }

    @Test
    void testHeadUnidirectionalRelationshipProjectCategory()
    {
        // getting relationships (doesnt matter if there are any or not since its just for the headers)
        Response response = given()
                .contentType("application/json")
                .pathParam("id", testProject.get("id"))
                .when()
                .head("/projects/{id}/categories");
        assertEquals(200,response.getStatusCode());

    }

    // THE FOLLOWING ENDPOINTS CONTRADICT THE RELATIONSHIP MODEL IN THE API DOCUMENTATION, BUT WE ARE STILL TESTING THEM FOR VERIFICATION PURPOSES
    @Test
    void testCreateUnidirectionalRelationshipCategoryProject()
    {
        //creating category-project relationship with invalid id
        Response response = given()
                .contentType("application/json")
                .body(testProject)
                .pathParam("id", 4444)
                .when()
                .post("/categories/{id}/projects");
        assertEquals(404,response.getStatusCode());
        //creating category-project relationship
        response = given()
                .contentType("application/json")
                .body(testProject)
                .pathParam("id", testCategory.get("id"))
                .when()
                .post("/categories/{id}/projects");
        assertEquals(201,response.getStatusCode());
        //validating
        updateTestVariables();
        // we check on one side only since its unidirectional
        assertEquals(((ArrayList<Map>)testCategory.get("projects")).get(0).get("id"),testProject.get("id")); // check on category's side
    }
    @Test
    void testDeleteUnidirectionalRelationshipCategoryProject()
    {
        //deleting category-project relationship with invalid ids
        Response response = given()
                .contentType("application/json")
                .pathParam("id", 4444)
                .pathParam("id2", 5555)
                .when()
                .delete("/categories/{id}/projects/{id2}");
        assertEquals(404,response.getStatusCode());
        //creating category-project relationship
        response = given()
                .contentType("application/json")
                .body(testProject)
                .pathParam("id", testCategory.get("id"))
                .when()
                .post("/categories/{id}/projects");
        assertEquals(201,response.getStatusCode());

        //deleting it
        response = given()
                .contentType("application/json")
                .pathParam("id", testCategory.get("id"))
                .pathParam("id2", testProject.get("id"))
                .when()
                .delete("/categories/{id}/projects/{id2}");
        assertEquals(200,response.getStatusCode());
        //validating
        updateTestVariables();
        // we check on one side only since its unidirectional
        assertNull(testCategory.get("projects")); // check on category's side
    }
    @Test
    void testGetUnidirectionalRelationshipCategoryProject()
    {
        //getting relationships when there are none
        Response response = given()
                .contentType("application/json")
                .pathParam("id", testCategory.get("id"))
                .when()
                .get("/categories/{id}/projects");
        assertEquals(200,response.getStatusCode());
        //validating the response
        assertEquals(0,response.jsonPath().getList("projects").size()); // no relationships


        //creating category-project relationship
        response = given()
                .contentType("application/json")
                .body(testProject)
                .pathParam("id", testCategory.get("id"))
                .when()
                .post("/categories/{id}/projects");
        assertEquals(201,response.getStatusCode());

        //getting it
        response = given()
                .contentType("application/json")
                .pathParam("id", testCategory.get("id"))
                .when()
                .get("/categories/{id}/projects");
        assertEquals(200,response.getStatusCode());
        //validating the response
        assertEquals(1,response.jsonPath().getList("projects").size()); // no relationships
    }

    @Test
    void testHeadBidirectionalRelationshipCategoryProject()
    {
        // getting relationships (doesnt matter if there are any or not since its just for the headers)
        Response response = given()
                .contentType("application/json")
                .pathParam("id", testCategory.get("id"))
                .when()
                .head("/categories/{id}/projects");
        assertEquals(200,response.getStatusCode());
    }
}
