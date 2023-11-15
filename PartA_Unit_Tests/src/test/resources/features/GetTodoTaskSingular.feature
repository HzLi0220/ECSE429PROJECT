Feature: Get singular todo task
  As a user, I want to find a specific todo in my list to check its information.

  Background:
    Given the Todo Manager API is running

  Scenario Outline: normal flow - get todo task by ID
    When I send a GET request to "/todos/<ID>"
    Then I should receive a response with status code 200

    Examples:
      | ID |
      | 1  |
      | 2  |

  Scenario Outline: alternative flow - get todo task by filtering ID
    When I send a GET request to "/todos" with filter "?id=<ID>"
    Then I should receive a response with status code 200

    Examples:
      | ID |
      | 1  |
      | 2  |

  Scenario: error flow - get todo using an invalid id
    When I send a GET request to "/todos" with an invalid ID
    Then I should receive a response with status code 404
