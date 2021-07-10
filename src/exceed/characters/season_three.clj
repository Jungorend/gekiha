(ns exceed.characters.season-three
  (:require [exceed.card.normals :refer [make-attackcard]]
    [exceed.movement :refer [move]]))

(def ryu
  {:cards {
    :ryu-donkey-kick (make-attackcard
      "Donkey Kick"
      [:force 0] 4 6 [1 2] 0 4
      (fn [state game active-player]
        (if (= state :hit) (-> (assoc game :next-player active-player)
                             (move (if (= active-player :p1) :p2 :p1) 2 :push))))
      "Ki Charge"
      false
      [:force 0]
      (fn [state game active-player]
        ;;TODO: IMPLEMENT MOVEMENT
        ))
    :ryu-hadoken (make-attackcard
      "Hadoken"
      [:force 0] 4 4 [3 6] 0 0
      (fn [state game active-player]
        (case state :reveal (if (get-in game [active-player :status :critical])
                              (assoc-in game [active-player :modifiers :speed] (+ 2 (get-in game [active-player :modifiers :speed])))
                              game)
                    :after (if (get-in game [active-player :status :hit])
                             game ;; TODO: add recursion
                             game)
                    :else game))
      "Defensive"
      [:force 0]
      true
      (fn [state game active-player]
        (case state :placement (-> (assoc-in game [active-player :modifiers :power] (+ 2 (get-in game [active-player :modifiers :speed])))
                                   (assoc-in game [active-player :modifiers :armor] (+ 1 (get-in game [active-player :modifiers :armor]))))
                    :else game)))

    }})
