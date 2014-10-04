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

    public Vector<ScoredDocument> runqueryCosine(String query) {
        Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
        for (int i = 0; i < _index.numDocs(); ++i) {
            retrieval_results.add(runqueryCosine(query, i));
        }
        return sort(retrieval_results);
    }

    public ScoredDocument runqueryCosine(String query, int did) {
        double cumulativeWt = 0.0, cumulativeQW = 0.0, score=0.0, dotProd = 0.0, magnitude = 0.0;

        HashMap<String, Integer> query_weight_map = new HashMap<String, Integer>();
        HashMap<String, Integer> document_weight_map = new HashMap<String, Integer>();
        Vector<String> query_vector = new Vector<String>();

        Document d = _index.getDoc(did);

        //Document vectors.
        Vector < String > dv = d.get_title_vector();
        Vector < String > db = d.get_body_vector();

        //Compute query weight and create query vector
        Scanner s = new Scanner(query);
        while(s.hasNext()) {
            String query_term=s.next();
            if(_index.termFrequency(query_term)>0) {
                query_vector.add(query_term);
                if(query_weight_map.containsKey(query_term)) {
                    //Increment the query weight for the term if it already exists
                    query_weight_map.put(query_term, query_weight_map.get(query_term)+1);
                } else {
                    query_weight_map.put(query_term, 1);
                }
            }
        }

        //Document weight
        for(int i = 0; i < dv.size(); i++) {
            if(document_weight_map.containsKey(dv.get(i))){
                document_weight_map.put(dv.get(i), document_weight_map.get(dv.get(i))+1);
            }else{
                document_weight_map.put(dv.get(i), 1);
            }
        }

        for(int i = 0; i < db.size(); i++) {
            if(document_weight_map.containsKey(db.get(i))) {
                document_weight_map.put(db.get(i), document_weight_map.get(db.get(i))+1);
            } else{
                document_weight_map.put(db.get(i), 1);
            }
        }

        for (int i = 0; i < db.size(); ++i){
            int tf = 0, df;
            double idf, weight, qw;

            df = Document.documentFrequency(db.get(i));

            idf = 1+Math.log(_index.numDocs()/df)/Math.log(2);

            if(document_weight_map.containsKey(db.get(i))){
                tf = document_weight_map.get(db.get(i));
            }

            weight = tf*idf;
            cumulativeWt += Math.pow(weight, 2);
            if(query_weight_map.containsKey(db.get(i))){
                qw = query_weight_map.get(db.get(i))*idf;
                dotProd += qw * weight;
                cumulativeQW += Math.pow(qw, 2);
            }
        }

        magnitude = cumulativeQW * cumulativeWt;

        if((magnitude) != 0){
            score = dotProd/Math.sqrt(magnitude);
        }

        return new ScoredDocument(did, d.get_title_string(), score);
    }

    public Vector<ScoredDocument> runqueryLinear(String query) {
        Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
        for (int i = 0; i < _index.numDocs(); ++i) {
            retrieval_results.add(runqueryLinear(query, i));
        }
        return sort(retrieval_results);
    }

    public ScoredDocument runqueryLinear(String query, int did) {
        ScoredDocument cosine = runqueryCosine(query, did);
        ScoredDocument QL = runqueryQL(query, did);
        ScoredDocument phrase = runqueryPhrase(query, did);
        ScoredDocument numviews = runqueryNumView(query, did);

        double score = Constants.BETA_1 * cosine.getScore() + Constants.BETA_2 * QL.getScore()
                + Constants.BETA_3 * phrase.getScore() + Constants.BETA_4 * numviews.getScore();

        return new ScoredDocument(did, _index.getDoc(did).get_title_string(), score);
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



    public double cosineRanker(Vector < String > qv, HashMap<String, Integer> query_weight, int did){
        Document d = _index.getDoc(did);
        double query_w = 0.0;
        double weight = 0.0;
        int doc_num = _index.numDocs();

        double IDF = 0.0;
        double all_termw = 0.0;
        double all_queryw = 0.0;
        double all_dot_product = 0.0;
        double cosine = 0.0;

        HashMap<String, Integer>doc_frequency = new HashMap<String, Integer>();
        Vector < String > dv = d.get_title_vector();
        Vector < String > db = d.get_body_vector();


        /*get current document term and frequency*/
        for(int i = 0; i < dv.size(); i++){
            if(doc_frequency.containsKey(dv.get(i))){
                doc_frequency.put(dv.get(i), doc_frequency.get(dv.get(i))+1);
            }else{
                doc_frequency.put(dv.get(i), 1);
            }
        }

        for(int i = 0; i < db.size(); i++){
            if(doc_frequency.containsKey(db.get(i))){
                doc_frequency.put(db.get(i), doc_frequency.get(db.get(i))+1);
            }else{
                doc_frequency.put(db.get(i), 1);
            }
        }

        Vector < Double > term_weight = new Vector < Double >();
        for (int i = 0; i < db.size(); ++i){
            int doc_f = Document.documentFrequency(db.get(i));
            IDF = 1+Math.log(doc_num/doc_f)/Math.log(2);
            int term_f = 0;

            if(doc_frequency.containsKey(db.get(i))){
                term_f = doc_frequency.get(db.get(i));
            }
            weight  = term_f*IDF;
            all_termw += Math.pow(weight, 2);
            if(query_weight.containsKey(db.get(i))){
                query_w = query_weight.get(db.get(i))*IDF;
                all_dot_product += query_w*weight;
                all_queryw +=Math.pow(query_w,2);
            }
        }
        all_termw = Math.sqrt(all_termw);
        all_queryw = Math.sqrt(all_queryw);
        if((all_queryw*all_termw) != 0){
            cosine = all_dot_product/(all_termw*all_queryw);
        }else{
            cosine = 0.0;
        }
        return cosine;
    }
}