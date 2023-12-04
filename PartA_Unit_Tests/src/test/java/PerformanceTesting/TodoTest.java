package PerformanceTesting;

import com.sun.management.OperatingSystemMXBean;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class TodoTest {

    private static final String BASE_URL = "http://localhost:4567";

    private Response createTodo(String title, String description) {
        Map<String, Object> todoData = new HashMap<>();
        todoData.put("title", title);
        todoData.put("description", description);

        return given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(todoData)
                .when()
                .post("/todos");
    }

    private Response updateTodo(int todoId, String title, String description) {
        Map<String, Object> todoData = new HashMap<>();
        todoData.put("title", title);
        todoData.put("description", description);

        return given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .pathParam("id", todoId)
                .body(todoData)
                .when()
                .post("/todos/{id}");
    }

    private Response deleteTodo(int todoId) {
        return given()
                .baseUri(BASE_URL)
                .pathParam("id", todoId)
                .when()
                .delete("/todos/{id}");
    }

    private void delay() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during delay", e);
        }
    }

    private Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Get memory usage
        Runtime runtime = Runtime.getRuntime();
        metrics.put("used_memory", (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0));

        // Get CPU usage
        OperatingSystemMXBean osMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        metrics.put("cpu_load", (osMXBean.getProcessCpuLoad() * 100));

        return metrics;
    }

    private void recordMetrics(FileWriter writer, int iteration, long timeToCreate, long timeToUpdate) {
        Map<String, Object> metrics = getSystemMetrics();
        try {
            writer.write(iteration + ", " + timeToCreate + ", " + timeToUpdate + ", " +
                    metrics.get("used_memory") + ", " + metrics.get("cpu_load") + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void performanceTest(int n) {
        List<Integer> todoIds = new ArrayList<>();
        try (FileWriter writer = new FileWriter("todoCreateUpdate.csv")) {
            writer.write("#n, time_to_create, time_to_update, used_memory, cpu_load\n");

            // Create and Update todos
            for (int i = 1; i <= n; i++) {
                // Create todo
                long startTime = System.nanoTime();
                Response createResponse = createTodo("Title " + i, "Description for " + i + "th object");
                long timeToCreate = System.nanoTime() - startTime;
                int todoId = createResponse.jsonPath().getInt("id");
                todoIds.add(todoId);
                delay();
                // Update todo
                startTime = System.nanoTime();
                updateTodo(todoId, "Updated Title for " + i + "th object", "Updated Description for " + i + "th object");
                long timeToUpdate = System.nanoTime() - startTime;

                // Store create and update times
//                writer.write(i + ", " + timeToCreate + ", " + timeToUpdate + "\n");
                recordMetrics(writer, i, timeToCreate, timeToUpdate);
                delay();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileWriter writer = new FileWriter("todoDelete.csv")) {
            writer.write("#n, time_to_delete, time_to_update(n/a), used_memory, cpu_load\n");

            // Delete todos and write metrics to CSV
            for (int i = 0; i < todoIds.size(); i++) {
                long startTime = System.nanoTime();
                deleteTodo(todoIds.get(i));
                long timeToDelete = System.nanoTime() - startTime;

                // Write all times to CSV
//                writer.write("" + (todoIds.size()-i) + ", "+ timeToDelete + "\n");
                recordMetrics(writer, todoIds.size()-i, timeToDelete, 0);
                delay();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TodoTest test = new TodoTest();
        int n = 1000;

        long startTime = System.nanoTime(); // Get the start time
        test.performanceTest(n);
        long endTime = System.nanoTime(); // Get the end time
    
        // Calculate the time taken in seconds
        double timeTaken = (endTime - startTime) / 1_000_000_000.0; // Convert nanoseconds to seconds
    
        System.out.println("Total time taken including delay to create " + n + " objects: " + timeTaken + " seconds");
    }
}