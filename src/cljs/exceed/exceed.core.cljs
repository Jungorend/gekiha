(ns exceed.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]))

(def game-state (r/atom [[:p "Player boosted Backstep."]
                         [:p "Opponent initiated a strike."]
                          [:hr] [:h3 {:class "strike"} "Strike!"]
                          [:p "Player had Cross."]
                          [:p "Opponent had EX-Block."]
                          [:p "Opponent spent 1 force on Block."]
                         [:hr] [:h3 {:class "turn"} "Turn 4"]
                          [:p "Player spent a Grasp from hand to move 1."]
                         [:hr] [:h3 {:class "turn"} "Turn 7"]]))

(defn add-history! [new-state]
  (swap! game-state (conj @game-state new-state)))

(defn display-history []
  (fn []
    (apply vector :ul @game-state)))

(dom/render
  [display-history] (.getElementById js/document "history-text"))