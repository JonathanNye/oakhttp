package oak.http;

// User: jnye
// Date: 9/18/12
// Time: 10:57 AM
// Copyright (c) 2012 WillowTree Apps, Inc. All rights reserved.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OAKResponse<T> {

    private T payload;
    private boolean fromCache;
    private String error;
    private Map<String, List<String>> headers = new HashMap<String,List<String>>();

    public OAKResponse(HttpURLConnection conn, AbstractOAKRequest<T> request) throws IOException {
        headers = conn.getHeaderFields();
        // TODO fromCache
        try {
            this.payload = request.parseResponse(conn.getInputStream());
        } catch(IOException e1) {
            try {
                this.error = streamToString(conn.getErrorStream());
            } catch (IOException e2) {
                throw e1;
            }
        }
    }

    public T getData() {
        return payload;
    }

    public String getError() {
        return error;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    private String streamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        String s;

        while(true) {
            s = buf.readLine();
            if(s == null) {
                break;
            }
            sb.append(s);
        }
        buf.close();

        return sb.toString();
    }

}
