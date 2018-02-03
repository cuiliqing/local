package modelValidate;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/9/28.
 * 提取每个人的个性化推荐的结果（/data/tvapk/person/lyt/）的movieID和序号
 * step 1
 */
public class personRec2 {

    public static void main(String[] args)throws IOException{
        String personRecPath = args[0];
        BufferedReader bfr = new BufferedReader(new FileReader(personRecPath));

        String out = args[1];
        BufferedWriter bfw = new BufferedWriter(new FileWriter(out));

        String line = null;
        while((line = bfr.readLine()) != null){
            int cnt = 0;
            StringTokenizer stk = new StringTokenizer(line, ",\t");
            String guid = stk.nextToken();
            String res = guid + "\t";
            while(stk.hasMoreTokens()){
                String mvid = stk.nextToken().substring(0, 32)+":" + ++cnt +",";
                res += mvid;
            }
            bfw.write(res);
            bfw.flush();
            bfw.newLine();
        }
        bfw.close();
        bfr.close();
    }

}
