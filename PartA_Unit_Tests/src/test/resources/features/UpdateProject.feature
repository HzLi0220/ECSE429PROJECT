Feature: Update Project Details
  As a user, I want to update a project so that I can correct or change its details.

  Background: 
    Given the Todo Manager API is running

  Scenario Outline: Successfully update a project
    When I send a PUT request to "/projects/:id" with ID "<id>", new name "<new_name>" and description "<new_description>"
    Then I should receive a response with status code 200
    And the response should contain a project with name "<new_name>" and description "<new_description>"

    Examples:
      | id | new_name  | new_description      |
      | 1  | New Proj1 | Updated Description1 |
      | 1  | New Proj2 | Updated Description2 |

  Scenario: Update a non-existing project
    When I send a PUT request to "/projects/:id" with ID "9999", new name "Non-Existing" and description "Does not exist"
    Then I should receive a response with status code 404
    And the response should contain the error message "Invalid GUID for 9999 entity project"

  Scenario: Update a project with string value of a boolean
    When I send a PUT request to "/projects/:id" with ID "1" with active "True"
    Then I should receive a response with status code 400
    And the response should contain the error message "Expected BEGIN_OBJECT but was STRING"
