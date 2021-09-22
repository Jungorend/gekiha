(ns exceed.game.exceed.game.core-test
  (:require [clojure.test :refer :all]
            [exceed.game.cards.normals :refer :all]
            [exceed.game.state-machine :refer [process complete-task set-phase]]
            [exceed.game.core :refer :all]))

(defn confirm-space
  "Helper function to return a specific space's objects"
  [game space]
  ((:play-area game) space))

(deftest basic-initialization
  (let [game @game-list]
    (testing "Confirm that Ryu and Ken are in starting positions"
      (is (= [{:player :p1 :name :ryu :type :character :deck :ryu}] (confirm-space game 2)))
      (is (= [{:player :p2 :name :ryu :type :character :deck :ryu}] (confirm-space game 6))))
    (testing "Confirm hand size is accurate for both players"
      (is (= 5 (count (get-in game [:p1 :areas :hand]))))
      (is (= 6 (count (get-in game [:p2 :areas :hand])))))))

(deftest boosts
  (let [game (assoc @game-list :input-required {:response 2})
        cant-move-set (assoc-in game [:p1 :status :can-move] false)]
    (testing "Confirm that Retreat moves back 2 spaces"
      (is (= [{:player :p1 :name :ryu :type :character :deck :ryu}] (confirm-space ((get-in normals [:assault :boost-text :placement]) game :p1) 0)))
      (is (= [{:player :p2 :name :ryu :type :character :deck :ryu}] (confirm-space ((get-in normals [:assault :boost-text :placement]) game :p2) 8))))
    (testing "Doesn't Retreat when can't move is set"
      (is (= [{:player :p1 :name :ryu :type :character :deck :ryu}] (confirm-space ((get-in normals [:assault :boost-text :placement]) cant-move-set :p1) 2))))))

(deftest move-action
  (let [start (process (set-phase @game-list :move :start))
        force-request (-> start
                          (assoc :input-required {:response
                                                  (mapv #(vector %1 [:p1 :areas :hand]) (take 2 (get-in start [:p1 :areas :hand])))})
                          (complete-task))]
    (testing "Confirm that the move action requests the player picks cards to spend on focus"
      (is (= {:player :p1 :request-type :force} (:input-required start))))
    (testing "Confirm that force value calculates correctly."
      (is (= {:player :p1 :request-type :move :target [:p1 :ryu] :range [-2 -2 2 2]} (:input-required force-request))))))