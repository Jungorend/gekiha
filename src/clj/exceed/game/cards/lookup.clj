(ns exceed.game.cards.lookup
  (:require [exceed.game.cards.season-three]
           [exceed.game.cards.normals]))

(defn get-character-info
  [character]
  (case character
    :ryu exceed.game.cards.season-three/ryu
    :normal exceed.game.cards.normals/normals))

(defn call-card-function
  "This takes a vector, func, of form
  [`ns` `character` `function` `phase`] and calls it.
  For example [:normal :assault :boost-text :placement].

  This function is useful for calling on input-responses as it will find the appropriate source
  if there is a requester."
  [game]
  (let [request (get-in game [:input-required :requester])
        c (get-character-info (first request))
        character (if (= :normal (first request))
                    c
                    (:cards c))]
    ((get-in character (drop 1 request)) game (get-in game [:input-required :player]))))

(defn get-card-info
  "This takes in a card from an area and returns its details."
  [card]
  (let [c (get-character-info (:deck card))]
    (get (:name c) [(if (= :normal (:type card))
                      c
                      (:cards c))])))

(defn get-force-value
  "Provides the the focus values like [min-value max-value]. Specials count as 1, and Ultras count as 2.
  If the optional key `with-areas` is set to true, then it will disregard every other field."
  [cards & {:keys [with-areas]}]
  (let [c (if with-areas (map first cards) cards)
        ultra-count (count (filter
                             (fn [card] (= :gauge
                                           (first (:cost (get-card-info card)))))
                             c))
        special-count (- (count c) ultra-count)]
    [(count c) (+ special-count (* 2 ultra-count))]))