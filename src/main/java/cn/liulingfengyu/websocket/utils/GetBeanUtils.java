package cn.liulingfengyu.websocket.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 解决jsb类中注入service问题
 *
 * @author LLFY
 */
@Component
public class GetBeanUtils implements ApplicationContextAware {
    private static ApplicationContext context;

    /**
     * 用这个获取
     *
     * @param tClass 类
     * @return <T>
     */
    public static <T> T getBean(Class<T> tClass) {
        return context.getBean(tClass);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}