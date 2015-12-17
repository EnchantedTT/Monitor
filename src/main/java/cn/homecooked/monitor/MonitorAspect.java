package cn.homecooked.monitor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by lichangjie on 16/12/2015.
 */
@Aspect
public class MonitorAspect {

    private static final Logger log = LoggerFactory.getLogger(MonitorAspect.class);


    String ip = getLocalIP();
    private String system;

    @Around("@annotation(cn.homecooked.monitor.Monitor)")
    public Object monitor(ProceedingJoinPoint call) throws Throwable {


        Monitor monitor = getMethod(call).getAnnotation(Monitor.class);
        String key = monitor.key();
        boolean tp = monitor.tp();
        long start = tp ? System.currentTimeMillis() : 0;

        Message message = new Message(key,ip,system);

        try {
            Object o = call.proceed();
            long cost = 0;
            if (start != 0) cost = System.currentTimeMillis() - start;
            message.setCost(cost);
            message.setResult(true);
            return o;
        } catch (Throwable e) {
            message.setResult(false);
            for (Class<? extends Exception> err : monitor.excludeErr()) {
                if (e.getClass() == err) {
                    message.setResult(true);
                }
            }
            throw e;
        } finally {
            log.info(message.toString());
            push(message);
        }
    }

    public void push(Message message) {
        if(QueueHandler.queue.offer(message)){
            log.info("------- Success -----> Put into Message queue!");
        }else {
            log.info("------- Dropped -----> Message queue is full! ");
        }
        log.info("--------- CURRENT QUEUE SIZE IS --------------------------->: " + QueueHandler.queue.size());
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }

    public void setSystem(String system) {
        this.system = system;
    }


    public static String getLocalIP(){
        String sIP = "";
        InetAddress ip = null;
        try {
            boolean bFindIP = false;
            Enumeration<NetworkInterface> netInterfaces = (Enumeration<NetworkInterface>) NetworkInterface
                    .getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                if(bFindIP){
                    break;
                }
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();

                //遍历所有ip
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    ip = (InetAddress) ips.nextElement();
                    if( ip.isSiteLocalAddress()
                            && !ip.isLoopbackAddress()   //127.开头的都是lookback地址
                            && ip.getHostAddress().indexOf(":")==-1){
                        bFindIP = true;
                        break;
                    }
                }

            }

        }
        catch (SocketException e) {
            log.info("SocketException");
            ip = null;
        }

        if(null != ip){
            sIP = ip.getHostAddress();
            log.info(sIP);

        }
        return sIP;
    }

}
