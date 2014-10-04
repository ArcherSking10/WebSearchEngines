package edu.nyu.cs.cs2580;

import java.io.*;

public class FileUtil {

    private String rootPath;

    public FileUtil() {
        rootPath = "../../../../results/";
        File file = new File(rootPath);

        //Create the path if the path does not exist
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    public String read(String filename, String results) {
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

    public void write(String filename, String results){
        createFile(filename);
        try{
            FileWriter fileWriter = new FileWriter(filename, true);
            BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(rootPath+filename,true));
            bufferWriter.write(results);
            bufferWriter.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void createFile(String filename) {
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
