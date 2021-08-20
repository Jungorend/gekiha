(ns exceed.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [ajax.core :refer [GET POST]]))

(def game-state (r/atom {}))

(defn add-history! [new-state]
  (swap! game-state (conj @game-state new-state)))

(defn display-history []
  (fn []
    (apply vector :ul (:game-history @game-state))))

(defn get-gamestate []
  (GET "/game-state"
       {:headers       {"Accept" "application/transit+json"}
        :handler       #((do (reset! game-state %)
                             (dom/render [display-history] (.getElementById js/document "history-text"))))
        :error-handler #(.log js/console (str "Error: " %))}))

(def game-outline
  [:container.container
   [:div.sidebar
    [:nav
     [:a {:href "/home"}
      [:img.logo {:src "/img/logo.png"}]]
     [:ul
      [:li [:a {:href "/lobby"} "Lobby"]]
      [:li [:a {:href "/account"} "My Account"]]]]
    [:div.history
     [:img#scroll-history-up.scroll-btn {:src "/img/arrow_circle_up_black_24dp.svg" :alt "scroll history up"}]
     [:div#history-text]
     [:img#scroll-history-down.scroll-btn {:src "/img/arrow_circle_down_black_24dp.svg" :alt "scroll history down"}]]]
   [:div.game-container
    [:canvas#game]]
   [:div.footer>p "Hypothetical cards go here."]
   [:script {:type "text/javascript" :src "/js/app.js"}]])

(dom/render game-outline (.-body js/document))

(get-gamestate)