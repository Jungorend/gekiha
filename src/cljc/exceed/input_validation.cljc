(ns exceed.input-validation
  (:require [clojure.spec.alpha :as spec]))

(defn non-neg-int?
  [value]
  (and (int? value) (not (neg? value))))


(spec/def ::request-type keyword?)
(spec/def ::quantity non-neg-int?)
(spec/def ::range (spec/coll-of int? :kind vector? :max-count 2 :min-count 1))
(spec/def ::options (spec/coll-of keyword? :kind vector?))
(spec/def ::player #{:p1 :p2})
(spec/def ::requester (spec/coll-of keyword? :kind vector?))

;; Input request specification
(defmulti request-type :request-type)
(defmethod request-type :cards [_]
  (spec/keys
    :req-un [::player
             ::destinations]
    :opt-un [::requester
             ::quantity]))

(defmethod request-type :action [_]
  (spec/keys
    :req-un [::player
             ::options]))

(defmethod request-type :number [_]
  (spec/keys
    :req-un [::quantity
             ::player]
    :opt-un [::requester
             ::range]))

(defmethod request-type :focus [_]
  (spec/keys
    :req-un [::player]
    :opt-un [::requester]))

(defmethod request-type :discard [_]
  (spec/keys
    :req-un [::player]))

(spec/def ::input-request (spec/multi-spec request-type :request-type))


;; Input response specification
;; player, deck name id type facing
(spec/def ::deck keyword?)
(spec/def ::name keyword?)
(spec/def ::type #{:normal :special :ultra :character})
(spec/def ::facing #{:face-up :face-down})
(spec/def ::id non-neg-int?)

(spec/def ::location (spec/coll-of keyword? :type vector?))
(spec/def ::card (spec/keys :req-un [::player ::deck ::name ::type]
                            :opt-un [::facing ::id]))

(spec/def ::card-loc-pair
  (spec/keys :req-un [::card
                      ::location]))

(spec/def ::card-coll
  (spec/coll-of ::card-loc-pair))

(defn valid-request?
  "This returns true if the provided map is a valid input-request."
  [m]
  (spec/valid? ::input-request m))

(defn valid-response?
  "This takes in the requested input and confirms that for the response is valid."
  [game player response]
  (let [input-required (:input-required game)]
    (and (= player (:player input-required))
         (case (:request-type input-required)
           :cards (and (spec/valid? ::card-coll response)
                       (or (not (contains? input-required :quantity))
                           (= (:quantity input-required) (count response)))
                       ;; Ensure all cards are from the valid locations
                       (reduce (fn [successful? next-card]
                                 (if (not successful?)
                                   false
                                   (seq (filter #(= next-card %1) (:destinations input-required)))))
                               true
                               (map :location response))
                       ;; Ensure all cards actually exist
                       (reduce (fn [successful? next-card]
                                 (if (not successful?)
                                   false
                                   (some #(= (:card next-card) %) (get-in game (:location next-card)))))
                               true
                               response))))))
