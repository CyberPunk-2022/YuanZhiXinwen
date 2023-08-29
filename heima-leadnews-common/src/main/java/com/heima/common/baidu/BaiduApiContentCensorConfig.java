package com.heima.common.baidu;


import com.baidu.aip.contentcensor.AipContentCensor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Getter
@Setter
//@Component
//@ConfigurationProperties(prefix = "baidu")
public class BaiduApiContentCensorConfig {
    private String AppID;

    private String API_Key;

    private String Secret_Key;

    @Bean(name = "censorClient")
    AipContentCensor commonTextCensorClient() {
        /**
         * 可以选择在客户端中添加参数，参考 https://ai.baidu.com/ai-doc/ANTIPORN/ik3h6xdze
         * 如：
         *         // 可选：设置网络连接参数
         *         client.setConnectionTimeoutInMillis(2000);
         *         client.setSocketTimeoutInMillis(60000);
         *
         *         // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
         *         client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
         *         client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理
         *
         *         // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
         *         // 也可以直接通过jvm启动参数设置此环境变量
         *         System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");
         */


        AipContentCensor client = new AipContentCensor(AppID, API_Key, Secret_Key);

        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        return client;
    }
}
