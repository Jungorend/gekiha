(ns exceed.game.movement
  (:require [exceed.game.utilities :refer [remove-card add-card]]))

;; This contains all the utility functions for basic movement either from boosts
;; or strikes. The primary functions to use are get-space to locate the space of an object
;; and move, which moves a piece to a new location

(defn make-character-card
  "This creates the format characters use for purpose of moving and identifying them."
  [game player]
  (let [name (get-in game [:p1 :character])]
    {:player player
     :type   :character
     :deck name
     :name name}))

(defn get-space
  "Takes `item` and returns which space it is in, or nil if it doesn't exist.
  `item` is of format [player card]"
  ([item play-area] (get-space item play-area 0))
  ([item play-area index] (cond (empty? play-area) nil
                                (some #(= item %) (first play-area)) index
                                true (recur item (rest play-area) (+ index 1)))))

(defn get-range
  "Returns the two characters ranges from each other"
  [game]
  (let [p1-space (get-space (make-character-card game :p1) (:play-area game))
        p2-space (get-space (make-character-card game :p2) (:play-area game))]
    (if (> p1-space p2-space)
      (- p1-space p2-space)
      (- p2-space p1-space))))

(defn move-card
  "Swaps a card from one position to another."
  [game item old-space new-space]
  (-> game
      (add-card item [:play-area new-space])
      (remove-card item [:play-area old-space])))

(defn can-move?
  "Returns true if the push/pull or move is allowed."
  [game player type]
  (case type
    :advance (get-in game [player :status :can-move])
    :close (get-in game [player :status :can-move])
    :retreat (get-in game [player :status :can-move])
    :push (get-in game [(if (= player :p1) :p2 :p1) :status :can-be-pushed])
    :pull (get-in game [(if (= player :p1) :p2 :p1) :status :can-be-pushed])
    :else false))

(defn move ;; May need to return details for future knowledge. Force-point cost, crossed over, etc.
  "Handles character movement on the board to ensure legal moves are made.
  `type` refers to whether this is an advance, retreat, close, or move.
  Negative movement is not taken into account. To ensure no issues, only pass in positive values."
  [game player move-value type]
  (let [p1-space (get-space (make-character-card game :p1) (:play-area game))
        p2-space (get-space (make-character-card game :p2) (:play-area game))
        old-space (if (= player :p1) p1-space p2-space)
        opponent (if (= player :p1) :p2 :p1)
        opponent-space (if (= player :p1) p2-space p1-space)
        player-facing (if (> old-space opponent-space) -1 1) ;; -1 player is on right, 1 player is on left
        distance (get-range game)
        move-character (partial move-card game (make-character-card game player) old-space)]
     (if (can-move? game player type)
       (case type
         :retreat (move-character (if (= 1 player-facing)
                                    (max (- old-space (* move-value player-facing)) 0)
                                    (min (- old-space (* move-value player-facing)) 8)))
         :close (move-character (+ old-space (* (min move-value (dec distance)) player-facing)))
         :push (move-card game [opponent (get-in game [opponent :character])] opponent-space
                          (if (= 1 player-facing)
                            (min (+ opponent-space move-value) 8)
                            (max (- opponent-space move-value) 0)))
         :pull (move-card game [opponent (get-in game [opponent :character])] opponent-space
                          (cond (and (= 1 player-facing) (< move-value distance)) (- opponent-space move-value)
                                (and (= 1 player-facing) (= 0 old-space)) 1
                                (= 1 player-facing) (max 0 (- opponent-space move-value 1))
                                (< move-value distance) (+ opponent-space move-value)
                                (= 8 old-space) 7
                                :else (min 8 (+ opponent-space move-value 1))))
         :advance (cond (< move-value distance) (move-character (+ old-space (* move-value player-facing)))
                        (and (> (+ old-space (* player-facing move-value) 1) 8) (= opponent-space 8)) (move-character 7)
                        (> (+ old-space (* player-facing move-value) 1 ) 8) (move-character 8)
                        (and (< (+ old-space (* player-facing move-value) -1) 0) (= opponent-space 0)) (move-character 1)
                        (< (+ old-space (* player-facing move-value) -1) 0) (move-character 0)
                        :else (move-character (+ old-space (* player-facing (+ move-value 1))))))
       game)))

