(ns bieno.events
  (:require [re-frame.core :as rf]
            [bieno.storage :as storage]
            [bieno.utils :as utils]))

(rf/reg-event-db
  ::initialize
  (fn [_ _]
    {:view :notes
     :confirm-dialog-data {}
     :scroll-from-top 0
     :screen-width 0
     :note-id nil
     :note nil
     :notes []}))

(rf/reg-event-fx
  ::initialize-data
  (fn [cofx [_ {:keys [name data]}]]
    {:db (assoc (get cofx :db) name data)}))

(rf/reg-event-fx
  ::set-view
  (fn [cofx [_ view]]
    {:db (assoc (get cofx :db) :view view)}))

(rf/reg-event-fx
  ::set-confirm-dialog-data
  (fn [cofx [_ data]]
    {:db (assoc (get cofx :db) :confirm-dialog-data data)}))

(rf/reg-event-fx
  ::set-note-id
  (fn [cofx [_ note-id]]
    (storage/set-item {:name :note-id
                       :value note-id})
    {:db (assoc (get cofx :db) :note-id note-id)}))

(rf/reg-event-fx
  ::set-note-content-in-storage
  (fn [cofx [_ {:keys [id content]}]]
    (let [notes (utils/update-in-collection #(= (:id %) id) (get-in cofx [:db :notes]) {:key   :content
                                                                                        :value content})]
      (storage/set-item {:name :notes
                         :value (storage/data->storage notes)})
      {})))

(rf/reg-event-fx
  ::view-note
  (fn [cofx [_ note-id]]
    {:dispatch-n [[::set-note-id note-id]
                  [::set-view :note]
                  [::set-scroll-from-top 0]]}))

(rf/reg-event-fx
  ::get-note-to-state
  (fn [cofx _]
    (let [note (utils/find-in-collection #(= (:id %) (get cofx :note-id)) (get cofx :notes))]
      {:db (assoc (get cofx :db) :note note)})))

(rf/reg-event-fx
  ::create-note
  (fn [cofx _]
    (let [notes-in-state (get-in cofx [:db :notes])]
      (if notes-in-state
        (let [note-id (str (random-uuid))
              notes (merge notes-in-state {:id note-id
                                           :content ""
                                           :status "private"
                                           :tags []})]
          (storage/set-item {:name :notes
                             :value (storage/data->storage notes)})
          {:db (assoc (get cofx :db) :notes notes)
           :dispatch [::view-note note-id]})
        (let [note-id (str (random-uuid))
              notes [{:id note-id
                      :content ""
                      :status "private"
                      :tags []}]]
          (storage/set-item {:name :notes
                             :value (storage/data->storage notes)})
          {:db (assoc (get cofx :db) :notes notes)
           :dispatch [::view-note note-id]})))))

(rf/reg-event-fx
  ::delete-note
  (fn [cofx _]
    (let [note-id (get-in cofx [:db :note-id])
          notes-in-state (get-in cofx [:db :notes])
          notes-after-delete (utils/remove-from-collection #(= (:id %) note-id) notes-in-state)]
      (storage/set-item {:name :notes
                         :value (storage/data->storage notes-after-delete)})
      {:db (assoc (get cofx :db) :notes notes-after-delete)
       :dispatch-n [[::set-note-id nil]
                    [::set-view :notes]
                    [::set-confirm-dialog-data {}]]})))

(rf/reg-event-fx
  ::set-scroll-from-top
  (fn [cofx [_ value]]
    {:db (assoc (get cofx :db) :scroll-from-top value)}))

(rf/reg-event-fx
  ::set-screen-width
  (fn [cofx [_ value]]
    {:db (assoc (get cofx :db) :screen-width value)}))