Feature: Delete Todo from Category
  As a user, I want to delete todos from a specific category
  so that I can organize my tasks within categories effectively.

  Background:
    Given the Todo Manager API is running
    Given the user sends a "POST" request "/categories/{id}/todos" to todo "1" to category "1"

  Scenario: Normal Flow - Successfully delete a todo from a category
    When the user sends a "DELETE" request "/categories/{id}/todos/{id2}" to todo "1" to category "1"
    Then I should receive a response with status code 200

  Scenario: Alternate Flow - Delete a todo from a non-existent category
    When the user sends a "DELETE" request "/categories/{id}/todos/{id2}" to todo "1" to category "999"
    Then I should receive a response with status code 400

  Scenario: Error Flow - Delete a non-existent todo from a category
    When the user sends a "DELETE" request "/categories/{id}/todos/{id2}" to todo "999" to category "1"
    Then I should receive a response with status code 404