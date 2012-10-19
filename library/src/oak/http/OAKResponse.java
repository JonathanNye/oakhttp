package oak.http;

// User: jnye
// Date: 9/18/12
// Time: 10:57 AM
// Copyright (c) 2012 WillowTree Apps, Inc. All rights reserved.

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OAKResponse<T> {

    private T payload;
    private boolean followUpExpected;
    private boolean isFollowUp;
    private boolean fromCache;
    private Map<String, List<String>> headers = new HashMap<String,List<String>>();

    public OAKResponse(HttpURLConnection conn, AbstractOAKRequest<T> request) throws Exception {
        headers = conn.getHeaderFields();
        followUpExpected = request.hasFollowUp();
        isFollowUp = request.getIsFollowUpRequest();
        // TODO fromCache
        this.payload = request.parseResponse(conn.getInputStream());
    }

    public T getData() {
        return payload;
    }

    public boolean isFollowUpExpected() {
        return followUpExpected;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public boolean isFollowUp() {
        return isFollowUp;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

}