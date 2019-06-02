(ns bieno.editor)

(defn listen-for-change [callback]
  (.addEventListener (.querySelector js/document ".note-editor") "input" (fn [blah]
                                                                           (prn "blah"))))