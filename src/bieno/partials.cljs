(ns bieno.partials
  (:require [bieno.utils :as utils]))

(defn header [{:keys [title shadow separation buttons]}]
  (let [left-button (utils/find-in-collection #(= (:left? %) true) buttons)
        buttons (if left-button (utils/remove-from-collection #(= (:left? %) true) buttons) buttons)
        class (if separation "separated" "")
        class (if (and separation shadow) "separated with-shadow" class)]
    [:div.header
     {:class class}
     [:div.header-container
      [:div.header-main
       (when left-button
         [:div.left-action-btn
          {:on-click (get left-button :callback)}
          [:i.material-icons (get left-button :icon)]])
       (when title
         [:div.title
          title])
       (when buttons
         [:div.right-action-btns
          (for [button buttons]
            ^{:key button} [:div.right-action-btn
                            {:on-click (get button :callback)}
                            [:i.material-icons (get button :icon)]])])]]]))

(defn content [& content]
  (into [:div.content] content))

(defn content-with-search [& content]
  (into [:div.content.with-search] content))

(defn action [{:keys [callback icon]}]
  [:div.action {:on-click callback}
   [:i.material-icons icon]])

(defn confirm [{:keys [data]}]
  [:div.confirm-container
   [:div.confirm
    [:h2 (get data :title)]
    [:p (get data :description)]
    [:div.buttons
     [:div.action-button
      {:on-click (get data :action-button-callback)}
      (get data :action-button-label)]
     [:div.cancel-button
      {:on-click (get data :cancel-button-callback)}
      (get data :cancel-button-label)]]]])