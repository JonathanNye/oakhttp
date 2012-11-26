package oak.http;

// User: jnye
// Date: 9/18/12
// Time: 10:55 AM
// Copyright (c) 2012 WillowTree Apps, Inc. All rights reserved.

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class OAKRequest<T> extends AbstractOAKRequest {

    private OAKResponseParser<T> parser;

    public OAKRequest(String url, OAKResponseParser<T> parser) {
        try {
            this.url = new URL(url);
            this.parser = parser;
        } catch(MalformedURLException e) {
            throw new InstantiationError("OAKRequest instantiated with invalid URL: " + url);
        }
    }

    public OAKRequest(URL url, OAKResponseParser<T> parser) {
        this.url = url;
        this.parser = parser;
    }

    protected OAKRequest<T> newInstance() {
        return new OAKRequest<T>(url, parser);
    }

    @Override
    protected T parseResponse(InputStream is) throws Exception {
        if(parser != null) {
            return parser.parseResponse(is);
        } else {
            return null;
        }
    }

}
