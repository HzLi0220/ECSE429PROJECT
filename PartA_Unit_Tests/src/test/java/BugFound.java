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

// Note all the tests in this class will be failing due to bugs/risks found in the application under the task.
public class BugFound {

    private int projectId;

    @BeforeAll
    public static void initialSetup() {
        RestAssured.baseURI = "http://localhost:4567";
    }

    public void creatProject() {
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

    @Test
    void testUpdateProjectWithStringValues() {
        creatProject();
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
}
