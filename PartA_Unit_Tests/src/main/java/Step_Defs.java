import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.given;


public class Step_Defs {

    private int todoId;
    private int statusCode;

    private final String TODO_TITLE = "Test Todo";
    private final String TODO_DESCRIPTION = "Description of the test todo";

    @Given("a task with known ID")
    public void aTaskWithKnownID() {

    }

    @When("I try to create a new todo task")
    public void iTryToCreateANewTodoTask() {
        Map<String, String> todoData = new HashMap<>();
        todoData.put("title", TODO_TITLE);
        todoData.put("description", TODO_DESCRIPTION);

        Response response = given()
                .contentType("application/json")
                .body(todoData)
                .when()
                .post("/todos");
        statusCode = response.getStatusCode();
        todoId = response.jsonPath().getInt("id");
        System.out.println("Attempting to create todo task");
    }

    @Then("I should see a success message")
    public void iShouldSeeASuccessMessage() {
        if (statusCode == 201) {
            System.out.println("Todo Task Successfully created with ID: " + todoId + ". Response Code: " + statusCode);
        } else {
            throw new RuntimeException("Error creating new Task. Response Code: " + statusCode);
        }
    }

    @Given("server is running")
    public void serverIsRunning() throws IOException {
        try {
            URL url = new URL("http://localhost:4567");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Server is running. Response Code: " + responseCode);
                RestAssured.baseURI = "http://localhost:4567";
                // Optionally, you can perform additional setup steps here if needed.
            } else {
                throw new RuntimeException("Server is not running. Response Code: " + responseCode);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error connecting to the server: " + e.getMessage());
        }
    }
}
