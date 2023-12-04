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

public class CategoryTest {

    private static final String BASE_URL = "http://localhost:4567";

    private Response createCategory(String title, String description) {
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("title", title);
        categoryData.put("description", description);

        return given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(categoryData)
                .when()
                .post("/categories");
    }

    private Response updateCategory(int categoryId, String title, String description) {
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("title", title);
        categoryData.put("description", description);

        return given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .pathParam("id", categoryId)
                .body(categoryData)
                .when()
                .post("/categories/{id}");
    }

    private Response deleteCategory(int categoryId) {
        return given()
                .baseUri(BASE_URL)
                .pathParam("id", categoryId)
                .when()
                .delete("/categories/{id}");
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

    private void recordMetrics(FileWriter writer, int iteration, long timeToCreate, long timeToUpdate, long timeToDelete,
                               double createMemoryUsage, double updateMemoryUsage, double deleteMemoryUsage,
                               double createCpuUsage, double updateCpuUsage, double deleteCpuUsage) {
        try {
            writer.write(iteration + ", " + timeToCreate + ", " + timeToUpdate + ", " + timeToDelete + ", " +
                    createMemoryUsage + ", " + updateMemoryUsage + ", " + deleteMemoryUsage + ", " +
                    createCpuUsage + ", " + updateCpuUsage + ", " + deleteCpuUsage + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void performanceTest(int n) {
        List<Integer> categoryIds = new ArrayList<>();
        try (FileWriter writer = new FileWriter("categoryCreateUpdate.csv")) {
            writer.write("#n, time_to_create, time_to_update, time_to_delete, create_memory_usage, update_memory_usage, delete_memory_usage, create_cpu_usage, update_cpu_usage, delete_cpu_usage\n");
            // Create and Update categories
            for (int i = 1; i <= n; i++) {
                // Create category
                long startTime = System.nanoTime();
                Response createResponse = createCategory("Title " + i, "Description for " + i + "th object");
                long timeToCreate = System.nanoTime() - startTime;
                int categoryId = createResponse.jsonPath().getInt("id");
                categoryIds.add(categoryId);
                delay();

                // Record metrics after creating
                Map<String, Object> createMetrics = getSystemMetrics();
                double createMemoryUsage = (double) createMetrics.get("used_memory");
                double createCpuUsage = (double) createMetrics.get("cpu_load");

                // Update category
                startTime = System.nanoTime();
                updateCategory(categoryId, "Updated Title for " + i + "th object", "Updated Description for " + i + "th object");
                long timeToUpdate = System.nanoTime() - startTime;

                // Record metrics after updating
                Map<String, Object> updateMetrics = getSystemMetrics();
                double updateMemoryUsage = (double) updateMetrics.get("used_memory");
                double updateCpuUsage = (double) updateMetrics.get("cpu_load");

                // Store create and update times
//                writer.write(i + ", " + timeToCreate + ", " + timeToUpdate + "\n");
//                recordMetrics(writer, i, timeToCreate, timeToUpdate);
                // Record metrics
                recordMetrics(writer, i, timeToCreate, timeToUpdate, 0,
                        createMemoryUsage, updateMemoryUsage, 0,
                        createCpuUsage, updateCpuUsage, 0);
                delay();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileWriter writer = new FileWriter("categoryDelete.csv")) {
            writer.write("#n, time_to_create, time_to_update, time_to_delete, create_memory_usage, update_memory_usage, delete_memory_usage, create_cpu_usage, update_cpu_usage, delete_cpu_usage\n");

            // Delete categories and write metrics to CSV
            for (int i = 0; i < categoryIds.size(); i++) {
                long startTime = System.nanoTime();
                deleteCategory(categoryIds.get(i));
                long timeToDelete = System.nanoTime() - startTime;

                // Record metrics after deleting
                Map<String, Object> deleteMetrics = getSystemMetrics();
                double deleteMemoryUsage = (double) deleteMetrics.get("used_memory");
                double deleteCpuUsage = (double) deleteMetrics.get("cpu_load");

                // Write all times to CSV
//                writer.write("" + (categoryIds.size()-i) + ", "+ timeToDelete + "\n");
//                recordMetrics(writer, categoryIds.size()-i, timeToDelete, 0);
                // Record metrics
                recordMetrics(writer, categoryIds.size() - i, 0, 0, timeToDelete,
                        0, 0, deleteMemoryUsage,
                        0, 0, deleteCpuUsage);
                delay();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CategoryTest test = new CategoryTest();
        int n = 1000;

        long startTime = System.nanoTime(); // Get the start time
        test.performanceTest(n);
        long endTime = System.nanoTime(); // Get the end time
    
        // Calculate the time taken in seconds
        double timeTaken = (endTime - startTime) / 1_000_000_000.0; // Convert nanoseconds to seconds
    
        System.out.println("Total time taken to create " + n + " objects: " + timeTaken + " seconds");
    }
}