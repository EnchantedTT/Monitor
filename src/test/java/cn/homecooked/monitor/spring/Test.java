package cn.homecooked.monitor.spring;

import cn.homecooked.monitor.Message;
import cn.homecooked.monitor.MonitorAspect;
import cn.homecooked.monitor.QueueHandler;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import retrofit.RetrofitError;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class Test {

    @Resource
    TestSpringBean testSpringBean;

    @Resource
    QueueHandler queueHandler;

    @org.junit.Test
    public void testGetIP(){
        MonitorAspect.getLocalIP();
    }

    @org.junit.Test
    public void testConn(){
        InfluxDB influxDB = queueHandler.connectToDB();
        System.out.println(influxDB);
    }

    @org.junit.Test
    public void testWrite(){
        Point point;

        BatchPoints batchPoints = BatchPoints
                .database("HAHA")
                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

        long start = System.currentTimeMillis();
        do {
            Message message = QueueHandler.queue.poll();
            if(message != null) {
                point = Point
                        .measurement("Monitor")
                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .tag("Key", message.getKey())
                        .tag("System", message.getSystem())
                        .tag("Ip", message.getIp())
                        .field("Result", message.isResult())
                        .field("Cost", message.getCost())
                        .build();
                batchPoints.point(point);
            }
        } while(batchPoints.getPoints().size() < 1024 && (System.currentTimeMillis() - start) <= 1000);

        try{
            queueHandler.connectToDB().write(batchPoints);
            System.out.println(batchPoints.toString());
        }catch (Exception e){
            System.out.println("Connection Error! Please check!");
        }

    }
}
