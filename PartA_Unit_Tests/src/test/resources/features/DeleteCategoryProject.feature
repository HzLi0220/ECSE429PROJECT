Feature: Delete Category from Project
  As a user, I want to delete categories from a specific project
  so that I can categorize and organize tasks within the project effectively.

  Background:
    Given the Todo Manager API is running
    Given the user sent a "POST" request "/projects/{id}/categories" to category "1" to project "1"

  Scenario: Normal Flow - Successfully delete a category from a project
    When the user sends a "DELETE" request "/projects/{id}/categories/{id2}" to category "1" to project "1"
    Then I should receive a response with status code 200

  Scenario: Alternate Flow - Delete a category from a non-existent project
    When the user sends a "DELETE" request "/projects/{id}/categories/{id2}" to category "1" to project "999"
    Then I should receive a response with status code 400

  Scenario: Error Flow - Delete a non-existent category from a project
    When the user sends a "DELETE" request "/projects/{id}/categories/{id2}" to category "999" to project "1"
    Then I should receive a response with status code 404