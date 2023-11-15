Feature: Get todo tasks
  As a user I want to be able to look at my tasks so I know what tasks I've yet to complete.

  Background:
    Given the Todo Manager API is running

  Scenario: normal flow - get all todo tasks
    When I send a GET request to "/todos"
    Then I should receive a response with status code 200
    And the response should contain a list of todos

  Scenario: alternative flow - get todo tasks not done yet
    When I send a GET request to "/todos" with filter "?doneStatus=false"
    Then I should receive a response with status code 200
    And the response should contain a list of todos

  Scenario: error flow - get all todos using an invalid endpoint
    When I send a GET request to an invalid endpoint "/invalid/todos"
    Then I should receive a response with status code 404
