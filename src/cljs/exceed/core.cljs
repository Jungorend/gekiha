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
    (assoc db :game gamestate)))

(rf/reg-event-db
  :add-selected
  (fn [db [_ card]]
    (update db :selected #(conj % card))))

(rf/reg-sub
  :selected
  (fn [db _]
    (get db :selected)))

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
       (get-in db [:game :input-required])))

(rf/reg-sub
  :is-selected?
  (fn [_ [_ card]]
    (if (seq (filter #(= % card) @(rf/subscribe [:selected])))
      " selected"
      "")))

(defn view-history
  []
  (reduce #(conj %1 [:li %2]) [:ul] @(rf/subscribe [:history])))

(defn render-card
  "This produces a valid hiccup vector with all the card details
  for the card. It will have the alt text of the description,
  and notifies `item-selected` if clicked."
  [card]
  (let [attack-card? (or (= :face-down (second (:card card)))
                         (= :face-up (second (:card card))))
        selected? @(rf/subscribe [:is-selected? card])]
    [:p {:class    (str "card"
                        (if (= :p1 (first (:card card))) " player-one" " player-two")
                        selected?)
         :title    (cards/key->description (if attack-card? (nth (:card card) 3) (nth (:card card) 1)) :description)
         :on-click #(rf/dispatch [:add-selected card])}
     (cards/key->description (if attack-card? (nth (:card card) 3) (nth (:card card) 1)) :name)]))

(defn view-game-area
  []
  (reduce (fn [results card]
            (conj results (if (empty? card)
                            [:div.play-space>p ""]
                            [:div.play-space (doall (map #(render-card {:location [:play-area (count results)]
                                                                        :card     %})
                                                         card))])))
          [:div.play-area] @(rf/subscribe [:play-area])))

(defn view-player-areas
  "Displays the player's cards. Hand, discard, gauge, boost, draw deck."
  []
  (let [player @(rf/subscribe [:player])
        {:keys [hand draw gauge strike boost discard]} @(rf/subscribe [:player-cards player])]
    [:div.footer
     [:h3 "Gauge"]
     [:div.gauge (map #(render-card {:location [player :areas :gauge] :card %}) gauge)]
     [:div.draw-deck [:h3 (str "Player deck count: " draw)]]
     [:div.hand [:h3 "Hand of cards"] (doall (map #(render-card {:location [player :areas :hand]
                                                                 :card     %})
                                                  hand))]
     [:div.discard [:h3 (str "Discard: " (count discard))]]]))

(defn player-input-status
  "If the game is waiting on the player for something, this reports what needs to be done."
  []
  (let [player @(rf/subscribe [:player])
        input-required @(rf/subscribe [:input-required player])]
    [:div.input-required
     (when (seq input-required)
       [:h3
        (case (:request-type input-required)
          :cards (str "You need to select cards from "
                      (:destinations input-required)
                      "."))])]))

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