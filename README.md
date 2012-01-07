# About

PrettyQL is a domain specific language for working with Factual's public API. It is similar to SQL... except it's a Lisp.

Here's an example of using PrettyQL to find restaurants near a lat lon that deliver dinner, sorted by distance:

    ```clojure
    (select restaurants-us  
               (fields :name :category :price :rating :$distance)
               (around {:lat 34.06021 :lon -118.4183 :miles 3})
               (where              
                   (= :meal_deliver true)
                   (= :meal_dinner true))
               (order :$distance)    
               (limit 3))

# Installation

HoneyQL is hosted at [Clojars](http://clojars.org/honey-ql). Just add this to your dependencies:

	[honey-ql "1.0.0"]

# More examples

    ```clojure
    (select :places (where (= :name "starbucks"))

    (select :restaurants-us
               (where
                   (like :name "starbucks*")
                   (= :locality "los angeles"))
               (order :postcode)
               (limit 12))

    (select restaurants-us
               (fields :name :owner)
               (where
                   (= :locality "los angeles")
                   (not-blank :owner)))

(select places
  (circle {:center [34.06021 -118.4183]
           :miles 3}))

(select places
  (around {:lat 34.06021 :lon -118.4183 :miles 3}))

(def base (-> (select* "restaurants-us")
                           (fields :name :owner :tel)
                           (where
                               (not-blank :owner)
                               (not-blank :tel))))

(def websites (-> base
                        (where (not-blank :website)) (order :website)))

(def a-few-sites (-> websites
                          (limit 3)))