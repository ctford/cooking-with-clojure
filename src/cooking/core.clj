(ns cooking.core)

    (def y 3)

    (+ 2 y)
      ;=> 5

    (reduce + [2 3 4 5])
      ;=> 14 

    (= y (+ y 1))
      ;=> false

    (defn plus-one [n] (+ 1 n))

    (plus-one 4)
      ;=> 5

    (defn plus [incrementor]
      (fn [n] (+ incrementor n)))

    (def plus-three (plus 3))

    (plus-three 4)
      ;=> 7

    (defn safe-plus [n] (fnil (plus n) 0))

    ((safe-plus 4) nil)
      ;=> 4

    {:butterbeans 150, :water 300}

    {:time 5, :butterbeans 150, :water 300}

    [{:time 0},
     {:time 1, :butterbeans 150},
     {:time 3, :butterbeans 150, :water 300}]

    (defn mix-in [dish ingredient quantity]
      (update-in dish [ingredient] (safe-plus quantity)))

    (mix-in {:time 1, :butterbeans 150} :water 300)
      ;=> {:time 1, :butterbeans 150, :water 300} 

    (defn add [ingredient quantity duration]
      (fn [dish] (mix-in (mix-in dish ingredient quantity) :time duration)))

    (add :water 300 1)
      ;=> #<user$add$fn__329 user$add$fn__329@316ae291>

    (def add-some-water (add :water 200 1)) 

    (add-some-water {:time 0, :butterbeans 100})
      ;=> {:time 1, :butterbeans 100, :water 200}


    (defn fry [minutes]
      (fn [dish]
        (update-in
          (assoc dish :temperature 50)
          [:water]
          (plus (- minutes)))))

    (def room-temperature 21)

    (defn sit [minutes]
      (fn [dish]
        (let [temperature (max
                            (- (:temperature dish) (* 2 minutes))
                            room-temperature)]
        (mix-in (assoc dish :temperature temperature) :time minutes)))) 
    
    (defn water-for [ingredient]
      (fn [dish]
        (let [quantity (* 2 (ingredient dish))]
          ((add :water quantity 3) dish))))

    (defn soak [ingredient minutes]
      (fn [dish]
        (let [absorbtion (/ (:water dish) 2)
              absorb (comp
                       (add :water (- absorbtion) 0)
                       (add ingredient absorbtion minutes))]
         (absorb dish))))

    (defn drain []
      (fn [dish]
        (mix-in (dissoc dish :water) :time 3)))

    (def recipe
      [(add :beans 150 1)
       (water-for :beans)
       (soak :beans (* 4 60))
       (drain)
       (add :water 50 1)
       (add :garlic 5 1)
       (fry 5)
       (add :olive-oil 5 1)
       (sit 10)])

    (defn preparations [steps]
      (let [perform (fn [dish step] (step dish))]
        (reductions perform {:time 0} steps)))

    (preparations recipe)
      ;=> [{:time 0}
      ;    {:beans {:weight 150}, :time 1}
      ;    {:beans {:weight 150}, :time 720}
      ;    {:beans {:weight 150}, :time 3}]

    (defn prepare [steps] (last (preparations steps)))

    (prepare recipe)
      ;=> {:beans {:weight 150}, :time 3}
