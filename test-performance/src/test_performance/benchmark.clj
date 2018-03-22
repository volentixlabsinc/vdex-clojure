(ns test-performance.benchmark
  (:require
    [integrant.core :as ig]
    [clj-time.core :as time]
    [clj-gatling.core   :as gatling]
    [org.httpkit.client :as http]
    [test-performance.simulations.simple :refer [simulations]]))

(defn- ramp-up-distribution [percentage-at _]
  (cond
    (< percentage-at 0.1) 0.1
    (< percentage-at 0.2) 0.2
    :else 1.0))

(defmethod ig/init-key :test-performance/benchmark [_ options]
  (let [simulation  (:ping simulations)
        concurrency  25
        requests-cnt 50]
    (gatling/run simulation
      {:skip-next-after-failure? false
       :concurrency concurrency
       :concurrency-distribution ramp-up-distribution
       :root "tmp"
       ;; :reporter {:writer (fn [_ _ _])
       ;;            :generator (fn [simulation]
       ;;                         (println "Ran" simulation "without report"))}
       :requests requests-cnt})))
