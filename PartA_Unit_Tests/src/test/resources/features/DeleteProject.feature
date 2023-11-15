Feature: Delete Project
  As a user, I want to delete a project to remove it from my list.

  Background: 
    Given the Todo Manager API is running

  # Normal Flow
  Scenario: Successfully delete an existing defualt project. (Note API always start with defalt state with defualt objects)
    When I send a DELETE request with ID "1"
    Then I should receive a response with status code 200
    And the project with name "Office Work" should no longer exist

  # Alternative Flow
  Scenario: Deleting a newly created project
    Given a project with the name "New Project to Be Deleted" already exists
    When I send a DELETE request to newly created project
    Then I should receive a response with status code 200
    And the project with name "inactive_id" should no longer exist

  # Error Flow
  Scenario: Attempt to delete a non-existing project
    When I send a DELETE request with ID "123"
    Then I should receive a response with status code 404
    And the response should contain the error message "Could not find any instances with projects/123"
