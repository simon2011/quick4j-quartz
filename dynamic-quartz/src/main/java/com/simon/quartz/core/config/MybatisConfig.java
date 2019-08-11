package com.simon.quartz.core.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName MybatisConfig
 * @Description TODO
 * @Author simon.pei
 * @Date 2019/8/4 22:14
 * @Version 1.0
 **/
@Configuration
@MapperScan("com.simon.quartz.mapper*")
public class MybatisConfig {

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        paginationInterceptor.setLimit(500);
        return paginationInterceptor;
    }
}
