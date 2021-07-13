(ns exceed.utilities)

(defn remove-first
  "This removes the first instance of an item in a vector.
  Useful for the play area and decks."
  ([area item] (remove-first area item []))
  ([area item results] (cond (empty? area) results
                             (= (first area) item) (concat (rest area) results)
                             :else (recur (rest area) item (conj results (first area))))))
