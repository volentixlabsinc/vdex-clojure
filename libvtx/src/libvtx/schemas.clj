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
  {:id [validators/required]})
