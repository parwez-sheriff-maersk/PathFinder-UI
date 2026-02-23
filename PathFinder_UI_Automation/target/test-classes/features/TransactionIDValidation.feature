Feature: Transaction ID status and error message validation in Pathfinder

  Background:
    Given the user is on the Pathfinder application
    When User sign in with environment credentials

  Scenario: Validate Transaction ID statuses and error message for a specific partner
    Then User validates transaction ID