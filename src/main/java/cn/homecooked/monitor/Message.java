package cn.homecooked.monitor;

import java.io.Serializable;

/**
 * Created by lichangjie on 16/12/2015.
 */
public class Message implements Serializable {

    private String  key;
    private String  ip;
    private String  system;
    private long    cost;
    private boolean result;

    public Message(String key, String ip, String system) {
        this.key = key;
        this.ip = ip;
        this.system = system;
    }

    @Override
    public String toString() {
        return "Message{" +
                "key='" + key + '\'' +
                ", ip='" + ip + '\'' +
                ", system='" + system + '\'' +
                ", cost=" + cost +
                ", result=" + result +
                '}';
    }

    public String getKey() {
        return key;
    }

    public String getIp() {
        return ip;
    }

    public String getSystem() {
        return system;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
