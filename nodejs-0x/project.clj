(defproject nodejs-0x "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [com.cemerick/piggieback "0.1.3"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-npm "0.6.2"]
            [lein-doo "0.1.8"]]

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :source-paths ["src"]

  :npm {:dependencies [[web3 "0.19.0"]
                       ["@0xproject/utils" "0.5.2"]
                       ["0x.js" "4.0.9"]]
        :devDependencies [[ws "2.0.1"]]}

  :cljsbuild {
    :builds [{:source-paths ["src"]
              :compiler {
                :target :nodejs
                :output-to "nodejs_0x.js"
                :optimizations :simple}}]})
