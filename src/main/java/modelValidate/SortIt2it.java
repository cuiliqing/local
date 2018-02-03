package modelValidate;

import PersonalRecommend.T2;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import models.Pair2;

import java.io.*;
import java.util.*;

/**
 * Created by qiguo on 17/9/6.
 */
public class SortIt2it {
    Model model;
    HashMap<String, String> movsmap;
    HashMap<String, String> likeMap;
    public SortIt2it(String pth1, String pth2,String pth3)throws IOException{
        this.model = getModel(pth1);
        this.movsmap = getmovFeatMap(pth2);
        this.likeMap = getLikeMap(pth3);
    }
    public Model getModel(String pth1)throws IOException{
        Model model = Model.load(new File(pth1));
        return model;
    }
    public HashMap<String, String> getmovFeatMap(String path)throws IOException{
        BufferedReader bfr = new BufferedReader(new FileReader(path));
        HashMap<String, String>mvFeatMap = new HashMap<String, String>();
        String line = null;
        while ((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, "\t");
            String mvId = stk.nextToken();
            String feats = stk.nextToken();
            feats = feats.substring(1);
            feats = feats.substring(0,feats.length()-1);
            mvFeatMap.put(mvId, feats);
        }
        return mvFeatMap;
    }
    public HashMap<String, String> getLikeMap(String pth)throws IOException{
        BufferedReader bfr = new BufferedReader(new FileReader(pth));
        HashMap<String, String>likeMap = new HashMap<String, String>();
        String line = null;
        while ((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, "\t");
            String guid = stk.nextToken();
            String likes = stk.nextToken();
            likes = likes.substring(1);
            likes = likes.substring(0, likes.length()-1);
            likeMap.put(guid, likes);
        }
        return likeMap;
    }

    public Feature[] getSortedFeatures(String str){

        StringTokenizer stk = new StringTokenizer(str," ,");

        TreeSet<Pair2> set = new TreeSet<Pair2>(new Comparator<Pair2>() {
            public int compare(Pair2 o1, Pair2 o2) {
                int res = o1.getIdx()< o2.getIdx() ? -1 : 1;
                return res;
            }
        });

        HashMap<Integer,Double> midMap = new HashMap<Integer, Double>();
        int movieTagStartIdx = 16812;
        int personlikesMovieEndIdx = 33327;

        while(stk.hasMoreTokens()){
            String tmp = stk.nextToken();
            Pair2 p2 = new Pair2(tmp);
            if(p2.getIdx() >= movieTagStartIdx && p2.getIdx() <= personlikesMovieEndIdx){
                midMap.put(p2.getIdx(), p2.getScore());
            }
            set.add(p2);
        }

        int likesMovieTagsStart = 25071;
        int likesMovieTagsEnd = 33327;
        int offset = 8259;
        for(Map.Entry<Integer, Double> enty : midMap.entrySet()){
            if(enty.getKey() >= likesMovieTagsStart && enty.getKey() <= likesMovieTagsEnd){
                int keyId = enty.getKey() - offset;
                if(midMap.containsKey(keyId)){
                    midMap.put(enty.getKey(), midMap.get(keyId));
                }else {
                    midMap.put(enty.getKey(), 0.0);
                }
            }
        }
        int start = 25071;
        int end = 33327;
        int featNodeNum = set.size();
        Feature[] feat = new FeatureNode[featNodeNum];
        int i = 0;
        for(Pair2 p : set){
            if(p.getIdx() >= start && p.getIdx() <= end){
                feat[i++] = new FeatureNode(p.getIdx(), midMap.get(p.getIdx()));
            }else{
                feat[i++] = new FeatureNode(p.getIdx(), p.getScore());
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

    public static void main(String[] args) throws IOException{

        String pth = args[0];
        BufferedReader bfr = new BufferedReader(new FileReader(pth));

        String modelPath = args[1];
        String moviesPath = args[2];
        String likePath = args[3];
        String output = args[4];

        SortIt2it it2it = new SortIt2it(modelPath, moviesPath,likePath);
        HashMap<String, String> mvfeatsmap = it2it.movsmap;
        HashMap<String, String> likeMap = it2it.likeMap;
        Model model = it2it.model;

        TreeSet<T2> can = new TreeSet<T2>(new Comparator<T2>() {
            public int compare(T2 p1, T2 p2){
                int res = p1.getVal() > p2.getVal()? -1: 1;
                return res;
            }
        });

        BufferedWriter bfw = new BufferedWriter(new FileWriter(output));

        String line = null;
        while ((line = bfr.readLine()) != null){

            HashSet<String> vvSet = new HashSet<String>();
            HashMap<String, HashSet<String>> mvsets = new HashMap<String, HashSet<String>>();
            HashSet<String> cansets = new HashSet<String>();

            StringTokenizer stk = new StringTokenizer(line, " ,\t");
            String guid = stk.nextToken();

            while (stk.hasMoreTokens()){
                String mvid = stk.nextToken();
                if(mvid.length() == 32 ){
                    vvSet.add(mvid);
                }else {
                    mvid = mvid.substring(0, 32);
                    if(!vvSet.contains(mvid)){
                        cansets.add(mvid);
                    }
                }
            }
            mvsets.put(guid, cansets);

            for(Map.Entry<String, HashSet<String>> entry : mvsets.entrySet()){
                //String guid = entry.getKey();
                for(String id : entry.getValue()){
                    if(mvfeatsmap.containsKey(id)){
                        String featStr = mvfeatsmap.get(id)+ "," + likeMap.get(guid);
                        Feature[] ft = it2it.getSortedFeatures(featStr);
                        double sc = it2it.predScore(ft, model);
                        can.add(new T2(id, sc));
                    }
                }
                String strtmp = guid + "\t";
                for(T2 t2: can){
                    strtmp +=t2.getKey() + " ";
                }
                bfw.write(strtmp);
                bfw.flush();
                bfw.newLine();
                can.clear();
            }

        }
        bfr.close();
        bfw.close();

    }

}
