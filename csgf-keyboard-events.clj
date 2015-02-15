(ns csgf.keyboard-events
  (:import (java.awt Color Dimension BorderLayout)
           (javax.swing JFrame JPanel)
           (java.awt.event KeyListener KeyEvent)))

;;; Create variables for window size (height, width) and
;;; a variable to store state
(def WIN-W 400)
(def WIN-H 400)
(def state (atom {:type nil
                  :ptype nil
                  :id nil
                  :code nil
                  :char nil
                  :text nil
                  :modifiers nil
                  :modText nil
                  :action? nil
                  :location nil}))

;;; update-state! : string KeyEvent -> map
;;; modifies state variable with input from key event
(defn update-state! [type event]
  (let [id (.getID event)
        typed? (if (== id KeyEvent/KEY_TYPED) true false)
        code (if typed? "" (.getKeyCode event))
        text (if typed? "" (KeyEvent/getKeyText code))
        char (if typed? (.getKeyChar event) "")
        mods (.getModifiersEx event)
        modText (KeyEvent/getModifiersExText mods)]
    (swap! state conj {:type (str type " (" id ")")
                       :ptype (:type @state)
                       :code code
                       :id id
                       :char char
                       :text text
                       :modifiers mods
                       :modText modText
                       :action? (.isActionKey event)
                       :location (.getKeyLocation event)})))

;;; pretty-print-state : map -> string
;;; human friendly display of state information
(defn pretty-print-state [state]
  (str "Type: " (:type state)
       "\r\nPrevious: " (:ptype state)
       "\r\n "
       "\r\nID: " (:id state)
       "\r\nKey Code: " (:code state)
       "\r\nKey Char: " (:char state)
       "\r\nKey Text: " (:text state)
       "\r\nModifiers: " (:modifiers state)
       "\r\nMod Text: " (:modText state)
       "\r\nAction Key: " (:action? state)
       "\r\nLocation: " (:location state)))

;;; draw-string : Graphics string int int -> nil
;;; renders text to screen via Graphics drawString at coordinate x,y
;;; - will split on "\r\n" in text and insert a break in the text
(defn draw-string [graphics text x y]
  (let [offset (-> graphics .getFontMetrics .getHeight)]
    (loop [lines (into [] (.split text "\r\n")), y2 y]
      (if (empty? lines) nil
          (do
            (.drawString graphics (first lines) x y2)
            (recur (rest lines) (+ y2 offset 4)))))))

;;; paint : graphics map -> map
;;; draws state information on screen as text
(defn paint [g {:keys [ky] :as state}]
  (doto g
    (draw-string (pretty-print-state state) 10 10)))

;; draw-panel : nothing -> modified JPanel
;; Creates and returns a modified JPanel that will listen to
;; key events and draw information about those events on the screen
(defn draw-panel []
  (proxy [JPanel KeyListener] []
    (paintComponent [graphics]
      (proxy-super paintComponent graphics)
      (paint graphics @state))
    (getPreferredSize []
      (Dimension. WIN-W WIN-H))
    (keyReleased [e]
      (update-state! 'Released e)
      (.repaint this))
    (keyTyped [e]
      (update-state! 'Typed e)
      (.repaint this))
    (keyPressed [e]
      (update-state! 'Pressed e)
      (.repaint this))))

;;; show : nothing -> JFrame
;;; Creates a JFrame with a modified JPanel and returns
;;; a reference to the JFrame
(defn show []
  (let [win (JFrame. "CSGF Keyboard Events")
        dp (draw-panel)]
    (doto dp
      (.setFocusable true)
      (.addKeyListener dp))
    (doto win
      (.setSize WIN-W WIN-H)
      (.add dp)
      (.setVisible true))
    win))
