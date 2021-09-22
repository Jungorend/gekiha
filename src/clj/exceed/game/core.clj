(ns exceed.game.core
  (:require [exceed.game.cards.lookup :refer [get-character-info get-card-info call-card-function]]
            [exceed.game.utilities :refer [get-input add-card pay-focus remove-card draw-card reshuffle get-response]]
            [exceed.game.state-machine :refer [process complete-task]]))

;; Notes for order of attacking
;; Reveal effects -> Calculate Speed -> Before Effects ->
;; Calculate Range -> Hit effects -> Damage -> After Effects
;; After both players -> Cleanup effects, discard cards

;; TODO: player turns, strikes
;; TODO: Macro to make attack cards easier (not need fn [game active-player] on everything
;; TODO: State between client and server

;; states
;; placement, reveal, hit before after cleanup

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
                                                               #(if (= :face-down (:facing %))
                                                                  {:player (:player %)
                                                                   :facing :face-down}
                                                                  %) boosts)))
        (update-in [other-player :areas :strike] (fn [boosts] (map
                                                                #(if (= :face-down (:facing %))
                                                                   {:player (:player %)
                                                                    :facing :face-down}
                                                                   %) boosts)))
        (update :input-required (fn [input]
                                  (if (= player (:player input))
                                    input
                                    {})))
        (update-in [player :areas :draw] #(count %)))))

(defn valid-response?
  "This takes in a `player` and confirms the right player is updating the game.
  It also checks to make sure that the response is valid for the data requested. This involves
  ensuring the files can be found in the appropriate locations and there are the correct number."
  [game player response]
  (let [request (get-input game)]
    (if (not= player (:player request))
      false
      true)))                                               ;; TODO: Implement full validation. Right now just confirms right player

(defn update-gamestate
  "When a response is provided from a player, this function can be called and it will validate the response.
  If the response matches the required request, it will send the response to the appropriate card function
  or state machine and update state."
  [game player response]
  (if (not (valid-response? game player response))
    game
    (if (contains? (get-input game) :requester)
      (complete-task (call-card-function (assoc-in game [:input-required :response] response)))
      (complete-task (assoc-in game [:input-required :response] response)))))

;; Game engine functionality

#_(defn play-boost
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