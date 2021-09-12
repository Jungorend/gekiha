(ns exceed.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [exceed.cards.core :as cards]
            [ajax.core :refer [GET POST]]))

;; TODO: Right now we're just assuming we're player 1 and there's no
;; separation of sessions. When multiple games are possible each game needs to be stored
;; by session ID

(defn key->str
  [x]
  (second (clojure.string/split (str x) #":")))

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

(rf/reg-sub
  :input-required
  (fn [db [_ player]]
       (get-in db [:game :input-required player])))

(defn view-history
  []
  (reduce #(conj %1 [:li %2]) [:ul] @(rf/subscribe [:history])))

(defn render-card
  "This produces a valid hiccup vector with all the card details
  for the card specified by `key`. It will have the alt text of the description,
  an optional `class` string to be added to the resulting class of card"
  ([key] (render-card key ""))
  ([key class]
   [:p {:class (str "card " class)
        :title (cards/key->description key :description)}
    (cards/key->description key :name)]))

(defn view-game-area
  []
  (reduce (fn [results card]
            (conj results (if (empty? card)
                            [:div.play-space>p ""]
                            [:div.play-space (map #(render-card (second %) (if (= :p1 (first %)) "player-1" "player-2"))
                                                  card)])))
          [:div.play-area] @(rf/subscribe [:play-area])))

(defn view-player-areas
  "Displays the player's cards. Hand, discard, gauge, boost, draw deck."
  []
  (let [player @(rf/subscribe [:player])
        {:keys [hand draw gauge strike boost discard]} @(rf/subscribe [:player-cards player])]
    [:div.footer
     [:h3 "Gauge"]
     [:div.gauge (map #(render-card (nth % 3) (if (= :p1 player) "player-1" "player-2")) gauge)]
     [:div.draw-deck [:h3 (str "Player deck count: " draw)]]
     [:div.hand [:h3 "Hand of cards"] (map #(render-card (nth % 3)
                                                         (if (= :p1 player)
                                                           "player-1"
                                                           "player-2"))
                                           hand)]
     [:div.discard [:h3 (str "Discard: " (count discard))]]]))

(defn player-input-status
  "If the game is waiting on the player for something, this reports what needs to be done."
  []
  (let [player @(rf/subscribe [:player])
        input-required @(rf/subscribe [:input-required player])]
    [:div.input-required
     (when (seq input-required)
       (map (fn [req]
              [:h3 (case (first req)
                     :card (str "Please select a card from your "
                                (when-not (= (get-in req [1 0]) player) "opponent's ")
                                (key->str (get-in req [1 2]))
                                "."))])
            input-required))]))

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
    [player-input-status]
    [view-game-area]]
   [view-player-areas]
   [:script {:type "text/javascript" :src "/js/app.js"}]])



;; Initialization
(rf/dispatch-sync [:init])
(dom/render game-outline (.getElementById js/document "main-container"))
(get-gamestate)