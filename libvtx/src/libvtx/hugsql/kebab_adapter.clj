(ns libvtx.hugsql.kebab-adapter
  (:gen-class)
  (:require
    [hugsql.adapter :as adapter]
    [clojure.java.jdbc :as jdbc]
    [hugsql.adapter.clojure-java-jdbc :refer [hugsql-adapter-clojure-java-jdbc]]
    [libvtx.transform :refer [->kebab]]))


(deftype KebabAdapter [jdbc-adapter]

  adapter/HugsqlAdapter
  (execute [this db sqlvec options]
    (adapter/execute jdbc-adapter db sqlvec options))

  (query [this db sqlvec options]
    (-> jdbc-adapter
        (adapter/query db sqlvec options)
        ->kebab))

  (result-one [this result options]
    (adapter/result-one jdbc-adapter result options))

  (result-many [this result options]
    (adapter/result-many jdbc-adapter result options))

  (result-affected [this result options]
    (adapter/result-affected jdbc-adapter result options))

  (result-raw [this result options]
    (adapter/result-raw jdbc-adapter result options))

  (on-exception [this exception]
    (throw exception)))

(defn kebab-adapter
  []
  (->KebabAdapter (hugsql-adapter-clojure-java-jdbc)))
