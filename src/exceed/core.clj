(ns exceed.core
  (:require [exceed.card.normals]
            [exceed.characters.season-three]))

;; Notes for order of attacking
;; Reveal effects -> Calculate Speed -> Before Effects ->
;; Calculate Range -> Hit effects -> Damage -> After Effects
;; After both players -> Cleanup effects, discard cards

;; TODO: player turns, strikes

;; states
;; placement, reveal, hit before after cleanup

(defn create-deck
  "This sets up the starting deck for each character.
  Decks consist of 2 of every normal, as well as 2 of every special and ultra unique to the character."
  [character player]
  (let [c (case character :ryu exceed.characters.season-three/ryu
                          :else exceed.characters.season-three/ryu)]
      (shuffle (concat
        (map #(vector player :facedown character %) (take 14 (cycle (keys (:cards c)))))
        (map #(vector player :facedown :normal %) (take 16 (cycle (keys exceed.card.normals/normals))))))))

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
      :next-player (if (= :p1 first-player) :p2 :p1)
      :current-player first-player
      :phase :mulligan
      :p1 (create-player p1-character p1-first?)
      :p2 (create-player p2-character (not p1-first?))}))
