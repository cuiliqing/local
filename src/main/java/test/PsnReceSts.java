package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/12/4.
 */
public class PsnReceSts {
    HashSet<String> guidSet = new HashSet();
    public PsnReceSts(String Path)throws IOException{
        this.guidSet = getGUidSet(Path);
    }

    public HashSet<String> getGUidSet(String Path)throws IOException{

        HashSet<String> guidset = new HashSet<String>();

        BufferedReader bfr = new BufferedReader(new FileReader(Path));
        String line = "";
        while((line = bfr.readLine()) != null){
            guidset.add(line.trim());
        }
        return guidset;
    }

    public static void main(String[] args)throws IOException{
        String path1 = args[0];
        String path2 = args[1];  // 对应的psnrecClk 或 psnRecDis
        String str = args[2];
        PsnReceSts psn = new PsnReceSts(path1);  // 目标guid集合
        BufferedReader bfr = new BufferedReader(new FileReader(path2));
        int sum = 0;
        String line = "";
        while ((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, " \t");
            if(stk.countTokens() == 4){
                String flag = stk.nextToken();
                if(str.equals(flag)){
                    String guid = stk.nextToken();
                    if(psn.guidSet.contains(guid)){
                        stk.nextToken();
                        sum += Integer.parseInt(stk.nextToken());
                    }
                }
            }
        }
        System.out.println(sum);
    }
}
