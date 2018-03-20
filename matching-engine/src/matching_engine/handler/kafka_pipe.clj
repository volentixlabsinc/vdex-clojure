(ns matching-engine.handler.kafka-pipe
  (:require
    [duct.logger :refer [log]]
    [integrant.core :as ig])
  (:import
    [org.apache.kafka.streams KafkaStreams StreamsConfig StreamsBuilder]
    [org.apache.kafka.streams.kstream KStreamBuilder ValueMapper]
    org.apache.kafka.common.serialization.Serdes))


(def default-props
  {StreamsConfig/APPLICATION_ID_CONFIG "matching-engine"
   StreamsConfig/KEY_SERDE_CLASS_CONFIG (.getName (.getClass (Serdes/String)))
   StreamsConfig/VALUE_SERDE_CLASS_CONFIG (.getName (.getClass (Serdes/String)))})


(def streams-atom (atom nil))


(defmethod ig/init-key :matching-engine.handler/kafka-pipe 
  [_ config]
  (println "Starting Kafka orderbook -> orderflow pipe.")
  (let [props (merge default-props {StreamsConfig/BOOTSTRAP_SERVERS_CONFIG (:kafka-host config)})
        kafka-config (StreamsConfig. props)
        builder (StreamsBuilder.)
        _ (-> (.stream builder "orderbook") (.to "orderflow"))
        streams (KafkaStreams. (.build builder) kafka-config)]
    (try
      (reset! streams-atom (.start streams))
      (catch Exception e 
        (log (:logger config) :error e)
        (reset! streams-atom nil)
        (ig/init-key :matching-engine.handler/kafka-pipe config)))))


(defmethod ig/halt-key! :matching-engine.handler/kafka-pipe 
  [_ config]
  (reset! streams-atom nil))
