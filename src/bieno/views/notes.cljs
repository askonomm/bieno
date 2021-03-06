(ns bieno.views.notes
  (:require [re-frame.core :as rf]
            [bieno.events :as events]
            [bieno.subscriptions :as subscriptions]
            [bieno.partials :as partials :refer [header content content-with-search action]]
            [bieno.utils :as utils]
            [bieno.storage :as storage]
            [reagent.core :as r]))

(defn- build-header []
  (let [scroll-from-top @(rf/subscribe [::subscriptions/scroll-from-top])
        search-visible? @(rf/subscribe [::subscriptions/search-visible?])]
    (header {:title "Notes"
             :shadow (when (and (not= 0 scroll-from-top) (nil? search-visible?)) true)
             :separation true
             :buttons [{:callback #(rf/dispatch [::events/set-view :settings])
                        :icon "settings"
                        :left? true}
                       {:callback #(rf/dispatch [::events/toggle-search])
                        :icon "search"}]})))

(defn- build-search []
  (let [search-visible? @(rf/subscribe [::subscriptions/search-visible?])
        scroll-from-top @(rf/subscribe [::subscriptions/scroll-from-top])]
    (when search-visible?
      [:div.search
       {:class (when-not (= 0 scroll-from-top) "with-shadow")}
       [:input {:type "text"
                :on-input #(rf/dispatch [::events/search (-> % .-target .-value)])
                :placeholder "Type some words here ..."}]])))

(defn- build-content->did-mount []
  (set!
    (.-onscroll (.querySelector js/document ".content"))
    (fn [event]
      (rf/dispatch [::events/set-scroll-from-top (.. event -target -scrollTop)]))))

(defn- build-content->render []
  (let [notes @(rf/subscribe [::subscriptions/notes])
        filtered-notes @(rf/subscribe [::subscriptions/notes-filtered])
        search-visible? @(rf/subscribe [::subscriptions/search-visible?])
        content-fn (fn [%]
                     (if search-visible?
                       (content-with-search %)
                       (content %)))]
    (content-fn
      (if-not (empty? (if filtered-notes filtered-notes notes))
        [:div.notes
         (for [note (reverse (if filtered-notes filtered-notes notes))]
           ^{:key note} [:div.note {:on-click #(rf/dispatch [::events/view-note (get note :id)])}
                         [:div.note-content.note-formatting
                          {:dangerouslySetInnerHTML {:__html (if (empty? (get note :content)) "Empty note ..." (get note :content))}}]])]
        [:div.loading
         [:div.icon]
         [:div.text "There's a whole lot of nothing here :("]]))))

(defn build-content []
  (r/create-class
    {:component-name "build-content"
     :component-did-mount (fn [] (build-content->did-mount))
     :reagent-render (fn [] (build-content->render))}))

(defn- build-action []
  (action {:callback #(rf/dispatch [::events/create-note])
           :icon "add"}))

(defn- dispatches []
  (storage/init {:name :notes
                 :type :json}))

(defn build []
  "Construct the view out of all the parts."
  (dispatches)
  (utils/build
    (build-header)
    (build-search)
    [build-content]
    (build-action)))