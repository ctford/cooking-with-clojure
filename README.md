Cooking with Clojure
====================

When I speak to developers about functional programming, they're often interested, but are
sometimes concerned that functional programming might make it hard to model the "real world".
The theory goes that the everyday world is full of objects that evolve over time, so the most
natural way to describe it is using object-oriented design. 

Leaving aside the question of whether or not programmers should be overly concerned about
this thing referred to as the "real world", functional programming provides a rich set of
concepts that are more than adequate for modelling complex domains. 

As a demonstration of functional design, I'll use Clojure to describe a recipe. Clojure is a
variant of Lisp designed to run on the Java Virtual Machine, and it has the key ingredient
that makes functional programming possible - functions that can be passed around as values, and
that reliably return the same output for the same inputs.

Here are a couple of simple examples of Clojure:

    (def y 3)

    (+ 2 y)
      ;=> 5

    (reduce + [2 3 4 5])
      ;=> 14 

The first example is straightforward enough, though it might seem strange that the function `+`
appears inside the braces and in the first position. That's the way that all Lisps depict
function invocation.

The second example has another curious aspect - `+` is being passed as an argument to the
`reduce` function, which uses it to boil down a list of numbers into a single total. Using
functions as values that can be passed around isn't possible in many object-oriented
programming languages like Java, but it turns out to be very useful. 

Of course, simple expressions on their own aren't very useful. Here is how to define a named
function in Clojure:

    (defn plus-one [n] (+ 1 n))

    (plus-one 4)
      ;=> 5

Clojure functions can themselves return functions. Here's a function that makes plus functions.
Note that while `defn` defines a named function, `fn` creates an anonymous function.

    (defn plus [x]
      (fn [n] (+ n x)))

    (def plus-three (plus 3))

    (plus-three 4)
      ;=> 7

Each stage in the recipe will be represented as a simple hash map. The following represents
butterbeans with some water added (measured in grams):

    {:butterbeans 150, :water 300}

But we're modelling a process, not a fixed state, so we also need a way to depict time and
change. The following represents the same ingredients, five minutes into the recipe.

    {:time 5, :butterbeans 150, :water 300}

The process of preparing a recipe can then be represented as a series of states:

    [{:time 0},
     {:time 1, :butterbeans 150},
     {:time 3, :butterbeans 150, :water 300}]

But how do we get from one state to another? This is where the functions come in. Functions are
just a way of representing a mapping from one state to another. Here is a simple function
that represents mixing in a certain amount of an ingredient:

    (defn mix-in [dish ingredient quantity]
      (assoc dish ingredient quantity))

    (mix-in {:time 1, :butterbeans 150} :water 300)
      ;=> {:time 1, :butterbeans 150, :water 300} 

There's no need to overwrite the original state of the dish. Instead of having objects with
identity that morph and mutate over time, functions take the original state and produce a new
state. In the example above, `mix-in` takes a dish that has one minute of elapsed time and 150
grams of butterbeans, and produced a new state that had one minute of elapsed time, 150
grams of butterbeans and 300 grams of water. 

Remember, functions are themselves values in a functional programming language, so we can
represent the addition of a particular ingredient as a function. Note that `add` is a function
that takes the ingredient and its quantity as arguments, and returns another function that
represents the actual addition. Clojure has no good way to print functions, so it's forced to
use a somewhat cryptic identifier when dislaying a function to the screen:

    (defn add [ingredient quantity]
      (fn [dish] (mix-in (mix-in dish ingredient quantity) :time 1)))

    (add :water 300)
      ;=> #<user$add$fn__329 user$add$fn__329@316ae291>

    (def add-some-water (add :water 200)) 

`add-some-water` is now a function representing the addition of some water. The function also
increments the time taken so far in the recipe. We can use it to transform one state into
another:

    (add-some-water {:time 0, :butterbeans 100})
      ;=> {:time 1, :butterbeans 100, :water 200}

We can represent any step in our recipe as a function of one state to another. `sit` leaves
the dish to sit for a certain number of minutes:

    (defn sit [minutes] (add :time minutes))
    
`water-for` adds water to the dish based on the weight of a certain ingredient:

    (defn water-for [ingredient]
      (fn [dish]
        (let [quantity (* 2 (ingredient dish))]
          (comp (add :water quantity) (add :time 2)))))

`water-for` builds its function by composing together the addition of water and the addition
of time using Clojure's `comp` function. Composing together two functions means that the
output of one is passed as the input to the other, forming a single, composite function.

`drain` just removes all water from the dish:

    (def drain
      (fn [dish] (mix-in (dissoc dish :water) 3)))
    
The recipe is therefore just a list of functions:

    (def recipe
      [(add :beans {:weight 150})
       (water-for :beans)
       (sit (* 12 60))
       drain])

To work out how the dish changes over the course of its preparation, we just need to
progressively apply each step to an initial state, which in this case is `{:time 0}`.
Clojure's standard library has a function called `reductions` that does that for us, returning 
a list of all the successive states.

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

One advantage of representing a process like this is that we are modelling each state
explicitly. For example, if we wanted to calculate what ingredients had been added halfway
through the preparation, we could. If our dish had been a mutable object, then each time
we performed a new step in the recipe the old state would have been lost.

Paradoxically, by avoiding changing individual values, functional programming languages make
representing change itself easier. Though functional programming can be used in any domain
that object-oriented programming, I have personally found that domains where time and change
are important concepts to be where functional programming languages like Clojure really have
the edge over using mutable objects.
