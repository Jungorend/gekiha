(ns exceed.game.utilities)

(defn remove-card
  "Remove card from an area"
  ([game card area] (assoc-in game area (into [] (remove-card game card (get-in game area) []))))
  ([game card area results]
   (cond (empty? area) results
         (= card (first area)) (concat results (rest area))
         :else (recur game card (rest area) (conj results (first area))))))

(defn add-card
  "Add card to an area"
  [game card area]
  (assoc-in game area (conj (get-in game area) card)))

(defn move-card
  "Moves a card from the old area to the new area."
  [game card old-area new-area]
  (-> game
      (remove-card card old-area)
      (add-card card new-area)))

(defn reshuffle
  "Reshuffles the discard pile into the draw pile."
  [game player]
  (-> game
      (assoc-in [player :areas :draw] (shuffle (concat (get-in game [player :areas :draw])
                                                       (get-in game [player :areas :discard]))))
      (assoc-in [player :areas :discard] [])
      (assoc-in [player :reshuffled?] true)))

(defn opponent
  "Provide the opposing player."
  [player]
  (if (= :p1 player)
    :p2
    :p1))

(defn draw-card
  "Draws `n` cards, and reshuffles the deck if needed."
  [game player n]
  (let [deck-size (count (get-in game [player :areas :draw]))]
    (if (> n deck-size)
      (if (get-in game [player :reshuffled?])
        (assoc game :phase (if (= player :p1) :p2-win :p1-win))
        (-> (draw-card game player deck-size)
            (reshuffle player)
            (recur player (- n deck-size))))
      (-> game
          (update-in [player :areas :hand] #(concat % (take n (get-in game [player :areas :draw]))))
          (update-in [player :areas :draw] #(drop n %))))))

(defn pay-focus
  [game player focus]
  (-> (reduce (fn [m [location card]]
                (remove-card m card location))
              game focus)
      (update-in [player :areas :discard] #(concat % (map #'second focus)))))