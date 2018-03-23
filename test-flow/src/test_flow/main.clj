(ns test-flow.main
  (:gen-class)
  (:require [clojure.java.io :as io]
            [duct.core :as duct]))

(defn -main [& args]
  (let [config (-> "test_flow/config.edn" io/resource duct/read-config)
        keys (-> args duct/parse-keys seq)]
    (duct/load-hierarchy)
    (if keys
      (-> config (duct/prep keys) (duct/exec keys))
      (-> config (duct/prep keys) duct/exec))))
