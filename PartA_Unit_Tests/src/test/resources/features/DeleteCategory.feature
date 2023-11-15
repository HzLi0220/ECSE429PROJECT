Feature: Delete Category by ID
  As a user, I want to delete a specific category by providing its ID to remove unnecessary information.

  Background:
    Given the Todo Manager API is running

  Scenario Outline: Normal flow - Delete an existing category by ID
    When I make a DELETE request with ID "<CategoryId>"
    Then I should receive a response with status code 200

    Examples:
      | CategoryId |
      | 1          |
      | 2          |

  Scenario Outline: Alternate flow - Delete an existing category by ID
    When I make a DELETE request with ID "<CategoryId>"
    Then I should receive a response with status code 200
    And I verify that the category with ID "<CategoryId>" no longer exists in the system by sending a GET request and receiving a response with status code 404

    Examples:
      | CategoryId |
      | 1          |
      | 2          |

  Scenario Outline: Error flow - Delete a category with an invalid ID
    When I make a DELETE request with ID "<InvalidCategoryId>"
    Then I should receive a response with status code 404

    Examples:
      | InvalidCategoryId |
      | 999               |
      | 500               |
