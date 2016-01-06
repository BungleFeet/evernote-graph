(defproject evernote-graph "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clojurenote "0.4.0"]
                 [clojurewerkz/propertied "1.2.0"]
                 [com.orientechnologies/orientdb-core "2.1.8"]
                 [org.allenai.tinkerpop.blueprints/blueprints-core "2.7.1"]
                 [com.orientechnologies/orientdb-graphdb "2.1.8"]
                 [net.java.dev.jna/jna "4.2.1"]
                 [net.java.dev.jna/jna-platform "4.2.1"]
                 [com.googlecode.concurrentlinkedhashmap/concurrentlinkedhashmap-lru "1.4.2"]
                 [org.clojure/java.data "0.1.1"]]
  :main ^:skip-aot evernote-graph.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
