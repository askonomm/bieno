(ns bieno.utils
  (:require [re-frame.core :as rf]))

(defn build [& content]
  "Construct the main app content"
  (into [:div.app-construct] content))

(defn find-in-collection
  "Finds the first value from coll that satisfies pred.
  Returns nil if it doesn't find such a value."
  [pred coll]
  (some #(when (pred %)
           %)
        coll))

(defn update-in-collection [pred coll {:keys [key value]}]
  "Update item that matches predicate in a given collection with provided {:key :value}"
  (vec (for [item coll]
         (if (pred item)
           (assoc item key value)
           item))))

(defn remove-from-collection
  "Remove item that matches predicate from given collection"
  [pred coll]
  (let [pred* (complement pred)]
    (cond
      (map? coll) (->> coll
                       (filter (fn [[k coll]] (and (pred* k) (pred* coll))))
                       (map (fn [[k coll]]
                              [k (remove-from-collection pred coll)]))
                       (into {}))
      (sequential? coll) (->> coll
                              (filter pred*)
                              (map (partial remove-from-collection pred))
                              (into (empty coll)))
      :default coll)))

(defn listen-for-viewport-change [callback]
  (callback (.-innerWidth js/window))
  (set!
   (.-onresize js/window)
   (fn [event]
     (callback (.-innerWidth js/window)))))

(defn disable-back-button [{:keys [current-view callback]}]
  "Since we do not use a router and instead keep the current view in state, we have no use for a back button.
  Instead, if a back button event is detected, we should direct the user to the default view."
  (.pushState js/history nil (.-title js/document) (.-href js/location))
  (.addEventListener js/window "popstate" (fn [event]
                                            (when (= :note @current-view)
                                              (callback)
                                              (.pushState js/history nil (.-title js/document) (.-href js/location))))))

(defn disable-formatted-paste []
  "Filters paste data so that whatever the user pastes into the contentEditable DOM element is pasted as plain text
  instead of the default, which takes all the copies contents formatting with it."
  (.addEventListener js/window "paste" (fn [event]
                                         (.preventDefault event)
                                         (let [paste (if (.-clipboardData event)
                                                       (.-clipboardData event)
                                                       (.-clipboardData js/window))
                                               data (.getData paste "text")]
                                           (.execCommand js/document "inserttext" false data)))))

(defn overwrite-checkbox-behaviour []
  "Sets the checkbox DOM item as checked when clicked on, overwriting the default behaviour of checkboxes in
  contentEditable DOM elements."
  (.addEventListener js/document "click" (fn [event]
                                           (let [target (.-target event)]
                                             (when (.contains (.-classList target) "checkbox")
                                               (if (.-checked target)
                                                 (.setAttribute target "checked" "checked")
                                                 (.setAttribute target "checked" "")))))))

