-- :name create-account! :! :n
-- :doc creates a new user account with the provided keys
INSERT INTO users
(username, email, password)
VALUES (:username, :email, :password)

-- :name get-password-hash :? :1
-- :doc takes a `username` and returns the password hash from the account
SELECT password FROM users
WHERE username = :username