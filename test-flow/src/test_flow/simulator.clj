(ns test-flow.simulator
  (:require
   [integrant.core :as ig]
   [datomic.api    :as datomic]
   [simulant.sim   :as sim]
   [test-flow.simulator.database :as db]
   [test-flow.simulator.schedule :as schedule]))

(defmethod ig/init-key :test-flow/simulator [_ options]
  (let [uri (str "datomic:mem://" (datomic/squuid))
        _  (db/setup-database! {:simulation-db-uri uri})
        sim-db (datomic/connect uri)
        flow-db (datomic/connect uri)
        model (schedule/create-model! sim-db)
        _ (println "== model ==")
        _ (println model)
        test (sim/create-test sim-db
                              model
                              {:db/id (datomic/tempid :test)
                               ;; :db/ident :vdex/test
                               ;; :source/codebase (:db/id codebase)
                               :test/duration 400000})
        _ (println "== test ==")
        _ (println test)
        simulation (sim/create-sim sim-db
                                   test
                                   {:db/id (datomic/tempid :sim)
                                    ;; :source/codebase (:db/id codebase)
                                    :sim/systemURI (str "datomic:free://localhost:4334/" (System/currentTimeMillis))
                                    :sim/processCount 10})
        _ (println "== simulation ==")
        _ (println simulation)
        _ (sim/create-action-log sim-db simulation)
        _ (sim/create-fixed-clock sim-db simulation {:clock/multiplier 960})
        _ (-> uri
              (schedule/pruns-sim simulation)
              schedule/wait-2-finish-simulation)
        ;; grab latest db version for query
        now-db (datomic/db sim-db)
        _ (schedule/list-order-schedules now-db)]
    ;; TODO: validate results of simulation
    ;; TODO: split schedule.clj into smaller parts
    {}))
