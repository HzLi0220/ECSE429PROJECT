package PerformanceTesting;

import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class ProjectTest {

    private static final String BASE_URL = "http://localhost:4567";

    private Response createProject(String title, String description, boolean completed) {
        Map<String, Object> projectData = new HashMap<>();
        projectData.put("title", title);
        projectData.put("description", description);
        projectData.put("completed", completed);

        return given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(projectData)
                .when()
                .post("/projects");
    }

    private Response updateProject(int projectId, String title, String description, boolean completed) {
        Map<String, Object> projectData = new HashMap<>();
        projectData.put("title", title);
        projectData.put("description", description);
        projectData.put("completed", completed);

        return given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .pathParam("id", projectId)
                .body(projectData)
                .when()
                .post("/projects/{id}");
    }

    private Response deleteProject(int projectId) {
        return given()
                .baseUri(BASE_URL)
                .pathParam("id", projectId)
                .when()
                .delete("/projects/{id}");
    }

    private void delay() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during delay", e);
        }
    }

    public void performanceTest(int n) {
        List<Integer> projectIds = new ArrayList<>();
        try (FileWriter writer = new FileWriter("projectCreateUpdate.csv")) {
            writer.write("#n, time_to_create, time_to_update\n");

            // Create and Update Projects
            for (int i = 1; i <= n; i++) {
                // Create Project
                long startTime = System.nanoTime();
                Response createResponse = createProject("Title " + i, "Description for " + i + "th object", false);
                long timeToCreate = System.nanoTime() - startTime;
                int projectId = createResponse.jsonPath().getInt("id");
                projectIds.add(projectId);
                delay();
                // Update Project
                startTime = System.nanoTime();
                updateProject(projectId, "Updated Title for " + i + "th object", "Updated Description for " + i + "th object", true);
                long timeToUpdate = System.nanoTime() - startTime;

                // Store create and update times
                writer.write(i + ", " + timeToCreate + ", " + timeToUpdate + "\n");
                delay();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileWriter writer = new FileWriter("projectDelete.csv")) {
            writer.write("#n, time_to_delete\n");

            // Delete Projects and write metrics to CSV
            for (int i = 0; i < projectIds.size(); i++) {
                long startTime = System.nanoTime();
                deleteProject(projectIds.get(i));
                long timeToDelete = System.nanoTime() - startTime;

                // Write all times to CSV
                writer.write("" + (projectIds.size()-i) + ", "+ timeToDelete + "\n");
                delay();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ProjectTest test = new ProjectTest();
        int n = 10000;

        long startTime = System.nanoTime(); // Get the start time
        test.performanceTest(n);
        long endTime = System.nanoTime(); // Get the end time
    
        // Calculate the time taken in seconds
        double timeTaken = (endTime - startTime) / 1_000_000_000.0; // Convert nanoseconds to seconds
    
        System.out.println("Total time taken to create " + n + " objects: " + timeTaken + " seconds");
    }
}
