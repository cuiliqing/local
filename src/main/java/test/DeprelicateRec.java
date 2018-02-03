package test;

import java.io.*;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/9/4.
 */
public class DeprelicateRec {
    HashMap<String, HashSet<String>> vvmap ;
    public DeprelicateRec(String vvPath) throws IOException{
        this.vvmap = getVvhisMap(vvPath);
    }

    public HashMap<String, HashSet<String>> getVvhisMap(String path)throws IOException {
        BufferedReader bfr = new BufferedReader(new FileReader(path));
        HashMap<String, HashSet<String>> vvMap = new HashMap<String, HashSet<String>>();
        String line = null;
        while ((line = bfr.readLine()) != null){

            StringTokenizer stk = new StringTokenizer(line);
            String guid = stk.nextToken();
            HashSet<String> mvset = new HashSet<String>();
            mvset.clear();
            while (stk.hasMoreTokens()){
                mvset.add(stk.nextToken());
            }
            vvMap.put(guid, mvset);
        }
        bfr.close();
        return vvMap;
    }

    public static void main(String[] args)throws IOException{
        String dayRec = args[0];
        String vvHistoryPath = args[1];
        String output = args[2];
        BufferedWriter bfw = new BufferedWriter(new FileWriter(output));

        DeprelicateRec dpr = new DeprelicateRec(vvHistoryPath);
        HashMap<String, HashSet<String>> vvHistory = dpr.vvmap;

        BufferedReader bfr = new BufferedReader(new FileReader(dayRec));
        String line = null;
        while ((line = bfr.readLine()) != null){
            String mvstr = "";
            int cnt = 0;
            StringTokenizer stk = new StringTokenizer(line);
            String guid = stk.nextToken();
            if(vvHistory.containsKey(guid)){
                HashSet<String> mvset = vvHistory.get(guid);
                while (stk.hasMoreTokens()){
                    String mvid = stk.nextToken();
                    if(! mvset.contains(mvid)){
                        cnt++;
                        mvstr += mvid + " ";
                    }
                }
            }else {
                cnt = 200;
                while (stk.hasMoreTokens()){
                    mvstr += stk.nextToken()+ " ";
                }
            }
            bfw.write(guid +"\t" + cnt +" "+ mvstr);
            bfw.flush();
            bfw.newLine();
        }
        bfw.close();
        bfr.close();
    }

}
