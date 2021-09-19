(ns exceed.game.core
  (:require [exceed.game.cards.lookup :refer [get-character-info get-card-info]]
            [exceed.game.utilities :refer [add-card pay-focus remove-card draw-card reshuffle]]
            [exceed.game.state-machine :refer [process]]))

;; Notes for order of attacking
;; Reveal effects -> Calculate Speed -> Before Effects ->
;; Calculate Range -> Hit effects -> Damage -> After Effects
;; After both players -> Cleanup effects, discard cards

;; TODO: player turns, strikes
;; TODO: Macro to make attack cards easier (not need fn [game active-player] on everything
;; TODO: State between client and server

;; states
;; placement, reveal, hit before after cleanup

(defn get-boosts
  "Returns a list of the boosts owned by player in whichever draw area."
  [player area]
  (->> area
       (filter #(or (= player (first %)) (= :face-up (second %)) (= :face-down (second %))))
       (map #(:boost-name ((nth % 3) (if (= :normal (nth % 2))
                                       (get-card-info (nth % 2))
                                       (:cards (get-card-info (nth % 2)))))))))

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
        (update :input-required (fn [input]
                                  (if (get input player)
                                    {player (player input)}
                                    {})))
        (update-in [player :areas :draw] #(count %)))))

(defn send-response
  "Provides a response to a function's request for player input."
  [game player response]
  (let [function-reference (get-in game [:input-required player 1])
        game-with-response (assoc-in (dissoc game :input-required) [:input-required :response] response)]
    ((get-in (get-character-info (first function-reference))
             (vec (rest function-reference)))
     game-with-response player)))

;; Game engine functionality
(defn prepare-action
  [game]
  (draw-card game (:current-player game) 1))

(defn change-cards-action
  [game]
  (let [changed-cards (get-in game [:input-required :response])
        player (:current-player game)]
    (if changed-cards
      (-> (pay-focus game player changed-cards)
          (draw-card player (count changed-cards))))))      ;; TODO: have ultras count for 2 force optionally

(defn play-boost
  ;; TODO: Implement removing boosts that are not continuous
  "Provides cards in hand and based on which one players wants,
  puts it in the boost area and calls its placement effects.
  Moves the card to face-up position if it isn't."
  [game player]
  (let [chosen-boost (get-in game [:input-required :response])]
    (if chosen-boost
      (-> game
          (remove-card chosen-boost [player :areas :hand])
          (add-card (assoc chosen-boost 1 :face-up) [player :areas :boost])
          (assoc ((:placement (:boost-text (get-card-info chosen-boost))) player) :input-required {}))
      (assoc game :input-required {player [[:card [player :areas :hand] ['exceed.game.core/core]]]}))))

(def game-list (atom (-> (process {:phase {:action :initialize :status :start}
                                   :p1-character :ryu
                                   :p2-character :ryu
                                   :first-player :p1})
                         (assoc :history [[:p "Player boosted Backstep."] ;; this is just test code for now
                                          [:p "Opponent initiated a strike."]
                                          [:hr] [:h3 {:class "strike"} "Strike!"]
                                          [:p "Player had Cross."]
                                          [:p "Opponent had EX-Block."]
                                          [:p "Opponent spent 1 force on Block."]
                                          [:hr] [:h3 {:class "turn"} "Turn 4"]
                                          [:p "Player spent a Grasp from hand to move 1."]
                                          [:hr] [:h3 {:class "turn"} "Turn 5"]]
                                :player :p1))))