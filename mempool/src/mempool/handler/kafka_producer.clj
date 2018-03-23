(ns mempool.handler.kafka-producer
  (:require
    [duct.logger :refer [log]]
    [integrant.core :as ig])
  (:import
    [org.apache.kafka.clients.producer KafkaProducer ProducerRecord]
    java.util.Properties))


(defn- create-properties
  [config]
  (doto (Properties.)
    (.put "acks" "all")
    (.put "retries" "0")
    (.put "batch.size" "16348")
    (.put "linger.ms" "1")
    (.put "buffer.memory" "33554432")
    (.put "key.serializer" "org.apache.kafka.common.serialization.StringSerializer")   
    (.put "value.serializer" "org.apache.kafka.common.serialization.StringSerializer")
    (.put "bootstrap.servers" (:kafka-host config))))


(def producer-atom (atom nil))


(defn produce-message
  [config msg-key msg]
  (println (format "Sending message '%s' with key '%s' to orderbook topic." msg msg-key))
  (try
    (let [record (ProducerRecord. "orderbook" msg-key msg)]
      (.send @producer-atom record))
    (catch Exception e
      (log (:logger config) :error e)
      (reset! producer-atom nil)
      (ig/init-key :mempool.handler/kafka-producer config))))


(defmethod ig/init-key :mempool.handler/kafka-producer 
  [_ config]
  (let [props (create-properties config)]
    (try
      (reset! producer-atom (KafkaProducer. props))
      (catch Exception e 
        (log (:logger config) :error e)
        (reset! producer-atom nil)
        (ig/init-key :mempool.handler/kafka-producer config)))))


(defmethod ig/halt-key! :mempool.handler/kafka-producer 
  [_ config]
  (reset! producer-atom nil))
