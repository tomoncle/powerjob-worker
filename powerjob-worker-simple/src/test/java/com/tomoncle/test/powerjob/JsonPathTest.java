package com.tomoncle.test.powerjob;

import com.alibaba.fastjson.JSONPath;
import com.tomoncle.powerjob.common.OkHttpRequest;
import lombok.SneakyThrows;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author tomoncle
 */
public class JsonPathTest {
    private String json = "{\"name\": \"John\", \"age\": 30}";

    @Test
    public void json() {
        String name = (String) JSONPath.extract(json, "$.name");
        System.out.println(name);
    }

    @SneakyThrows
    @Test
    public void http() {
        String request = OkHttpRequest.GET.request("https://api.tomoncle.com");
        System.out.println(request);
    }

    @SneakyThrows
    @Test
    public void post() {
        String url = "https://api.tomoncle.com/?user=tom&age=21";
        OkHttpRequest.BodyMap map = new OkHttpRequest.BodyMap();
        map.put("id", 12);
        map.put("names", new ArrayList<String>() {{
            add("Jackson");
            add("Michael");
        }});
        String response = OkHttpRequest.POST.request(url, map);
        System.out.println(JSONPath.extract(response, "$.args.user"));
        System.out.println(JSONPath.extract(response, "$.args.age"));
    }

    @SneakyThrows
    @Test
    public void url() {
        String url = "https://api.tomoncle.com/deploy?serviceId=svc-001&serviceName=testService&@serverId&@serverName#top";
        URL urlObj = new URL(url);
        StringBuffer params = new StringBuffer();
        Arrays.asList(urlObj.getQuery().split("&")).forEach(param -> {
            if (param.length() > 0) {
                params.append("&");
            }
            if (param.startsWith("@")) {
                param = param.replace("@", "");
                params.append(param).append("=").append("xxx");
            } else {
                params.append(param);
            }
        });
        if (null != urlObj.getRef()) {
            params.append("#").append(urlObj.getRef());
        }
        url = url.split("\\?")[0] + "?" + params.toString();
        System.out.println(url);
    }
}
