package test;

import de.bwaldvogel.liblinear.Model;
import models.Pair2;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/12/13.
 */
public class WeightSum2 {

    public static void main(String[] args)throws IOException {


        WeightSum2 hp = new WeightSum2();
        double[] modelWei = hp.getModelWei("/Users/qiguo/Documents/fearureData/model");
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

            for ( int i = 0 ;i < modelWei.length; i++){

                if(resMap.containsKey(i+1) && resMap.get(i+1) != 0.0 ){

                    res1 += modelWei[i] * resMap.get(i+1);
                    //System.out.println(res1);
                    int weiid = (i+1);
                    str = id + "\t" + weiid + "\t" + modelWei[i] + "\t" + resMap.get(i+1) + "\t" + modelWei[i ]* resMap.get(i+1) +"\t" +res1;
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
    double[] getModelWei(String Path)throws IOException{
        Model model = Model.load(new File(Path));
        double[] wei = model.getFeatureWeights();
        return wei;
    }

    HashMap<Integer, Double> getResMap(HashMap<Integer, Double> movmp1, HashMap<Integer, Double> likesmp2){

        int joinStartId = 23202;
        int joinEndId = 30688;
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
