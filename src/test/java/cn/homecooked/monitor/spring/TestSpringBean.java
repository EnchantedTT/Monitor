package cn.homecooked.monitor.spring;


import cn.homecooked.monitor.Monitor;
import java.util.Random;

public class TestSpringBean {

    @Monitor(key = "testKey", tp = true, excludeErr = {RuntimeException.class})
    public void doMethod() throws InterruptedException {
        Thread.sleep(20);
    }

    @Monitor(key = "anotherKey", tp = true, excludeErr = {MyException.class})
    public void doAnother() throws Exception{
        Random random = new Random();
        int r1 = random.nextInt(10);
        int r2 = random.nextInt(500);

        try {
            Thread.sleep(r1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        try{
//            if(r2 == 200) {
//                throw new MyException("Generic Error");
//            }
//            if(true) {
//                throw new Exception();
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
    }
}
