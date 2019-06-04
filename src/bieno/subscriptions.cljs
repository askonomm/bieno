(ns bieno.subscriptions
  (:require [re-frame.core :as rf]
            [bieno.utils :as utils :refer [find-in-collection]]
            [bieno.storage :as storage]))

(rf/reg-sub
  ::view
  (fn [db _]
    (get db :view)))

(rf/reg-sub
  ::note-id
  (fn [db _]
    (get db :note-id)))

(rf/reg-sub
  ::note
  (fn [db _]
    (let [note (find-in-collection #(= (:id %) (get db :note-id)) (get db :notes))]
      note)))

(rf/reg-sub
  ::notes
  (fn [db _]
    (get db :notes)))

(rf/reg-sub
  ::confirm-dialog
  (fn [db _]
    (let [confirm-dialog-data (get db :confirm-dialog-data)]
      (if-not (empty? confirm-dialog-data)
        confirm-dialog-data
        nil))))

(rf/reg-sub
  ::scroll-from-top
  (fn [db _]
    (get db :scroll-from-top)))

(rf/reg-sub
  ::screen-width
  (fn [db _]
    (get db :screen-width)))

(rf/reg-sub
  ::mobile-device?
  (fn [db _]
    (< (get db :screen-width) 650)))