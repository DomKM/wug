(ns domkm.wug.markov
  (:refer-clojure :exclude [< > <= >= = not=])
  (:require [clojure.core :as clj]
            [clojure.string :as str]))

(def n-gram-range (range 1 4))

(defn n-grams
  ([s] (mapcat #(n-grams % s) n-gram-range))
  ([n s] (map #(apply str %)
              (partition n 1 s))))

(def n-gram-frequencies
  (let [words (->> (slurp "resources/words")
                   str/split-lines
                   (filter #(clj/= % (str/lower-case %)))
                   set)]
    (frequencies (mapcat n-grams words))))

(def n-gram-sums
  (reduce-kv (fn [coll k v]
               (let [n (count k)]
                 (assoc coll n (+ v (coll n)))))
             (zipmap n-gram-range (repeat 0))
             n-gram-frequencies))

(defn n-gram-probability [n-gram]
  (/ (n-gram-frequencies n-gram 0)
     (n-gram-sums (count n-gram))))

(defn word-probability [word]
  (->> (n-grams word)
       (map n-gram-probability)
       (reduce *)))

(defn ^:private def-comparator-fn [sym]
  `(defn ~sym [& args#]
     (apply ~(symbol "clojure.core" (name sym))
            (map word-probability args#))))

(defmacro ^:private def-comparator-fns []
  `(do
     ~@(map def-comparator-fn '[< > <= >= = not=])))

(def-comparator-fns)
