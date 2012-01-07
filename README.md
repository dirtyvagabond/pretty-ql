PrettyQL is a domain specific language for working with Factual's public API. It is similar to SQL... except it's a Lisp.

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