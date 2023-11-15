Feature: Update category
  As a user, I want to update the details of a specific category using its ID to keep information accurate.

  Background:
    Given the Todo Manager API is running

  Scenario Outline: Normal flow - Replace an existing category
    When I send a PUT request with ID "<categoryId>" and request body containing new title "<newTitle>" and new description "<newDescription>"
    Then I should receive a response with status code 200

    Examples:
      | categoryId | newTitle       | newDescription    |
      | 1          | Gadgets         | Modern devices     |
      | 2          | Home Decor      | Stylish interiors  |

  Scenario Outline: Alternate flow - Replace an existing category after modification
    When I send a PUT request with ID "<categoryId>" and request body containing modified title "<modifiedTitle>" and new description "<newDescription>"
    Then I should receive a response with status code 200

    Examples:
      | categoryId | modifiedTitle   | newDescription    |
      | 1          | Updated Gadgets | Advanced devices   |
      | 2          | Modern Decor     | Elegant interiors  |

  Scenario Outline: Error flow - Replace an existing category with invalid ID
    When I send a PUT request with ID "<InvalidCategoryId>" and request body containing new title "<newTitle>" and new description "<newDescription>"
    Then I should receive a response with status code 404

    Examples:
      | InvalidCategoryId | newTitle          | newDescription        |
      | 999               | Outdoor Gadgets   | Adventure devices     |
      | 500               | Home Furnishings   | Elegant interiors     |
