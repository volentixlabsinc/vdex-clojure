(ns snapshot-maker.main
  (:gen-class)
  (:require [clojure.java.io :as io]
            [duct.core :as duct]))

(duct/load-hierarchy)

(defn -main [& args]
  (let [keys (or (duct/parse-keys args) [:duct/daemon])]
    (-> (duct/read-config (io/resource "snapshot_maker/config.edn"))
        (duct/prep keys)
        (duct/exec keys))))
