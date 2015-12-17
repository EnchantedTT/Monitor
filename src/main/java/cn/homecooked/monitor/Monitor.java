package cn.homecooked.monitor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lichangjie on 16/12/2015.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Monitor {

    String key();                                               //标记所执行的方法

    boolean tp() default true;                                //是否计算运行时间

    Class<? extends Exception>[] excludeErr() default {};       //可忽略的异常集

}
