(ns test-flow.simulator.schedule
  (:require
   [clojure.data.generators :as gen]
   [clojure.pprint  :as pp]
   [clojure.edn     :as edn]
   [clojure.java.io :as io]
   [clojure.pprint  :as pp]
   [clj-http.client :as http]
   [datomic.api     :as datomic]
   [simulant.sim    :as sim]
   [simulant.util   :as simu]))

(def model-ident :vdex/model)
(def test-ident  :vdex/test)

(def model-type :model.type/vdex)
(def test-type :test.type/vdex)

(defn create-model! [conn]
  (let [model-id (datomic/tempid :model)
        model {:model/type model-type
               :model/users-num 100
               :model/seconds-between-orders 100
               :db/ident model-ident}]
    (-> @(datomic/transact conn [(assoc model :db/id model-id)])
        (simu/tx-ent model-id))))

(defn get-model [conn]
  (-> (datomic/db conn)
      (datomic/entity model-ident)
      datomic/touch))

(defn create-users!
  "Returns users ids sorted"
  [conn test]
  (let [model (-> test :model/_tests simu/solo)
        ids (repeatedly (:model/users-num model) #(datomic/tempid :test))
        txresult (->> ids
                      (map (fn [id] {:db/id id
                                     :agent/type :agent.type/user
                                     :test/_agents (simu/e test)}))
                      (datomic/transact conn))]
    (simu/tx-entids @txresult ids)))

(defn create-test!
  "Returns test entity"
  [conn model test]
  (simu/require-keys test :db/id :test/duration)
  (-> @(datomic/transact conn
                         [(assoc test
                                 :test/type test-type
                                 :db/ident test-ident
                                 :model/_tests (simu/e model))])
      (simu/tx-ent (:db/id test))))

(defn generate-order
  "Generate a order request from user, based on the model"
  [test from-user users at-time]
  (let [model (-> test :model/_tests first)]
    [[{:db/id (datomic/tempid :test)
       :agent/_actions     (simu/e from-user)
       :action/atTime      at-time
       :action/type        :action.type/order
       :order/from         (simu/e from-user)
       :order/requested-at at-time}]]))

(defn generate-user-orders
  "Generate all actions for user, based on model"
  [test from-user users]
  (let [model (-> test :model/_tests first)
        limit (:test/duration test)
        ;; step #(random-long (fmap (partial * 1000) (:seconds-between-orders model)))
        step #(gen/geometric (/ 1 (* 1000 (:model/seconds-between-orders model))))]
    (->> (reductions + (repeatedly step))
         (take-while (fn [t] (< t limit)))
         (mapcat #(generate-order test from-user users %)))))

(defn generate-all-orders
  [test users]
  (mapcat
   (fn [from-user] (generate-user-orders test from-user users))
   users))

(defmethod sim/create-test :model.type/vdex
  [conn model test]
  (let [test (create-test! conn model test)
        users (create-users! conn test)]
    (simu/transact-batch conn (generate-all-orders test users) 1000)
    (datomic/entity (datomic/db conn) (simu/e test))))

(defmethod sim/create-sim :test.type/vdex
  [conn test sim]
  (-> @(datomic/transact conn (sim/construct-basic-sim test sim))
      (simu/tx-ent (:db/id sim))))

(defn create-action-log! [conn simulation]
  (sim/create-action-log conn simulation))

(defn pruns-sim
  "Returns vector of simulation processes to run"
  [sim-db-uri simulation]
  (->> #(sim/run-sim-process sim-db-uri (:db/id simulation))
       (repeatedly (:sim/processCount simulation))
       (into [])))

(defn wait-2-finish-simulation
  "Run simulation processess and wait to finish"
  [pruns]
  (time
    (mapv (fn [prun] @(:runner prun)) pruns)))

(def ping-url "http://127.0.0.1:5000/")

(defmethod sim/perform-action :action.type/order
  [action process]
  (let [_ (println "Fire order")
        simulation (-> process :sim/_processes simu/only)
        action-log (simu/getx sim/*services* :simulant.sim/actionLog)
        before-nano (System/nanoTime)
        before-millis (System/currentTimeMillis)
        job-uuid (datomic/squuid)]
    (-> (http/get ping-url {}) :status println)
    (action-log [{:actionLog/nsec (- (System/nanoTime) before-nano)
                  :db/id (datomic/tempid :db.part/user)
                  :actionLog/sim (simu/e simulation)
                  :actionLog/action (simu/e action)
                  :order/requested-at before-millis
                  :order/uuid job-uuid}])))

(defn transaction-times
  "Given a Datomic db snapshot and an entity id, returns the times associated with
  all transactions affecting the entity."
  [db eid]
  (->> (datomic/q '[:find ?instant
                    :in $ ?e
                    :where
                    [?e _ _ ?tx]
                    [?tx :db/txInstant ?instant]]
                  (datomic/history db) eid)
       (map first)
       (sort)))

(defn created-at
  "Given a Datomic db snapshot and an entity id, returns the time when the entity
  was first created (first transaction)."
  [db eid]
  (first (transaction-times db eid)))

(defn- format-schedule
  "Prepares a simulation id result from query for output via print-table"
  [db [id duration num-users]]
  {"ID" id
   "Duration" (str (/ duration 1000) " secs")
   "Num Users" num-users
   "Created" (created-at db id)})

(defn list-order-schedules
  [db]
  (println "== Simulation: available order schedules ==")
  (pp/print-table (map (partial format-schedule db)
                        (datomic/q '[:find ?e ?dur (count ?agt)
                                    :where [?e]
                                    [?e :test/agents ?agt]
                                    [?e :test/duration ?dur]
                                    [?e :test/type :test.type/vdex]]
                            db))))
