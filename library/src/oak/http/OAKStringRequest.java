package oak.http;

// User: jnye
// Date: 9/18/12
// Time: 5:36 PM
// Copyright (c) 2012 WillowTree Apps, Inc. All rights reserved.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class OAKStringRequest extends AbstractOAKRequest<String>{

    public OAKStringRequest(String url) {
        try {
            this.url = new URL(url);
        } catch(MalformedURLException e) {
            throw new InstantiationError("OAKRequest instantiated with invalid URL: " + url);
        }
    }

    public OAKStringRequest(URL url) {
        this.url = url;
    }

    @Override
    protected AbstractOAKRequest<String> newInstance() {
        return new OAKStringRequest(url);
    }

    @Override
    protected String parseResponse(InputStream is) throws IOException {
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
