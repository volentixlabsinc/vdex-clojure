(defproject libvtx "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 ;; duct
                 [duct/core "0.6.2"]
                 [duct/module.logging "0.3.1"]
                 [duct/module.sql "0.4.0"]
                 [duct/module.web "0.6.4"]
                 [com.mjachimowicz/duct-migrations-auto-cfg "0.1.0"]
                 ;; ring
                 [ring/ring-json "0.4.0"]
                 ;; db
                 [org.xerial/sqlite-jdbc "3.21.0.1"]
                 [org.slf4j/slf4j-nop "1.7.25"]
                 [org.clojure/java.jdbc "0.7.5"]
                 [com.layerware/hugsql "0.4.8" :exclusions [org.clojure/java.jdbc]]
                 ;; Utils
                 [clj-time "0.14.3"]
                 [rop "0.2.1"]
                 [bouncer "1.0.1"]
                 [camel-snake-kebab "0.4.0"]
                 ;; scheduler
                 [tick "0.3.3"]]

  :plugins [[duct/lein-duct "0.10.6"]]
  :main ^:skip-aot libvtx.main
  :resource-paths ["resources" "target/resources"]
  :prep-tasks     ["javac" "compile" ["run" ":duct/compiler"]]
  :profiles
  {:dev  [:project/dev :profiles/dev]
   :repl {:prep-tasks   ^:replace ["javac" "compile"]
          :repl-options {:init-ns user}}
   :uberjar {:aot :all}
   :profiles/dev {}
   :project/dev  {:source-paths   ["dev/src"]
                  :resource-paths ["dev/resources"]
                  :dependencies   [[integrant/repl "0.2.0"]
                                   [eftest "0.4.1"]
                                   [kerodon "0.9.0"]]}})
