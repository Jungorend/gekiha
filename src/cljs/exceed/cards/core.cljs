(ns exceed.cards.core
  (:require
    [exceed.cards.season-three :as s3]))

(def normals
  {:grasp ["Grasp"
           "Normal
           Speed 7.
           Power 3.
           Range 1.

           Push or Pull 1 or 2.

           Boost: Fierce (+)
           +2 Power."]
   :cross ["Cross"
           "Normal
           Speed 6.
           Power 3.
           Range 1~2.

           After: Retreat 3.

           Boost: Run
           Advance up to 3."]
   :assault ["Assault"
             "Normal
             Speed 5.
             Power 4.
             Range 1.
             Before: Close 2.
             Hit: Gain Advantage.

             Boost: Backstep
             Retreat up to 4."]
   :dive ["Dive"
          "Normal
          Speed 4.
          Power 4.
          Range 1.

          Before: Advance 3.
          If you advanced past your opponent, their attacks do not hit you.

          Boost: Tech
          Choose and discard a boost in play."]})


(defn key->description
  "Returns the full name for a given key"
  [key type]
  (get-in (case key
            :ryu s3/ryu
            :ryu-hadoken s3/ryu
            :ryu-donkey-kick s3/ryu
            :grasp normals
            :cross normals
            :assault normals
            :dive normals
            ) [key (if (= type :name)
                               0
                               1)]))