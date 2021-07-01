(ns exceed.core)

(defrecord AttackCard [
  name
  cost ;; [type value] /:force or /:gauge
  speed
  power
  range ;; [min max]
  card-text ;; function, pass in current game state /(:before), get prizes
  boost-name
  boost-continuous? ;; boolean
  boost-cost ;; [type value]
  boost-text
  ])

(defn setup-game
  "Creates initial game state. inputs are characters."
  [p1-character p2-character]
  {:play-area [[] [] [[:p1 p1-character]] [] [] [] [[:p2 p2-character]] [] []]
  	:p1 {:health 30,
  	     :character p1-character
  	     :exceeded? false}
  	:p2 {:health 30,
  		    :character p2-character
  		    :exceeded? false}})

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
  (let [p1-space (get-space [:p1 (get-in game [:p1 :character])] (:play-area game))
        p2-space (get-space [:p2 (get-in game [:p2 :character])] (:play-area game))]
    (if (> p1-space p2-space)
      (- p1-space p2-space)
      (- p2-space p1-space))))

(defn remove-card
  "Takes the first entry of the item it finds and removes it from the play area."
  [play-area item space]
  (letfn [(remove-first [iter item results]
            (cond (empty? iter) results
                  (and (= (first (first iter)) (first item))
                       (= (second (first iter)) (second item))) (concat (rest iter) results)
                  :else (recur (rest iter) item (conj results (first iter)))))]
    (assoc play-area space (into [] (remove-first (get play-area space) item [])))))

(defn add-card
  "Takes an entry of the item and adds it to the play area."
  [play-area item space]
  (assoc play-area space (conj (get play-area space) item)))

(defn move-card
  "Swaps a card from one position to another."
  [play-area item old-space new-space]
  (remove-card (add-card play-area item new-space) item old-space))

(defn move
  "Handles character movement on the board to ensure legal moves are made.
  `type` refers to whether this is an advance, retreat, close, or move.
  Negative movement is away from your opponent, positive movement towards and past them."
  [game player move-value type]
  (let [p1-space (get-space [:p1 (get-in game [:p1 :character])] (:play-area game))
        p2-space (get-space [:p2 (get-in game [:p2 :character])] (:play-area game))
        player-facing (if (or            ;; Player Facing returns -1 if you're going towards 0 and 1 if towards 8
                            (and (> p1-space p2-space) (= player :p1)) 
                            (and (> p2-space p1-space) (= player :p2))) 
                       -1 1)
        distance (get-range game)
        ]
     (assoc game :play-area
        (case type
           :retreat (move-card (:play-area game)
                               [player (get-in game [player :character])]
                               (if (= player :p1) p1-space p2-space)
                               (if (= player :p1)
                                 (if (= 1 player-facing)
                                   (max (- p1-space (* move-value player-facing)) 0)
                                   (min (- p1-space (* move-value player-facing)) 8))
                                 (if (= 1 player-facing)
                                   (max (- p2-space (* move-value player-facing)) 0)
                                   (min (- p2-space (* move-value player-facing)) 8))))
           :close (move-card (:play-area game)
                             [player (get-in game [player :character])]
                             (if (= player :p1) p1-space p2-space)
                             (if (= player :p1)
                               (+ p1-space (* (min move-value (dec distance)) player-facing))
                               (+ p2-space (* (min move-value (dec distance)) player-facing))))
           :advance (if (< move-value distance)
                      (move-card (:play-area game)
                                 [player (get-in game [player :character])]
                               (if (= player :p1) p1-space p2-space)
                               (if (= player :p1) 
                                 (+ p1-space (* move-value player-facing))
                                 (+ p2-space (* move-value player-facing))))
                      (if (= player :p1)
                        (cond (and (> (+ p1-space (* player-facing move-value) 1) 8)
                                   (= p2-space 8))
                             (move-card (:play-area game) [:p1 (get-in game [:p1 :character])] p1-space 7)
                             (> (+ p1-space (* player-facing move-value) 1) 8)
                             (move-card (:play-area game) [:p1 (get-in game [:p1 :character])] p1-space 8)
                             (and (< (+ p1-space (* player-facing move-value) -1) 0)
                                  (= p2-space 0))
                             (move-card (:play-area game) [:p1 (get-in game [:p1 :character])] p1-space 1)
                             (< (+ p1-space (* player-facing move-value) -1) 0)
                             (move-card (:play-area game) [:p1 (get-in game [:p1 :character])] p1-space 0)
                             :else (move-card (:play-area game) [:p1 (get-in game [:p1 :character])] p1-space (+ p1-space (* player-facing (+ move-value 1)))))
                        (cond (and (> (+ p2-space (* player-facing move-value) 1) 8)
                                   (= p1-space 8))
                              (move-card (:play-area game) [:p2 (get-in game [:p2 :character])] p2-space 7)
                              (> (+ p2-space (* player-facing move-value) 1) 8)
                              (move-card (:play-area game) [:p2 (get-in game [:p2 :character])] p2-space 8)
                              (and (< (+ p2-space (* player-facing move-value) -1) 8)
                                   (= p1-space 0))
                              (move-card (:play-area game) [:p2 (get-in game [:p2 :character])] p2-space 1)
                              (< (+ p2-space (* player-facing move-value) -1) 0)
                              (move-card (:play-area game) [:p2 (get-in game [:p2 :character])] p2-space 0)
                              :else (move-card (:play-area game) [:p2 (get-in game [:p2 :character])] p2-space (+ p2-space (* player-facing (+ move-value 1)))))))))))


