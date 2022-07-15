(ns pdf-gen-performance.core
  (:require
    [clj-pdf.core :refer [pdf]]
    [criterium.core :as criterium]
    [clojure.java.io :as io])
  (:import
    (java.awt
      Desktop
      HeadlessException)
    (java.io
      File)
    (com.lowagie.text Document Paragraph Anchor ElementTags Chunk HeaderFooter Phrase Element)
    (com.lowagie.text.pdf PdfWriter)))

(def last-file-name (atom ""))

(defn open [file-path]
  (try
    (let [file (io/file file-path)]
      (.open (Desktop/getDesktop) file)
      file)
    (catch HeadlessException e
      (throw (ex-info "There's no desktop." {:opening file-path} e)))))

(defn create-temp-file! [tag]
  (-> (File/createTempFile "performance-test-with-" tag ".pdf") .getCanonicalPath))

(defn create-pdf-data []
  (concat
    [{:title "Title",
      :font {:ttf-name "Serif"},
      :footer {:text "Footer | Page"}}
     [:spacer 28]
     [:paragraph
      {:align :center}
      [:heading
       {:style {:size 14, :style :bold}}
       "Heading"]
      [:paragraph
       [:anchor
        {:target "https://www.google.com/",
         :style {:color [0 114 228]}}
        "Link"]]
      [:paragraph "Description"]]]
    (apply concat
      (repeat 1000
        [[:pagebreak]
         [:spacer 28]
         [:paragraph
          {:align :center}
          [:heading
           {:style {:size 14, :style :bold}}
           "Heading"]
          [:paragraph
           [:anchor
            {:target "https://www.google.com/",
             :style {:color [0 114 228]}}
            "Link"]]
          [:paragraph "Description"]]]))))

(defn create-pdf [output-stream]
  (pdf (create-pdf-data) output-stream))

(defn create-pdf-with-clj-pdf-&-save []
  (let [file-name (create-temp-file! "clj-pdf")]
    (reset! last-file-name file-name)
    (with-open [output-stream (io/output-stream file-name)]
      (create-pdf output-stream)
      file-name)))

(defn create-pdf-with-openpdf []
  (let [file-name (create-temp-file! "openpdf")
        _ (reset! last-file-name file-name)
        document (Document.)
        _ (PdfWriter/getInstance document (io/output-stream file-name))]
    (doto document
      (.open)
      (.setFooter
        (doto (HeaderFooter. (Phrase. "Footer | Page ") true)
          (.setBorder 0)
          (.setAlignment ^int Element/ALIGN_RIGHT))))
    (dotimes [_ 1001]
      (doto document
        (.add (doto (Paragraph.)
                (.add (Chunk. ^String (apply str (repeat 25 "\n"))))
                (.setAlignment ElementTags/ALIGN_CENTER)
                (.add "Heading")
                (.add Chunk/NEWLINE)
                (.add (doto (Anchor. "Link")
                        (.setReference "https://www.google.com/")))
                (.add Chunk/NEWLINE)
                (.add "Description")))
        (.newPage)))
    (.close document)
    file-name))

(comment
  (open (create-pdf-with-clj-pdf-&-save))
  (open (create-pdf-with-openpdf)))

(defn -main [& args]
  (criterium/with-progress-reporting
    (criterium/bench (create-pdf-with-clj-pdf-&-save)
      :verbose))
  (criterium/with-progress-reporting
      (criterium/bench (create-pdf-with-openpdf)
        :verbose))
  (open @last-file-name)
  (println @last-file-name)
  (println "done"))

