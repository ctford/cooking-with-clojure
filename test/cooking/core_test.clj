(ns cooking.core-test
  (:use clojure.test
        cooking.core))

(deftest check-result 
  (testing
    (is (=
         (prepare recipe)
         {:olive-oil 5, :temperature 30, :garlic 5, :water 45, :beans 300, :time 258}))))
