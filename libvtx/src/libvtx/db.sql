-- :name get-tokens :?
select * from tokens

-- :name create-token :! :n
insert into tokens (address, name, precision) values (:address, :name, 18)

-- :name get-token :? :1
select * from tokens t where t.address = :address



-- :name get-balances :?
select * from balances

-- :name create-balance :! :n
insert into balances (address, token_address, amount) values (:address, :token-address, 0)

-- :name get-account-token-balance :? :1
select * from balances b where b.address = :address and b.token_address = :token-address

-- :name get-account-balance :?
select * from balances b where b.address = :address



-- :name get-accounts :?
select * from accounts

-- :name create-account :! :n
insert into accounts (address) values (:address)

-- :name get-account :? :1
select * from accounts a where a.address = :address
