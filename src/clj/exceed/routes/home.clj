(ns exceed.routes.home
  (:require
   [exceed.layout :as layout]
   [clojure.java.io :as io]
   [exceed.middleware :as middleware]
   [exceed.game.core :as game]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn home-page [request]
  (layout/render request "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page [request]
  (layout/render request "about.html"))

(defn game-page [request]
  (layout/render request "game.html"))

(defn game-state [_]
  (response/ok (game/display-view @game/*game-list* :p1)))

(defn home-routes []
  [ "" 
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/game-state" game-state]
   ["/game" {:get game-page}]
   ["/about" {:get about-page}]])

