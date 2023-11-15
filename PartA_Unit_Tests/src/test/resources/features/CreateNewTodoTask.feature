Feature: Create New Todo
  As a user, I want to add a task to my todo list so I can remember to complete it.

  Background:
    Given the Todo Manager API is running

  Scenario Outline: Normal Flow - Successfully creating a new todo with title and description
    When I send a POST request to "/todos" with name "<name>" and description "<description>"
    Then I should receive a response with status code 201
    And the response should contain a todo task with name "<name>" and description "<description>"

    Examples:
      | name          | description       |
      | Todo task A   | A's Description   |
      | Todo task B   | B's Description   |

  Scenario Outline: Alternative Flow - Successfully creating a new todo with title only
    When I send a POST request to "/todos" with name "<name>" and description "<description>"
    Then I should receive a response with status code 201
    And the response should contain a todo task with name "<name>" and description "<description>"

    Examples:
      | name          | description       |
      | Todo task A   |                   |
      | Todo task B   |                   |

  Scenario Outline: Error Flow - Attempting creation of task with no title
    When I send a POST request to "/todos" with name "<name>" and description "<description>"
    Then I should receive a response with status code 400

    Examples:
      | name          | description       |
      |               | A's Description   |
      |               | B's Description   |