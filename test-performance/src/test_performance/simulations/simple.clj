(ns test-performance.simulations.simple
  (:require
    [clojure.core.async :refer [chan go >!]]
    [org.httpkit.client :as http]))

(def base-url "http://127.0.0.1:5000")

(defn- http-get [url _]
  (let [response (chan)
        check-status (fn [{:keys [status body]}]
                       (go
                         (>! response (and (= 200 status)
                                           (= "Microservice" body)))))]
    (http/get (str base-url url) {} check-status)
    response))

(def ping
  (partial http-get "/"))

(def ping-simulation
  {:name "Microservice ping simulation"
   :scenarios [{:name "Ping scenario"
                :steps [{:name "Ping Endpoint" :request ping}]}]})

(def simulations
  {:ping ping-simulation})
