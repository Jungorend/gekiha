(defproject exceed "0.1.0"
  :description "Exceed Fighting System with rules-enforcement. Intended to be played in the browser."
  ;;:jvm-opts ["-Dclojure.server.repl={:port 5555 :accept clojure.core.server/repl}"]
  :url "http://yonayonayona.net"
  :license {:name "MIT License"}
  :dependencies [[ch.qos.logback/logback-classic "1.2.3"]
                 [cheshire "5.10.0"]
                 [clojure.java-time "0.3.2"]
                 [com.h2database/h2 "1.4.200"]
                 [conman "0.9.1"]
                 [cprop "0.1.17"]
                 [day8.re-frame/re-frame-10x "1.1.13"]
                 [expound "0.8.9"]
                 [funcool/struct "1.4.0"]
                 [luminus-http-kit "0.1.9"]
                 [luminus-migrations "0.7.1"]
                 [luminus-transit "0.1.2"]
                 [luminus-undertow "0.1.11"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [markdown-clj "1.10.5"]
                 [metosin/muuntaja "0.6.8"]
                 [metosin/reitit "0.5.10"]
                 [metosin/ring-http-response "0.9.2"]
                 [mount "0.1.16"]
                 [nrepl "0.8.3"]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.cli "1.0.206"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.webjars.npm/bulma "0.9.2"]
                 [org.webjars.npm/material-icons "0.7.0"]
                 [org.webjars/webjars-locator "0.41"]
                 [org.webjars/webjars-locator-jboss-vfs "0.1.0"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.9.3"]
                 [ring/ring-defaults "0.3.2"]
                 [selmer "1.12.40"]
                 ;; CLJS
                 [cljs-ajax "0.8.1"]
                 [org.clojure/clojurescript "1.10.764" :scope "provided"]
                 [re-frame "1.2.0"]
                 [reagent "1.0.0"]]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :test-paths ["test/clj"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main ^:skip-aot exceed.core

  :plugins [[lein-codox "0.10.7"]]
  :codox {
          :metadata {:doc/format :markdown}
          :source-uri "http://github.com/jungorend/gekiha/blob/v{version}/{filepath}#L{line}"}

  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "exceed.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn" ]
                  :dependencies [[pjstadig/humane-test-output "0.11.0"]
                                 [prone "2021-04-23"]
                                 [com.bhauman/figwheel-main "0.2.14"]
                                 [day8.re-frame/re-frame-10x "1.1.13"]
                                 [ring/ring-devel "1.9.3"]
                                 [ring/ring-mock "0.4.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.24.1"]
                                 [jonase/eastwood "0.3.5"]
                                 [cider/cider-nrepl "0.26.0"]]
                  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]}

                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user
                                 :timeout 120000}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn" ]
                  :resource-paths ["env/test/resources"] }
   :profiles/dev {}
   :profiles/test {}})
