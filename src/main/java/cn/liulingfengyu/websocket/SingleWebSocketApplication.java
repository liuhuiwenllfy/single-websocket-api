package cn.liulingfengyu.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


/**
 * 启动类
 *
 * @author LLFY
 */
@SpringBootApplication
@ComponentScan(basePackages = "cn.liulingfengyu")
public class SingleWebSocketApplication {
    public static void main(String[] args) {
        SpringApplication.run(SingleWebSocketApplication.class, args);
    }
}
