package test;

import de.bwaldvogel.liblinear.Model;

import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/9/14.
 * @function: 把topk的推荐集合转化为电影title
 * @param input 0: 以用户guid为key，电影ID为推选集的文件；
 *              1: 所有电影集合的ID－> name的map
 * @param output 2: 输出
 */
public class Id2ItemName {
    HashMap<String, String> mvMp;
    public Id2ItemName(String movieInput)throws IOException{
        this.mvMp = getMvMp(movieInput);
    }

    public HashMap<String, String>getMvMp(String path)throws IOException{
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
        String inputPred = args[0];  // 预测文件
        BufferedReader bfr = new BufferedReader(new FileReader(inputPred));
        Id2ItemName idmap = new Id2ItemName(args[1]);   // 电影map集  movie Id -> name

        HashMap<String, String>mvMap = idmap.mvMp;

        BufferedWriter bfw = new BufferedWriter(new FileWriter(args[2]));  // 输出

        String line = "";
        while((line = bfr.readLine()) != null){
            String outStr = "";

            StringTokenizer stk = new StringTokenizer(line, " ,\t");
            String guid = stk.nextToken();
            outStr += guid+"\t";
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
