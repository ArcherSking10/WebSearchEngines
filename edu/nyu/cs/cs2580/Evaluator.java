package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

class Evaluator {

    public static void main(String[] args) throws IOException {
        HashMap < String , HashMap < Integer , Double > > relevance_judgments =
                new HashMap < String , HashMap < Integer , Double > >();
        HashMap < String , HashMap < Integer , Double > > relevance_judgments_2 =
                new HashMap < String , HashMap < Integer , Double > >();

        if (args.length < 1){
            System.out.println("need to provide relevance_judgments");
            return;
        }
        String p = args[0];
        // first read the relevance judgments into the HashMap
        readRelevanceJudgments(p,relevance_judgments, relevance_judgments_2);
        // now evaluate the results from stdin
        evaluateStdin(relevance_judgments, System.in);

        double v1 = evaluatePrecision(1, relevance_judgments, System.in);
        double v2 = evaluatePrecision(5, relevance_judgments, System.in);
        double v3 = evaluatePrecision(10, relevance_judgments, System.in);
        double v4 = evaluateRecall(1, relevance_judgments, System.in);
        double v5 = evaluateRecall(5, relevance_judgments, System.in);
        double v6 = evaluateRecall(10, relevance_judgments, System.in);
        double v7 = evaluateFMeasure(1, relevance_judgments, 0.5, System.in);
        double v8 = evaluateFMeasure(5, relevance_judgments, 0.5, System.in);
        double v9 = evaluateFMeasure(10, relevance_judgments, 0.5, System.in);
        double v10 = evaluateAveragePrecision(relevance_judgments, System.in);
        double v11 = evaluateNDCG(1, relevance_judgments, System.in);
        double v12 = evaluateNDCG(5, relevance_judgments, System.in);
        double v13 = evaluateNDCG(10, relevance_judgments, System.in);
        double v14 = evaluateReciprocalRank(relevance_judgments, System.in);

        System.out.println(v1 +"\t" +  v2 +"\t" + v3+"\t" +v4+"\t" +v5+"\t" +v6+"\t" +v7+"\t" +v8+"\t" +v9+"\t"
                +v10+"\t" +v11+"\t" +v12+"\t" +v13+"\t" +v14);

    }

    public static void readRelevanceJudgments(
            String p,HashMap < String , HashMap < Integer , Double > > relevance_judgments,
            HashMap < String , HashMap < Integer , Double > > relevance_judgments_2){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(p));
            try {
                String line = null;
                while ((line = reader.readLine()) != null){
                    // parse the query,did,relevance line
                    Scanner s = new Scanner(line).useDelimiter("\t");
                    String query = s.next();
                    int did = Integer.parseInt(s.next());
                    String grade = s.next();
                    double rel = 0.0;
                    double rel2= 0.0;
                    // convert to binary relevance
                    if ((grade.equals("Perfect")) ||
                            (grade.equals("Excellent")) ||
                            (grade.equals("Good"))){
                        rel = 1.0;
                    }
                    if (grade.equals("Perfect")) {
                        rel2= 10;
                    }
                    if(grade.equals("Excellent")) {
                        rel2= 7;
                    }
                    if(grade.equals("Good")) {
                        rel2= 5;
                    }
                    if(grade.equals("Fair")) {
                        rel2= 1;
                    }
                    if(grade.equals("Bad")) {
                        rel2= 0;
                    }
                    if (relevance_judgments.containsKey(query) == false){
                        HashMap < Integer , Double > qr = new HashMap < Integer , Double >();
                        relevance_judgments.put(query,qr);
                        relevance_judgments_2.put(query,qr);
                    }
                    HashMap < Integer , Double > qr = relevance_judgments.get(query);
                    qr.put(did,rel);
                    qr=relevance_judgments_2.get(query);
                    qr.put(did, rel2);

                }
            } finally {
                reader.close();
            }
        } catch (IOException ioe){
            System.err.println("Oops " + ioe.getMessage());
        }
    }

    public static void evaluateStdin(
            HashMap < String , HashMap < Integer , Double > > relevance_judgments, InputStream path){
        // only consider one query per call

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(path));

            String line = null;
            double RR = 0.0;
            double N = 0.0;
            while ((line = reader.readLine()) != null){
                Scanner s = new Scanner(line).useDelimiter("\t");
                String query = s.next();
                int did = Integer.parseInt(s.next());
                String title = s.next();
                double rel = Double.parseDouble(s.next());
                if (relevance_judgments.containsKey(query) == false){
                    throw new IOException("query not found");
                }
                HashMap < Integer , Double > qr = relevance_judgments.get(query);
                if (qr.containsKey(did) != false){
                    RR += qr.get(did);
                }
                ++N;
            }

            // CALL THE EVALUATORS

            System.out.println(Double.toString(RR/N));

        } catch (Exception e){
            System.err.println("Error:" + e.getMessage());
        }
    }


    public static double evaluatePrecision(int k, HashMap<String, HashMap<Integer,Double>> relevance_judgments,
                                           InputStream path){
        double value = 0.0;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(path));
            int RR = 0;
            String readLineString = null;
            for(int i=0; i<k; i++){
                readLineString = reader.readLine();
                Scanner s = new Scanner(readLineString).useDelimiter("\t");
                String query = s.next();
                int did = s.nextInt();
                String title  = s.next();
                double rel = s.nextDouble();
                if(relevance_judgments.containsKey(query) == false){
                    throw new IOException("query not found");
                }
                HashMap<Integer,Double> qr = relevance_judgments.get(query);
                if(qr.containsKey(did)){
                    RR++;
                }
			/*
			if (k == 0) {
			    throw new IllegalArgumentException("Argument 'K' is 0");
			}
			value = (double)RR/k;
			*/

                if (k!=0) {
                    value = (double)RR/k;
                }

            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }

    public static double evaluateRecall(int k, HashMap<String, HashMap<Integer,Double>> relevance_judgments,
                                        InputStream path){
        double value = 0.0;
        int countRelevance = 0;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(path));
            int RR = 0;
            String readLineString = null;
            for(int i=0; i<k; i++){
                readLineString = reader.readLine();
                Scanner s = new Scanner(readLineString).useDelimiter("\t");
                String query = s.next();
                int did = s.nextInt();
                String title  = s.next();
                double rel = s.nextDouble();
                if(relevance_judgments.containsKey(query) == false){
                    throw new IOException("query not found");
                }
                HashMap<Integer,Double> qr = relevance_judgments.get(query);
                countRelevance = 0;
                for (int key : qr.keySet()) {
                    if (qr.get(key)>0.0){
                        countRelevance++;
                    }
                }
                if(qr.containsKey(did)){
                    RR++;
                }
            }
            if(countRelevance != 0){
                value = (double)RR/countRelevance;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }


    public static double evaluateFMeasure(int k,HashMap<String,HashMap<Integer,Double>> relJud, double lambda,  InputStream path){
        double result = 0.0;
        try{
            double precision = evaluatePrecision(k, relJud, path);
            double recall = evaluateRecall(k, relJud, path);
            if((precision > 0.0) && (recall > 0.0)) {
                result = 1 / (lambda * reciprocal(precision)+(1-lambda) * reciprocal(recall));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //Returns the reciprocal of a number
    public static double reciprocal(double x) {
        return (1/x);
    }

    public static double evaluateAveragePrecision( HashMap<String, HashMap<Integer,Double>> relevance_judgments,
                                                   InputStream path){
        double value = 0.0;
        double AP = 0.0;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(path));
            int RR = 0;
            int i=0;
            String readLineString = null;
            while(reader.readLine()!=null){
                i++;
                readLineString = reader.readLine();
                Scanner s = new Scanner(readLineString).useDelimiter("\t");
                String query = s.next();
                int did = s.nextInt();
                String title  = s.next();
                double rel = s.nextDouble();
                if(!relevance_judgments.containsKey(query)){
                    throw new IOException("query not found");
                }
                HashMap<Integer,Double> qr = relevance_judgments.get(query);
                if(qr.containsKey(did)){
                    RR++;
                    AP = AP + (double)RR/i;
                }
            }
            if(RR != 0){
                value = (double)AP/RR;
            }



        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }

    public static double evaluateReciprocalRank( HashMap<String, HashMap<Integer,Double>> relevance_judgments,
                                                 InputStream path){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(path));
            int i=0;
            String readLineString = null;
            while(reader.readLine()!=null){
                i++;
                readLineString = reader.readLine();
                Scanner s = new Scanner(readLineString).useDelimiter("\t");
                String query = s.next();
                int did = s.nextInt();
                String title  = s.next();
                double rel = s.nextDouble();
                if(relevance_judgments.containsKey(query) == false){
                    throw new IOException("query not found");
                }
                HashMap<Integer,Double> qr = relevance_judgments.get(query);
                if(qr.containsKey(did)){
                    return reciprocal((double)i);
                }
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0.0;
    }

    public static HashMap<Double,Double> evaluatePrecisionRecallGraph(HashMap<String, HashMap<Integer,Double>> relevance_judgments,
                                                                      InputStream path){
        double precision = 0.0;
        double recall = 0.0;
        int countRelevance = 0;
        HashMap<Double,Double> PR=new HashMap<Double,Double>();
        HashMap<Double,Double> value=new HashMap<Double,Double>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(path));
            int RR = 0;
            int K=1;
            String readLineString = null;
            while(reader.readLine()!=null){
                readLineString=reader.readLine();
                Scanner s = new Scanner(readLineString).useDelimiter("\t");
                String query = s.next();
                int did = s.nextInt();
                String title = s.next();
                double rel = s.nextDouble();
                if (relevance_judgments.containsKey(query) == false){
                    throw new IOException("query not found");
                }
                HashMap < Integer , Double > qr = relevance_judgments.get(query);
                countRelevance=0;
                for (Integer key : qr.keySet()) {
                    if (qr.get(key)> 0.0){
                        countRelevance++;
                    }
                }
                if (qr.containsKey(did) != false){
                    RR++;
                }
                recall=(double)RR/countRelevance;
                precision=(double)RR/K;
                if(!PR.containsKey(recall)){
                    PR.put(recall, precision);
                    if(recall==1.0){
                        break;
                    }
                }
                K++;
            }

            for(double j=0.1;j<=1.0;j+=0.1){
                double max=0.0;
                for (double key : PR.keySet()) {
                    if((key>=j)&&(PR.get(key)>max)){
                        max=PR.get(key);
                    }
                }
                value.put(j,max);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }


    public static double evaluateNDCG(int k, HashMap<String, HashMap<Integer,Double>> relevance_judgments_2,
                                      InputStream path){
        double NDCG = 0.0;
        List<Double> relevance = new ArrayList<Double>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(path));
            double DCG = 0.0;
            String readLineString = null;
            for(int i=0; i<k; i++){
                readLineString = reader.readLine();
                Scanner s = new Scanner(readLineString).useDelimiter("\t");
                String query = s.next();
                int did = s.nextInt();
                String title  = s.next();
                double rel = s.nextDouble();
                if(relevance_judgments_2.containsKey(query) == false){
                    throw new IOException("query not found");
                }
                HashMap<Integer,Double> qr = relevance_judgments_2.get(query);
                if(qr.containsKey(did)){
                    if (i==0) {
                        DCG = qr.get(did);
                    }
                    else {
                        DCG = DCG + qr.get(did) / Math.log(i + 1) / Math.log(2);
                    }
                    relevance.add(qr.get(did));
                }
                else {
                    relevance.add(0.0);
                }
            }

            double IDCG=0.0;
            Collections.sort(relevance, Collections.reverseOrder());
            for (int i=0; i<k; i++) {
                if (i==0) {
                    IDCG = relevance.get(i);
                }
                else {
                    IDCG = IDCG + relevance.get(i) / Math.log(i + 1) / Math.log(2);
                }
            }

            if (IDCG != 0.0)
                NDCG = DCG / IDCG;

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return NDCG;
    }


    /**
     * MY VERSION OF PRECISION RECALL. WE CAN CHECK IF RESULTS FROM THIS ARE SAME AS THE ABOVE METHOD.
     * @param relevance_judgments
     * @param path
     * @return
     */
    public static HashMap<Double,Double> evaluatePrecisionRecallGraph_MINE(HashMap<String, HashMap<Integer,Double>> relevance_judgments,
                                                                           InputStream path){
        double precision = 0.0;
        double recall = 0.0;
        int countRelevance = 0;
        HashMap<Double,Double> PR=new HashMap<Double,Double>();
        HashMap<Double,Double> value=new HashMap<Double,Double>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(path));
            int RR = 0;
            int K=0;
            String readLineString = null;
            while(reader.readLine()!=null){
                K++;
                readLineString=reader.readLine();
                precision = evaluatePrecision(K, relevance_judgments, path);
                recall = evaluateRecall(K, relevance_judgments, path);

                if(!PR.containsKey(recall)){
                    PR.put(recall, precision);
                    if(recall==1.0){
                        break;
                    }
                }
            }

            for(double j=0.1;j<=1.0;j+=0.1){
                double max=0.0;
                for (double key : PR.keySet()) {
                    if((key>=j)&&(PR.get(key)>max)){
                        max=PR.get(key);
                    }
                }
                value.put(j,max);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }


}
