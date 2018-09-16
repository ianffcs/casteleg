(ns casteleg.core
  (:require [clojure.core.async :as async]
            [clojure.data.json :as json]
            [etaoin.api :as eta]
            [etaoin.keys :as ks])
  (:import (java.util Date)))

(def telegram-token
  (System/getenv "TELEGRAM_TOKEN"))

(defn telegram-request
  ([method]
   (format "https://api.telegram.org/bot%s/%s"
           telegram-token (name method)))
  ([method params]
   (telegram-request method)))

(defn telegram-response
  [response]
  (-> response
      (json/read-str :key-fn keyword)
      :result))

(defmulti run (fn [{:keys [cmd]} _] cmd))

(defmethod run :go
  [{:keys [url]} driver]
  (eta/go driver url))

(defmethod run :default
  [err _]
  (prn [:error err]))

(defn init-browser
  []
  (let [port (async/chan)
        browser (eta/firefox)]
    (async/go-loop [cmd (async/<! port)]
      (prn [:cmd cmd])
      (when cmd
        (try
          (run cmd browser)
          (catch Throwable e
            (prn e)))
        (recur (async/<! port))))
    port))

(defn after?
  [x y]
  (neg? (compare x y)))

(defn parse-message
  [{:keys [entities text]}]
  (let [{:keys [type offset length]} (first entities)
        data (subs text offset length)]
    (cond
      (= type "url") {:cmd :go :url data}
      (= type "bot_command") (case (keyword (subs data 1))
                               :stop {:cmd :go :url "about:blank"}))))

(defn watch-telegram
  [now browser]
  (future (loop [now now]
            (Thread/sleep 5000)
            (let [{:keys [date]
                   :as   message} (-> :getUpdates telegram-request slurp telegram-response last :message)
                  inst (new Date (* 1000 date))]
              (if (after? now inst)
                (do (async/>!! browser (parse-message message))
                    (recur inst))
                (recur now))))))

(defonce browser (atom nil))
(defonce telegram (atom nil))

(defn -main
  [& args]
  (assert (string? telegram-token) "Please set your env var 'TELEGRAM_TOKEN'")
  (swap! telegram (fn [x] (when (future? x) (future-cancel x)) nil))
  (swap! browser (fn [x] (when x (async/close! x)) nil))
  (reset! browser (init-browser))
  (reset! telegram (watch-telegram (new Date) @browser)))
