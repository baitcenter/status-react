(ns status-im.ui.screens.keycard.keycard-interaction
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.keycard.components.keycard-animation
             :refer [animated-circles]]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.keycard.components.description :as description]
            [status-im.ui.screens.keycard.components.turn-nfc :as turn-nfc]
            [status-im.ui.screens.keycard.components.style :as styles]))

(def state->translations
  {:init       {:title       :t/keycard-init-title
                :description :t/keycard-init-description}
   :awaiting   {:title       :t/keycard-awaiting-title
                :description :t/keycard-awaiting-description}
   :processing {:title       :t/keycard-processing-title
                :description :t/keycard-processing-description}
   :connected  {:title       :t/keycard-connected-title
                :description :t/keycard-connected-description}
   :error      {:title       :t/keycard-error-title
                :description :t/keycard-error-description}
   :success    {:title       :t/keycard-success-title
                :description :t/keycard-success-description}})

(defn card-sync-flow []
  (let [state (reagent/atom nil)]
    (fn [{:keys [on-card-connected on-card-disconnected]}]
      (let [translation (get state->translations @state)]
        [react/view {:style styles/container-style}
         [react/view {:height        200
                      :margin-bottom 20}
          [animated-circles {:state                state
                             :on-card-disconnected on-card-disconnected
                             :on-card-connected    on-card-connected}]]
         (when translation
           [description/animated-description
            {:title       (i18n/label (:title translation))
             :description (i18n/label (:description translation))}])]))))

(defn connect-keycard [{:keys [on-connect on-cancel on-disconnect]}]
  (fn []
    [react/view {:style {:flex            1
                         :align-items     :center
                         :justify-content :center}}
     (when on-cancel
       [react/touchable-highlight
        {:on-press on-cancel
         :style    {:position :absolute
                    :top      0
                    :right    0}}
        [react/text {:style {:line-height        22
                             :padding-horizontal 16
                             :color              colors/blue
                             :text-align         :center}}
         (i18n/label :t/cancel)]])
     (if @(re-frame/subscribe [:hardwallet/nfc-enabled?])
       [card-sync-flow {:on-card-disconnected
                        #(re-frame/dispatch [on-disconnect])
                        :on-card-connected
                        #(re-frame/dispatch [on-connect])}]
       [turn-nfc/turn-nfc-on])]))
