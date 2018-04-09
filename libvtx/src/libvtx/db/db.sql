-- :name get-tokens :?
select * from tokens

-- :name create-token :! :n
insert into tokens (address, name, precision) values (:address, :name, :precision)

-- :name get-token :? :1
select * from tokens t where t.address = :address

-- :name create-transaction :!
insert into transactions (from_address, to_address, amount, token_address
--~ (when (contains? params :message) ", message")
--~ (when (contains? params :created-at) ", created_at")
) values (:from-address, :to-address, :amount, :token-address
--~ (when (contains? params :message) ", :message")
--~ (when (contains? params :created-at) ", :created-at")
)

-- :name get-transaction-by-params :? :1
select * from transactions where from_address = :from-address and to_address = :to-address and amount = :amount and token_address = :token-address

-- :name get-transactions-by-address
select * from transactions where to_address = :address
--~ (when (contains? params :token-address) " and token_address = :token-address")
order by created_at

-- :name remove-transaction-from-mempool :!
update transactions set mempool = 0, confirmed_at = current_timestamp where id = :id

-- :name get-balance-by-address
select * from balances where address = :address
--~ (when (contains? params :token-address) " and token_address = :token-address")

-- :name create-balance :!
insert into balances (address, token_address, balance) values (:address, 
--~ (if (:token-address params) ":token-address, " "null, ") 	
:balance)

-- :name update-balance :!
update balances set balance = :balance where address = :address and token_address = :token-address 

-- :name create-pair :!
insert into pairs (p1, p2, name) values (:address, :pair, :pair-name)

-- :name get-pair-name :? :1
select name from pairs where p1 = :address and p2 = :pair-address
