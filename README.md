Cooking with Clojure
====================

    (defn takes [dish minutes]
      (-> dish (update-in [:time] #(+ % minutes))))

    (defn sit [minutes]
      (fn [dish] (-> dish (takes minutes))))

    (defn add [ingredient attributes]
      (fn [dish] (-> dish (assoc ingredient attributes) (takes 1))))

    (defn water-for [ingredient]
      (fn [dish]
        (let [water (-> dish ingredient :weight (* 2))]
          (-> dish ((add :water {:millilitres water})) (takes 2)))))

    (def drain
      (fn [dish] (-> dish (dissoc :water) (takes 3))))

    (def recipe
      [(add :beans {:weight 150})
       (water-for :beans)
       (add :bicarbonate {:teaspooons 1})
       (sit (* 12 60))
       drain])

    (defn preparations [steps]
      (let [perform (fn [dish step] (step dish))]
        (reductions perform {:time 0} steps)))

    (defn prepare [steps] (last (preparations steps)))

    (preparations recipe) ; A timeline of all states.
    (prepare recipe)      ; The final result.
