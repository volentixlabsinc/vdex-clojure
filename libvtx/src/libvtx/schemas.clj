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
