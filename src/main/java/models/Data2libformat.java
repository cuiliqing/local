package models;

import java.io.*;
import java.util.*;

/**
 * Created by Tsui on 17/8/29.
 */
public class Data2libformat {
    public void transfer(String input, String output)throws IOException{
        BufferedReader bfr = new BufferedReader(new FileReader(input));
        BufferedWriter bfw = new BufferedWriter(new FileWriter(output));

        int movieTagStartIdx = 16812;
        int personlikesMovieEndIdx = 33327;
        String line  = null;
        HashMap<Integer,Double>taglabMap = new HashMap<Integer, Double>();
        while((line = bfr.readLine()) != null){
            //ouble lab = 0.0;
            StringTokenizer sTo = new StringTokenizer(line, " ,");
            sTo.nextToken();   // label
            while(sTo.hasMoreTokens()){
                String p2 = sTo.nextToken();
                Pair2 pair2 = new Pair2(p2);
                if(pair2.getIdx() >= movieTagStartIdx && pair2.getIdx() <= personlikesMovieEndIdx ){
                    taglabMap.put(pair2.getIdx(), pair2.getScore());
                }
            }
        }
        bfr.close();
        HashMap<Integer, Double> midMap = joinMap(taglabMap);
        BufferedReader br = new BufferedReader(new FileReader(input));
        while ((line = br.readLine()) != null){

            String liblinearformat = "";
            TreeSet<Pair2> pairSet = new TreeSet<Pair2>(new Comparator<Pair2>() {
                public int compare(Pair2 p1, Pair2 p2) {
                    int res = p1.getIdx() < p2.getIdx() ? -1 : 1;
                    return res;
                }
            });
            StringTokenizer stk = new StringTokenizer(line," ,");
            double lab = Double.parseDouble(stk.nextToken());
            liblinearformat += lab + " ";

            while(stk.hasMoreTokens()){
                String p2 = stk.nextToken();
                Pair2 pair2 = new Pair2(p2);
                if(pair2.getIdx() >= 25071 && pair2.getIdx() <= 33327){
                    pairSet.add(new Pair2(pair2.getIdx(), midMap.get(pair2.getIdx())));
                }else {
                    pairSet.add(pair2);
                }
            }
            for(Pair2 p: pairSet){
                liblinearformat += p.toString()+ " ";
            }
            bfw.write(liblinearformat);
            bfw.flush();
            bfw.newLine();
        }
        br.close();
        bfw.close();
    }

    public HashMap<Integer, Double> joinMap(HashMap<Integer, Double> map){
        HashMap<Integer, Double> tmpMap = map;
        int likesMovieTagsStart = 25071;
        int likesMovieTagsEnd = 33327;
        int offset = 8259;
        for(Map.Entry<Integer, Double> enty : tmpMap.entrySet()){
            if(enty.getKey() >= likesMovieTagsStart && enty.getKey() <= likesMovieTagsEnd){
                int keyId = enty.getKey() - offset;
                if(tmpMap.containsKey(keyId)){
                    tmpMap.put(enty.getKey(), map.get(keyId));
                }else {
                    tmpMap.put(enty.getKey(), 0.0);
                }
            }
        }
        return tmpMap;
    }

}
