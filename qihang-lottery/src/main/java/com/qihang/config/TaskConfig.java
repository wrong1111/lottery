package com.qihang.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "task")
public class TaskConfig {

    Boolean day;
    Boolean hour;
    Boolean match;
    Boolean omit;
    Boolean minute;
    Boolean award;
}
