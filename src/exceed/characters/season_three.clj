(ns exceed.characters.season-three
  (:require [exceed.card.normals :refer [make-attackcard]]
    [exceed.movement :refer [move]]))

(def ryu
  {:cards {
    :ryu-donkey-kick (make-attackcard
      "Donkey Kick"
      [:force 0] 4 6 [1 2] 0 4
      (fn [game state active-player]
        (case state :hit (-> (assoc game :next-player active-player)
                             (move (if (= active-player :p1) :p2 :p1) 2 :push))
                    game))
      "Ki Charge"
      false
      [:force 0]
      (fn [game state active-player]
        ;;TODO: IMPLEMENT MOVEMENT
        ))
    :ryu-hadoken (make-attackcard
      "Hadoken"
      [:force 0] 4 4 [3 6] 0 0
      (fn [game state active-player]
        (case state :reveal (if (get-in game [active-player :status :critical])
                              (assoc-in game [active-player :modifiers :speed] (+ 2 (get-in game [active-player :modifiers :speed])))
                              game)
                    :after (if (get-in game [active-player :status :hit])
                             game ;; TODO: add recursion
                             game)
                    game))
      "Defensive"
      [:force 0]
      true
      (fn [game state active-player]
        (case state :placement (-> (assoc-in game [active-player :modifiers :power] (+ 2 (get-in game [active-player :modifiers :speed])))
                                   (assoc-in [active-player :modifiers :armor] (+ 1 (get-in game [active-player :modifiers :armor]))))
                    :remove (-> (assoc-in game [active-player :modifiers :power] (- (get-in game [active-player :modifers :speed]) 2))
                                (assoc-in [active-player :modifiers :power] (- (get-in game [active-player :modifier :armor]) 1)))
                    game)))

    }})
