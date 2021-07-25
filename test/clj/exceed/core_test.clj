(ns exceed.core-test
  (:require [clojure.test :refer :all]
            [exceed.core :refer :all]
            [exceed.card.normals :refer :all]))

(defn confirm-space
  "Helper function to return a specific space's objects"
  [game space]
  ((:play-area game) space))

(deftest basic-initialization
  (let [game (setup-game :ryu :ryu :p1)]
    (testing "Confirm that Ryu and Ken are in starting positions"
      (is (= [[:p1 :ryu]] (confirm-space game 2)))
      (is (= [[:p2 :ryu]] (confirm-space game 6))))
    (testing "Confirm hand size is accurate for both players"
      (is (= 5 (count (get-in game [:p1 :areas :hand]))))
      (is (= 6 (count (get-in game [:p2 :areas :hand])))))))

(deftest boosts
  (let [game (setup-game :ryu :ryu :p1)
        cant-move-set (assoc-in game [:p1 :status :can-move] false)]
    (testing "Confirm that Retreat moves back 2 spaces"
      (is (= [[:p1 :ryu]] (confirm-space ((get-in normals [:assault :boost-text]) nil game :p1) 0)))
      (is (= [[:p2 :ryu]] (confirm-space ((get-in normals [:assault :boost-text]) nil game :p2) 8))))
    (testing "Doesn't Retreat when can't move is set"
      (is (= [[:p1 :ryu]] (confirm-space ((get-in normals [:assault :boost-text]) nil cant-move-set :p1) 2))))))
