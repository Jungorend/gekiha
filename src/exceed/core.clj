(ns exceed.core
  (:require [exceed.card.normals]))

;; Notes for order of attacking
;; Reveal effects -> Calculate Speed -> Before Effects ->
;; Calculate Range -> Hit effects -> Damage -> After Effects
;; After both players -> Cleanup effects, discard cards

;; TODO: Need to either have the move function notify when you cross over
;; or a separate function to confirm this
;; TODO: player turns, strikes
;; TODO: Break this up into multiple files like normal people do





(defn create-deck ;; TODO: Add logic to add cards based on character as well
  "This will setup the decks for each character.
  Decks consist of 2 of every normal, as well as 2 of every special and ultra unique to the character."
  [character player]
  (shuffle (map #(vector player %) (take 16 (cycle (keys exceed.card.normals/normals))))))

(defn setup-game ;; TODO: Once we have a deck or two, should just call a function to set up decks from characters, for now we're just going to use normals
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
         :areas {
           :strike [] ;; Strike will only ever need a max of 2 cards
           :discard [] ;; TODO: Consider ramifications of lists instead
           :draw []
           :hand []
           :gauge []
           :boost []
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
         :areas {
           :strike []
           :discard []
           :draw []
           :hand []
           :gauge []
           :boost []
         }
         :status {
           :can-move true
           :can-be-pushed true
           :can-hit true
           :stunned false
           }}})
