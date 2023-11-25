package com.qihang.common.util.http;

import com.alibaba.fastjson.JSON;
import com.qihang.common.util.SpringContextUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wyong
 * @Date: 2023/11/25 025 21:18
 * @Description:
 **/

public class HttpReq {


    public static String postJSON(String url, Map<String, Object> objectMap, Map<String, String> headers) {
        RestTemplate restTemplate = SpringContextUtils.getBean(RestTemplate.class);
        RequestEntity requestEntity = null;
        if (null != headers) {
            HttpHeaders httpHeaders = new HttpHeaders();
            for (Map.Entry<String, String> key : headers.entrySet()) {
                httpHeaders.add(key.getKey(), key.getValue());
            }

            requestEntity = RequestEntity.post(url).headers(httpHeaders).body(JSON.toJSONString(objectMap));
        } else {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Content-Type", "application/json;charset=utf-8");
            requestEntity = RequestEntity.post(url).headers(httpHeaders).body(JSON.toJSONString(objectMap));
        }
        ResponseEntity<String> forEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        // restTemplate.getForEntity(createMenuUrl, String.class);
        return forEntity.getBody();
    }

    public static String postJSON(String url, Map<String, Object> objectMap) {
        return postJSON(url, objectMap, null);
    }


    public static String postJSON(String url) {
        return postJSON(url, new HashMap<>(), null);
    }
}
