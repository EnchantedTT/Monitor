package cn.homecooked.monitor;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by com.jskz.lichangjie on 09/12/2015.
 */
public class QueueHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(QueueHandler.class);

    private String url;                         //InfluxDB url
    private String username;                    //InfluxDB用户名
    private String password;                    //InfluxDB密码
    private String database;                    //数据库名
    private long   timeout;                     //设定发送超时(ms)
    private int    max_send_size;               //批量发送的最大SIZE

    public  static final int MAX_QUEUE_SIZE = 60000;                 //队列最大长度

    public static ArrayBlockingQueue<Message> queue = new ArrayBlockingQueue<Message>(MAX_QUEUE_SIZE);

    public void run() {

        BatchPoints batchPoints = BatchPoints
                .database(database)
                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

        while(true){

            batchPoints.getPoints().clear();

            long start = System.currentTimeMillis();
            do {
                Message message = QueueHandler.queue.poll();
                if(message != null) {
                    Point point = Point
                            .measurement(message.getSystem())
                            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                            .tag("Key", message.getKey())
                            .tag("Ip", message.getIp())
                            .field("Result", message.isResult())
                            .field("Cost", message.getCost())
                            .build();
                    batchPoints.point(point);
                }
            } while(batchPoints.getPoints().size() < max_send_size && (System.currentTimeMillis() - start) <= timeout);

            if(batchPoints.getPoints().size() == 0){
                continue;
            }

            try{
                connectToDB().write(batchPoints);
                log.info(batchPoints.toString());
            }catch (Exception e){
                log.debug("Connection Error! Please check!");
            }
        }

    }

    public InfluxDB connectToDB(){
        InfluxDB influxDB;
        if(url == null || url == "" || username == null || username == ""){
            log.debug("Shut Down Monitor");
            influxDB = null;
        }else {
            influxDB = InfluxDBFactory.connect(url, username, password);
            influxDB.setConnectTimeout(2000, TimeUnit.MILLISECONDS);
        }
        return influxDB;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setMax_send_size(int max_send_size) {
        this.max_send_size = max_send_size;
    }

    public QueueHandler(){}

    public QueueHandler(String url, String username, String password, String database, long timeout, int max_send_size) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.database = database;
        this.timeout = timeout;
        this.max_send_size = max_send_size;
    }

    public static void main(String[] args){
        QueueHandler queueHandler = new QueueHandler("http://192.168.1.245:8086", "root", "root", "JSKZ", 3000, 1024);
        queueHandler.start();
    }
}
