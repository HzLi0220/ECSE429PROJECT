Feature: List All Projects
  As a user, I want to list all projects to view my entire project list.

  Background: 
    Given the Todo Manager API is running

  # Normal Flow
  Scenario: Successfully list all projects
    Given a project with the name "Project A" already exists
    When I send a GET request to "/projects"
    Then I should receive a response with status code 200
    Then the response should contain a list of projects with "Project A"

  # Alternative Flow
  Scenario: List projects when there are no projects
    Given no project exists
    When I send a GET request to "/projects"
    Then I should receive a response with status code 200
    Then the response should contain an empty list of projects

  # Error Flow
  Scenario: Attempt to list projects from an invalid endpoint
    When I send a GET request to an invalid endpoint "/invalidProjects"
    Then I should receive a response with status code 404
