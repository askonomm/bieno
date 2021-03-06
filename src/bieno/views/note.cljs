(ns bieno.views.note
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [bieno.events :as events]
            [bieno.subscriptions :as subscriptions]
            [bieno.partials :as partials :refer [header content confirm]]
            [bieno.utils :as utils]
            [bieno.storage :as storage]))

(def state (r/atom {:toolbar-open? false}))

(def delete-note-confirm-dialog-data {:title "Delete note"
                                      :description "Are you sure you want to delete this note?"
                                      :action-button-label "Yes, sure!"
                                      :action-button-callback #(rf/dispatch [::events/delete-note])
                                      :cancel-button-label "No, cancel!"
                                      :cancel-button-callback #(rf/dispatch [::events/set-confirm-dialog-data {}])})

(defn- format-selection->title []
  "Formats the current selection as title"
  (if (= "H1" (get @state :parent-node))
    (.execCommand js/document "formatblock" false "div")
    (.execCommand js/document "formatblock" false "h1")))

(defn- format-selection->bold []
  "Formats the current selection as bold"
  (.execCommand js/document "bold" false ""))

(defn- format-selection->italic []
  "Formats the current selection as italic"
  (.execCommand js/document "italic" false ""))

(defn- format-selection->strikethrough []
  "Formats the current selection as strikethrough"
  (.execCommand js/document "strikethrough" false ""))

(defn- format-selection->unordered-list []
  "Formats the current selection as unordered list"
  (.execCommand js/document "insertunorderedlist" false ""))

(defn- format-selection->ordered-list []
  "Formats the current selection as ordered list"
  (.execCommand js/document "insertorderedlist" false ""))

(defn- format-selection->checkbox []
  "Formats the current selection as a checkbox"
  (.execCommand js/document "insertHTML" false "<input type='checkbox' class='checkbox'>"))

(defn- format-selection [format event]
  "Prevent default event action and dispatch a formatting action instead"
  (.preventDefault event)
  (cond
    (= :title format) (format-selection->title)
    (= :bold format) (format-selection->bold)
    (= :italic format) (format-selection->italic)
    (= :strikethrough format) (format-selection->strikethrough)
    (= :unordered-list format) (format-selection->unordered-list)
    (= :ordered-list format) (format-selection->ordered-list)
    (= :checkbox format) (format-selection->checkbox)))

(defn- update-note-content []
  "Updates the note content in storage, not in state, because otherwise contentEditable would refresh and set the
  cursor to be in the beginning of text each time we update state. So, a work-around for that is to set the note content
  in storage, and sync it to state when going to your Notes page."
  (let [note @(rf/subscribe [::subscriptions/note])
        content (.-innerHTML (.querySelector js/document ".note-editor"))]
    (rf/dispatch [::events/set-note-content-in-storage {:id (get note :id)
                                                        :content content}])))

(defn- build-header []
  (let [scroll-from-top @(rf/subscribe [::subscriptions/scroll-from-top])]
    (header {:title "Edit Note"
             :shadow (when-not (= 0 scroll-from-top) true)
             :separation true
             :buttons [{:callback #(rf/dispatch [::events/set-view :notes])
                        :icon "arrow_back"
                        :left? true}
                       {:callback #(rf/dispatch [::events/set-confirm-dialog-data delete-note-confirm-dialog-data])
                        :icon "delete"}]})))

(defn- build-content->did-mount []
  (let [mobile? @(rf/subscribe [::subscriptions/mobile-device?])]
    (set!
      (.-onscroll (.querySelector js/document (if mobile? ".note-editor" ".content")))
      (fn [event]
        (rf/dispatch [::events/set-scroll-from-top (.. event -target -scrollTop)])))))

(defn build-content->render []
  (let [note @(rf/subscribe [::subscriptions/note])
        toolbar-open? (get @state :toolbar-open?)]
    (content
      {:class (if toolbar-open? "toolbar-opened" "")}
      [:div.note-editor.note-formatting {:class (if toolbar-open? "toolbar-opened" "")
                                         :content-editable true
                                         :auto-focus false
                                         :tab-index -1
                                         :on-focus #(swap! state assoc :toolbar-open? true)
                                         :on-input #(update-note-content)
                                         :on-blur #(swap! state assoc :toolbar-open? false)
                                         :placeholder "Your note goes here ..."
                                         :dangerouslySetInnerHTML {:__html (get note :content)}}])))

(defn build-content []
  (r/create-class
    {:component-name "build-content"
     :component-did-mount #(build-content->did-mount)
     :reagent-render #(build-content->render)}))

(defn build-toolbar []
  (let [toolbar-open? (get @state :toolbar-open?)]
    (when toolbar-open?
      [:div.note-toolbar
       [:ul
        [:li
         {:on-mouse-down #(format-selection :title %)}
         [:i.material-icons "title"]]
        [:li
         {:on-mouse-down #(format-selection :bold %)}
         [:i.material-icons "format_bold"]]
        [:li
         {:on-mouse-down #(format-selection :italic %)}
         [:i.material-icons "format_italic"]]
        [:li
         {:on-mouse-down #(format-selection :strikethrough %)}
         [:i.material-icons "strikethrough_s"]]
        [:li
         {:on-mouse-down #(format-selection :unordered-list %)}
         [:i.material-icons "format_list_bulleted"]]
        [:li
         {:on-mouse-down #(format-selection :ordered-list %)}
         [:i.material-icons "format_list_numbered"]]
        [:li
         {:on-mouse-down #(format-selection :checkbox %)}
         [:i.material-icons "check"]]]])))

(defn- dispatches []
  (rf/dispatch [::events/get-note-to-state]))

(defn build []
  "Construct the view out of all the parts."
  (let [confirm-dialog @(rf/subscribe [::subscriptions/confirm-dialog])]
    (dispatches)
    (utils/build
      (when confirm-dialog
        (confirm {:data confirm-dialog
                  :on-close-callback #(rf/dispatch [::events/set-confirm-dialog-data {}])}))
      (build-header)
      [build-content]
      (build-toolbar))))