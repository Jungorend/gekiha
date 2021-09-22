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
          Choose and discard a boost in play."]
   :spike ["Spike"
           "Normal
           Speed 3.
           Power 5.
           Range 2~3.
           Guard 4.
           Ignore Armor.
           Ignore Guard.

           Boost: Defend (+)
           +1 Armor, +3 Guard."]
   :sweep ["Sweep"
           "Normal"
           "Speed 2.
           Power 6.
           Range 1~3.
           Guard 6.
           Hit: The opponent must discard a card at random.

           Boost: Light (+)
           +2 Speed."]
   :focus ["Focus"
           "Normal
           Speed 1.
           Power 4.
           Range 1~2.
           Armor: 2.
           Guard: 5.
           Opponent Cannot Move you.
           After: Draw a card.

           Boost: Reading
           Name a Normal Attack and then Strike. The Opponent must Strike with that card,
           or reveal a hand without any."]
   :block ["Block"
           "Normal
           Speed 0.
           Power N/A.
           Range N/A.
           Armor 2.
           Guard 3.
           When you are hit, after hit effects, spend Force for +2 Armor per Force Spent.
           After: Add this card to your Gauge at the end of the Strike.

           Boost: Parry
           Name a card. The opponent must discard a copy of that card, or reveal a hand without any."]})


(defn card->description
  "Returns the name or description of the given card keyword.
  :name for name, :description for description."
  [card type]
  (get-in (if (= :normal (:type card))
            normals
            (case (:deck card)
              :ryu s3/ryu)) [(:name card) (if (= type :name)
                               0
                               1)]))