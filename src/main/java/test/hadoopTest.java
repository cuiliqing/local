package test;

import PersonalRecommend.T2;
import de.bwaldvogel.liblinear.Model;
import models.Pair2;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/8/22.
 */
public class hadoopTest {

    public static void main(String[] args)throws IOException{


        hadoopTest hp = new hadoopTest();
        HashMap<Integer, Double> modelMap = hp.getModelWeiMap("/Users/qiguo/Documents/fearureData/wei.txt");
        //modelweight
        HashMap<Integer,Double> movMap = new HashMap<Integer, Double>();


        movMap.put(25069, 0.3);
        movMap.put(21450, 1.0);
        movMap.put(20443, 0.95);
        movMap.put(21586, 0.9);
        movMap.put(16942, 0.85);
        movMap.put(18725, 0.8);
        movMap.put(20619, 0.75);
        movMap.put(20630, 0.7);
        movMap.put(23459, 0.65);
        movMap.put(18347, 0.6);
        movMap.put(25070, 0.6);


        BufferedReader bfr2 = new BufferedReader(new FileReader("/Users/qiguo/Documents/likes.txt"));
        BufferedWriter bfw = new BufferedWriter(new FileWriter("/Users/qiguo/Documents/result.txt"));
        String line = null;
        int id = 1;
        while((line = bfr2.readLine()) != null){
            HashMap<Integer, Double> likMap = hp.getLikes(line);
            HashMap<Integer,Double> resMap = hp.getResMap(movMap,likMap);

            String str = "";
            double res =0.0;
            for (Map.Entry<Integer, Double> entry : resMap.entrySet()){
                if(entry.getKey() > 25070){
                    res += modelMap.get(entry.getKey())*entry.getValue();
                    str = id + "\t" + entry.getKey() + "\t" + modelMap.get(entry.getKey()) + "\t" + modelMap.get(entry.getKey())*entry.getValue()+ "\t" + res;
                    bfw.write(str);
                    bfw.flush();
                    bfw.newLine();
                }
            }
            id++;
            likMap.clear();
            resMap.clear();
        }
        bfr2.close();
        bfw.close();


    }
    HashMap<Integer, Double> getLikes(String str)throws IOException{
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();

        StringTokenizer stk = new StringTokenizer(str, "\t");
        String uid = stk.nextToken();
        while(stk.hasMoreTokens()){
            String likes = stk.nextToken();
            likes = likes.substring(1);
            likes = likes.substring(0, likes.length()-1);
            StringTokenizer subStk = new StringTokenizer(likes, " ,");
            while(subStk.hasMoreTokens()){
                String s = subStk.nextToken();
                Pair2 p2 = new Pair2(s);
                map.put(p2.getIdx(), p2.getScore());
            }
        }
        return map;
    }
    HashMap<Integer, Double> getModelWeiMap(String Path)throws IOException{
        BufferedReader bfr = new BufferedReader(new FileReader(Path));
        String line = null;
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();
        while((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, " \t");
            int id =  Integer.parseInt(stk.nextToken());
            double w = Double.parseDouble(stk.nextToken());
            map.put(id, w);
        }
        return map;
    }

    HashMap<Integer, Double> getResMap(HashMap<Integer, Double> mp1, HashMap<Integer, Double> mp2){
        int TagsStart = 16812;
        int TagsEnd = 25068;
        int joinStartId = 25071;
        int joinEndId = 33327;
        HashMap<Integer,Double> map = mp2;
        for(Map.Entry<Integer, Double> entry : map.entrySet()){
            if(entry.getKey()<= joinEndId && entry.getKey()>= joinStartId){
                if(mp1.containsKey(entry.getKey() - 8259)){
                    map.put(entry.getKey(), mp1.get(entry.getKey() - 8259));
                } else{
                    map.put(entry.getKey(), 0.0);
                }
            }
        }
        return map;
    }

}
