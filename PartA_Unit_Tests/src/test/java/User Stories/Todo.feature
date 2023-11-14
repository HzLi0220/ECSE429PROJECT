Feature: Todo task list management

    Background:
      Given server is running
      And a task with known ID

    Scenario: User creates new todo task
      When I try to create a new todo task
      Then I should see a success message