(ns exceed.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [exceed.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[exceed started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[exceed has shut down successfully]=-"))
   :middleware wrap-dev})
