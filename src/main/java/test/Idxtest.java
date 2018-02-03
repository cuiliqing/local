package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tsui on 17/11/15.
 */
public class Idxtest {
    public static void main(String[] args)throws IOException{

        String format = "yyyyMMdd";
        String date = "20171111"; //1510243200000  1510329600000 1513440000000

        String date2 = "20171217";
        try{
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            System.out.println(sdf.parse(date).getTime());
            System.out.println(sdf.parse(date2).getTime());
        }catch (Exception e){
            e.printStackTrace();
        }

        String line = "a35b29b965867433ebedf4f257be3f7a\t30\t196\t4\t6.53\t0.13";
        String[] arr = line.split("\t");
        int idx = line.indexOf("\t");
        System.out.println(idx);
        System.out.println(arr[0]);
        int i =0 ;
        for(String v: line.split("\t")){
            System.out.println(i++ + "\t" + v);
        }
        double vp =1/ (Math.log(6) / Math.log(2)) * 15;
        System.out.println(vp);
        System.out.println(new Date().getTime());

        SimpleDateFormat sp = new SimpleDateFormat("yyyyMMdd");
        System.out.println(sp.format(new Date()).substring(0, 4));
    }
}
