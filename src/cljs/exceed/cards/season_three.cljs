(ns exceed.cards.season-three)

(def ryu
  "Contains the names and descriptions for
  Ryu's cards. Each is a tuple of `name` and
  then the card `description`"
  {:ryu ["Ryu"
         "As an action, you may Move 1.\nExceed Cost: 1"]
   :ryu-donkey-kick ["Donkey Kick"
                     "Special
                     Speed 4.
                     Power 6.
                     Range 1~2.
                     Guard 4.
                     Hit: Push 2 and Gain Advantage.

                     Boost: Ki Charge
                     Add this card to your Gauge."]
   :ryu-hadoken ["Hadoken"
                 "Special
                 Speed 4.
                 Power 4.
                 Range 3~6.
                 Critical: +2 Speed
                 After: If you hit, you may return this card to your deck.
                 If you do, add the top card of your discard to your gauge.

                 Boost: Defensive (+)
                 +2 Power and +1 Armor."]
   :ryu-one-inch-punch ["One Inch Punch"
                        "Special
                        Speed 3.
                        Power 5.
                        Range 1.
                        Guard 6.
                        Critical: +2 Armor
                        Ignore Armor
                        Hit: Push 3.

                        Boost: Quick Step
                        Move 1. Take another action."]
   :ryu-shoryuken ["Shoryuken"
                   "Special
                   Speed 5.
                   Power 4.
                   Range 1.
                   +2 Speed if the opponent initiated this Strike.
                   Critical, Before: Close 1.

                   Boost: Overhead (+)
                   Ignore Armor.
                   Now: Strike
                   Hit: Add this boost to your gauge."]
   :ryu-tatsu ["Tatsumaki Senpukyaku"
               "Special
               Speed 3.
               Power 5.
               Range 1~2.
               Guard 4.
               Before: Advance 2.
               Hit: If you advanced past your opponent during this Strike, gain Advantage.

               Boost: Swift (+)
               +1 Power and +1 Speed."]
   :ryu-metsu-hadoken ["Metsu Hadoken"
                       "Ultra (3 Gauge)
                       Speed 5.
                       Power 7.
                       Range 3~5.
                       After: Move up to 2.

                       Boost: Way of the Warrior (+)
                       1 Force Cost.
                       +2 Power and +2 Speed"]
   :ryu-metsu-shoryuken ["Metsu Shoryuken"
                         "Ultra (4 Gauge)
                         5 Speed.
                         7 Power.
                         Range 1.
                         +3 Speed if the opponent initiated this Strike.

                         Boost: Lighting Reflexes
                         1 Force Cost
                         Move 2 then Strike."]})