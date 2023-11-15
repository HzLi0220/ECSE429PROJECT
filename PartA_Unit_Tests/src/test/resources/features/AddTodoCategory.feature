Feature: Add Todo to Category
  As a user, I want to add todos to a specific category
  so that I can organize my tasks within categories effectively.

  Background:
    Given the Todo Manager API is running

  Scenario: Normal Flow - Successfully add a todo to a category
    When the user sends a "POST" request "/categories/{id}/todos" to todo "1" to category "1"
    Then I should receive a response with status code 201

  Scenario: Alternate Flow - Add a todo to a non-existent category
    When the user sends a "POST" request "/categories/{id}/todos" to todo "1" to category "999"
    Then I should receive a response with status code 404

  Scenario: Error Flow - Add a non-existent todo to a category
    When the user sends a "POST" request "/categories/{id}/todos" to todo "999" to category "1"
    Then I should receive a response with status code 404