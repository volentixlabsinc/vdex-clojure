(ns libvtx.transform
  (:require
   [clojure.walk :refer [postwalk]]
   [camel-snake-kebab.core :refer [->kebab-case ->snake_case]]))

(defn- format-hashmap-key
  "Formats a given key value pair by a given `format-fn` and returns as a vector."
  [format-fn [k v]]
  (if (or (string? k)
          (keyword? k))
    [(format-fn k) v]
    [k v]))

(defn- format-keys
  "Maps a format function onto a hash-map keys"
  [format-fn data]
  (let [f (partial format-hashmap-key format-fn)]
    (postwalk (fn [x]
                (if (map? x)
                  (into {} (map f x))
                  x))
              data)))

(def ->kebab (partial format-keys ->kebab-case))

(def ->snake (partial format-keys ->snake_case))
