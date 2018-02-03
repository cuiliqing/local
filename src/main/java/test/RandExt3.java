package test;

import java.io.*;
import java.util.Random;

/**
 * Created by qiguo on 17/9/26.
 */
public class RandExt3 {
    public static void main(String[] args)throws IOException {

        //String input = args[0];
        //BufferedReader bfr = null;//= new BufferedReader(new FileReader(input));

        //String output = args[1];
        //BufferedWriter bfw = new BufferedWriter(new FileWriter(output));

        String line = null;

        int cnt = 0;
        int tm = 100;
        int tot = 30000;

        int row = 0;
        outlp:while (cnt < tm){
            int rd = randInt();
            int num = 0;
            //System.out.println(cnt + ":" +rd);

            //bfr = new BufferedReader(new FileReader(input));

            while(true){
                if(num < rd){
                    num++;
                }else if(num == rd){

                    //bfw.write(line);
                    //bfw.flush();
                    System.out.println(row++ +":"+rd);
                    //bfw.newLine();
                    for(int i =1; i< tot-1; i++){
                        if(true){
                            System.out.println(row++ +" " + (rd + i));
                            //bfw.write(line);
                            //bfw.flush();
                            //bfw.newLine();
                        }
                    }
                    cnt++;
                    continue outlp;
                }
            }

        }
        //bfw.close();
        //bfr.close();
    }
    public static int randInt(){
        Random rd = new Random();
        int val = rd.nextInt(500);  //15843042
        //System.out.println(val);
        return val;
    }
}
