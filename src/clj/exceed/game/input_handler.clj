(ns exceed.game.input-handler
  (:require [clojure.spec.alpha :as spec]))

(defn player?
  "Returns whether or not the value is a valid player."
  [value]
  (or (= :p1 value) (= :p2 value)))

(defn non-neg-int?
  [value]
  (and (int? value) (not (neg? value))))

(spec/def ::request-type keyword?)
(spec/def ::quantity non-neg-int?)
(spec/def ::range (spec/coll-of int? :kind vector? :max-count 2 :min-count 1))
(spec/def ::player player?)
(spec/def ::requester (spec/coll-of keyword? :kind vector?))

(defmulti request-type :request-type)
(defmethod request-type :cards [_]
  (spec/keys
    :req-un [::quantity
             ::player
             ::destinations]
    :opt-un [::requester]))

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

(defn valid-request?
  "This returns true if the provided map is a valid input-request."
  [m]
  (spec/valid? ::input-request m))