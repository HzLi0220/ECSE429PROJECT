Feature: Delete todo task
  As a user I want to delete a task from my todo list so I can cross it off if I no longer need to complete it.

  Background:
    Given the Todo Manager API is running

  Scenario Outline: normal flow - delete todo task
    When I send a DELETE request to "/todos/<ID>"
    Then I should receive a response with status code 200

    Examples:
      | ID |
      | 1  |
      | 2  |

  Scenario Outline: alternative flow - delete todo task after checking list of tasks
    When I send a GET request to "/todos"
    And  I send a DELETE request to "/todos/<ID>"
    Then I should receive a response with status code 200

    Examples:
      | ID |
      | 1  |
      | 2  |

  Scenario: error flow - delete todo task  with invalid ID
    When I send a DELETE request to "/todos" with invalid ID
    Then I should receive a response with status code 404
