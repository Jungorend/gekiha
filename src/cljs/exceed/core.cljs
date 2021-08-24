(ns exceed.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [ajax.core :refer [GET POST]]))

;; TODO: Right now we're just assuming we're player 1 and there's no
;; separation of sessions. When multiple games are possible each game needs to be stored
;; by session ID

(defn keyword->name
  [key]
  (case key
    :ryu "Ryu"
    :ken "Ken"))

(rf/reg-event-db
  :init
  (fn [_ _]
    {:game {}}))

(rf/reg-event-db
  :set-gamestate
  (fn [db [_ gamestate]]
    ;; TODO: Remove forced :p1 when users added
    (assoc db :game gamestate)))

(rf/reg-sub
  :history
  (fn [db _]
    (get-in db [:game :history])))

(rf/reg-sub
  :play-area
  (fn [db _]
    (get-in db [:game :play-area])))

(rf/reg-sub
  :player
  (fn [db _]
    (get-in db [:game :player])))

(rf/reg-sub
  :player-cards
  (fn [db [_ player]]
    (get-in db [:game player :areas])))


(defn view-history
  []
  (reduce #(conj %1 [:li %2]) [:ul] @(rf/subscribe [:history])))

(defn view-game-area
  []
  (reduce (fn [results card]
            (conj results (if (empty? card)
                            [:div.play-space>p ""]
                            [:div.play-space (map #(vector :p {:class (if (= :p1 (first %)) "player-1" "player-2")}
                                                             (keyword->name (second %))) card)])))
          [:div.play-area] @(rf/subscribe [:play-area])))

(defn view-player-areas
  "Displays the player's cards. Hand, discard, gauge, boost, draw deck."
  []
  (let [player @(rf/subscribe [:player])
        {:keys [hand draw gauge strike boost discard]} @(rf/subscribe [:player-cards player])]
    [:div.footer
     [:h3 "Gauge"]
     [:div.gauge (map #(vector :p {:class (if (= :p1 player) "player-1" "player-2")} (str (nth % 3)))
                      gauge)]
     [:div.draw-deck [:h3 (str "Player deck count: " draw)]]
     [:div.hand [:h3 "Hand of cards"] (map #(vector :p {:class (if (= :p1 player) "player-1" "player-2")} (str (nth % 3)))
                     hand)]
     [:div.discard [:h3 (str "Discard: " (count discard))]]]))

(defn get-gamestate []
  (GET "/game-state"
       {:headers       {"Accept" "application/transit+json"}
        :handler       #(rf/dispatch [:set-gamestate %])
        :error-handler #(.log js/console (str "Error: " %))}))

(def game-outline
  [:div.container
   [:div.sidebar
    [:nav
     [:a {:href "/home"}
      [:img.logo {:src "/img/logo.png"}]]
     [:ul
      [:li [:a {:href "/lobby"} "Lobby"]]
      [:li [:a {:href "/account"} "My Account"]]]]
    [:div.history
     [:img#scroll-history-up.scroll-btn {:src "/img/arrow_circle_up_black_24dp.svg" :alt "scroll history up"}]
     [:div#history-text
      [view-history]]
     [:img#scroll-history-down.scroll-btn {:src "/img/arrow_circle_down_black_24dp.svg" :alt "scroll history down"}]]]
   [:div.game-container
    [view-game-area]]
   [view-player-areas]
   [:script {:type "text/javascript" :src "/js/app.js"}]])

(rf/dispatch-sync [:init])

(dom/render game-outline (.getElementById js/document "main-container"))

(get-gamestate)