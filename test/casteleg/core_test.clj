(ns casteleg.core-test
  (:require [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [casteleg.core :as casteleg]))

(def message-to-stop
  {:text     "/stop",
   :entities [{:offset 0, :length 5, :type "bot_command"}]})

(def message-to-url
  {:text     "https://youtu.be/LsBrT6vbQa8",
   :entities [{:offset 0, :length 28, :type "url"}]})


(deftest parse-message-test
  (fact
    (casteleg/parse-message message-to-url)
    => {:cmd :go :url "https://youtu.be/LsBrT6vbQa8"})
  (fact
    (casteleg/parse-message message-to-stop)
    => {:cmd :go :url "about:blank"}))
