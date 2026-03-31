Feature: Drone Delivery System

  Scenario: Registering a new account
    Given the account service is running
    When a user registers with username "alice" and password "secret"
    Then the account should be created successfully
    And I can get info for account "alice"

  Scenario: User logging in
    Given the dispatch service and account service are running
    And an account exists with username "bob" and password "password123"
    When user "bob" logs in with password "password123"
    Then the login is successful and a session ID is returned

  Scenario: Creating a shipping request
    Given all services are running
    And a user "charlie" is logged in with session ID "session-xyz"
    When the user requests a shipping from 10.0, 10.0 to 20.0, 20.0
    Then a shipping ID and assigned drone URI should be returned
