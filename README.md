Cooking with Clojure
====================

When I speak to developers about functional programming, they're often interested, but are
sometimes concerned that functional programming might make it hard to model the "real world".
The theory goes that the everyday world is full of objects, so the most natural way to describe
it is using object-oriented design. 

Leaving aside the question of whether or not programmers should be overly concerned about
this thing referred to as the "real world", functional programming provides a rich set of
concepts that are more than adequate to model complex domains. 

As an example, I'll use Clojure to describe a recipe. Clojure is a variant of Lisp designed
to run on the Java Virtual Machine, and it has the key ingredient that makes functional
programming possible - functions that can be passed around as values, and that reliably
return the same output for the same inputs.

    (+ 2 3)
      ;=> 5

    (reduce + [2 3 4])
      ;=> 9

Each stage in the recipe will be represented as a simple hash map. The following represents
the ingredients for pancakes (measured in grams):

    {:flour 150, :milk 300, :eggs 100}

But we're modelling a process, not a fixed state, so we also need a way to depict time and
change. The following represents the ingredients for pancakes, five minutes into the recipe.

    {:time 5, :flour 150, :milk 300, :eggs 100}

The process of making pancakes can then be represented as a series of states: 

    [{:time 0},
     {:time 1, :flour 150},
     {:time 3, :flour 150, :eggs 100},
     {:time 5, :flour 150, :milk 300, :eggs 100}]

But how do we get from one state to another? This is where the functions come in. Functions are
just a way of representing a mapping from one state to another. Here is a simple function
that represents the passage of time by incrementing the `:time` key of a dish by a specified
number of minutes:  

    (defn takes [dish minutes]
      (-> dish (update-in [:time] #(+ % minutes))))

We can use this function to build others. Here is a function that mixes in an ingredient into
a dish. The original dish is not changed; we just create a new state of the dish containing
the new ingredient and its attributes:

    (defn mix-in [dish ingredient attributes]
      (-> dish (assoc ingredient attributes)))

But remember, functions are themselves values in a functional programming language, so we can
represent the addition of a particular ingredient as a function. Note that `add` is a function
that takes the ingredient and its attributes as arguments, and returns another function that
represents the actual addition. Clojure has no good way to print functions, so it's forced to
use a somewhat cryptic representation:

    (defn add [ingredient attributes]
      (fn [dish] (-> dish (mix-in ingredient attributes) (takes 1))))

    (add :water 300)
      ;=> #<user$add$fn__329 user$add$fn__329@316ae291>

    (def add-some-water (add :water 300)) 

`add-some-water` is now a function representing adding some water. The function also
increments the time taken so far in the recipe. We can use it to transform one state into
another:

    (add-some-water {:time 0, :rice 100})
      ;=> {:time 1, :rice 100, :water 300}

We can represent any stage in our recipe as a function of one state to another:

    (defn sit [minutes]
      (fn [dish] (-> dish (takes minutes))))

    (defn water-for [ingredient]
      (fn [dish]
        (let [water (-> dish ingredient :weight (* 2))]
          (-> dish ((add :water {:millilitres water})) (takes 2)))))

    (def drain
      (fn [dish] (-> dish (dissoc :water) (takes 3))))

The recipe is therefore just a list of functions:

    (def recipe
      [(add :beans {:weight 150})
       (water-for :beans)
       (add :bicarbonate {:teaspooons 1})
       (sit (* 12 60))
       drain])

To work out how the dish changes over the course of its preparation, we just need to
successively apply each step to an initial state. Clojure's standard library has a function
called `reductions` that does that for us, returning a list of all the successive states.

    (defn preparations [steps]
      (let [perform (fn [dish step] (step dish))]
        (reductions perform {:time 0} steps)))

    (preparations recipe)
      ;=>[{:time 0},
      ;   {:time 1, :flour 150},
      ;   {:time 3, :flour 150, :eggs 100},
      ;   {:time 5, :flour 150, :milk 300, :eggs 100}]

To prepare a receipe, we just need to take the final state:

    (defn prepare [steps] (last (preparations steps)))

    (prepare recipe)
      ;=> {:time 5, :flour 150, :milk 300, :eggs 100}]
