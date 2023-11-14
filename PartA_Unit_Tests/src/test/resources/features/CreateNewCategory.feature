Feature: Create Category
  As a user, I want to create a new category without specifying an ID to easily add new items to the system.

  Background:
    Given the Todo Manager API is running

  Scenario: Normal flow - Create new category with title and description
    When I send a POST request to "/categories" with the following details:
      | title              | description               |
      | New Category Title | Description for Category  |
    Then I should receive a response with status code 201
    And the response should contain the created category details

  Scenario: Alternate flow - Create new category with only title and no description
    When I send a POST request to "/categories" with the following details:
      | title              |
      | New Category Title |
    Then I should receive a response with status code 201
    And the response should contain the created category details

  Scenario: Error flow - Create new category with no title nor description
    When I send a POST request to "/categories" without a title
    Then I should receive a response with status code 400
