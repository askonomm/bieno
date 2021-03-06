(ns bieno.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [bieno.events :as events]
            [bieno.subscriptions :as subscriptions]
            [bieno.views.settings :as views.settings]
            [bieno.views.notes :as views.notes]
            [bieno.views.note :as views.note]
            [bieno.storage :as storage]
            [bieno.utils :as utils]))

(defmulti app-view (fn [view] view) :default :error)

(defmethod app-view :settings []
  (views.settings/build))

(defmethod app-view :notes []
  (views.notes/build))

(defmethod app-view :note []
  (views.note/build))

(defmethod app-view :error []
  [:div "Something went wrong :("])

(defn app []
  (let [view @(rf/subscribe [::subscriptions/view])]
    (app-view view)))

(defn- set-up []
  (utils/listen-for-viewport-change (fn [screen-width] (rf/dispatch [::events/set-screen-width screen-width])))
  (utils/disable-back-button {:current-view (rf/subscribe [::subscriptions/view]) :callback #(rf/dispatch [::events/set-view :notes])})
  (utils/disable-formatted-paste)
  (utils/overwrite-checkbox-behaviour))

(defn ^:export run []
  (set-up)
  (rf/dispatch-sync [::events/initialize])
  (storage/init {:name :notes
                 :type :json})
  (r/render [app] (js/document.getElementById "app")))