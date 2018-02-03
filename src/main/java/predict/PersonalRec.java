package predict;

import PersonalRecommend.T2;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import models.Pair2;

import java.io.*;
import java.util.*;

/**
 * Created by Tsui on 17/8/31.
 * 弃用
 */
public class PersonalRec {
    Model model;

    public PersonalRec(String model)throws IOException{
        this.model = Model.load(new File(model));
    }

    public void recommendTopk(String input, Model model, int topk, String output)throws IOException{
        BufferedReader bfr = new BufferedReader(new FileReader(input));
        BufferedWriter bfw = new BufferedWriter(new FileWriter(output));

        String line = null;
        TreeSet<T2> canset = new TreeSet<T2>(new Comparator<T2>() {
            public int compare(T2 p1, T2 p2){
                int res = p1.getVal() > p2.getVal()? -1: 1;
                return res;
            }
        });

        String uid = "";
        while((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, "\t");
            String guidmovie = stk.nextToken();
            StringTokenizer stk1 = new StringTokenizer(guidmovie);
            String guid = stk1.nextToken();
            //uid = guid;
            String movieId = stk1.nextToken();
            if(uid.equals("")){
                uid = guid;
            }
            if(!guid.equals(uid)){
                String outStr = uid + "\t";
                for(T2 t2 : canset){
                    outStr += t2.toString()+" ";
                }
                bfw.write(outStr);
                bfw.flush();
                bfw.newLine();
                uid = guid;
                canset.clear();
            }
            while(stk.hasMoreTokens()){
                String featStr = stk.nextToken();
                StringTokenizer substk = new StringTokenizer(featStr);

                //String movieId = substk.nextToken();
                String feat = substk.nextToken();

                Feature[] feature = getFeatures(feat);
                double sc = predScore(feature, model);

                if(canset.size() >= topk){
                    if(sc > canset.last().getVal()){
                        canset.pollLast();
                        canset.add(new T2(movieId, sc));
                    }
                }else {
                    canset.add(new T2(movieId, sc));
                }
            }

        }
        bfr.close();
        bfw.close();
    }

    public Feature[] getFeatures(String str){
        StringTokenizer stk = new StringTokenizer(str);
        int featnodeNum = stk.countTokens();
        Feature[] feat = new FeatureNode[featnodeNum];
        HashMap<Integer,Double> midMap = joinMap(str);
        int i = 0;
        int start = 25071;
        int end = 33327;
        while(stk.hasMoreTokens()){
            String pair = stk.nextToken();
            Pair2 p2 = new Pair2(pair);
            if(p2.getIdx() >= start && p2.getIdx() <= end){
                feat[i++] = new FeatureNode(p2.getIdx(), midMap.get(p2.getIdx()));
            }else {
                feat[i++] = new FeatureNode(p2.getIdx(), p2.getScore());
            }
        }
        return feat;
    }
    public static double predScore(Feature[] feature, Model model) {
        int[] labels = model.getLabels();
        int loc1;
        loc1 = labels[0] == 1 ? 0 : 1;
        double[] res = new double[2];
        Linear.predictProbability(model, feature, res);
        return res[loc1];
    }
    public HashMap<Integer, Double> joinMap(String featStr){
        StringTokenizer stk = new StringTokenizer(featStr);

        int movieTagStartIdx = 16812;
        int personlikesMovieEndIdx = 33327;
        HashMap<Integer, Double>tmpMap = new HashMap<Integer, Double>();
        while (stk.hasMoreTokens()) {
            String pair = stk.nextToken().trim();
            Pair2 tup = new Pair2(pair);
            if(tup.getIdx() >= movieTagStartIdx && tup.getIdx() <= personlikesMovieEndIdx){
                tmpMap.put(tup.getIdx(), tup.getScore());
            }
        }
        int likesMovieTagsStart = 25071;
        int likesMovieTagsEnd = 33327;
        int offset = 8259;
        for(Map.Entry<Integer, Double> enty : tmpMap.entrySet()){
            if(enty.getKey() >= likesMovieTagsStart && enty.getKey() <= likesMovieTagsEnd){
                int keyId = enty.getKey() - offset;
                if(tmpMap.containsKey(keyId)){
                    tmpMap.put(enty.getKey(), tmpMap.get(keyId));
                }else {
                    tmpMap.put(enty.getKey(), 0.0);
                }
            }
        }
        return tmpMap;
    }

    public static void main(String[] args)throws IOException{

        String input = args[0];
        String modelpath = args[1];
        int topk = Integer.parseInt(args[2]);
        String output = args[3];
        File outFile = new File(output);
        if(! outFile.exists()){
            outFile.createNewFile();
        }
        PersonalRec pr = new PersonalRec(modelpath);
        Model model = pr.model;
        pr.recommendTopk(input, model, topk, output);
    }

}
