(ns exceed.game.state-machine
  (:require [exceed.game.cards.lookup :refer [get-character-info]]))

(defn create-deck
  "This sets up the starting deck for each character.
  Decks consist of 2 of every normal, as well as 2 of every special and ultra unique to the character."
  [character player]
  (shuffle (concat
             (map #(vector player :face-down character %) (take 14 (cycle (keys (:cards (get-character-info character)))))) ;; fake card
             (map #(vector player :face-down :normal %) (take 16 (cycle (keys exceed.game.cards.normals/normals)))))))

(defn create-player
  "Create initial player stats"
  [character player first?]
  (let [deck (create-deck character player)
        draw-count (if first? 5 6)]
    {:health 30
     :character character
     :exceeded? false
     :reshuffled? false
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
    {:history []
     :play-area [[] [] [[:p1 p1-character]] [] [] [] [[:p2 p2-character]] [] []]
     :next-player (if p1-first? :p2 :p1)
     :current-player first-player
     :input-required {}                                  ;; Which actions the players need to do, :p1 or :p2. Response is set as :response
     :phase [:initialize :complete]
     :p1 (create-player p1-character :p1 p1-first?)
     :p2 (create-player p2-character :p2 (not p1-first?))}))

(defn select-action
  "Provides a list of available options for the player to pick, potentially including from cards.
  Updates :input-required to request from the player."
  [game]
  (let [player (:current-player game)]
    (->
      (assoc game :input-required {player
                                   [:action [:move :boost :strike :prepare :exceed :change-cards]]})
      (assoc :phase [:select-action :processing]))))           ;; TODO: Implement actions from cards

(defn process
  "This function takes in a current game state, and based on any newly present information,
  will potentially move it to another state."
  [game]
  (let [phase-name (get-in game [:phase 0])
        phase-status (get-in game [:phase 1])]
    (case phase-name
      :initialize (-> (setup-game (:p1-character game)
                                  (:p2-character game)
                                  (:first-player game))
                      (assoc :phase [:mulligan :start])
                      (process))
      :mulligan (case phase-status
                  :start (-> game
                             (assoc :phase [:mulligan :processing :p1])
                             (assoc-in [:input-required (:current-player game)]
                                       [:cards [[(:current-player game) :areas :hand]]]))
                  :processing game
                  :complete (if (= :p1 (get-in game [:phase 2]))
                              (-> game
                                  (assoc :phase [:mulligan :processing :p2])
                                  (assoc :input-required {(:next-player game)
                                                          [:cards [[(:next-player game) :areas :hand]]]}))
                              (process (assoc game :phase [:select-action :start]))))
      :select-action (case phase-status
                       :start (select-action game)
                       :processing game
                       :complete (process (assoc game [:phase]
                                                      [(get-in game [:input-required :response]) :start])))
      )))