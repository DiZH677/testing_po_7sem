Feature: Two-factor authentication

Scenario: Successful two-factor authentication via email
  Given a user with email "island03@mail.ru" and password "password123" exists
  When the user logs in with email "island03@mail.ru" and password "password123"
  Then a two-factor authentication code is sent to "island03@mail.com"
  When the user enters the two-factor code received on email
  Then the user is successfully authenticated

Scenario: Successful password change after authentication
  Given a user with email "island03@mail.ru" and password "pass123" exists
  And the user is authenticated with "island03@mail.ru" and password "pass123" with two-factor authentication
  When the user "island03@mail.ru" requests to change the password from "pass123" to "pass567"
  And the user confirms the password change with email "island03@mail.ru" and two-factor code
  Then the user can log in with email "island03@mail.ru" and the new password "pass567"
  And the user cannot log in with email "island03@mail.ru" and the old password "pass123"