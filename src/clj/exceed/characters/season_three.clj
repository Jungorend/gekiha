(ns exceed.characters.season-three
  (:require [exceed.card.normals :refer [make-attackcard]]
    [exceed.movement :refer [move]]))

(def ryu
  {:cards {
    :ryu-donkey-kick (make-attackcard
      "Donkey Kick"
      [:force 0] 4 6 [1 2] 0 4
      {:hit (fn [game active-player]
              (-> (assoc game :next-player active-player)
                  (move (if (= active-player :p1) :p2 :p1) 2 :push)))}
      "Ki Charge"
      false
      [:force 0]
      (fn [game state active-player]
        ;;TODO: IMPLEMENT MOVEMENT
        ))
    :ryu-hadoken (make-attackcard
      "Hadoken"
      [:force 0] 4 4 [3 6] 0 0
      {:reveal (fn [game active-player]
                 (if (get-in game [active-player :status :critical])
                   (update-in game [active-player :modifiers :speed] #(+ 2 %))
                   game))
       :after (fn [game active-player]
                (if (get-in game [active-player :status :hit])
                  game                                      ;; TODO: add recursion
                  game))}
      "Defensive"
      [:force 0]
      true
      {:placement (fn [game active-player]
                    (-> (update-in game [active-player :modifiers :power] #(+ 2 %))
                        (update-in [active-player :modifiers :armor] #(+ 1 %))))
       :remove (fn [game active-player]
                 (-> (update-in game [active-player :modifiers :power] #(- % 2))
                     (update-in [active-player :modifiers :armor] #(- % 1))))})

    }})
