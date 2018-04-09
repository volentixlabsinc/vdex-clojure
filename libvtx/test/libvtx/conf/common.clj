(ns libvtx.conf.common
  (:require
    [duct.logger :as logger]))


(defrecord NoOpLogger []
  logger/Logger
  (-log [logger level ns-str file line id event data]))


(def api-config
  {:environment :testing
   :block-time 1
   :logger (->NoOpLogger)})
