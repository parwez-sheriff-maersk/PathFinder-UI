@ALL
Feature: Advanced Search validation in Pathfinder

  Background:
    Given the user is on the Pathfinder application
    When User sign in with environment credentials

  Scenario: Validate status using Advanced Search with Transaction ID and Platform ID from DB
    When User sign in and navigates to Trace Table for Advanced Search
    Then User validates Advanced Search for all DB records
