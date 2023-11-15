Feature: Add Todo to Project
  As a user, I want to add todos to a specific project
  so that I can organize my tasks within the project effectively.

  Background:
    Given the Todo Manager API is running

  Scenario: Normal Flow - Successfully add a todo to a project
    When the user sends a "POST" request "/categories/{id}/todos" to todo "1" to project "1"
    Then I should receive a response with status code 201

  Scenario: Alternate Flow - Add a todo to a non-existent project
    When the user sends a "POST" request "/categories/{id}/todos" to todo "1" to project "999"
    Then I should receive a response with status code 404

  Scenario: Error Flow - Add a non-existent todo to a project
    When the user sends a "POST" request "/categories/{id}/todos" to todo "999" to project "1"
    Then I should receive a response with status code 404