Feature: Get all categories

  Background:
    Given the Todo Manager API is running

  Scenario: normal flow - get all categories in json format
    When I send a GET request to "/categories" with Accept header "application/json"
    Then I should receive a response with status code 200
    And the response should contain a list of categories in JSON format

  Scenario: normal flow - get all categories in xml format
    When I send a GET request to "/categories" with Accept header "application/xml"
    Then I should receive a response with status code 200
    And the response should contain a list of categories in XML format

  Scenario: error flow: get all categories using an invalid endpoint
    When I send a GET request to an invalid endpoint "/invalid/categories"
    Then I should receive a response with status code 404
