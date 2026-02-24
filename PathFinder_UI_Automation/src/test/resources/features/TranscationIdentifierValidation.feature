Feature: PlatformIdentifier status and error message validation in Pathfinder

  Background:
    Given the user is on the Pathfinder application
    When User sign in with environment credentials

  Scenario: Validate PlatformIdentifier statuses and error message
    Then User validates TranscationIdentifier