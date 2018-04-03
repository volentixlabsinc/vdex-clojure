-- :name get-tokens :?
select * from tokens

-- :name create-token :! :n
insert into tokens (address, name, precision) values (:address, :name, 18)

-- :name get-token :? :1
select * from tokens t where t.address = :address
