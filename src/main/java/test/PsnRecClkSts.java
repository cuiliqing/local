package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/12/6.
 */
public class PsnRecClkSts {
    HashSet<String> joinguidSet = new HashSet();
    HashSet<String> oldGuid = new HashSet<String>();
    public PsnRecClkSts(String Path1, String path2)throws IOException {
        this.joinguidSet = getGUidSet(Path1);
        this.oldGuid = getGUidSet(path2);
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
        String path1 = args[0];   // joinguid
        String path2 = args[1];  //oldguid
        String path3 = args[2];   //对应的psnrecClk 或 psnRecDis
        String str = args[3];
        PsnRecClkSts psn = new PsnRecClkSts(path1, path2);  // 目标joinguid集合 , oldguid
        BufferedReader bfr = new BufferedReader(new FileReader(path3));
        int sum = 0;
        String line = "";
        while ((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, " \t");
            if(stk.countTokens()==4){
                String flag = stk.nextToken();
                if(str.equals(flag)){
                    String guid = stk.nextToken();
                    if(psn.joinguidSet.contains(guid) && !psn.oldGuid.contains(guid)){
                        stk.nextToken();
                        sum += Integer.parseInt(stk.nextToken());
                    }
                }
            }

        }
        System.out.println(sum);
    }
}
