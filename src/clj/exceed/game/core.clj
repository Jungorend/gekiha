(ns exceed.game.core
  (:require     [exceed.game.cards.normals]
                [exceed.game.utilities :refer [add-card remove-card]]
                [exceed.game.input :refer [get-card]]
                [exceed.game.cards.season-three]))

;; Notes for order of attacking
;; Reveal effects -> Calculate Speed -> Before Effects ->
;; Calculate Range -> Hit effects -> Damage -> After Effects
;; After both players -> Cleanup effects, discard cards

;; TODO: player turns, strikes
;; TODO: Macro to make attack cards easier (not need fn [game active-player] on everything
;; TODO: State between client and server

;; states
;; placement, reveal, hit before after cleanup

(defn get-card-info
  "This takes in a card from an area and returns its details"
  [character]
  (let [c (case (nth character 2)
            :ryu exceed.game.cards.season-three/ryu
            :normal exceed.game.cards.normals/normals)]
    ((nth character 3) (if (= :normal (nth character 2))
                         c
                         (:cards c)))))

(defn get-character-info
  [character]
  (case character
    :ryu exceed.game.cards.season-three/ryu
    :normal exceed.game.cards.normals/normals))

(defn create-deck
  "This sets up the starting deck for each character.
  Decks consist of 2 of every normal, as well as 2 of every special and ultra unique to the character."
  [character player]
  (shuffle (concat
             (map #(vector player :face-down character %) (take 14 (cycle (keys (:cards (get-character-info character)))))) ;; fake card
             (map #(vector player :face-down :normal %) (take 16 (cycle (keys exceed.game.cards.normals/normals)))))))

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
     :actions_required {}                                  ;; Which actions the players need to do
     :phase :mulligan
     :p1 (create-player p1-character p1-first?)
     :p2 (create-player p2-character (not p1-first?))}))

(defn display-view
  "This converts the game that the server uses, for the view the player can see.
  As a result all hidden decks are replaced with the number of cards remaining, and
  in areas where they can be mix-matched such as the boost area, face-down cards are stripped
  of information besides the player and they are there."
  [game player]
  (let [other-player (if (= :p1 player) :p2 :p1)]
    (-> game
        (update-in [other-player :areas :hand] #(count %))
        (update-in [other-player :areas :draw] #(count %))
        (update-in [other-player :areas :boost] (fn [boosts] (map
                                                               #(if (= :face-down (second %))
                                                                  [(first %) :face-down]
                                                                  %) boosts)))
        (update-in [other-player :areas :strike] (fn [boosts] (map
                                                                #(if (= :face-down (second %))
                                                                   [(first %) :face-down]
                                                                   %) boosts)))
        (update-in [player :areas :draw] #(count %)))))

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
        ((:placement (:boost-text (get-card-info chosen-boost))) player))))
