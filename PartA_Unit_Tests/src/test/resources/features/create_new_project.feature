Feature: Create New Project

  Background: 
    Given the Todo Manager API is running

  Scenario Outline: Successfully creating a new project
    When I send a POST request to "/projects" with name "<name>" and description "<description>"
    Then I should receive a response with status code 201
    And the response should contain a project with name "<name>" and description "<description>"

    Examples:
      | name        | description       |
      | Project A   | A's Description   |
      | Project B   | B's Description   |