-- :name get-tokens :?
select * from tokens

-- :name create-token :! :n
insert into tokens (address, name, precision) values (:address, :name, 18)

-- :name get-token :? :1
select * from tokens t where t.address = :address

-- :name create-transaction :<! :1
insert into transactions (from_account, to_account, amount, token_address, message, mempool) values (:from, :to, :amount, :token-address, :message, true)
