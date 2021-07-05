(ns exceed.core)

;; Notes for order of attacking
;; Reveal effects -> Calculate Speed -> Before Effects ->
;; Calculate Range -> Hit effects -> Damage -> After Effects
;; After both players -> Cleanup effects, discard cards

;; TODO: Need to either have the move function notify when you cross over
;; or a separate function to confirm this
;; TODO: player turns, strikes


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

(defn request-player-input ;; TODO: implement player input.
  "This function is the interface between the game logic and making changes.
  It allows mid-any stage more information to be requests from specific players.
  Active player is the player to request input from.
  type is what sort of request is being made.
  bounds is any restrictions on the request, for example minimum/max values.
  For now it just takes the input from the CLI and compares it to bounds"
  [player type bounds]
  2)

(defn setup-game
  "Creates initial game state. inputs are characters."
  [p1-character p2-character]
  {:play-area [[] [] [[:p1 p1-character]] [] [] [] [[:p2 p2-character]] [] []]
    :next-player :p1
  	:p1 {:health 30,
  	     :character p1-character
  	     :exceeded? false
         :modifiers {
           :power 0
           :speed 0
           :range [0 0]
           :guard 0
           :armor 0
         }
         :status {
           :can-move true
           :can-be-pushed true
           :can-hit true
           :stunned false
           }}
  	:p2 {:health 30,
  		   :character p2-character
         :exceeded? false
         :modifiers {
           :power 0
           :speed 0
           :range [0 0]
           :guard 0
           :armor 0
         }
         :status {
           :can-move true
           :can-be-pushed true
           :can-hit true
           :stunned false
           }}})

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

(defn move ;; May need to return details for future knowledge. Force-point cost, crossed over, etc.
  "Handles character movement on the board to ensure legal moves are made.
  `type` refers to whether this is an advance, retreat, close, or move.
  Negative movement is to the beginning of the arena, positive movement towards the end."
  [game player move-value type]
  (let [p1-space (get-space [:p1 (get-in game [:p1 :character])] (:play-area game))
        p2-space (get-space [:p2 (get-in game [:p2 :character])] (:play-area game))
        old-space (if (= player :p1) p1-space p2-space)
        opponent-space (if (= player :p1) p2-space p1-space)
        player-facing (if (> old-space opponent-space) -1 1) ;; -1 for towards 0, 1 towards 8
        distance (get-range game)
        move-character (partial move-card (:play-area game) [player (get-in game [player :character])] old-space)]
     (assoc game :play-area
        (case type
          :retreat (move-character (if (= 1 player-facing)
                                     (max (- old-space (* move-value player-facing)) 0)
                                     (min (- old-space (* move-value player-facing)) 8)))
          :close (move-character (+ old-space (* (min move-value (dec distance)) player-facing)))
          :advance (cond (< move-value distance) (move-character (+ old-space (* move-value player-facing)))
                         (and (> (+ old-space (* player-facing move-value) 1) 8) (= opponent-space 8)) (move-character 7)
                         (> (+ old-space (* player-facing move-value) 1 ) 8) (move-character 8)
                         (and (< (+ old-space (* player-facing move-value) -1) 0) (= opponent-space 0)) (move-character 1)
                         (< (+ old-space (* player-facing move-value) -1) 0) (move-character 0)
                         :else (move-character (+ old-space (* player-facing (+ move-value 1)))))))))

(def normals
  ;; state in boost is not always used but may be useful for future things
  ;; will call all attacks and boosts each turn
  {:assault (AttackCard.
    "Assault"
    [:force 0] 5 4 [1 1]
    (fn [state game active-player]
      (case state :before (assoc game :play-area (move game active-player 2 :close))
        :hit (assoc game :next-player active-player)
        :else game))
    "Backstep"
    false
    [:force 0]
    (fn [state game active-player]
      (let [move-value (request-player-input active-player :number [0 4])]
        (assoc game :play-area (move game active-player move-value :retreat)))))

    :cross (AttackCard.
      "Cross"
      [:force 0] 6 3 [1 1]
      (fn [state game active-player]
        (case state :after (assoc game :play-area (move game active-player 3 :retreat))
          :else game))
      "Run"
      false
      [:force 0]
      (fn [state game active-player]
        (let [move-value (request-player-input active-player :number [0 3])]
          (assoc game :play-area (move game active-player move-value :advance)))))

    :grasp (AttackCard.
      "Grasp"
      [:force 0] 7 3 [1 1]
      (fn [state game active-player]
        (case state :hit (let [receiving-player (if (= :p1 active-player) :p2 :p1)]
            (if (get-in game [:receiving-player :status :can-be-pushed])
              (assoc game :play-area (move game receiving-player (request-player-input active-player :number [-2 2]) :advance))
              game)
              :else game)))
      "Fierce"
      true
      [:force 0]
      (fn [state game active-player]
        (assoc-in game [:modifiers :power] (+ 2 (get-in game [:modifiers :power])))))
      })
