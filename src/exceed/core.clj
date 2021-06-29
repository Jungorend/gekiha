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
        player-facing (if (or            ;; Player Facing returns false if you're going towards 0 and true if towards 8
                            (and (> p1-space p2-space) (= player :p1)) 
                            (and (> p2-space p1-space) (= player :p2))) 
                       false true)
        distance (if (> p1-space p2-space) (- p1-space p2-space) (- p2-space p1-space))
        ]
     (assoc game :play-area
        (case type
           :advance (if (>= distance (get-range game))
                      '() )))))
