(ns swap-tokens.main
  (:gen-class)
  (:require
    [clojure.java.io :as io]
    [duct.core :as duct]))


(defn -main
  "During compilation there is :duct/compiler passed in so that duct will make only compilation.
   During uberjar launch there no args passed in so that we force duct to launch migrations and web server"
  [& args]
  (let [config (-> "swap_tokens/config.edn" io/resource duct/read-config)
        keys (-> args duct/parse-keys seq)]
    (duct/load-hierarchy)
    (if keys
        (-> config (duct/prep keys) (duct/exec keys))
        (-> config (duct/prep keys) duct/exec))))
