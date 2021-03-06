(ns exceed.game.cards.normals
  (:require
    [clojure.spec.alpha :as spec]
    [exceed.game.movement :refer [move get-space]]
    [exceed.game.utilities :as utility]))

(defn non-neg-int?
  [value]
  (and (int? value) (not (neg? value))))

(spec/def
  ::cost
  (spec/and vector?
            #(= (count %) 2)
            #(or (= :gauge (first %))
                 (= :force (first %)))
            #(non-neg-int? (second %))))

(spec/def
  ::boost-cost
  (spec/and vector?
            #(= (count %) 2)
            #(or (= :gauge (first %))
                 (= :force (first %)))
            #(non-neg-int? (second %))))

(spec/def
  ::range
  (spec/coll-of int? :count 2))


(spec/def ::power non-neg-int?)
(spec/def ::armor non-neg-int?)
(spec/def ::guard non-neg-int?)
(spec/def ::speed non-neg-int?)
(spec/def ::boost-continuous? boolean?)
(spec/def ::boost-name string?)
(spec/def ::name string?)

(spec/def
  ::attackcard
  (spec/keys :req-un [::cost
                      ::power
                      ::armor
                      ::guard
                      ::range
                      ::speed
                      ::name
                      ::boost-name
                      ::boost-continuous?
                      ::boost-cost]))

(defn make-attackcard
  [name cost speed power range armor guard card-text boost-name boost-continuous? boost-cost boost-text]
  {
    :name name
    :cost cost
    :speed speed
    :power power
    :range range
    :armor armor
    :guard guard
    :card-text card-text
    :boost-name boost-name
    :boost-continuous? boost-continuous?
    :boost-cost boost-cost
    :boost-text boost-text
    })

(defmacro make-ability
    "This specifies card text and abilities.
  All card's `:card-text` and `:boost-text` should be made of this.
  You can pass an arbitrary number of state ability pairs. The `state` will
  be the game-state that `ability` is called on. The code will have `game`
  and `active-player` passed to them, so this can be freely referenced.
  Whatever is returned will be the new game state.

  Example:
  ```(make-ability :after (move game active-player 3 :retreat))```"
  ([] {})
  ([state ability] `(hash-map ~state (fn [~'game ~'active-player] ~ability)))
  ([state ability & args] (if (seq (rest args))             ;; only one arg
                            `(conj (hash-map ~state (fn [~'game ~'active-player] ~ability))
                                   (make-ability ~@args))
                            nil)))

(def normals
  {:assault (make-attackcard
              "Assault"
              [:force 0] 5 4 [1 1] 0 0
              (make-ability :before (move game active-player 2 :close)
                            :hit (assoc game :next-player active-player))
              "Backstep"
              false
              [:force 0]
              (make-ability :placement (let [move-value (get-in game [:input-required :response])]
                                         (if move-value
                                           (assoc (move game active-player move-value :retreat) :input-required {})
                                           (assoc game :input-required {active-player [[:number [0 4]] [:normal :assault :boost-text :placement]]})))))

   :cross   (make-attackcard
              "Cross"
              [:force 0] 6 3 [1 2] 0 0
              (make-ability :after (move game active-player 3 :retreat))
              "Run"
              false
              [:force 0]
              (make-ability :placement (let [move-value (get-in game [:input-required :response])]
                                         (if move-value
                                           (assoc (move game active-player move-value :advance) :input-required {})
                                           (assoc game :input-required {active-player [[:number [0 3]] [:normal :cross :boost-text :placement]]})))))

   :grasp   (make-attackcard
              "Grasp"
              [:force 0] 7 3 [1 1] 0 0
              (make-ability :hit (let [receiving-player (if (= :p1 active-player) :p2 :p1)]
                                   (if (get-in game [:receiving-player :status :can-be-pushed])
                                     (if (get-in game [:input-required :response])
                                       (assoc (move game receiving-player (get-in game [:input-required :response]) :advance) :input-required {})
                                       (assoc game :input-required {active-player [[:number [-2 2]] [:normal :grasp :card-text :hit]]}))
                                     game)))
              "Fierce"
              true
              [:force 0]
              (make-ability :placement (update-in game [active-player :modifiers :power] #(+ % 2))
                            :remove (update-in game [active-player :modifiers :power] #(- % 2))))

   :dive    (make-attackcard
              "Dive"
              [:force 0] 4 4 [1 1] 0 0
              (make-ability :before (let [new-movement (move game active-player 3 :advance)
                                          original-space (get-space [active-player (get-in game [active-player :character])] (:play-area game))
                                          new-space (get-space [active-player (get-in new-movement [active-player :character])] (:play-area new-movement))
                                          opponent (if (= :p1 active-player) :p2 :p1)
                                          opponent-space (get-space [opponent (get-in game [opponent :character])] (:play-area game))]
                                      (if (or (and (< original-space opponent-space) (> new-space opponent-space))
                                              (and (> original-space opponent-space) (< new-space opponent-space)))
                                        (assoc-in new-movement [:opponent-status :can-hit] false) ;; Crossed over, opponent cannot hit
                                        new-movement)))
              "Tech"
              false
              [:force 0]
              (make-ability :placement (if-let [card (get-in game [:input-required :response])] ; TODO: Accommodate discarding your own boosts
                                         (-> (utility/remove-card game card [(utility/opponent active-player) :areas :boost])
                                             (utility/add-card card [(utility/opponent active-player) :areas :discard]))
                                         (assoc game :input-required {active-player [[:card [(utility/opponent active-player) :areas :boost]
                                                                                      [:normal :dive :boost-text :placement]]]}))))
   })


