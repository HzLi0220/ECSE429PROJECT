Feature: Get Category by ID
  As a user, I want to retrieve detailed information about a specific category by providing its ID for more in-depth insights.

  Background:
    Given the Todo Manager API is running

  Scenario Outline: Normal flow - Get category by ID in JSON format
    When I send a GET request to "/categories/{id}" with ID "<CategoryId>" and with Accept header "application/json"
    Then I should receive a response with status code 200
    And the response should contain the category details in JSON format

    Examples:
      | CategoryId |
      | 1          |
      | 2          |

  Scenario Outline: Alternate flow - Get category by ID in XML format
    When I send a GET request to "/categories/{id}" with ID "<CategoryId>" and with Accept header "application/xml"
    Then I should receive a response with status code 200
    And the response should contain the category details in XML format

    Examples:
      | CategoryId |
      | 1          |
      | 2          |

  Scenario Outline: Error flow - Get category by a non-existent ID
    When I send a GET request to "/categories/{id}" with ID "<CategoryId>"
    Then I should receive a response with status code 404

    Examples:
      | CategoryId |
      | 999        |
