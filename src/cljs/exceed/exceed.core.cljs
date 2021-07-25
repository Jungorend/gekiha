(ns exceed.core
    (:require [reagent.core :as r]
              [reagent.dom :as dom]))

(def game-state (r/atom ["<h1>Is state working?</h1>"]))

(defn add-history! [new-state]
    (swap! game-state (conj @game-state new-state)))

(defn display-history []
    (fn []
        [:div.history>div#history-text
         (first @game-state)]))