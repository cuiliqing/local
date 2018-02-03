package tools;

import java.util.Random;

/**
 * Created by qiguo on 18/1/30.
 */
public class RandTest {
    public static void main(String[] args){
        Random rd = new Random();
         //15843042
        //System.out.println(val);
        for(int i = 0; i< 10; i++){
            int val = rd.nextInt(10);
            System.out.println(val);
        }

    }
}
