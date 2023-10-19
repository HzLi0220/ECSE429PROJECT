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

public class ProjectTest {

    private int projectId;
    private boolean projectDeleted = false; // Tells Tear Down to not delete the project as the Test already done so.

    @BeforeAll
    public static void initialSetup() {
        RestAssured.baseURI = "http://localhost:4567";
    }

    @Test
    void testServerIsRunning() {
        projectDeleted = false;  // Reset the flag during setup
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

    @AfterEach
    public void tearDown() {
        if (!projectDeleted) {
            // Delete the project after each test
            Response response = given()
                    .pathParam("id", projectId)
                    .when()
                    .delete("/projects/{id}");
            // The response code is actually 200 not 204. I think this is kinda a not so good practise tho. Delete request should have returned 204 on sucess. 
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    void testGetAllProjects() {
        Response response = given()
                .when()
                .get("/projects");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testGetAllProjectsWithFilter() {
        Response response = given()
                .queryParam("title", "prehenderit%20in%20volup")
                .when()
                .get("/projects");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testGetProjectHeaders() {
        Response response = given()
                .when()
                .head("/projects");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testCreateProject() {
        Map<String, String> projectData = new HashMap<>();
        projectData.put("title", "New Project");
        projectData.put("description", "Description of the new project");

        Response response = given()
                .body(projectData)
                .when()
                .post("/projects");
        assertEquals(201, response.getStatusCode());
    }

    @Test
    void testGetSpecificProject() {
        Response response = given()
                .pathParam("id", projectId)
                .when()
                .get("/projects/{id}");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testGetSpecificProjectHeaders() {
        Response response = given()
                .pathParam("id", projectId)
                .when()
                .head("/projects/{id}");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testUpdateSpecificProjectUsingPOST() {
        Map<String, String> projectData = new HashMap<>();
        projectData.put("title", "Updated Project");
        projectData.put("description", "Updated description");

        Response response = given()
                .pathParam("id", projectId)
                .body(projectData)
                .when()
                .post("/projects/{id}");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testUpdateSpecificProjectUsingPUT() {
        Map<String, String> projectData = new HashMap<>();
        projectData.put("title", "Updated Project Again");
        projectData.put("description", "Updated description again");

        Response response = given()
                .pathParam("id", projectId)
                .body(projectData)
                .when()
                .put("/projects/{id}");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testDeleteSpecificProject() {
        Response response = given()
                .pathParam("id", projectId)
                .when()
                .delete("/projects/{id}");
        assertEquals(200, response.getStatusCode());
        projectDeleted = true;
    }
}