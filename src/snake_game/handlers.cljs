(ns snake-game.handlers
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch]]
            [goog.events :as events]
            [snake-game.utils :as utils]))

(def board [35 25])

(def snake {:direction [1 0]
            :body [[3 2] [2 2] [1 2] [0 2]]})

(def initial-state {:board board
                    :snake snake
                    :point (utils/rand-free-position snake board)
                    :points 0
                    :game true})

(register-handler
 :initialize
 (fn
   [db _]
   (merge db initial-state)))

(register-handler
 :next-state
 (fn
   [{:keys [snake board] :as db} _]
   (if (:game db)
     (if (utils/collisions snake board)
       (assoc-in db [:game] false)
       (-> db
           (update-in [:snake] utils/move-snake)
           (as-> after-move
               (utils/process-move after-move))))
     db)))

(register-handler
 :change-direction
 (fn [db [_ new-direction]]
   (update-in db [:snake :direction]
              (partial utils/change-snake-direction new-direction))))

(defonce key-handler
  (events/listen js/window "keydown"
                 (fn [e]
                   (let [key-code (.-keyCode e)]
                     (when (contains? utils/key-code->move key-code)
                       (dispatch [:change-direction (utils/key-code->move key-code)]))))))


(register-sub
 :board
 (fn
   [db _]
   (reaction (:board @db))))

(register-sub
 :snake
 (fn
   [db _]
   (reaction (:body (:snake @db)))))

(register-sub
 :point
 (fn
   [db _]
   (reaction (:point @db))))

(register-sub
 :points
 (fn
   [db _]
   (reaction (:points @db))))

(register-sub
 :game
 (fn
   [db _]
   (reaction (:game @db))))
