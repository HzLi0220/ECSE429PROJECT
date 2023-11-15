Feature: Add Category to Project
  As a user, I want to add categories to a specific project
  so that I can categorize and organize tasks within the project effectively.

  Background:
    Given the Todo Manager API is running

  Scenario: Normal Flow - Successfully add a category to a project
    When the user sends a "POST" request "/projects/{id}/categories" to category "1" to project "1"
    Then I should receive a response with status code 201

  Scenario: Alternate Flow - Add a category to a non-existent project
    When the user sends a "POST" request "/projects/{id}/categories" to category "1" to project "999"
    Then I should receive a response with status code 404

  Scenario: Error Flow - Add a non-existent category to a project
    When the user sends a "POST" request "/projects/{id}/categories" to category "999" to project "1"
    Then I should receive a response with status code 404