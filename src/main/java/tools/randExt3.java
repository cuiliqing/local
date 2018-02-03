package tools;

import java.io.*;
import java.util.Random;

/**
 * Created by qiguo on 17/9/26.
 */
public class randExt3 {
    public static void main(String[] args)throws IOException {

        String input = args[0];
        BufferedReader bfr = null;//= new BufferedReader(new FileReader(input));

        String output = args[1];
        BufferedWriter bfw = new BufferedWriter(new FileWriter(output));

        String line = null;

        int cnt = 0;
        int tm = 10000;
        int tot = 300;
        outlp:while (cnt < tm){
            int rd = randInt();
            int num = 0;

            bfr = new BufferedReader(new FileReader(input));

            while((line = bfr.readLine()) != null){
                if(num < rd*tot){
                    num++;
                }else if(num == rd*tot){

                    bfw.write(line);
                    bfw.flush();
                    bfw.newLine();
                    for(int i =1; i< tot; i++){
                        if((line = bfr.readLine())!= null){
                            bfw.write(line);
                            bfw.flush();
                            bfw.newLine();
                        }
                    }
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
        int val = rd.nextInt(50000);  //15843042
        //System.out.println(val);
        return val;
    }
}
