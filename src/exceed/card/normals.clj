(ns exceed.card.normals
  (:require [exceed.movement :refer [move get-space]]
            [exceed.input :refer [request-player-input]]))

(defrecord AttackCard [
  name
  cost ;; [type value] /:force or /:gauge
  speed
  power
  range ;; [min max]
  card-text ;; function, pass in current game state /(:before), game, active player, get prizes
  boost-name
  boost-continuous? ;; boolean
  boost-cost ;; [type value]
  boost-text
  ])

(def normals
  ;; state in boost is not always used but may be useful for future things
  ;; will call all attacks and boosts each turn
  {:assault (AttackCard.
    "Assault"
    [:force 0] 5 4 [1 1]
    (fn [state game active-player]
      (case state :before (move game active-player 2 :close)
        :hit (assoc game :next-player active-player)
        :else game))
    "Backstep"
    false
    [:force 0]
    (fn [state game active-player]
      (let [move-value (request-player-input active-player :number [0 4])]
        (move game active-player move-value :retreat))))

    :cross (AttackCard.
      "Cross"
      [:force 0] 6 3 [1 1]
      (fn [state game active-player]
        (case state :after (move game active-player 3 :retreat)
          :else game))
      "Run"
      false
      [:force 0]
      (fn [state game active-player]
        (let [move-value (request-player-input active-player :number [0 3])]
          (move game active-player move-value :advance))))

    :grasp (AttackCard.
      "Grasp"
      [:force 0] 7 3 [1 1]
      (fn [state game active-player]
        (case state :hit (let [receiving-player (if (= :p1 active-player) :p2 :p1)]
            (if (get-in game [:receiving-player :status :can-be-pushed])
              (move game receiving-player (request-player-input active-player :number [-2 2]) :advance)
              game)
              :else game)))
      "Fierce"
      true
      [:force 0]
      (fn [state game active-player]
        (assoc-in game [:modifiers :power] (+ 2 (get-in game [:modifiers :power])))))

    :dive (AttackCard.
      "Dive"
      [:force 0] 4 4 [1 1]
      (fn [state game active-player]
        (case state :before (let [new-movement (move game active-player 3 :advance)
                                  original-space (get-space [active-player (get-in game [active-player :character])] (:play-area game))
                                  new-space      (get-space [active-player (get-in new-movement [active-player :character])] (:play-area new-movement))
                                  opponent (if (= :p1 active-player) :p2 :p1)
                                  opponent-space (get-space [opponent (get-in game [opponent :character])] (:play-area game))]
            (if (or (and (< original-space opponent-space) (> new-space opponent-space))
                    (and (> original-space opponent-space) (< new-space opponent-space)))
                (assoc-in new-movement [:opponent :status :can-hit] false)
                new-movement))))
      "Tech"
      false
      [:force 0]
      (fn [state game active-player] ;; TODO: Implement Tech
        game))
      })
