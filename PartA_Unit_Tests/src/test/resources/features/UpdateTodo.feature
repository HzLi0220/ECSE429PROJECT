Feature: Update todo task
  As a user, I want to update an existing task to change information.

  Background:
    Given the Todo Manager API is running

  Scenario Outline: normal flow - update todo task description
    When I send a PUT request to "/todos/<ID>" with name "<name>" and description "<description>"
    Then I should receive a response with status code 200
    And the response should contain a todo task with name "<name>" and description "<description>"

    Examples:
      | ID | name      | description            |
      | 1  | project A | project A updated desc |
      | 2  | project B | project B updated desc |

  Scenario Outline: alternative flow - update todo task description with POST
    When I send a POST request to "/todos/<ID>" with name "<name>" and description "<description>"
    Then I should receive a response with status code 200
    And the response should contain a todo task with name "<name>" and description "<description>"

    Examples:
      | ID | name      | description            |
      | 1  | project A | project A updated desc |
      | 2  | project B | project B updated desc |

  Scenario Outline: error flow - update todo task description with invalid ID
    When I send a PUT request to "/todos" with name "<name>" and description "<description>" and invalid ID
    Then I should receive a response with status code 404

    Examples:
      |name      | description            |
      |project A | project A updated desc |
      |project B | project B updated desc |