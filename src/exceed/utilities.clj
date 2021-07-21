(ns exceed.utilities)

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