package oak.http;

// User: jnye
// Date: 9/19/12
// Time: 11:28 AM
// Copyright (c) 2012 WillowTree Apps, Inc. All rights reserved.

import java.io.InputStream;

public abstract class OAKResponseParser<T> {
    public abstract T parseResponse(InputStream is) throws Exception;
}
