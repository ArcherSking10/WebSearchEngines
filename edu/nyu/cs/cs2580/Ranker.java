package edu.nyu.cs.cs2580;

import java.util.*;

class Ranker {
    private Index _index;

    public Ranker(String index_source) {
        _index = new Index(index_source);
    }

    public Vector<ScoredDocument> runquery(String query) {
        Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
        for (int i = 0; i < _index.numDocs(); ++i) {
            retrieval_results.add(runquery(query, i));
        }
        return retrieval_results;
    }

    public ScoredDocument runquery(String query, int did) {

        // Build query vector
        Scanner s = new Scanner(query);
        Vector<String> qv = new Vector<String>();
        while (s.hasNext()) {
            String term = s.next();
            qv.add(term);
        }

        // Get the document vector. For hw1, you don't have to worry about the
        // details of how index works.
        Document d = _index.getDoc(did);
        Vector<String> dv = d.get_title_vector();


        // Score the document. Here we have provided a very simple ranking model,
        // where a document is scored 1.0 if it gets hit by at least one query term.
        double score = 0.0;
        for (int i = 0; i < dv.size(); ++i) {
            for (int j = 0; j < qv.size(); ++j) {
                if (dv.get(i).equals(qv.get(j))) {
                    score = 1.0;
                    break;
                }
            }
        }

        return new ScoredDocument(did, d.get_title_string(), score);
    }

    /**
     * This method will be called from QueryHandler.java. The job of this method is to
     * run the runqueryNumView(query, id) for every document in the corpus
     *
     * @param query The query words
     * @return The sorted results based on NumView
     */
    public Vector<ScoredDocument> runqueryNumView(String query) {
        Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
        for (int i = 0; i < _index.numDocs(); ++i) {
            retrieval_results.add(runqueryNumView(query, i));
        }
        return sort(retrieval_results);
    }

    /**
     * This method scores each document based on NumViews
     *
     * @param query The query words
     * @param did   The document id
     * @return The scored document
     */
    public ScoredDocument runqueryNumView(String query, int did) {
        // Get the document vector. For hw1, you don't have to worry about the
        // details of how index works.
        Document d = _index.getDoc(did);

        double score = d.get_numviews();

        return new ScoredDocument(did, d.get_title_string(), score);
    }

    public Vector<ScoredDocument> runqueryPhrase(String query) {
        Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
        for (int i = 0; i < _index.numDocs(); ++i) {
            retrieval_results.add(runqueryPhrase(query, i));
        }
        return sort(retrieval_results);
    }

    public ScoredDocument runqueryPhrase(String query, int did) {

        // Build query vector
        Scanner s = new Scanner(query);
        Vector<String> qv = new Vector<String>();
        while (s.hasNext()) {
            String term = s.next();
            qv.add(term);
        }

        // Get the document vector.
        Document d = _index.getDoc(did);
        Vector<String> dv = new Vector<String>();
        dv.addAll(d.get_title_vector());
        dv.addAll(d.get_body_vector());

        double score = 0.0;
        if (qv.size() == 1) {
            // Score the document as a unigram
            for (int i = 0; i < dv.size(); ++i) {
                for (int j = 0; j < qv.size(); ++j) {
                    if (dv.get(i).equals(qv.get(j))) {
                        score = 1.0;
                        break;
                    }
                }
            }
        } else {
            for (int i = 0; i < qv.size() - 1; i++) {
                for (int j = 0; j < dv.size() - 1; j++) {
                    if (dv.get(j).equals(qv.get(i)) && dv.get(j + 1).equals(qv.get(i + 1)))
                        score = score + 1;
                }
            }
        }

        return new ScoredDocument(did, d.get_title_string(), score);
    }

    /**
     * This method will be called from QueryHandler.java. The job of this method is to
     * run the runqueryQL(query, id) for every document in the corpus
     * @param query the query words
     * @return sorted list of scored documents
     */
    public Vector<ScoredDocument> runqueryQL(String query) {
        Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
        for (int i = 0; i < _index.numDocs(); ++i) {
            retrieval_results.add(runqueryQL(query, i));
        }
        return sort(retrieval_results);
    }

    /**
     * Method for scoring documents based on QL
     * @param query the query words
     * @param did the document id
     * @return the scored document
     */
    public ScoredDocument runqueryQL(String query, int did) {

        float lambda = 0.5f;
        Scanner s = new Scanner(query);
        Vector<String> qv = new Vector<String>();
        while (s.hasNext()) {
            String term = s.next();
            qv.add(term);
        }

        Document d = _index.getDoc(did);
        Vector<String> dv = new Vector<String>();
        dv.addAll(d.get_title_vector());
        dv.addAll(d.get_body_vector());

        double score = 0.0;
        for (String wordInQuery : qv) {
            int count = 0;
            for (String wordInDocument : dv) {
                if(wordInQuery.equals(wordInDocument)) {
                    count++;
                }
            }
            double doclike = (double) count / (double) dv.size();
            double termlike = (double) _index.termFrequency(wordInQuery) / (double) _index.termFrequency();
            score = score + Math.log(((1-lambda) * doclike) + (lambda * termlike));
        }

        return new ScoredDocument(did, d.get_title_string(), score);
    }

    /*
     * Method to sort the scored documents
     */
    private Vector<ScoredDocument> sort(Vector<ScoredDocument> scoredDocuments) {
        class scoreDocumentComparator implements Comparator<ScoredDocument> {

            @Override
            public int compare(ScoredDocument scoredDocument, ScoredDocument scoredDocument2) {

                double score = scoredDocument._score;
                double score2 = scoredDocument2._score;
                if (score == score2) {
                    return 0;
                } else if (score > score2) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }

        Collections.sort(scoredDocuments, new scoreDocumentComparator());

        return scoredDocuments;
    }
}