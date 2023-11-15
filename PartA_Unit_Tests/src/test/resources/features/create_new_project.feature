Feature: Create New Project

As a user, I want to create a new project so that I can manage a new set of tasks.

  Background: 
    Given the Todo Manager API is running

  # Normal Flow
  Scenario Outline: Successfully creating a new project
    When I send a POST request to "/projects" with name "<name>" and description "<description>"
    Then I should receive a response with status code 201
    And the response should contain a project with name "<name>" and description "<description>"

    Examples:
      | name        | description       |
      | Project A   | A's Description   |
      | Project B   | B's Description   |

  # Alternative flow, creating this duplicate project with the same name will be allowed as the ID is the unique field
  Scenario: Alternate Flow - Creating a project with an existing name
    Given a project with the name "Existing Project" already exists
    When I send a POST request to "/projects" with name "Existing Project" and description "Duplicate project description"
    Then I should receive a response with status code 201
    And the response should contain a project with name "Existing Project" and description "Duplicate project description"

  # Error Flow
  Scenario: Attempt to create a project with a specified ID
    When I send a POST request to "/projects" with ID "12345" and name "New Project" and description "New Description"
    Then I should receive a response with status code 400
    And the response should contain the error message "Invalid Creation: Failed Validation: Not allowed to create with id"


