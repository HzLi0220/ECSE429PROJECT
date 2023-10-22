import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.delete;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;
// For ramdom order tests
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.Random;

@TestMethodOrder(Random.class)
public class CategoryTest {
    private int categoryId;
    private boolean categoryDeleted = false;

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
        Map<String, String> categoryData = new HashMap<>();
        categoryData.put("title", "Test Category");
        categoryData.put("description", "Description of the test category");

        Response response = given()
                .contentType("application/json")
                .body(categoryData)
                .when()
                .post("/categories");
        assertEquals(201, response.getStatusCode());
        categoryId = response.jsonPath().getInt("id");
        System.out.println("Set Up category with ID " + categoryId);
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


// ------------------------------ /categories/:id -------------------------------

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
                .queryParam("title", "test filtered categories")
                .when()
                .get("/categories");
        assertEquals(200, response.getStatusCode());
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));

    }

    // --------------------- /categories/:id/todos --------------------

    @Test
    void testGetCategoryById() {
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .get("/categories/" + categoryId);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));
        String responseBody = response.getBody().asString();

        // Expected title and description
        String expectedTitle = "Test Category";
        String expectedDescription = "Description of the test category";

        JsonPath jsonPath = new JsonPath(responseBody);
        String actualTitle = jsonPath.getString("categories[0].title");
        String actualDescription = jsonPath.getString("categories[0].description");

        // Compare the actual and expected values
        assertEquals(expectedTitle, actualTitle);
        assertEquals(expectedDescription, actualDescription);

    }

    @Test
    public void testGetCategoryByIdJSON() {
        Response response = given()
                .header("Accept", "application/json")
                .get("/categories/" + categoryId);
        assertEquals(200, response.getStatusCode());
        assertEquals("application/json", response.getHeader("Content-Type"));
    }

    @Test
    public void testGetCategoryByIdXML() {
        Response response = given()
                .header("Accept", "application/xml")
                .get("/categories/" + categoryId);
        assertEquals(200, response.getStatusCode());
        assertEquals("application/xml", response.getHeader("Content-Type"));
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
        String requestBody = "{\"title\": \"UpdatedTitle\", \"description\": \"UpdatedDescription\"}";
        Response response = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/categories/" + categoryId);
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 201);
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));

    }

    @Test
    void testPutCategoryById() {
        String requestBody = "{\"title\": \"UpdatedTitle again\", \"description\": \"UpdatedDescription again\"}";
        Response response = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/categories/" + categoryId);
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 204);
        assertTrue(response.contentType().contains(ContentType.JSON.toString()));

    }

    @Test
    void testDeleteCategoryById() {
        Response response = given()
                .pathParam("id", categoryId)
                .when()
                .delete("/categories/{id}");
        categoryDeleted = true;
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 204);
        assertEquals("", response.getBody().asString());

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

    // ------ The endpoints below will be tested during the interoperability testing ------
    // /categories/:id/todos/:id
    // POST /categories/:id/projects
    // /categories/:id/projects/:id

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

}