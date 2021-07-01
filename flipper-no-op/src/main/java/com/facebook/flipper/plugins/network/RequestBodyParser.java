package com.facebook.flipper.plugins.network;

import java.util.Map;

import okhttp3.Request;
import okio.Buffer;

public interface RequestBodyParser {

    boolean parseRequestBoddy(Request request, Buffer bodyBuffer, NetworkReporter.RequestInfo info, Map<String,String> bodyMetaData);
}
