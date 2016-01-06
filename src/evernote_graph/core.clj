(ns evernote-graph.core
  (:require
    [clojurewerkz.propertied.properties :as p]
    [clojure.java.io :as io]
    [clojurenote.notes :as notes])
  (:import (com.tinkerpop.blueprints.impls.orient OrientGraphFactory OrientVertex OrientElement OrientBaseGraph)
           (com.evernote.edam.type Notebook))
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn load-auth-properties
  "Load the access-token, notestore-url and notestore object from the app configuration"
  []
  (let [auth (-> (io/resource "auth.properties")
                 (p/load-from)
                 (p/properties->map true))
        notestore (notes/create-note-store (:notestore-url auth))]
    (assoc auth :notestore notestore)))

(defonce en-user (load-auth-properties))

(defonce conn (-> (OrientGraphFactory. "plocal:./db")
                  (.setupPool 1 10)))

(defn with-graph-txn! [update-fn]
  (let [graph (.getTx conn)]
    (try
      (let [result (update-fn graph)]
        (.commit graph)
        result)
      (catch Exception _ (.rollback graph)))))

(defn with-graph! [graph-fn]
  (let [graph (.getNoTx conn)]
    (graph-fn graph)))

(defn get-vertex-type [^String name]
  (with-graph! (fn [^OrientBaseGraph graph]
                 (.getVertexType graph name))))

(defn create-vertex-type [^String name]
  (when (nil? (get-vertex-type name))
    (with-graph! (fn [^OrientBaseGraph graph]
                   (.createVertexType graph name)))))

(defonce notebook-type (create-vertex-type "Notebook"))

(defn get-nested-prop [object path]
  (let [object-map (bean object)
        path-seq (seq path)
        prop-key (first path-seq)
        value (if (empty? path-seq) object (prop-key object-map))
        remaining-path (rest path-seq)]
    (if (empty? remaining-path)
      (bean value)
      (recur value remaining-path))))

(defn keywordify [m]
  (into {} (map (fn [[k v]] [(keyword k) v]) m)))

(defn en-object-graph-id [object]
  (let [obj-class (.getSimpleName (.getClass object))
        obj-guid (.getGuid object)]
    (str "class:" obj-class "," obj-guid)))

(defn en-object-props [object]
  (condp instance? object
    Notebook {:name (.getName object)}))

(defn set-vertex-props! [^OrientVertex vertex props]
  (doseq [[k v] (map identity props)]
    (. vertex (setProperty (name k) v))))

(defn graph-object-props [^OrientElement element]
  (keywordify (into {} (.getProperties element))))

(defn add-vertex! [object]
  (let [v-id (en-object-graph-id object)
        v-props (en-object-props object)
        update-fn (fn [graph]
                    (let [vertex (.addVertex graph v-id)]
                      (set-vertex-props! vertex v-props)
                      (.commit graph)
                      (graph-object-props vertex)))]
    (with-graph-txn! update-fn)))

(defn vertices []
  (with-graph-txn! (fn [graph]
                     (let [verts (.getVertices graph)]
                       (map graph-object-props (seq verts))))))