(ns dev
  (:refer-clojure :exclude [test])
  (:require [clojure.repl :refer :all]
            [fipp.edn :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [duct.core :as duct]
            [duct.core.repl :as duct-repl]
            [eftest.runner :as eftest]
            [figwheel-sidecar.repl-api :as figwheel-repl]
            [integrant.core :as ig]
            [integrant.repl :refer [clear halt go init prep reset]]
            [integrant.repl.state :refer [config system]]))

(def ui        figwheel-repl/start-figwheel!)
(def ui-stop   figwheel-repl/stop-figwheel!)
(def cljs-repl figwheel-repl/cljs-repl)

(duct/load-hierarchy)

(defn read-config []
  (duct/read-config (io/resource "dev.edn")))

(defn test []
  (eftest/run-tests (eftest/find-tests "test")))

(clojure.tools.namespace.repl/set-refresh-dirs "dev/src" "src" "test")

(when (io/resource "local.clj")
  (load "local"))

(integrant.repl/set-prep! (comp duct/prep read-config))

;; commands
(comment
  (dev)

  (go)

  (ui)

  (cljs-repl)

  (ui-stop)

  (halt)

  (reset)

)


(comment
  (require '[cljs-web3.core :refer [http-provider]])

  (require '[cljs.nodejs :as nodejs])

)
