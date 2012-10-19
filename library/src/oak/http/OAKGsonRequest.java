package oak.http;

// User: jnye
// Date: 9/24/12
// Time: 2:22 PM
// Copyright (c) 2012 WillowTree Apps, Inc. All rights reserved.

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class OAKGsonRequest<T> extends AbstractOAKRequest<T> {

    Class<T> clazz;

    public OAKGsonRequest(String url, Class<T> clazz) {
        try {
            this.url = new URL(url);
            this.clazz = clazz;
        } catch(MalformedURLException e) {
            throw new InstantiationError("OAKRequest instantiated with invalid URL: " + url);
        }
        this.clazz = clazz;

    }

    public OAKGsonRequest(URL url, Class<T> clazz) {
        this.url = url;
        this.clazz = clazz;
    }

    @Override
    protected AbstractOAKRequest<T> newInstance() {
        return new OAKGsonRequest<T>(url, clazz);
    }

    @Override
    protected T parseResponse(InputStream is) {
        return new Gson().fromJson(new InputStreamReader(is), clazz);
    }
}
