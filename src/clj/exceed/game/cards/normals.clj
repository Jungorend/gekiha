(ns exceed.game.cards.normals
  (:require [exceed.game.movement :refer [move get-space]]
            [exceed.game.input :refer [request-player-input]]))

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

(defmacro make-ability
  ([] {})
  ([state ability] `(hash-map ~state (fn [~'game ~'active-player] ~ability)))
  ([state ability & args] (if (seq (rest args))             ;; only one arg
                            `(conj (hash-map ~state (fn [~'game ~'active-player] ~ability))
                                   (make-ability ~@args))
                            nil)))

(def normals
  ;; state in boost is not always used but may be useful for future things
  ;; will call all attacks and boosts each turn
  {:assault (make-attackcard
              "Assault"
              [:force 0] 5 4 [1 1] 0 0
              (make-ability :before (move game active-player 2 :close)
                            :hit (assoc game :next-player active-player))
              "Backstep"
              false
              [:force 0]
              (make-ability :placement (move game active-player
                                             (request-player-input active-player :number [0 4]) :retreat)))

   :cross   (make-attackcard
              "Cross"
              [:force 0] 6 3 [1 1] 0 0
              (make-ability :after (move game active-player 3 :retreat))
              "Run"
              false
              [:force 0]
              (make-ability :placement (let [move-value (request-player-input active-player :number [0 3])]
                              (move game active-player move-value :advance))))

   :grasp   (make-attackcard
              "Grasp"
              [:force 0] 7 3 [1 1] 0 0
              (make-ability :hit (let [receiving-player (if (= :p1 active-player) :p2 :p1)]
                                   (if (get-in game [:receiving-player :status :can-be-pushed])
                                     (move game receiving-player (request-player-input active-player :number [-2 2]) :advance))))
              "Fierce"
              true
              [:force 0]
              (make-ability :placement (update-in game [active-player :modifiers :power] #(+ % 2))
                            :remove (update-in game [active-player :modifiers :power] #(- % 2))))

   :dive    (make-attackcard
              "Dive"
              [:force 0] 4 4 [1 1] 0 0
              (make-ability :before (let [new-movement (move game active-player 3 :advance)
                                          original-space (get-space [active-player (get-in game [active-player :character])] (:play-area game))
                                          new-space (get-space [active-player (get-in new-movement [active-player :character])] (:play-area new-movement))
                                          opponent (if (= :p1 active-player) :p2 :p1)
                                          opponent-space (get-space [opponent (get-in game [opponent :character])] (:play-area game))]
                                      (if (or (and (< original-space opponent-space) (> new-space opponent-space))
                                              (and (> original-space opponent-space) (< new-space opponent-space)))
                                        (assoc-in new-movement [:opponent-status :can-hit] false) ;; Crossed over, opponent cannot hit
                                        new-movement)))
              "Tech"
              false
              [:force 0]
              (make-ability)                                ;; TODO: Implement Tech
              )
   })


