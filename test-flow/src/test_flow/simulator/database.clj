(ns test-flow.simulator.database
  (:require
    [clojure.java.io :as io]
    [datomic.api :as datomic]))

(defn recreate-database!
  "Given a Datomic database uri, deletes any existing database at the database,
  and creates a new one."
  [uri]
  (datomic/delete-database uri)
  (datomic/create-database uri))

(defn load-schema
  "Given a Datomic database connection and an IO resource location (e.g. filename),
  transacts the contents of the file."
  [conn resource]
  (let [m (-> resource io/resource slurp read-string)]
    (doseq [v (vals m)
            tx v]
      @(datomic/transact conn tx))))

(defn setup-database!
  "Given the settings, loads both the Simulant schema and extensions to support
  Simulator specifically into the database at :simulation-db-uri"
  [{:keys [simulation-db-uri] :as settings}]
  (prn "setting up the schema in ..." simulation-db-uri)
  (recreate-database! simulation-db-uri)
  (let [conn (datomic/connect simulation-db-uri)]
    (load-schema conn "simulant/schema.edn")
    (load-schema conn "test_flow/vdex-simulation.edn")
    (load-schema conn "test_flow/vdex-flow.edn")))
