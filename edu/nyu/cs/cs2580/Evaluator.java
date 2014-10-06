package edu.nyu.cs.cs2580;

import java.io.*;
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
        evaluateStdin(relevance_judgments, relevance_judgments_2, System.in);



    }

    public static void readRelevanceJudgments(
            String p,HashMap < String , HashMap < Integer , Double > > relevance_judgments,
            HashMap < String , HashMap < Integer , Double > > relevance_judgments_2){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(p));
            try {
                String line = null;
                while ((line = reader.readLine()) != null ){
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
                        rel2= 10.0;
                    }
                    if(grade.equals("Excellent")) {
                        rel2= 7.0;
                    }
                    if(grade.equals("Good")) {
                        rel2= 5.0;
                    }
                    if(grade.equals("Fair")) {
                        rel2= 1.0;
                    }
                    if(grade.equals("Bad")) {
                        rel2= 0.0;
                    }
                    if (!relevance_judgments.containsKey(query)){
                        HashMap < Integer , Double > qr = new HashMap < Integer , Double >();
                        relevance_judgments.put(query,qr);
                        HashMap < Integer , Double > qr2 = new HashMap < Integer , Double >();
                        relevance_judgments_2.put(query,qr2);
                    }
                    HashMap < Integer , Double > qr = relevance_judgments.get(query);
                    qr.put(did,rel);
                    HashMap < Integer , Double > qr2 = relevance_judgments_2.get(query);
                    qr2.put(did, rel2);
                }
            } finally {
                reader.close();
            }
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public static void evaluateStdin(
            HashMap < String , HashMap < Integer , Double > > relevance_judgments, HashMap < String ,
            HashMap < Integer , Double > > relevance_judgments_2, InputStream path){
        // only consider one query per call
        StringBuffer input = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(path));

            String line = null;
            double RR = 0.0;
            double N = 0.0;
            String query="";
            StringBuffer output = new StringBuffer();
            while ((line = reader.readLine()) != null && !line.equalsIgnoreCase("sanghavistop")){
                input.append(line);
                input.append("\n");
                Scanner s = new Scanner(line).useDelimiter("\t");
                query = s.next();
                int did = Integer.parseInt(s.next());

                if (!relevance_judgments.containsKey(query)){
                    throw new IOException("query not found");
                }
                HashMap < Integer , Double > qr = relevance_judgments.get(query);
                if (qr.containsKey(did)){
                    RR += qr.get(did);
                }
                ++N;
            }

            File file = new File ("../results/input-to-evaluator.tsv");
            if (file.exists()) {
                file.delete();
            }
            FileUtil.write("input-to-evaluator.tsv", input.toString());
            // CALL THE EVALUATORS

            double v1 = evaluatePrecision(1, relevance_judgments, "./results/input-to-evaluator.tsv");
            double v2 = evaluatePrecision(5, relevance_judgments, "./results/input-to-evaluator.tsv");
            double v3 = evaluatePrecision(10, relevance_judgments, "./results/input-to-evaluator.tsv");
            double v4 = evaluateRecall(1, relevance_judgments, "./results/input-to-evaluator.tsv");
            double v5 = evaluateRecall(5, relevance_judgments, "./results/input-to-evaluator.tsv");
            double v6 = evaluateRecall(10, relevance_judgments, "./results/input-to-evaluator.tsv");
            double v7 = evaluateFMeasure(1, relevance_judgments, 0.5, "./results/input-to-evaluator.tsv");
            double v8 = evaluateFMeasure(5, relevance_judgments, 0.5, "./results/input-to-evaluator.tsv");
            double v9 = evaluateFMeasure(10, relevance_judgments, 0.5, "./results/input-to-evaluator.tsv");
            HashMap<Double,Double> prg = evaluatePrecisionRecallGraph(relevance_judgments, "./results/input-to-evaluator.tsv");
            double v10 = evaluateAveragePrecision(relevance_judgments, "./results/input-to-evaluator.tsv");
            double v11 = evaluateNDCG(1, relevance_judgments_2, "./results/input-to-evaluator.tsv");
            double v12 = evaluateNDCG(5, relevance_judgments_2, "./results/input-to-evaluator.tsv");
            double v13 = evaluateNDCG(10, relevance_judgments_2, "./results/input-to-evaluator.tsv");
            double v14 = evaluateReciprocalRank(relevance_judgments, "./results/input-to-evaluator.tsv");

            System.out.println(v1 +"\t" +  v2 +"\t" + v3+"\t" +v4+"\t" +v5+"\t" +v6+"\t" +v7+"\t" +v8+"\t" +v9+"\t"
                    +v10+"\t" +v11+"\t" +v12+"\t" +v13+"\t" +v14);

            output.append(query+"\t");
            output.append(v1 +"\t" +  v2 +"\t" + v3+"\t" +v4+"\t" +v5+"\t" +v6+"\t" +v7+"\t" +v8+"\t" +v9+"\t");
            for(double i = 0.0; i <= 1.0; i = i + 0.1){
                double t = prg.get(i);
                output.append(t +"\t");
            }
            output.append(+v10+"\t" +v11+"\t" +v12+"\t" +v13+"\t" +v14);

            FileUtil.write("output-file-please-rename.tsv", output.toString());

        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static double evaluatePrecision(int k, HashMap<String, HashMap<Integer,Double>> relevance_judgments,
                                           String path){
        double value = 0.0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            int RR = 0;
            String readLineString = null;
            for(int i=0; i<k; i++){
                readLineString = reader.readLine();
                Scanner s = new Scanner(readLineString).useDelimiter("\t");
                String query = s.next();
                int did = s.nextInt();

                if(!relevance_judgments.containsKey(query)){
                    throw new IOException("query not found");
                }
                HashMap<Integer,Double> qr = relevance_judgments.get(query);
                if(qr.containsKey(did) && qr.get(did) == 1){
                    RR++;
                }
            }
            if (k!=0) {
                value = (double)RR/k;
            }
            reader.close();
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
                                        String path){
        double value = 0.0;
        int countRelevance = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            int RR = 0;
            String readLineString = null;
            for(int i=0; i<k; i++){
                readLineString = reader.readLine();
                Scanner s = new Scanner(readLineString).useDelimiter("\t");
                String query = s.next();
                int did = s.nextInt();

                if(!relevance_judgments.containsKey(query)){
                    throw new IOException("query not found");
                }
                HashMap<Integer,Double> qr = relevance_judgments.get(query);
                countRelevance = 0;
                for (int key : qr.keySet()) {
                    if (qr.get(key)>0.0){
                        countRelevance++;
                    }
                }
                if(qr.containsKey(did) && qr.get(did) > 0.0){
                    RR++;
                }
            }
            if(countRelevance != 0){
                value = (double)RR/countRelevance;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }


    public static double evaluateFMeasure(int k,HashMap<String,HashMap<Integer,Double>> relJud, double lambda,  String path){
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
                                                   String path){
        double value = 0.0;
        double AP = 0.0;
        int RR = 0;
        int i=0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));

            String readLineString = reader.readLine();
            while(readLineString!=null){
                i++;
                Scanner s = new Scanner(readLineString).useDelimiter("\t");
                String query = s.next();
                int did = s.nextInt();

                if(!relevance_judgments.containsKey(query)){
                    throw new IOException("query not found");
                }
                readLineString = reader.readLine();
                HashMap<Integer,Double> qr = relevance_judgments.get(query);
                if(qr.containsKey(did) && qr.get(did) == 1.0){
                    RR++;
                    AP = AP + (double)RR/i;
                }
            }
            if(RR != 0){
                value = AP/RR;
            }
            reader.close();
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
                                                 String path){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            int count=0;
            String readLineString = null;
            while(reader.readLine()!=null){
                count++;
                readLineString = reader.readLine();
                Scanner s = new Scanner(readLineString).useDelimiter("\t");
                String query = s.next();
                int did = s.nextInt();

                if(!relevance_judgments.containsKey(query)){
                    throw new IOException("query not found");
                }

                HashMap<Integer,Double> qr = relevance_judgments.get(query);
                if(qr.containsKey(did)){
                    return reciprocal((double)count);
                }
            }
            reader.close();
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
                                                                      String path){
        double precision = 0.0;
        double recall = 0.0;
        int countRelevance = 0;
        HashMap<Double,Double> PR=new HashMap<Double,Double>();
        HashMap<Double,Double> value=new HashMap<Double,Double>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            int RR = 0;
            int K=1;
            String readLineString = null;
            while(reader.readLine()!=null){
                readLineString=reader.readLine();
                Scanner s = new Scanner(readLineString).useDelimiter("\t");
                String query = s.next();
                int did = s.nextInt();

                if (!relevance_judgments.containsKey(query)){
                    throw new IOException("query not found");
                }
                HashMap < Integer , Double > qr = relevance_judgments.get(query);
                countRelevance=0;
                for (Integer key : qr.keySet()) {
                    if (qr.get(key)> 0.0){
                        countRelevance++;
                    }
                }
                if (qr.containsKey(did)){
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

            for(double j=0.0;j<=1.0;j+=0.1){
                double max=0.0;
                for (double key : PR.keySet()) {
                    if((key>=j)&&(PR.get(key)>max)){
                        max=PR.get(key);
                    }
                }
                value.put(j ,max);
            }
            reader.close();
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
                                      String path){
        double NDCG = 0.0, IDCG=0.0;

        List<Double> relevance = new ArrayList<Double>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            double DCG = 0.0;
            String readLineString = null;
            for(int i=0; i<k; i++){
                readLineString = reader.readLine();
                Scanner s = new Scanner(readLineString).useDelimiter("\t");
                String query = s.next();
                int did = s.nextInt();

                if(!relevance_judgments_2.containsKey(query)){
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

            reader.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return NDCG;
    }

//
//    /**
//     * MY VERSION OF PRECISION RECALL. WE CAN CHECK IF RESULTS FROM THIS ARE SAME AS THE ABOVE METHOD.
//     * @param relevance_judgments
//     * @param path
//     * @return
//     */
//    public static HashMap<Double,Double> evaluatePrecisionRecallGraph_MINE(HashMap<String, HashMap<Integer,Double>> relevance_judgments,
//                                                                           String path){
//        double precision = 0.0;
//        double recall = 0.0;
//        int countRelevance = 0;
//        HashMap<Double,Double> PR=new HashMap<Double,Double>();
//        HashMap<Double,Double> value=new HashMap<Double,Double>();
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(path));
//            int RR = 0;
//            int K=0;
//            String readLineString = null;
//            while(reader.readLine()!=null){
//                K++;
//                readLineString=reader.readLine();
//                precision = evaluatePrecision(K, relevance_judgments, path);
//                recall = evaluateRecall(K, relevance_judgments, path);
//
//                if(!PR.containsKey(recall)){
//                    PR.put(recall, precision);
//                    if(recall==1.0){
//                        break;
//                    }
//                }
//            }
//
//            for(double j=0.1;j<=1.0;j+=0.1){
//                double max=0.0;
//                for (double key : PR.keySet()) {
//                    if((key>=j)&&(PR.get(key)>max)){
//                        max=PR.get(key);
//                    }
//                }
//                value.put(j,max);
//            }
//
//            reader.close();
//
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return value;
//    }


}
