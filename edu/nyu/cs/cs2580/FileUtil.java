package edu.nyu.cs.cs2580;

import java.io.*;
import java.util.Vector;

public class FileUtil {

    private static String rootPath;

   // public FileUtil()
    static{
        rootPath = "./results/";
        File file = new File(rootPath);

        File logs = new File(rootPath+"hw1.4-log.tsv");

        if(!logs.exists()) {
            try {
                logs.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Create the path if the path does not exist
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    public static String read(String filename, String results) {
        StringBuilder fileContents = new StringBuilder();
        createFile(filename);
        try {
            BufferedReader bufferReader = new BufferedReader(new FileReader(rootPath+filename));
            String line = null;
            try{
                while((line = bufferReader.readLine())!=null){
                    fileContents.append(line);
                    fileContents.append("\n");
                }
            }finally{
                bufferReader.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return fileContents.toString();
    }

    public static void write(String filename, String results){
        createFile(filename);
        try{
            BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(rootPath+filename,true));
            bufferWriter.write(results);
            bufferWriter.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //For Render Logging
    public void writeRenderLogs(int sessionId, String query, Vector<ScoredDocument> documents){
        try {
            BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(rootPath+"hw1.4-log.tsv",true));
            for(ScoredDocument doc : documents){
                String line = sessionId+"\t"+query+"\t"+doc._did+"\t"+"RENDER"+"\t"+System.currentTimeMillis()+"ms\r\n";
                bufferWriter.append(line);
            }
            bufferWriter.flush();
            bufferWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeClickLogs(String line) {
        try{
            BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(rootPath+"hw1.4-log.tsv",true));
            bufferWriter.append(line);
            bufferWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createFile(String filename) {
        File file = new File(rootPath+filename);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
