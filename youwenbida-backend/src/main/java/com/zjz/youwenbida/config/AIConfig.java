package com.zjz.youwenbida.config;

import com.zhipu.oapi.ClientV4;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 配置
 */
@Configuration
@ConfigurationProperties("ai")
@Data
public class AIConfig {

    /**
     * 密钥
     */
    private String key;

    @Bean
    public ClientV4 clientV4(){
        return new ClientV4.Builder(key).build();
    }
}
