(ns exceed.card.normals
  (:require [exceed.movement :refer [move get-space]]
            [exceed.input :refer [request-player-input]]))

(defn make-attackcard
  [name cost speed power range armor guard card-text boost-name boost-continuous? boost-cost boost-text]
  {
    :name name
    :cost cost
    :speed speed
    :power power
    :range range
    :armor armor
    :guard guard
    :card-text card-text
    :boost-name boost-name
    :boost-continuous? boost-continuous?
    :boost-cost boost-cost
    :boost-text boost-text
    })

(def normals
  ;; state in boost is not always used but may be useful for future things
  ;; will call all attacks and boosts each turn
  {:assault (make-attackcard
    "Assault"
    [:force 0] 5 4 [1 1] 0 0
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

    :cross (make-attackcard
      "Cross"
      [:force 0] 6 3 [1 1] 0 0
      (fn [state game active-player]
        (case state :after (move game active-player 3 :retreat)
          :else game))
      "Run"
      false
      [:force 0]
      (fn [state game active-player]
        (let [move-value (request-player-input active-player :number [0 3])]
          (move game active-player move-value :advance))))

    :grasp (make-attackcard
      "Grasp"
      [:force 0] 7 3 [1 1] 0 0
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
        (if (= state :placement)
          (assoc-in game [active-player :modifiers :power] (+ 2 (get-in game [active-player :modifiers :power])))
          game)))

    :dive (make-attackcard
      "Dive"
      [:force 0] 4 4 [1 1] 0 0
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
