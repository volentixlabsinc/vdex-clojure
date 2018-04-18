(defproject swap-tokens "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [duct/core "0.6.2"]
                 [duct/module.logging "0.3.1"]
                 [duct/module.web "0.6.3"]

                 ;; cljs stuff
                 [mount "0.1.11"]
                 #_[cljsjs/web3 "0.19.0-0"]
                 [cljs-web3 "0.19.0-0-11"] ;; cljs web3 wrapper
                 [cljsjs/zero-ex "0.21.0-0"]
                 #_[print-foo-cljs "2.0.3"]]

  :plugins [[duct/lein-duct "0.10.6"]
            [lein-cljsbuild "1.1.6"]]

  :main ^:skip-aot swap-tokens.main

  :source-paths ["src/clj"  "src/cljc"]
  :test-paths   ["test/clj" "test/cljc"]

  :resource-paths ["resources" "target/resources"]
  :prep-tasks     ["javac" "compile" ["run" ":duct/compiler"]]

  :npm {:dependencies [[web3 "0.19.0"]
                       [ganache-core "2.0.2"]]
        :devDependencies [[ws "2.0.1"]]}

  :profiles
  {:dev  [:project/dev :profiles/dev]
   :repl {:prep-tasks   ^:replace ["javac" "compile"]
          :repl-options {:init-ns user}}
   :uberjar {:aot :all}
   :profiles/dev {}
   :project/dev  {:source-paths   ["dev/src/clj"]
                  :resource-paths ["dev/resources"]
                  :dependencies   [[binaryage/devtools "0.8.3"]
                                   [integrant/repl "0.2.0"]
                                   [eftest "0.4.1"]
                                   [kerodon "0.9.0"]
                                   ;; cljs stuff
                                   [com.cemerick/piggieback "0.2.2"]
                                   [figwheel "0.5.14"]
                                   [figwheel-sidecar "0.5.14"]
                                   [binaryage/devtools "0.9.7"]
                                   [org.clojure/tools.nrepl "0.2.13"]]
                  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                  :plugins [[lein-figwheel "0.5.13"]
                            [lein-npm "0.6.2"]
                            [lein-doo "0.1.8"]]}}

  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src/cljs" "src/cljc" "dev/src/cljs"]
     :compiler {:main swap-tokens.dev
                :output-to "resources/swap_tokens/public/js/compiled/app/app.js"
                :output-dir "resources/swap_tokens/public/js/compiled/app/out"
                :asset-path "js/compiled/app/out"
                :source-map-timestamp true
                :preloads [devtools.preload]
                :external-config {:devtools/config {:features-to-install :all}}}}

    #_{:id "test"
     :source-paths ["src/cljs" "src/cljc" "test/cljs" "test/cljc"]
     :compiler {:main swap-tokens.runner
                :output-to "resources/swap_tokens/public/js/compiled/test.js"
                :output-dir "resources/swap_tokens/public/js/compiled/test/out"
                :optimizations :none
                ;workaround for running lein doo with latest CLJS, see https://github.com/bensu/doo/pull/141
                :process-shim false}}]})
