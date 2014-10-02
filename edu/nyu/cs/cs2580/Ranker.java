package edu.nyu.cs.cs2580;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Scanner;

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

    public Vector<ScoredDocument> runqueryNumView(String query) {
        Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
        for (int i = 0; i < _index.numDocs(); ++i) {
            retrieval_results.add(runqueryNumView(query, i));
        }
        return retrieval_results;
    }

    public ScoredDocument runqueryNumView(String query, int did) {
        // Get the document vector. For hw1, you don't have to worry about the
        // details of how index works.
        Document d = _index.getDoc(did);
        Vector<String> dv = d.get_title_vector();

        double score=d.get_numviews();
        return new ScoredDocument(did, d.get_title_string(), score);
    }

    public Vector<ScoredDocument> runqueryQL(String query) {
        Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
        for (int i = 0; i < _index.numDocs(); ++i) {
            retrieval_results.add(runqueryQL(query, i));
        }
        return retrieval_results;
    }

    public ScoredDocument runqueryQL(String query, int did) {

        float lambda = 0.5f;
        Scanner s = new Scanner(query);
        Vector<String> qv = new Vector<String>();
        while (s.hasNext()) {
            String term = s.next();
            qv.add(term);
        }

        Document d = _index.getDoc(did);
        Vector<String> dv = d.get_body_vector();

        Long totalWordsInDocument = 0l;
        HashMap<String, Integer> wordFrequency = new HashMap<String, Integer>();
        for (String word : dv) {
            totalWordsInDocument++;
            if (!wordFrequency.containsKey(word)) {
                wordFrequency.put(word, 1);
            }
            else {
                wordFrequency.put(word, wordFrequency.get(word) + 1);
            }
        }

        double score = 0.0;
        for (String wordInQuery : dv) {
            score = score + Math.log(((1-lambda)*(wordFrequency.get(wordInQuery)/totalWordsInDocument))
                    +(lambda * _index.termFrequency(wordInQuery)/_index.termFrequency()));
        }

        return new ScoredDocument(did, d.get_title_string(), score);
    }
}