(ns matching-engine.handler.kafka-pipe
  (:require
    [duct.logger :refer [log]]
    [integrant.core :as ig])
  (:import
    [org.apache.kafka.streams KafkaStreams StreamsConfig StreamsBuilder KeyValue]
    [org.apache.kafka.streams.kstream Materialized Produced KStreamBuilder KeyValueMapper ValueMapper]
    org.apache.kafka.streams.state.QueryableStoreTypes
    org.apache.kafka.common.serialization.Serdes))


(def default-props
  {StreamsConfig/APPLICATION_ID_CONFIG "matching-engine"
   StreamsConfig/KEY_SERDE_CLASS_CONFIG (.getName (.getClass (Serdes/String)))
   StreamsConfig/VALUE_SERDE_CLASS_CONFIG (.getName (.getClass (Serdes/String)))})


(def streams-atom (atom nil))


;Store needs to be initialized, otherwise throws exception
;(defn- wait-for-store
;  []
;  (loop [store-started? false]
;    (when-not 
;      (try
;        (.store @streams-atom "wordcount" (QueryableStoreTypes/keyValueStore))
;        (catch Exception e
;          (Thread/sleep 100)))
;      (recur false))))


(defmethod ig/init-key :matching-engine.handler/kafka-pipe 
  [_ config]
  (let [props (merge default-props {StreamsConfig/BOOTSTRAP_SERVERS_CONFIG (:kafka-host config)})
        kafka-config (StreamsConfig. props)
        builder (StreamsBuilder.)
        _ (-> (.stream builder "orderbook") 
              (.map (reify KeyValueMapper (apply [_ k v] 
                                            (println (format "Transforming value '%s'" v)) 
                                            (KeyValue. k (str "Transformed value of " v)))))
              (.to "orderflow"))]
;        create Kafka table, similar to stream, depends on methods called on builder
;        _ (-> (.stream builder "orderbook") 
;              (.flatMapValues (reify ValueMapper (apply [_ v]
;                                                   (clojure.string/split v #" "))))
;              (.groupBy (reify KeyValueMapper (apply [_ k v] v)))
;              (.count (Materialized/as "wordcount")))]
    (try
;      Describe Kafka stream/table topology
;      (println (.describe (.build builder)))
      (reset! streams-atom (KafkaStreams. (.build builder) kafka-config))
      (.start @streams-atom)
;      Querying Kafka table example
;      (wait-for-store)
;      (println (str "count " (.get (.store @streams-atom "wordcount" (QueryableStoreTypes/keyValueStore)) "foo")))
      (catch Exception e 
        (log (:logger config) :error e)
        (reset! streams-atom nil)
        (ig/init-key :matching-engine.handler/kafka-pipe config)))))


(defmethod ig/halt-key! :matching-engine.handler/kafka-pipe 
  [_ config]
  (reset! streams-atom nil))
