# About

PrettyQL is a domain specific language for working with Factual's public API. It is similar to SQL... except it's a Lisp.

Here's a Pretty statement that finds restaurants near a lat lon, where each restaurant delivers dinner, sorted by distance:

```clojure
(select restaurants-us  
  (around {:lat 34.06021 :lon -118.4183 :miles 3})
  (where              
    (= :meal_deliver true)
    (= :meal_dinner true))
  (order :$distance)    
  (limit 3))
```

# Installation

PrettyQL is hosted at [Clojars](http://clojars.org/pretty-ql). Add this to your project dependencies:

````clojure
[pretty-ql "1.0.0"]
````

Now you can use Pretty on the REPL:

````clojure
(use 'pretty-ql.core)
````

Or in your project:

````clojure
(ns your-ns
  ...
  (require [pretty-ql.core :as pretty])
  ...)
````

# Query Composition

Pretty provides support for composing queries. For example, imagine you want to define a base query that finds U.S. restaurants that have valid owners and telephones:

````clojure
(def base (-> (select* "restaurants-us")
              (fields :name :owner :tel)
              (where
                (not-blank :owner)
                (not-blank :tel))))
````

Running that query is as easy as using <tt>exec</tt>:

````clojure
(exec base)
````

But you can also define a new query that adds to <tt>base</tt>. For example, let's define a new query that uses <tt>base</tt> but adds a filter for non-null websites, and also adds a sort based on website:

````clojure
(def websites
  (-> base
    (where 
      (not-blank :website))
    (order :website)))
````

Now you can run the <tt>websites</tt> query if you want...

````clojure
(exec websites)
````

... and you can go on to create a new query that uses websites but, say, adds a limit clause:

````clojure
(def a-few-sites (-> websites (limit 3)))
````

You can run that at anytime:

````clojure
(exec a-few-sites)
````

And the previous <tt>base</tt> and <tt>websites</tt> queries are unchanged, so you can continuing building new queries off of them as well.
