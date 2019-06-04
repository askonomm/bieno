(ns bieno.views.settings
  (:require [re-frame.core :as rf]
            [bieno.utils :as utils]
            [bieno.events :as events]
            [bieno.partials :refer [header content]]))

(defn- build-header []
  (header {:title "Settings"
           :separation true
           :buttons [{:callback #(rf/dispatch [::events/set-view :notes])
                      :icon "arrow_back"
                      :left? true}]}))

(defn- build-content []
  (content
    [:div.placeholder "There will be things here in the future :)"]))

(defn build []
  "Construct the view out of all the parts."
  (utils/build
    (build-header)
    (build-content)))