-- :name get-tokens :?
select * from tokens

-- :name create-token :! :n
insert into tokens (address, name, precision) values (:address, :name, 18)

-- :name get-token :? :1
select * from tokens t where t.address = :address

-- :name create-transaction :!
insert into transactions (from_address, to_address, amount, token_address
--~ (when (contains? params :message) ", message")
) values (:from-address, :to-address, :amount, :token-address
--~ (when (contains? params :message) ", :message")
)

-- :name get-transaction-by-params :? :1
select * from transactions where from_address = :from-address and to_address = :to-address and amount = :amount and token_address = :token-address

-- :name get-transactions-by-address
select * from transactions where to_address = :address
--~ (when (contains? params :token-address) " and token_address = :token-address")
order by created_at
