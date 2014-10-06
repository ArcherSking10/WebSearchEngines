package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Vector;

class QueryHandler implements HttpHandler {
    private static String plainResponse =
            "Request received, but I am not smart enough to echo yet!\n";

    private Ranker _ranker;
    private HashMap<InetSocketAddress,Integer> session = new HashMap<InetSocketAddress,Integer>();

    public QueryHandler(Ranker ranker) {
        _ranker = ranker;
    }

    public static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (!requestMethod.equalsIgnoreCase("GET")) {  // GET requests only.
            return;
        }

        // Print the user request header.
        Headers requestHeaders = exchange.getRequestHeaders();
        System.out.print("Incoming request: ");
        for (String key : requestHeaders.keySet()) {
            System.out.print(key + ":" + requestHeaders.get(key) + "; ");
        }
        String queryResponse = "";
        InetSocketAddress remote = exchange.getRemoteAddress();
        String uriQuery = exchange.getRequestURI().getQuery();
        String uriPath = exchange.getRequestURI().getPath();
        String fileName = null;
        String responseType = "text/plain";

        if ((uriPath != null) && (uriQuery != null)) {
            if (uriPath.equals("/search")) {
                Map<String, String> query_map = getQueryMap(uriQuery);
                Set<String> keys = query_map.keySet();
                Vector<ScoredDocument> sds = null;
                if (keys.contains("query")) {
                    int sessionId = -1;
                    if(session.containsKey(remote)){
                        sessionId=session.get(remote);
                    }else{
                        sessionId=(int) Math.abs(Math.random()*10000);
                        session.put(remote, sessionId);
                    }
                    if (keys.contains("ranker")) {
                        String ranker_type = query_map.get("ranker");
                        // @CS2580: Invoke different ranking functions inside your
                        // implementation of the Ranker class.
                        if (ranker_type.equals("cosine")) {
                            sds = _ranker.runqueryCosine(query_map.get("query"));
                            fileName = "hw1.1-vsm.tsv";
                        } else if (ranker_type.equals("QL")) {
                            sds = _ranker.runqueryQL(query_map.get("query"));
                            fileName = "hw1.1-ql.tsv";
                        } else if (ranker_type.equals("phrase")) {
                            sds = _ranker.runqueryPhrase(query_map.get("query"));
                            fileName = "hw1.1-phrase.tsv";
                        } else if (ranker_type.equals("linear")) {
                            sds = _ranker.runqueryLinear(query_map.get("query"));
                            fileName = "hw1.2-linear.tsv";
                        } else {
                            sds = _ranker.runqueryNumView(query_map.get("query"));
                            fileName = "hw1.1-numviews.tsv";
                        }
                    } else {
                        // @CS2580: The following is instructor's simple ranker that does not
                        // use the Ranker class.
                       sds = _ranker.runquery(query_map.get("query"));
                    }

                    //Write ranker output to file
                    FileUtil.write(fileName, queryResponse);

                    //Write render details to log file
                    new FileUtil().writeRenderLogs(sessionId, query_map.get("query"), sds);

                    if(keys.contains("format") && query_map.get("format").equals("html")) {
                        String response = HtmlOutput.getResponse(sds, query_map.get("query"), sessionId);
                        queryResponse += response;
                        responseType = "text/html";
                    } else {
                        queryResponse = queryResponseGenerator(sds, query_map.get("query"));
                    }
                }
            } else if(uriPath.equals("/url")) {
                Map<String, String> query_map = getQueryMap(uriQuery);
                int sessionId = 0, did = 0;
                String query=null;

                if(query_map.containsKey("sessionId")&&query_map.containsKey("did")&&query_map.containsKey("query")){
                    Document d = _ranker.getDocument(did);
                    sessionId = Integer.parseInt(query_map.get("sessionId"), 32);
                    did = Integer.parseInt(query_map.get("did"), 32);
                    query = query_map.get("query").replace("+", " ");
                    String line=sessionId+"\t"+query+"\t"+did+"\t"+"CLICK"+"\t"+System.currentTimeMillis();
                    new FileUtil().writeClickLogs(line+"\n");

                    queryResponse = line+"\n\n";

                    queryResponse+="Title\n"+d.get_title_string()+"\n\n";
                    String body="";
                    for(String terms : d.get_body_vector()){
                        body+=terms+" ";
                    }
                    queryResponse+="Body\n"+body+"\n";
                }

            }
        }

        // Construct a simple response.
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", responseType);
        exchange.sendResponseHeaders(200, 0);  // arbitrary number of bytes
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(queryResponse.getBytes());
        responseBody.close();
    }

    private String queryResponseGenerator(Vector<ScoredDocument> sds, String query) {
        Iterator<ScoredDocument> itr = sds.iterator();
        String queryResponse="";

        while (itr.hasNext()) {
            ScoredDocument sd = itr.next();
            if (queryResponse.length() > 0) {
                queryResponse = queryResponse + "\n";
            }
            queryResponse = queryResponse + query + "\t" + sd.asString();
        }
        if (queryResponse.length() > 0) {
            queryResponse = queryResponse + "\n";
        }

        return queryResponse;
    }
}
