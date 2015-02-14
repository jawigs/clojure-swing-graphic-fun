(ns cljf.mouse-events
  (:import (java.awt Color Dimension BorderLayout)
           (javax.swing JFrame JPanel)
           (java.awt.event MouseEvent MouseListener MouseAdapter
                           MouseMotionListener MouseMotionAdapter
                           MouseWheelEvent MouseWheelListener)))

;;; Create variables for window size (height, width) and
;;; a variable to store state
(def WINW 400)
(def WINH 400)
(def state (atom {:x 100 :y 100 :w 40 :h 40 :times 0 :dir 0}))

;;; update-pos! : int int -> dereferenced state map
;;; modifies state, in effect, repositioning the square to
;; the x and y coordinate
(defn update-pos! [x y]
  (let [{:keys [h w]} @state]
    (swap! state conj {:x (- x (/ w 2))
                       :y (- y (/ h 2))})))

;;; grow-square! : int int -> dereferenced state map
;;; modifies state, in effect, making the square grow or shrink
;;; per the following:
;;; 1) if "dir" is 1, square gets larger by "times" pixels
;;; 2) if "dir" is -1, square gets smaller by "times" pixels
(defn grow-square! [dir times]
  (let [{:keys [y x w h]} @state
        growth (* dir times)]
    (swap! state conj {:x (- x (/ growth 2))
                       :y (- y (/ growth 2))
                       :w (+ w growth)
                       :h (+ h growth)
                       :dir dir
                       :times times})))

;;; pretty-print-state : map -> string
;;; human friendly display of state information
(defn pretty-print-state [state]
  (str "X:|" (:x state)
       "| Y:|" (:y state)
       "| W:|" (:w state)
       "| H:|" (:h state)
       "| T:|" (:times state)
       "| D:|" (:dir state)))

;;; paint : graphics map -> map
;;; draws a red square on the passed graphics parameter and
;;; displays the state information at top of window
(defn paint [g {:keys [x y w h] :as state}]
  (doto g
    (.drawString (pretty-print-state state) 10 10)
    (.setColor Color/RED)
    (.fillRect x y w h)
    (.setColor Color/BLACK)
    (.drawRect x y w h)))

;; draw-panel : nothing -> modified JPanel
;; Creates and returns a modified JPanel that will listen to
;; mouse events and display a red square which changes based
;; on the mouse events
;; 1. Mouse-Click to reposition square to that location
;; 2. Mouse-Drag to have the square follow the mouse
;; 3. Mouse-Wheel will grow \ shrink the square
(defn draw-panel []
  (proxy [JPanel MouseListener
          MouseMotionListener MouseWheelListener] []
    (paintComponent [graphics]
      (proxy-super paintComponent graphics)
      (paint graphics @state))
    (getPreferredSize []
      (Dimension. WINW WINH))
    (mousePressed [e]
      (update-pos! (.getX e) (.getY e))
      (.repaint this))
    (mouseEntered [e])
    (mouseExited [e])
    (mouseClicked [e])
    (mouseReleased [e])
    (mouseDragged [e]
      (update-pos! (.getX e) (.getY e))
      (.repaint this))
    (mouseMoved [e])
    (mouseWheelMoved [e]
      (grow-square! (.getWheelRotation e) 3)
      (.repaint this))))

;;; show : nothing -> JFrame
;;; Creates a JFrame with a modified JPanel and returns
;;; a reference to the JFrame
(defn show []
  ;; Initialize a window and the draw panel
  (let [win (JFrame. "CSGF Mouse Events")
        dp (draw-panel)]
    ;; Add mouse events to draw panel and set focusable
    (doto dp
      (.setFocusable true)
      (.addMouseListener dp)
      (.addMouseMotionListener dp)
      (.addMouseWheelListener dp))
    ;; Add draw panel to window and set a few properties
    (doto win
      (.setSize WINW WINH )
      (.add dp)
      (.setVisible true))
    win))

;; Create and display the window and store a reference
;; to the window in win variable
(def win (show))

(.setTitle win "CSGF Mouse Events") ; change title
