package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/11/30.
 */
public class BufferReadFile {
    public static void main(String[] args)throws IOException{
        String in = "/Users/qiguo/Documents/relatedRec/expFeatKeys";
        String mun = "/Users/qiguo/Documents/relatedRec/mum.txt";
        BufferedReader bfr = new BufferedReader(new FileReader(in));
        BufferedReader bfr2 = new BufferedReader(new FileReader(mun));

        String line = "";
        HashSet<String> set = new HashSet<String>();
        while((line = bfr.readLine()) != null){
            //System.out.println(line);
            line = line.substring(2);
            set.add(line);
        }

        String line2 ="";
        while ((line2 = bfr2.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line2, "\t");
            while(stk.hasMoreTokens()){
                String info = stk.nextToken();
                if(info.startsWith("tags")){
                    info = info.substring(5);
                    StringTokenizer substk = new StringTokenizer(info, ",");

                    while(substk.hasMoreTokens()){
                        String tag = substk.nextToken();
                        if(set.contains(tag)){
                            System.out.println(tag);
                        }
                    }
                }
            }
        }
    }
}
