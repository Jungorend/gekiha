(ns exceed.game.state-machine
  (:require [exceed.game.cards.lookup :refer [get-character-info get-force-value]]
            [exceed.game.utilities :refer [set-input get-response]]))

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
     :phase {:action :initialize :sub-action :complete}
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
      )))           ;; TODO: Implement actions from cards

(defn complete-task
  "Updates the game state to notify process when done. Will then call process
  and return the next state of the game."
  [game]
  (assoc game :phase
              {:action (get-in game [:phase :action])
               :status (if (contains? (:phase game) :next-action)
                             (get-in game [:phase :next-action])
                             :complete)}))
(defn get-phase-status
  "Returns the status for the current phase."
  [game]
  (get-in game [:phase :status]))

(defn set-phase
  "Sets the phase to the requested parameters."
  ([game action status] (assoc game :phase {:action action :status status :next-action :complete}))
  ([game action status next-action] (assoc game :phase {:action action :status status :next-action next-action})))

(defmulti proc
  "This function takes in a current game state, and based on any newly present information,
  will move it to the next state. If the :status of the phase is `:processing` it is waiting
  on information and will not proceed. Any function that finished processing requested info should call
  `complete-task` to update this function."
          #(get-in % [:phase :action]))

(defn process
  [game]
  (if (= :processing (get-phase-status game))
    game
    (proc game)))

(defmethod proc :initialize [game]
  (-> (setup-game (:p1-character game)
                  (:p2-character game)
                  (:first-player game))
      (set-phase :mulligan :start)
      (process)))

(defmethod proc :mulligan [game]
  (case (get-phase-status game)
    :start (-> (set-phase game :mulligan :processing :opponent-mulligan)
               (set-input (:current-player game)
                          [:cards [[(:current-player game) :areas :hand]]]))
    :opponent-mulligan (-> (set-phase game :mulligan :processing)
                           (set-input (:next-player game)
                                      [:cards [[(:next-player game) :areas :hand]]]))
    :complete (process (set-phase game :select-action :start))))

(defmethod proc :select-action [game]
  (case (get-phase-status game)
    :start (select-action (set-phase game :select-action :processing))
    :complete (process (set-phase game (get-response game) :start))))

(defmethod proc :move-action [game]
  (case (get-phase-status game)
    :start (-> (set-phase game :move-action :processing :force-request)
               (set-input (:current-player game) [:force]))
    :force-request (let [[min-force max-force] (get-force-value (get-response game) :with-areas true)]
                     (-> (set-phase game :move-action :processing :move-request)
                         (set-input (:current-player game)
                                    [:move [(:current-player game)
                                            (get-in game [(:current-player game) :character])]
                                     [(- max-force) (- min-force) min-force max-force]])))
    :move-request game)) ;; TODO: Complete function

(defmethod proc :default [game]
  game)