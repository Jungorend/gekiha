(ns exceed.db.core-test
  (:require [clojure.test :refer :all]
            [exceed.db.core :refer [*db*] :as db]
            [java-time.pre-java8]
            [luminus-migrations.core :as migrations]
            [next.jdbc :as jdbc]
            [exceed.config :refer [env]]
            [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
      #'exceed.config/env
      #'exceed.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))
