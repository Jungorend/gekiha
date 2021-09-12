(ns exceed.game.cards.lookup
  (:require [exceed.game.cards.season-three]
           [exceed.game.cards.normals]))

(defn get-character-info
  [character]
  (case character
    :ryu exceed.game.cards.season-three/ryu
    :normal exceed.game.cards.normals/normals))

(defn get-card-info
  "This takes in a card from an area and returns its details"
  [character]
  (let [c (get-character-info (nth character 2))]
    ((nth character 3) (if (= :normal (nth character 2))
                         c
                         (:cards c)))))