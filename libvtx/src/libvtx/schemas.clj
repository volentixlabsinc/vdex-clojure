(ns libvtx.schemas
  (:require
    [bouncer.validators :as validators]))


(def transaction-schema
  {:from-address [validators/required]
   :to-address [validators/required]
   :amount [validators/required]
   :token-address [validators/required]})


(def receive-schema
  {:address [validators/required]})


(def token-schema
  {:address [validators/required]
   :name [validators/required]
   :precision [validators/required] })


(def transaction-confirmation-schema
  {:transaction-id [validators/required]})


(def check-deposit-schema
  {:address [validators/required]
   :token-address [validators/required]})


(def deposit-schema
  (merge check-deposit-schema {:receiver-address [validators/required]}))
