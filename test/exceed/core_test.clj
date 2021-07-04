(ns exceed.core-test
  (:require [clojure.test :refer :all]
            [exceed.core :refer :all]))

(deftest basic-initialization
  (let [game (setup-game :ryu :ken)]
    (testing "Confirm that Ryu and Ken are in starting positions"
      (is (= [[:p1 :ryu]] ((:play-area game) 2)))
      (is (= [[:p2 :ken]] ((:play-area game) 6))))))
