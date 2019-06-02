(ns bieno.storage
  (:require [re-frame.core :as rf]))

(defn data<-storage [data]
  "Converts stringified JSON to Clojure format, EDN"
  (when data
    (js->clj (.parse js/JSON data) :keywordize-keys true)))

(defn data->storage [data]
  "Converts data to storage-valid format, stringified JSON"
  (when data
    (.stringify js/JSON (clj->js data))))

(defn convert-data [data type]
  "Converts data in storage to a desired format."
  (cond
    (= :json type) (data<-storage data)
    (= :keyword type) (keyword (subs data 1))
    (= :integer type) (int data)
    :else data))

(defn set-item [{:keys [name value]}]
  "Sets an item into storage"
  (.setItem js/localforage (str name) (str value)))

(defn get-item [{:keys [name]}]
  "Gets an item from storage as a Promise object"
  (.getItem js/localforage (str name)))

(defn remove-item [{:keys [name]}]
  "Removes an item from storage"
  (.removeItem js/localforage (str name)))

(defn init [{:keys [name type]}]
  "Used for syncing data in storage to state, if there is data, otherwise leaves it as it is"
  (let [data-in-storage (get-item {:name name})]
    (.then data-in-storage (fn [data]
                             (when data
                               (let [data (convert-data data type)]
                                 (rf/dispatch [:bieno.events/initialize-data {:name name
                                                                              :data data}])))))))