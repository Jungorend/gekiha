(ns exceed.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [ajax.core :refer [GET POST]]))

(def game-state (r/atom {}))

(defn add-history! [new-state]
  (swap! game-state (conj @game-state new-state)))

(defn display-history []
  (fn []
    (apply vector :ul (:game-history @game-state))))

(defn get-gamestate []
  (GET "/game-state"
       {:headers       {"Accept" "application/transit+json"}
        :handler       #((do (reset! game-state %)
                             (dom/render [display-history] (.getElementById js/document "history-text"))))
        :error-handler #(.log js/console (str "Error: " %))}))

(get-gamestate)

