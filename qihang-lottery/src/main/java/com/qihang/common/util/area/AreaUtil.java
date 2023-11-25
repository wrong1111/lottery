package com.qihang.common.util.area;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Subdivision;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.InetAddress;

/**
 * @author: bright
 * @description: 地区
 * @time: 2023-04-12 11:36
 */
@Component
public class AreaUtil {
    public String getIp() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ip = servletRequestAttributes.getRequest().getHeader("x-access-ip");
        if (StringUtils.isBlank(ip)) {
            return getIpAddr(servletRequestAttributes.getRequest());
        }
        return ip;
    }

    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : getMultistageReverseProxyIp(ip);
    }

    /**
     * 从多级反向代理中获得第一个非unknown IP地址
     *
     * @param ip 获得的IP地址
     * @return 第一个非unknown IP地址
     */
    public static String getMultistageReverseProxyIp(String ip) {
        // 多级反向代理检测
        if (ip != null && ip.indexOf(",") > 0) {
            final String[] ips = ip.trim().split(",");
            for (String subIp : ips) {
                if (false == isUnknown(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }
        return StringUtils.substring(ip, 0, 255);
    }

    public static boolean isUnknown(String checkString) {
        return StringUtils.isBlank(checkString) || "unknown".equalsIgnoreCase(checkString);
    }

    public String getAreaByIp(String ip) {
        if ("127.0.0.0".equals(ip)) {
            return "localhost";
        }
        try {
            // 创建 GeoLite2 数据库
            File database = new File(this.getClass().getClassLoader().getResource("GeoLite2-City.mmdb").getPath());
            // 读取数据库内容
            DatabaseReader reader = new DatabaseReader.Builder(database).build();
            InetAddress ipAddress = InetAddress.getByName(ip);

            // 获取查询结果
            CityResponse response = reader.city(ipAddress);

            // 获取国家信息
            Country country = response.getCountry();

            // 获取省份
            Subdivision subdivision = response.getMostSpecificSubdivision();
            // 获取城市
            City city = response.getCity();
            return country.getNames().get("zh-CN") + " " + subdivision.getNames().get("zh-CN") + " " + city.getNames().get("zh-CN");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
