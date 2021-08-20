(ns exceed.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [ajax.core :refer [GET POST]]))

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
  :set-history
  (fn [db [_ history]]
    (assoc db :game history)))

(rf/reg-sub
  :history
  (fn [db _]
    (get-in db [:game :game-history])))

(rf/reg-sub
  :play-area
  (fn [db _]
    (get-in db [:game :play-area])))

(defn view-history
  []
  (reduce #(conj %1 [:li %2]) [:ul] @(rf/subscribe [:history])))

(defn view-game-area
  []
  (reduce (fn [results card]
            (conj results (if (empty? card)
                            [:div.play-space>p ""]
                            [:div.play-space>p (map #(vector :p {:class (if (= :p1 (first %)) "player-1" "player-2")}
                                                             (keyword->name (second %))) card)])))
          [:div.play-area] @(rf/subscribe [:play-area])))

(defn get-gamestate []
  (GET "/game-state"
       {:headers       {"Accept" "application/transit+json"}
        :handler       #(rf/dispatch [:set-history %])
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
   [:div.footer>p "Hypothetical cards go here."]
   [:script {:type "text/javascript" :src "/js/app.js"}]])

(rf/dispatch-sync [:init])

(dom/render game-outline (.getElementById js/document "main-container"))

(get-gamestate)