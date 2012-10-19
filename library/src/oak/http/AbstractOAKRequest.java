package oak.http;

// User: jnye
// Date: 9/24/12
// Time: 4:47 PM
// Copyright (c) 2012 WillowTree Apps, Inc. All rights reserved.

import android.os.Handler;
import android.os.Message;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractOAKRequest<T> {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_POST = "POST";

    protected URL url;
    private boolean noCache;
    private boolean noStore;
    private boolean cacheOnly;
    private boolean isFollowUpRequest;
    private int maxStale = -1;
    private String method = "GET";
    private OAKRequest.FreshDataCallback<T> callback;
    private Map<String, String> headers = new HashMap<String, String>();
    private PostWriter postWriter;

    protected abstract AbstractOAKRequest<T> newInstance();

    //region getters and setters
    public URL getUrl() {
        return url;
    }

    public AbstractOAKRequest setNoCache(boolean noCache) {
        this.noCache = noCache;
        return this;
    }

    public AbstractOAKRequest setNoStore(boolean noStore) {
        this.noStore = noStore;
        return this;
    }

    public AbstractOAKRequest setMaxStale(int maxStale) {
        this.maxStale = maxStale;
        return this;
    }

    public AbstractOAKRequest setFreshDataCallback(FreshDataCallback<T> callback) {
        this.callback = callback;
        return this;
    }

    public AbstractOAKRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    public AbstractOAKRequest<T> setCacheOnly(boolean cacheOnly) {
        this.cacheOnly = cacheOnly;
        return this;
    }

    protected void setIsFollowUpRequest(boolean b) {
        this.isFollowUpRequest = b;
    }

    protected boolean getIsFollowUpRequest() {
        return isFollowUpRequest;
    }

    protected boolean hasFollowUp() {
        return !isFollowUpRequest && callback != null;
    }
    //endregion

    protected abstract T parseResponse(InputStream is) throws Exception;

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public static abstract class FreshDataCallback<T> extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof OAKResponse) {
                onFreshDataReceived((OAKResponse<T>) msg.obj);
            }
        }

        public abstract void onFreshDataReceived(OAKResponse<T> response);
    }

    private static abstract class PostWriter {
        abstract void configureConnection(HttpURLConnection conn);
        abstract void writeToStream(HttpURLConnection conn) throws Exception;
    }

    public void setPostData(Map<String, String> webFormPairs) {

        StringBuilder sb = new StringBuilder();
        String[] keys = (String[]) webFormPairs.keySet().toArray();
        for(int i = 0; i < keys.length; i++) {
            if(i > 0) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(keys[i]).replaceAll("\\+", "%20"));
            sb.append("=");
            sb.append(URLEncoder.encode(webFormPairs.get(keys[i])).replaceAll("\\+", "%20"));
        }
        setPostData(sb.toString());
    }

    public void setPostData(final InputStream stream) {

        postWriter = new PostWriter() {
            @Override
            void configureConnection(HttpURLConnection conn) {
                conn.setDoOutput(true);
                conn.setChunkedStreamingMode(1024);
            }

            @Override
            void writeToStream(HttpURLConnection conn) throws Exception {
                byte[] buff = new byte[1024];
                OutputStream out = conn.getOutputStream();
                while(stream.read(buff) > 0) {
                    out.write(buff);
                }
                out.close();
            }
        };
    }

    public void setPostData(final String s) {

        postWriter = new PostWriter() {

            byte[] data = s.getBytes();

            @Override
            void configureConnection(HttpURLConnection conn) {
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(data.length);
            }

            @Override
            void writeToStream(HttpURLConnection conn) throws Exception {
                conn.getOutputStream().write(data);
                conn.getOutputStream().close();
            }
        };
    }

    public OAKResponse<T> execute() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);

        addHeaders(conn);
        resolveRequestCacheSettings(conn);

        if(postWriter != null && METHOD_POST.equals(method)) {
            postWriter.configureConnection(conn);
            postWriter.writeToStream(conn);
        }

        OAKResponse<T> response = new OAKResponse<T>(conn, this);
        conn.disconnect();
        // Queue up fresh data request
        if (callback != null && !isFollowUpRequest) {
            final AbstractOAKRequest<T> freshReq = newInstance();
            freshReq.setMethod(method);
            freshReq.setMaxStale(maxStale);
            addHeaders(conn);
            freshReq.setNoStore(noStore);
            freshReq.setNoCache(true);
            freshReq.setIsFollowUpRequest(true);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        OAKResponse<T> resp = freshReq.execute();
                        callback.onFreshDataReceived(resp);
                    } catch (Exception e) {
                        // TODO So what here...?
                    }
                }
            }).start();
        }
        return response;
    }


    // TODO old Pragmas and other directives, max-age?
    private void resolveRequestCacheSettings(HttpURLConnection conn) {
        if (noStore) {
            conn.addRequestProperty("Cache-Control", "no-store");
        }
        if (cacheOnly || callback != null) {
            conn.addRequestProperty("Cache-Control", "only-if-cached");
        }
        if (noCache && !(cacheOnly || callback != null)) {
            conn.addRequestProperty("Cache-Control", "no-cache");
        }
        if (maxStale >= 0) {
            conn.addRequestProperty("Cache-Control", "max-stale=" + maxStale);
        }
    }

    private void addHeaders(HttpURLConnection conn) {
        for (String key : headers.keySet()) {
            conn.addRequestProperty(key, headers.get(key));
        }
    }

}