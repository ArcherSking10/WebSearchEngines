package edu.nyu.cs.cs2580;

import java.util.Vector;

public class HtmlOutput {
    static String template="<p><a href=URL>TITLE</a>RANK</p>";

    public static String getResponse(Vector<ScoredDocument> documents, String query, int identifier){
        StringBuilder response=new StringBuilder();
        response.append("<html>\n<body>\n");
        response.append("Query: "+query+"\n");
        for(ScoredDocument doc : documents){
            String url="url?"+"sessionId="+Integer.toHexString(identifier)+"&did="+Integer.toHexString(doc._did)+"&query="+query.replace(" ", "+");
            String line=template.replace("URL", url);
            line = line.replace("TITLE", doc._title);
            line = line.replace("RANK", "\t" + Double.toString(doc._score));
            response.append(line);
        }
        response.append("</body>\n</html>\n");
        return response.toString();
    }
}
