(ns exceed.core
  (:require [exceed.card.normals]
            [exceed.utilities :refer [add-card remove-card]]
            [exceed.input :refer [get-card]]
            [exceed.characters.season-three]))

;; Notes for order of attacking
;; Reveal effects -> Calculate Speed -> Before Effects ->
;; Calculate Range -> Hit effects -> Damage -> After Effects
;; After both players -> Cleanup effects, discard cards

;; TODO: player turns, strikes

;; states
;; placement, reveal, hit before after cleanup

(defn get-card-info
  "This takes in a card from an area and returns its details"
  [character]
  (let [c (case (nth character 2)
            :ryu exceed.characters.season-three/ryu
            :normal exceed.card.normals/normals)]
    ((nth character 3) (if (= :normal (nth character 2))
                         c
                         (:cards c)))))

(defn create-deck
  "This sets up the starting deck for each character.
  Decks consist of 2 of every normal, as well as 2 of every special and ultra unique to the character."
  [character player]
  (shuffle (concat
    (map #(vector player :face-down character %) (take 14 (cycle (keys (:cards (get-card-info character))))))
    (map #(vector player :face-down :normal %) (take 16 (cycle (keys exceed.card.normals/normals)))))))

(defn get-boosts
  "Returns a list of the boosts owned by player in whichever draw area."
  [player area]
  (->> area
    (filter #(or (= player (first %)) (= :face-up (second %)) (= :face-down (second %))))
    (map #(:boost-name ((nth % 3) (if (= :normal (nth % 2))
                                      (get-card-info (nth % 2))
                                      (:cards (get-card-info (nth % 2)))))))))

(defn create-player
  "Create initial player stats"
  [character first?]
  (let [deck (create-deck character :p1)
        draw-count (if first? 5 6)]
    {:health 30
     :character character
     :exceeded? false
     :modifiers {
       :power 0
       :speed 0
       :range [0 0]
       :guard 0
       :armor 0}
    :areas {
      :strike []
      :discard []
      :draw (into [] (drop draw-count deck))
      :hand (into [] (take draw-count deck))
      :gauge []
      :boost []}
   :status {
     :can-move true
     :can-be-pushed true
     :can-hit true
     :hit false ;; Tracking if hit during a strike
     :stunned false
     :critical false}
   }))

(defn setup-game
  "Creates initial game state. Inputs are characters and starting player"
  [p1-character p2-character first-player]
  (let [p1-first? (= :p1 first-player)]
    {:play-area [[] [] [[:p1 p1-character]] [] [] [] [[:p2 p2-character]] [] []]
      :next-player (if p1-first? :p2 :p1)
      :current-player first-player
      :phase :mulligan
      :p1 (create-player p1-character p1-first?)
      :p2 (create-player p2-character (not p1-first?))}))

(defn play-boost
  ;; TODO: Implement removing boosts that are not continuous
  "Provides cards in hand, and based on which one player wants
  puts its in the boost area and calls its placement effects.
  Moves the card to face-up position if it isn't."
  [game player]
  (let [chosen-boost (get-card game player [player :areas :hand])]
    (-> game
        (remove-card chosen-boost [player :areas :hand])
        (add-card (assoc chosen-boost 1 :face-up) [player :areas :boost])
        ((:boost-text (get-card-info chosen-boost)) :placement player))))
