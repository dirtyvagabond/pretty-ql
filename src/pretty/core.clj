(ns pretty.core
  (:require [funnyplaces.api :as fun]
            [clojure.walk :as walk]
            [clojure.string :as str]))

;; --- pretty helpers ---

;; assumes s always like "term*"
;; PS: Laura says: iloveyou
(defn like-str->m [s]
  {:$bw (apply str (butlast s))})

;; assumes s always like "term*"
(defn not-like-str->m [s]
  {:$nbw (apply str (butlast s))})

(defn pred
  "Creates the most common predicate spec. %1 is field name, %2 is val.
   Example use in fn lookup table:
   '> (pred :$gt)
   Example final result when used to interpret (... (> :rank 8.5) ...):
   {:rank {:$gt 8.5}}"
  [pred-name]
  `#(hash-map %1 {~pred-name %2}))

;; --- pretty fn translation tables ---

(def keywords
  {'where 'pretty.core/where
   'fields 'pretty.core/fields
   'order 'pretty.core/order
   'search 'pretty.core/search
   'offset 'pretty.core/offset
   'limit 'pretty.core/limit
   'circle 'pretty.core/circle
   'around 'pretty.core/around})

(def preds {'and 'pretty.core/+and  
            'or 'pretty.core/+or
            'search 'pretty.core/search
            'like 'pretty.core/+like
            'not-like 'pretty.core/+not-like
            'blank 'pretty.core/+blank
            'not-blank 'pretty.core/+not-blank
            '= (pred :$eq)
            'not= (pred :$neq)
            '> (pred :$gt)
            '>= (pred :$gte)
            '< (pred :$lt)
            '<= (pred :$lte)
            'in (pred :$in)
            'not-in (pred :$nin)})

;; --- pretty fns ---

(defn fields [q & forms]
  ;;;(update-in q [:select] into (map name forms))
  (assoc q :select (str/join "," (map name forms)))
  )

(defn order [q & forms]
  (assoc q :sort (str/join "," (map name forms))))

(defn +or [& clauses]
  {:$or (vec clauses)})

(defn +and [& clauses]
  {:$and (vec clauses)})

(defn search
  "Supports 2 variants of full text search:

   1) At top level of query, so FTS across row, like:
   (select ... (search \"myterm\"))
   arg1 will be the query map, arg2 the search term.

   2) Within the where clause, so for a specific field, like:
   (select ... (where ... (search :tel \"myterm\")))
   arg1 will be the field name, arg2 the searh term."
  [arg1 arg2]
  (if (map? arg1)
    (assoc arg1 :q arg2)
    {arg1 {:$search arg2}}))

(defn +like [field term]
  {field (like-str->m term)})

(defn +not-like [field term]
  {field (not-like-str->m term)})

(defn +blank [field]
  {field {:$blank true}})

(defn +not-blank [field]
  {field {:$blank false}})

(defn limit [q limit]
  (assoc q :limit limit))

(defn offset [q offset]
  (assoc q :offset offset))

(defn circle
  "Adds a geo proximity filter to query q.
   Expects a hash-map describing a circle, like:
    {:center [40.73 -74.01]
     :meters 5000}

   Supports one of :miles or :meters"
  [q circ]
  (let [meters (or (:meters circ) (* (:miles circ) 1609.344))
        center {:$center (:center circ) :$meters meters}]
    (assoc q :geo {:$circle center})))

(defn around
  "Adds a geo proximity filter to query q.
   Expects a hash-map describing a circle, like:
   {:lat 34.06021 :lon -118.4183 :miles 3}
  
   Only supports :miles"
  [q {:keys [lat lon miles]}]
  (circle q {:center [lat lon] :miles miles}))

;; --- pretty DSL ---

(defn init!
  "Establishes your oauth credentials for Factual. You only need to
   do this once for the lifetime of your application."
  [key secret]
  (fun/factual! key secret))

(defn pretty!
  "DEPRECATED: Use 'init!' instead"
  [key secret]
  {:added "0.0.1"
   :deprecated "1.0.4"}
  (fun/factual! key secret))

(defn exec [query]
  (let [table (:table (meta query))]
    (fun/fetch-q table query)))

(defn select* [table]
  (with-meta
    {}
    {:type :select
     :table table}))

(defn where*
  "Add a where clause to the query. Clause can be either a map or a string, and
  will be AND'ed to the other clauses."
  [query clause]
  (update-in query [:filters] conj clause))

(defmacro where [query & clauses]
  (let [xform (walk/postwalk-replace preds clauses)]
    `(update-in ~query [:filters] merge ~@xform)))

(defmacro select [table & clauses]
  (let [xform (walk/postwalk-replace keywords clauses)]
    `(let [query# (-> (select* ~(name table)) ~@xform)]
       (exec query#))))

(defmacro schema [table]
  `(fun/schema ~(name table)))

(defn resolve
  "Direct support for Resolve. values must be a hashmap, where keys
   are valid attributes for the schema, and values are the values on
   which to match."
  [values]
  (fun/resolve values))