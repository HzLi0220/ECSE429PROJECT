import io.restassured.http.ContentType;
import org.apache.commons.lang3.StringEscapeUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

// For ramdom order tests
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.Random;

@TestMethodOrder(Random.class)
public class ProjectTest {

    private int projectId;
    private boolean projectDeleted = false; // Tells Tear Down to not delete the project as the Test already done so.
    private String mockTitle = "Test Project";
    private String mockDescription = "Description of the test project";
    private boolean mockCompleted = false;
    private String mockUpdateTitle = "Updated Test Project";
    private String mockUpdateDescription = "Updated Description of the test project";
    private boolean mockUpdateCompleted = true;

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
        Map<String, Object> projectData = new HashMap<>();
        projectData.put("title", mockTitle);
        projectData.put("description", mockDescription);
        projectData.put("completed", mockCompleted);

        Response response = given()
                .contentType("application/json")
                .body(projectData)
                .when()
                .post("/projects");
        assertEquals(201, response.getStatusCode());

        // Extract the project ID for further operations
        projectId = response.jsonPath().getInt("id");
        System.out.println("Set Up project with ID projectId");

        // Validate that the created project object indeed have the matching fields.
        assertEquals(mockTitle, response.jsonPath().getString("title"));
        assertEquals(mockDescription, response.jsonPath().getString("description"));
        assertEquals(String.valueOf(mockCompleted), response.jsonPath().getString("completed"));
    }

    @AfterEach
    public void tearDown() {
        if (!projectDeleted) {
            // Delete the project after each test
            Response response = given()
                    .pathParam("id", projectId)
                    .when()
                    .delete("/projects/{id}");
            // The response code is actually 200 OK without a body.
            //I think this is kinda a not so good practise tho.
            //Delete request should have returned 204 on sucess if there is no return body anyway. 
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
                .queryParam("title", mockTitle)
                .when()
                .get("/projects");
        assertEquals(200, response.getStatusCode());

        // Validate that the filtered project object indeed have the matching fields.
        assertEquals(mockTitle, response.jsonPath().getString("projects[0].title"));
    }

    @Test
    void testGetProjectHeaders() {
        Response response = given()
                .when()
                .head("/projects");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testGetSpecificProject() {
        Response response = given()
                .pathParam("id", projectId)
                .when()
                .get("/projects/{id}");
        assertEquals(200, response.getStatusCode());

        // Validate that the created project object indeed have the matching fields.
        assertEquals(mockTitle, response.jsonPath().getString("projects[0].title"));
        assertEquals(mockDescription, response.jsonPath().getString("projects[0].description"));
    }

    @Test
    public void testGetProjectByIdXML() {
        Response response = given()
                .header("Accept", "application/xml")
                .get("/projects/" + projectId);
        assertEquals(200, response.getStatusCode());
        assertEquals("application/xml", response.getHeader("Content-Type"));

        // Unescape XML entities in the response body and parse it as XML
        String responseBody = StringEscapeUtils.unescapeXml(response.getBody().asString());

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(responseBody));
            Document doc = builder.parse(is);

            // Extract and assert the title and description elements
            String title = doc.getElementsByTagName("title").item(0).getTextContent();
            String description = doc.getElementsByTagName("description").item(0).getTextContent();
            assertEquals(mockTitle, title);
            assertEquals(mockDescription, description);
        } catch (Exception e) {
            System.out.println("Failed to parse or extract data from the XML response.");
        }
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
        Map<String, Object> projectData = new HashMap<>();
        projectData.put("title", mockUpdateTitle);
        projectData.put("description", mockUpdateDescription);
        projectData.put("completed", mockUpdateCompleted);

        Response updateResponse = given()
                .pathParam("id", projectId)
                .body(projectData)
                .when()
                .post("/projects/{id}");
        assertEquals(200, updateResponse.getStatusCode());

        // Fetch the updated project and validate
        Response fetchResponse = given()
                .pathParam("id", projectId)
                .when()
                .get("/projects/{id}");
        String updatedTitle = fetchResponse.jsonPath().getString("projects[0].title");
        String updatedDescription = fetchResponse.jsonPath().getString("projects[0].description");
        String updatedComplted = fetchResponse.jsonPath().getString("projects[0].completed");

        assertEquals(mockUpdateTitle, updatedTitle);
        assertEquals(mockUpdateDescription, updatedDescription);
        assertEquals(String.valueOf(mockUpdateCompleted), updatedComplted);
    }

    @Test
    void testUpdateSpecificProjectUsingPUT() {
        Map<String, Object> projectData = new HashMap<>();
        projectData.put("title", mockUpdateTitle);
        projectData.put("description", mockUpdateDescription);
        projectData.put("completed", mockUpdateCompleted);

        Response updateResponse = given()
                .pathParam("id", projectId)
                .body(projectData)
                .when()
                .put("/projects/{id}");
        assertEquals(200, updateResponse.getStatusCode());

        // Fetch the updated project and validate
        Response fetchResponse = given()
                .pathParam("id", projectId)
                .when()
                .get("/projects/{id}");
        String updatedTitle = fetchResponse.jsonPath().getString("projects[0].title");
        String updatedDescription = fetchResponse.jsonPath().getString("projects[0].description");
        String updatedComplted = fetchResponse.jsonPath().getString("projects[0].completed");

        assertEquals(mockUpdateTitle, updatedTitle);
        assertEquals(mockUpdateDescription, updatedDescription);
        assertEquals(String.valueOf(mockUpdateCompleted), updatedComplted);
    }

    @Test
    void testPostProjectAsXmlWithoutID() {
        String xmlBody = "<project>"
                + "<title>PostCategoryAsXml</title>"
                + "<description>testPostCategoryAsXmlWithoutID</description>"
                + "</project>";
        Response response = given()
                .contentType(ContentType.XML)
                .body(xmlBody)
                .when()
                .post("/projects");
        assertEquals(201, response.getStatusCode());
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));

        //delete the created category to restore system state
        int projectID = response.jsonPath().getInt("id");
        Response deleteResponse = given()
                .pathParam("id", projectID)
                .when()
                .delete("/projects/{id}");
        assertEquals(200, deleteResponse.getStatusCode());
    }

    @Test
    void testDeleteSpecificProject() {
        Response response = given()
                .pathParam("id", projectId)
                .when()
                .delete("/projects/{id}");
        assertEquals(200, response.getStatusCode());

        // Make sure it is indeed deleted as that 404 is returned by the get by ID.
        Response getResponse = given()
                .pathParam("id", projectId)
                .when()
                .get("/projects/{id}");
        assertEquals(404, getResponse.getStatusCode());

        projectDeleted = true;
    }

    // ---------------- Additional Unit Test Considerations ----------------

    @Test
    public void testMalformedJSONPayload() {
        String requestBody = "{Invalid JSON}";
        Response response = given()
                .body(requestBody)
                .post("/projects");
        assertEquals(400, response.getStatusCode());
    }

    @Test
    public void testMalformedXMLPayload() {
        String requestBody = "<Invalid XML>";
        Response response = given()
                .body(requestBody)
                .post("/projects");
        assertEquals(400, response.getStatusCode());
    }

    @Test
    public void testDeleteNonExistingCategory() {
        int projectId = 1000;
        Response response = given()
                .pathParam("id", projectId)
                .when()
                .delete("/projects/{id}");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    void testPutProjectAsXmlWithoutIDNotAllowed() {
        String xmlBody = "<project>"
                + "<title>PostCategoryAsXml</title>"
                + "<description>testPostCategoryAsXmlWithoutID</description>"
                + "</project>";
        Response response = given()
                .contentType(ContentType.XML)
                .body(xmlBody)
                .when()
                .put("/projects");
        assertEquals(405, response.getStatusCode());
    }

    @Test
    void testFetchProjectsWithNonExistentFilter() {
        Response response = given()
                .queryParam("title", "non-existent-filter")
                .when()
                .get("/projects");

        assertEquals(200, response.getStatusCode());
        List<?> projects = response.jsonPath().getList("projects");
        assertTrue(projects.isEmpty());
    }

    @Test
    void testFetchHeadersForNonExistingProject() {
        Response response = given()
                .pathParam("id", "non-existing-id")
                .when()
                .head("/projects/{id}");
    
        assertEquals(404, response.getStatusCode());
    }

    @Test
    void testFetchNonExistingProject() {
        Response response = given()
                .pathParam("id", "non-existing-id")
                .when()
                .get("/projects/{id}");

        assertEquals(404, response.getStatusCode());
    }

}
