(ns exceed.game.cards.season-three
  (:require
    [exceed.game.cards.normals :refer [make-attackcard make-ability]]
    [exceed.game.movement :refer [move]]))

(def ryu
  {:cards
   {:ryu-donkey-kick (make-attackcard
                       "Donkey Kick"
                       [:force 0] 4 6 [1 2] 0 4
                       (make-ability :hit (-> (assoc game :next-player active-player)
                                              (move (if (= active-player :p1) :p2 :p1) 2 :push)))
                       "Ki Charge"
                       false
                       [:force 0]
                       (make-ability))                                       ;; TODO: Implement Movement
    :ryu-hadoken (make-attackcard
                   "Hadoken"
                   [:force 0] 4 4 [3 6] 0 0
                   (make-ability :placement (if (get-in game [active-player :status :critical])
                                           (update-in game [active-player :modifiers :speed] #(+ 2 %))
                                           game)
                                 :after (if (get-in game [active-player :status :hit])
                                          game   ;; TODO: Add recursion option
                                          game))
                   "Defensive"
                   [:force 0]
                   true
                   (make-ability :placement (-> (update-in game [active-player :modifiers :power] #(+ % 2))
                                                (update-in [active-player :modifiers :armor] #(+ 1 %)))
                                 :remove (-> (update-in game [active-player :modifiers :power] #(- % 2))
                                             (update-in [active-player :modifiers :armor] #(- % 1)))))

           }})
