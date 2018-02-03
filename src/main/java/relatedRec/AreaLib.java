package relatedRec;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/11/28.
 */
public class AreaLib {
    public static void main(String[] args)throws IOException {
        String inpath = args[0];  // area
        String outsingle = args[1];  // 国家地区唯一性map输出
        String out = args[2];   //

        BufferedWriter bfw = new BufferedWriter(new FileWriter(out));
        BufferedWriter bfw1 = new BufferedWriter(new FileWriter(outsingle));

        HashMap<String, String> areaMp = new HashMap<String, String>();

        BufferedReader bfr = new BufferedReader(new FileReader(inpath));
        String line ="";
        while ((line = bfr.readLine()) != null) {

            StringTokenizer stk = new StringTokenizer(line);
            if(stk.countTokens() > 1){
                String key = stk.nextToken().substring(2);
                String val = stk.nextToken();
                areaMp.put(key, val);
                bfw.write("t4" + key + "\t" + val);
                bfw.newLine();
            }
        }
        bfw.close();
        bfr.close();
        HashMap<String, Integer>Mp = new HashMap<String, Integer>();
        Iterator<String> iter = areaMp.values().iterator();
        int cnt = 0;
        while (iter.hasNext()){
            String country = iter.next();

            Mp.put(country, cnt);
            bfw1.write(country + "\t" + cnt);
            bfw1.newLine();
            cnt++;
        }
        bfw1.close();
        // System.out.println(Mp);
        // System.out.println("----");
        HashMap<String, Integer> allmap = new HashMap<String, Integer>();
        for(Map.Entry<String, String>entry : areaMp.entrySet()){
            allmap.put(entry.getKey(), Mp.get(entry.getValue()));
        }
        //System.out.println(allmap);
    }
}
