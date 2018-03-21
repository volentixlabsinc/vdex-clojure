(ns blockchain-writer.handler.kafka-consumer
  (:require
    [duct.logger :refer [log]]
    [integrant.core :as ig])
  (:import
    org.apache.kafka.clients.consumer.KafkaConsumer
    java.util.Properties))


(defn- create-properties
  [config]
  (doto (Properties.)
    (.put "group.id" "kafka-consumer")
    (.put "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer")   
    (.put "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer")
    (.put "bootstrap.servers" (:kafka-host config))))


(def consumer-atom (atom nil))


(defn- run-consumer
  []
  (while true
    (let [records (.poll @consumer-atom Long/MAX_VALUE)]
      (doseq [record records]
        (println (.value record))))))


(defmethod ig/init-key :blockchain-writer.handler/kafka-consumer
  [_ config]
  (let [props (create-properties config)]
    (try
      (reset! consumer-atom (KafkaConsumer. props))
      (.subscribe @consumer-atom ["orderflow"])
      (future (run-consumer))
      (catch Exception e 
        (log (:logger config) :error e)
        (reset! consumer-atom nil)
        (ig/init-key :blockchain-writer.handler/kafka-consumer config)))))


(defmethod ig/halt-key! :blockchain-writer.handler/kafka-consumer 
  [_ config]
  (reset! consumer-atom nil))
