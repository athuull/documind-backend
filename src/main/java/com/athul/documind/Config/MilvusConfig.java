//package com.athul.documind.Config;
//
//import io.milvus.v2.client.ConnectConfig;
//import io.milvus.v2.client.MilvusClientV2;
//import org.springframework.context.annotation.Bean;
//
//public class MilvusConfig {
//
//    @Bean
//    public MilvusClientV2 milvusClient() {
//        ConnectConfig config = ConnectConfig.builder()
//                .uri("http://localhost:19530")
//                // .token("root:Milvus") if auth is enabled
//                .build();
//
//        return new MilvusClientV2(config);
//    }
//}