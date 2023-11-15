Feature: Retrieve Project Details

  As a user, I want to retrieve details of a specific project to understand its current state.

  Background: 
    Given the Todo Manager API is running

  # Normal Flow
  Scenario: Successfully retrieve project details
    Given a project with the name "Existing Project" already exists
    When I send a GET request to "/projects" with filter "Existing Project"
    Then I should receive a response with status code 200
    And the response should contain a project with name "Existing Project"

  # Alt Flow
  Scenario: Retrieve details of a non-existing project
    When I send a GET request to "/projects/9999"
    Then I should receive a response with status code 404
    And the response should contain the error message "Could not find an instance with projects/9999"

  # Error Flow, Actually the API should indicate the ID is Invalid instead of returning not found
  Scenario: Retrieve project details with invalid ID format
    When I send a GET request to "/projects/abc123"
    Then I should receive a response with status code 404
    And the response should contain the error message "Could not find an instance with projects/abc123"





