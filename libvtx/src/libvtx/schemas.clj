(ns libvtx.schemas
  (:require
    [bouncer.validators :as validators]))


(def transaction-schema
  {:from [validators/required]
   :to [validators/required]
   :amount [validators/required]
   :token-address [validators/required]})
