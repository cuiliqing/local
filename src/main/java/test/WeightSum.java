package test;

import de.bwaldvogel.liblinear.Linear;
import models.Pair2;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/9/13.
 */
public class WeightSum {


    public static void main(String[] args)throws IOException {


        WeightSum hp = new WeightSum();
        HashMap<Integer, Double> modelMap = hp.getModelWeiMap("/Users/qiguo/Documents/fearureData/wei.txt");
        //modelweight
        //HashMap<Integer,Double> movMap = new HashMap<Integer, Double>();

        BufferedReader bfr2 = new BufferedReader(new FileReader("/Users/qiguo/Documents/likes.txt"));
        BufferedWriter bfw = new BufferedWriter(new FileWriter("/Users/qiguo/Documents/result1.txt"));

        BufferedReader bfr = new BufferedReader(new FileReader("/Users/qiguo/Documents/tmp.txt"));   //  movieFeat
        HashMap<Integer, Double> mvMap = new HashMap<Integer, Double>();

        String lin = null;
        while((lin = bfr.readLine()) != null){

            StringTokenizer stk = new StringTokenizer(lin,"\t");
            stk.nextToken();
            String movFeat = stk.nextToken();
            movFeat = movFeat.substring(1);
            movFeat = movFeat.substring(0, movFeat.length()-1);
            StringTokenizer subStk = new StringTokenizer(movFeat, " ,");
            while (subStk.hasMoreTokens()){
                String tmp = subStk.nextToken();
                Pair2 p2 = new Pair2(tmp);
                mvMap.put(p2.getIdx(), p2.getScore());

            }

        }
        String line = null;
        int id = 1;
        while((line = bfr2.readLine()) != null){
            HashMap<Integer, Double> likMap = hp.getLikes(line);
            HashMap<Integer,Double> resMap = hp.getResMap(mvMap,likMap);
            double res1 =0.0;
            double res2 = 0.0;
            String str = "";
            for(Map.Entry<Integer, Double> entry : mvMap.entrySet()){
                resMap.put(entry.getKey(), entry.getValue());
            }
            for(Map.Entry<Integer, Double>entry : resMap.entrySet()){
                System.out.println(entry.getKey() +"-"+entry.getValue());
            }

            for (Map.Entry<Integer, Double> entry : resMap.entrySet()){

                if(entry.getValue() !=0.0 ){

                    res1 += modelMap.get(entry.getKey()) * entry.getValue();
                    //System.out.println(res1);
                    str = id + "\t" + entry.getKey() + "\t" + modelMap.get(entry.getKey()) + "\t" + entry.getValue() + "\t" + modelMap.get(entry.getKey())*entry.getValue() +"\t" +res1;
                    bfw.write(str);
                    bfw.flush();
                    bfw.newLine();
                }
            }
            System.out.println(res1);
            double score = 1./(1. + Math.exp(-res1));
           // System.out.println(score);
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

        while(stk.hasMoreTokens() ){
            String likes = stk.nextToken();
            likes = likes.substring(1);
            likes = likes.substring(0, likes.length()-1);
            StringTokenizer subStk = new StringTokenizer(likes, " ,");
            while(subStk.hasMoreTokens()  ){
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

    HashMap<Integer, Double> getResMap(HashMap<Integer, Double> movmp1, HashMap<Integer, Double> likesmp2){

        int joinStartId = 23209;
        int joinEndId = 30702;
        int offset = joinEndId+1-joinStartId+2;
        HashMap<Integer,Double> map = likesmp2;
        for(Map.Entry<Integer, Double> entry : map.entrySet()){
            if(entry.getKey()<= joinEndId && entry.getKey()>= joinStartId){
                if(movmp1.containsKey(entry.getKey() - offset)){
                    //map.put(entry.getKey(), movmp1.get(entry.getKey() - offset));
                    map.put(entry.getKey(), 1.0);
                } else{
                    map.put(entry.getKey(), 0.0);
                }
            }
        }
        return map;
    }

}
