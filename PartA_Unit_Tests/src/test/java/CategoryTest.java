import io.restassured.http.ContentType;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.delete;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;
// For random order tests
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.Random;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@TestMethodOrder(Random.class)
public class CategoryTest {
    private int categoryId;
    private boolean categoryDeleted = false;

    private String mockTitle = "Test Category";
    private String mockDescription = "Description of the test Category";
    private String mockUpdateTitle = "Updated Test Category";
    private String mockUpdateDescription = "Updated Description of the Test Category";

    @BeforeAll
    public static void initialSetup() {
        RestAssured.baseURI = "http://localhost:4567";
    }

    @Test
    void testServerIsRunning() {
        categoryDeleted = false;  // Reset the flag during setup
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
        // Create a mock category instance before each test
        categoryDeleted = false;
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("title", mockTitle);
        categoryData.put("description", mockDescription);

        Response response = given()
                .contentType("application/json")
                .body(categoryData)
                .when()
                .post("/categories");
        assertEquals(201, response.getStatusCode());

        categoryId = response.jsonPath().getInt("id");
        System.out.println("Set Up category with ID " + categoryId);

        assertEquals(mockTitle, response.jsonPath().getString("title"));
        assertEquals(mockDescription, response.jsonPath().getString("description"));
    }

    @AfterEach
    public void tearDown() {
        // Delete the category after each test
        if (!categoryDeleted) {
            Response response = given()
                    .pathParam("id", categoryId)
                    .when()
                    .delete("/categories/{id}");
            assertEquals(200, response.getStatusCode());
        }
        // Mark the category as deleted
        categoryDeleted = true;
    }


// ------------------------------ /categories -------------------------------
    @Test
    void testGetAllCategories() {
        Response response = given()
                .when()
                .get("/categories");
        assertEquals(200, response.getStatusCode());
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));
    }

    @Test
    void testHeadAllCategories() {
        Response response = given()
                .when()
                .head("/categories");
        assertEquals(200, response.getStatusCode());
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));

    }

    @Test
    void testPostCategoryWithoutID() {
        String requestBody =
                "{\"title\": \"post category\", " +
                "\"description\": \"testPostCategoryWithoutID\"}";
        Response response = given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .post("/categories");
        assertEquals(201, response.getStatusCode());
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));
        // Validate the content
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("post category"));
        assertTrue(responseBody.contains("testPostCategoryWithoutID"));

        //delete the created category to restore system state
        int categoryId = response.jsonPath().getInt("id");
        Response deleteResponse = given()
                .pathParam("id", categoryId)
                .when()
                .delete("/categories/{id}");
        assertEquals(200, deleteResponse.getStatusCode());
    }

    @Test
    void testPostCategoryAsXmlWithoutID() {
        String xmlBody = "<category>"
                + "<title>PostCategoryAsXml</title>"
                + "<description>testPostCategoryAsXmlWithoutID</description>"
                + "</category>";
        Response response = given()
                .contentType(ContentType.XML)
                .body(xmlBody)
                .when()
                .post("/categories");
        assertEquals(201, response.getStatusCode());
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));

        //delete the created category to restore system state
        int categoryId = response.jsonPath().getInt("id");
        Response deleteResponse = given()
                .pathParam("id", categoryId)
                .when()
                .delete("/categories/{id}");
        assertEquals(200, deleteResponse.getStatusCode());
    }

    @Test
    void testGetFilteredCategories() {
        Response response = given()
                .accept(ContentType.JSON)
                .queryParam("title", "Test Category")
                .when()
                .get("/categories");
        assertEquals(200, response.getStatusCode());
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));

        // Validate that the filtered category object indeed have the matching fields.
        assertEquals("Test Category", response.jsonPath().getString("categories[0].title"));
    }

    // --------------------- /categories/:id --------------------
    @Test
    public void testGetCategoryByIdJSON() {
        Response response = given()
                .header("Accept", "application/json")
                .get("/categories/" + categoryId);
        assertEquals(200, response.getStatusCode());
        assertEquals("application/json", response.getHeader("Content-Type"));

        assertEquals(mockTitle, response.jsonPath().getString("categories[0].title"));
        assertEquals(mockDescription, response.jsonPath().getString("categories[0].description"));

    }

    @Test
    public void testGetCategoryByIdXML() {
        Response response = given()
                .header("Accept", "application/xml")
                .get("/categories/" + categoryId);
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
    void testHeadCategoryById() {
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .head("/categories/" + categoryId);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));
    }

    @Test
    void testPostCategoryById() {
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("title", mockUpdateTitle);
        categoryData.put("description", mockUpdateDescription);

        Response updateResponse = given()
                .body(categoryData)
                .when()
                .post("/categories/" + categoryId);
        assertTrue(updateResponse.getStatusCode() == 200 || updateResponse.getStatusCode() == 201);
        assertTrue(updateResponse.contentType().contains(ContentType.JSON.toString()));

        String updatedTitle = updateResponse.jsonPath().getString("title");
        String updatedDescription = updateResponse.jsonPath().getString("description");

        assertEquals(mockUpdateTitle, updatedTitle);
        assertEquals(mockUpdateDescription, updatedDescription);
    }

    @Test
    void testPutCategoryById() {
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("title", mockUpdateTitle);
        categoryData.put("description", mockUpdateDescription);

        Response response = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(categoryData)
                .when()
                .put("/categories/" + categoryId);
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 204);
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));

        String updatedTitle = response.jsonPath().getString("title");
        String updatedDescription = response.jsonPath().getString("description");

        assertEquals(mockUpdateTitle, updatedTitle);
        assertEquals(mockUpdateDescription, updatedDescription);

    }

    @Test
    void testDeleteCategoryById() {
        Response response = given()
                .pathParam("id", categoryId)
                .when()
                .delete("/categories/{id}");
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 204);

        Response getResponse = given()
                .pathParam("id", categoryId)
                .when()
                .get("/categories/{id}");
        assertEquals(404, getResponse.getStatusCode());
        categoryDeleted = true;

    }

    // --------------------- /categories/:id/todos ----------------------------
    @Test
    void testGetCategoryTodos() {
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .get("/categories/" + categoryId + "/todos");
        assertEquals(200, response.getStatusCode());
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));

    }

    // --------------------- /categories/:id/projects ---------------------
    @Test
    void testGetProjectsForCategory() {
        Response response = given()
                .when()
                .get("/categories/" + categoryId + "/projects");
        assertEquals(200, response.getStatusCode());
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));
    }

    @Test
    void testHeadProjectsForCategory() {
         Response response = given()
                .when()
                .head("/categories/" + categoryId + "/projects");
        assertEquals(200, response.getStatusCode());
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));
    }

    // ---------------- Additional Unit Test Considerations ----------------
    @Test
    public void testMalformedJSONPayload() {
        String requestBody = "{Invalid JSON}";
        Response response = given()
                .body(requestBody)
                .post("/categories");
        assertEquals(400, response.getStatusCode());
    }

    @Test
    public void testMalformedXMLPayload() {
        String requestBody = "<Invalid XML>";
        Response response = given()
                .body(requestBody)
                .post("/categories");
        assertEquals(400, response.getStatusCode());
    }

    @Test
    public void testDeleteNonExistingCategory() {
        int categoryId = 1000;
        Response response = delete("/categories/" + categoryId);
        assertEquals(404, response.getStatusCode());
    }

    @Test
    void testFetchCetegoryWithNonExistentFilter() {
        Response response = given()
                .queryParam("title", "non-existent-filter")
                .when()
                .get("/categories");

        assertEquals(200, response.getStatusCode());
        List<?> categories = response.jsonPath().getList("categories");
        assertTrue(categories.isEmpty());
    }

    @Test
    void testFetchHeadersForNonExistingCategory() {
        Response response = given()
                .pathParam("id", "non-existing-id")
                .when()
                .head("/categories/{id}");
    
        assertEquals(404, response.getStatusCode());
    }

    @Test
    void testFetchNonExistingCategory() {
        Response response = given()
                .pathParam("id", "non-existing-id")
                .when()
                .get("/categories/{id}");

        assertEquals(404, response.getStatusCode());
    }

}