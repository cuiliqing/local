package tools;

import java.io.*;
import java.util.Random;

/**
 * Created by qiguo on 17/9/26.
 */
public class RandomExt {
    public static void main(String[] args)throws IOException{
        String input = args[0];
        //String input = "/Users/qiguo/Documents/fearureData/cui.txt";
        String output = args[1];
        //String output = "/Users/qiguo/Documents/fearureData/Randres.txt";
        BufferedWriter bfw = new BufferedWriter(new FileWriter(output));

        //String line = null;
        RandomAccessFile raf = new RandomAccessFile(input, "r");
        int tot = 600000;
        int cnt = 0;
        while(cnt < tot){
            int rand = randInt();
            int subline = 1;
            raf.seek(0);
            long totBytes = 0;

            long len = raf.readLine().length()+1;
            totBytes += len;
            while(subline < rand-1){
                raf.seek(totBytes);
                totBytes += raf.readLine().length()+1;
                //totBytes += len;
                subline++;
            }
            raf.seek(totBytes);
            String line = raf.readLine();
            bfw.write(line);
            bfw.flush();
            bfw.newLine();
            cnt++;
        }
        bfw.close();
        raf.close();
    }

    public static int randInt(){
        Random rd = new Random();
        int val = rd.nextInt(3000000);  //15843042
        //System.out.println(val);
        return val;
    }
}
