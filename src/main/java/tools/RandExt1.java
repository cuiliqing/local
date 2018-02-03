package tools;

import java.io.*;
import java.util.Random;

/**
 * Created by qiguo on 17/9/26.
 */
public class RandExt1 {
    public static void main(String[] args)throws IOException{

        String input = args[0];
        BufferedReader bfr = null;//= new BufferedReader(new FileReader(input));

        String output = args[1];
        BufferedWriter bfw = new BufferedWriter(new FileWriter(output));

        String line = null;

        int cnt = 0;
        int tot = 3000000;
        outlp:while (cnt < tot){
            int rd = randInt();
            int num = 0;
            System.out.println(cnt + ":" +rd);

            bfr = new BufferedReader(new FileReader(input));

            while((line = bfr.readLine()) != null){
                if(num < rd){
                    num++;
                }else if(num == rd){
                    bfw.write(line);
                    bfw.flush();
                    bfw.newLine();
                    cnt++;
                    continue outlp;
                }
            }

        }
        bfw.close();
        bfr.close();
    }
    public static int randInt(){
        Random rd = new Random();
        int val = rd.nextInt(15843040);  //15843042
        //System.out.println(val);
        return val;
    }
}
