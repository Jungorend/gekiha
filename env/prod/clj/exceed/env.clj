(ns exceed.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[exceed started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[exceed has shut down successfully]=-"))
   :middleware identity})
