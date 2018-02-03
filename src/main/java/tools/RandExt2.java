package tools;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by qiguo on 17/9/26.
 */
public class RandExt2 {
    public static void main(String[] args)throws IOException {
        String input = args[0];
        //String input = "/Users/qiguo/Documents/fearureData/cui.txt";
        String output = args[1];
        //String output = "/Users/qiguo/Documents/fearureData/Randres.txt";
        BufferedWriter bfw = new BufferedWriter(new FileWriter(output));

        //String line = null;
        //RandomAccessFile raf = new RandomAccessFile(input, "r");
        int tot = 3000000;
        int cnt = 0;
        out:while(cnt < tot){
            Scanner sc = new Scanner(new FileReader(input));
            int l = 0;
            String line = "";
            for(; sc.hasNext() ; ){
                if(l < randInt()-1){
                    sc.nextLine();
                    l++;
                }
                else if(l == randInt()-1){
                    line = sc.nextLine();
                    bfw.write(line);
                    bfw.flush();
                    bfw.newLine();
                    cnt++;
                    continue out;
                }
            }

        }
        bfw.close();

    }


    public static int randInt(){
        Random rd = new Random();
        int val = rd.nextInt(15843040);  //15843042
        //System.out.println(val);
        return val;
    }
}
