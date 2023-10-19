/*
 * Copyright 2018 tomoncle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tomoncle.powerjob.common;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import javax.annotation.Nullable;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import static okhttp3.MediaType.parse;

/**
 * Time : 2018/12/15 22:56
 * Author : liyuanjun
 * File : OkHttpRequest.java
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class OkHttpRequest {

    public static final OkHttpRequest DEFAULT=new OkHttpRequest();

    private static final MediaType JSON=parse("application/json; charset=utf-8");
    private static final MediaType FORM=parse("application/x-www-form-urlencoded");
    private static final MediaType OCTET_STREAM=parse("application/octet-stream");
    public static final OkHttpRequest.Get GET= DEFAULT.new Get();
    public static final OkHttpRequest.Post POST= DEFAULT.new Post();
    public static final OkHttpRequest.Put PUT = DEFAULT.new Put();
    public static final OkHttpRequest.Patch PATCH = DEFAULT.new Patch();
    public static final OkHttpRequest.Delete DELETE= DEFAULT.new Delete();
    public static final OkHttpRequest.Head HEAD= DEFAULT.new Head();
    public static final HostnameVerifier HOSTNAME_VERIFIER=(s, sslSession) -> true;
    public static final X509TrustManager X_509_TRUST_MANAGER= x509TrustManager();
    public static final TrustManager[] TRUST_MANAGERS=new TrustManager[]{X_509_TRUST_MANAGER};
    private static OkHttpClient client= getClient(false);
    private static final Headers HEADERS;


    static {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("Accept", "*/*");
        HEADERS = Headers.of(stringStringHashMap);
    }

    private OkHttpRequest() {
    }

    /**
     * OkHttpRequest.enableSSL();
     * String request = OkHttpRequest.GET.request("https://172.16.110.125:6443")
     */
    public static void enableSSL() {
        client = getClient(true);
    }

    private static X509TrustManager x509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }
        };
    }

    private static OkHttpClient getClient(boolean ssl) {
        if (!ssl) {
            return new OkHttpClient();
        }
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, TRUST_MANAGERS, new SecureRandom());
            return new OkHttpClient().newBuilder()
                    .sslSocketFactory(sslContext.getSocketFactory(), X_509_TRUST_MANAGER)//配置
                    .hostnameVerifier(HOSTNAME_VERIFIER)//配置
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 验证 headers
     *
     * @param headers h
     * @return Headers
     */
    private static Headers buildHeaders(Headers headers) {
        return null == headers ? OkHttpRequest.HEADERS : headers;
    }


    /**
     * create request builder
     *
     * @param url     request address
     * @param headers headers
     * @return Request.Builder
     */
    private static Request.Builder requestBuilder(String url, Headers headers) {
        return new Request.Builder().url(url).headers(buildHeaders(headers));
    }

    /**
     * 解析map为 form string
     *
     * @param map Map
     * @return string
     */
    private static FormBody formBody(Map<String, Object> map) {
        FormBody.Builder builder = new FormBody.Builder();
        for (String name : map.keySet()) {
            builder.add(name, map.get(name).toString());
        }
        return builder.build();
    }

    public enum RequestDataType {
        BODY, FORM, STREAM
    }


    /**
     * 流式操作接口
     */
    private interface U {
        Response response(String url, byte[] bytes, Headers headers) throws IOException;
    }

    // 参数父类
    private static class HttpMap extends JSONObject {
    }

    // body 传递参数
    public static class BodyMap extends OkHttpRequest.HttpMap {
    }

    // form 传递参数
    public static class FormMap extends OkHttpRequest.HttpMap {
    }

    /**
     * 不用在body或form中传递参数的抽象方法
     */
    private abstract class G {
        public
        @Nullable
        String request(String url) throws IOException {
            try (ResponseBody responseBody = response(url).body()) {
                return null == responseBody ? null : responseBody.string();
            }
        }

        public
        @Nullable
        String request(String url, Headers headers) throws IOException {
            try (ResponseBody responseBody = response(url, headers).body()) {
                return null == responseBody ? null : responseBody.string();
            }
        }

        public Response response(String url) throws IOException {
            return response(url, null);
        }

        public Response response(String url, Headers headers) throws IOException {
            return method(url, null, headers, null);
        }

        /**
         * 实现该方法执行对应的请求
         *
         * @throws java.io.IOException 需要捕获
         */
        abstract Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException;
    }


    /**
     * 验证 RequestBody
     *
     * @param map data
     * @return RequestBody
     */
    private static RequestBody buildRequestBody(HttpMap map, RequestDataType bodyType) {
        if (null == map) {
            map = new HttpMap();
        }
        return (bodyType == RequestDataType.FORM) ? formBody(map) : RequestBody.create(JSON, map.toJSONString());
    }

    /**
     * 需要在body或form传递参数的
     */
    private abstract class P extends G {

        public
        @Nullable
        String request(String url, FormMap map) throws IOException {
            try (ResponseBody body = response(url, map).body()) {
                return body != null ? body.string() : null;
            }
        }

        public
        @Nullable
        String request(String url, BodyMap map) throws IOException {
            try (ResponseBody body = response(url, map).body()) {
                return body != null ? body.string() : null;
            }
        }

        public
        @Nullable
        String request(String url, FormMap map, Headers headers) throws IOException {
            try (ResponseBody body = response(url, map, headers).body()) {
                return body != null ? body.string() : null;
            }
        }

        public
        @Nullable
        String request(String url, BodyMap map, Headers headers) throws IOException {
            try (ResponseBody body = response(url, map, headers).body()) {
                return body != null ? body.string() : null;
            }
        }

        public Response response(String url, FormMap map, Headers headers) throws IOException {
            return method(url, map, headers, RequestDataType.FORM);
        }

        public Response response(String url, BodyMap map, Headers headers) throws IOException {
            return method(url, map, headers, RequestDataType.BODY);
        }

        public Response response(String url, FormMap map) throws IOException {
            return response(url, map, null);
        }

        public Response response(String url, BodyMap map) throws IOException {
            return response(url, map, null);
        }

    }

    public class Head extends P {
        @Override
        Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException {
            return client.newCall(requestBuilder(url, headers).head().build()).execute();
        }
    }

    public class Get extends G {
        @Override
        Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException {
            return client.newCall(requestBuilder(url, headers).build()).execute();
        }
    }

    public class Post extends P implements U {
        @Override
        Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException {
            return client.newCall(requestBuilder(url, headers).post(buildRequestBody(map, dataType)).build()).execute();
        }

        @Override
        public Response response(String url, byte[] bytes, Headers headers) throws IOException {
            return client.newCall(requestBuilder(url, headers).post(RequestBody.create(OCTET_STREAM, bytes)).build()).execute();
        }

    }

    public class Put extends P {
        @Override
        Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException {
            return client.newCall(requestBuilder(url, headers).put(buildRequestBody(map, dataType)).build()).execute();
        }
    }

    public class Patch extends P {
        @Override
        Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException {
            return client.newCall(requestBuilder(url, headers).patch(buildRequestBody(map, dataType)).build()).execute();
        }
    }

    public class Delete extends P {
        @Override
        Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException {
            return client.newCall(requestBuilder(url, headers).delete(buildRequestBody(map, dataType)).build()).execute();
        }
    }

}