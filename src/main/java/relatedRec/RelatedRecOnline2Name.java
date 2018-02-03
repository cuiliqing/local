package relatedRec;

import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/11/15.
 */
public class RelatedRecOnline2Name {
    HashMap<String, String> allmvMp;     //电影集合

    public RelatedRecOnline2Name(String movieInput)throws IOException {
        this.allmvMp = getMvMp(movieInput);
    }

    public HashMap<String, String> getMvMp(String path)throws IOException{
        HashMap<String, String>movieMap = new HashMap<String, String>();
        String movietable = path;
        BufferedReader bfr2 = new BufferedReader(new FileReader(movietable));
        String line = "";
        while((line = bfr2.readLine())!= null){
            String[] strs = line.split("\t");
            movieMap.put(strs[0].trim(), strs[1].trim());
        }
        bfr2.close();
        return movieMap;
    }

    public static void main(String[] args)throws IOException{

        String inputPred = args[0];   // 预测集合文件
        BufferedReader bfr = new BufferedReader(new FileReader(inputPred));

        ConvertId2Name idmap = new ConvertId2Name(args[1]);
        HashMap<String, String>mvMap = idmap.allmvMp;

        BufferedWriter bfw = new BufferedWriter(new FileWriter(args[2]));  // output

        String line = "";
        while((line = bfr.readLine()) != null){
            String outStr = "";

            StringTokenizer stk = new StringTokenizer(line, " ,\t");
            String mvkey = stk.nextToken();
            if(mvMap.containsKey(mvkey)){
                outStr += mvMap.get(mvkey) + "\t";
            }
            //outStr += mvkey+"\t";
            while(stk.hasMoreTokens()){
                String[] strarr = stk.nextToken().split(":");
                String mvid = strarr[0];
                if(mvMap.containsKey(mvid)){
                    String name = mvMap.get(mvid);
                    outStr += name+" ";
                }
            }
            bfw.write(outStr);
            bfw.flush();
            bfw.newLine();
        }
        bfr.close();
        bfw.close();
    }
}
