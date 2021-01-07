-- users

-- :name create-users-table
-- :command :execute
-- :result :raw
-- :doc Create users table
create table users (email blob,
                    password blob,
                    uid string)

-- :name get-user :?
-- :result :1
select email, password, uid from users where email = :email;

-- :name get-user-by-uid :?
-- :result :1
select email, password, uid from users where uid = :uid;

-- :name insert-user :! :n
insert into users (email, password, uid) values (:email, :password, :uid);

-- :name update-password :! :n
update users set password = :password where uid = :uid;
