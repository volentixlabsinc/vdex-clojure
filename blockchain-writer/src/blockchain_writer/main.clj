(ns blockchain-writer.main
  (:gen-class)
  (:require [clojure.java.io :as io]
            [duct.core :as duct]))

(duct/load-hierarchy)

(defn -main [& args]
  (let [keys (duct/parse-keys args)
        config (duct/read-config (io/resource "blockchain_writer/config.edn"))]
    (if keys
      (-> config (duct/prep keys) (duct/exec keys))
      (-> config (duct/prep keys) duct/exec))))
