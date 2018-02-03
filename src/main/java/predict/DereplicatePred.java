package predict;

import PersonalRecommend.T2;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import models.Pair2;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Tsui on 17/9/4.
 * dereplicationPred : 对用户vv纪录下的movie 不再做推荐
 *输入 1：model 文件 2:features Id文件  3:用户偏好文件 4:guid 文件 5；电影特征文件 6: 观看历史文件vv 7: topk
 *输出 8：输出文件
 */
public class DereplicatePred {

    HashSet<String> guids ;
    HashMap<String, String> movieFeatMap ;
    HashMap<String, HashSet<String>> vvhistory;

    public DereplicatePred(String guidPath1, String moviesPath2, String vvhistoryPath)throws IOException {
        this.guids = getGuidSet(guidPath1);
        this.movieFeatMap = getMovieMap(moviesPath2);
        this.vvhistory = getVvhisMap(vvhistoryPath);
    }
    public HashSet<String> getGuidSet(String path)throws IOException{
        BufferedReader bfr1 = new BufferedReader(new FileReader(path));
        HashSet<String> set = new HashSet<String>();
        String line = null;
        while((line = bfr1.readLine()) != null){
            String guid = line.trim();
            set.add(guid);
        }
        return set;
    }
    public HashMap<String, String> getMovieMap(String path)throws IOException{
        BufferedReader bfr2 = new BufferedReader(new FileReader(path));
        HashMap<String, String> map = new HashMap<String, String>();
        String line = null;
        while((line = bfr2.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line,"\t");
            String movieId = stk.nextToken();
            String feats = stk.nextToken();
            feats = feats.substring(1);
            feats = feats.substring(0, feats.length()-1);
            map.put(movieId, feats);
        }
        return map;
    }

    public HashMap<String, HashSet<String>> getVvhisMap(String path)throws IOException{
        BufferedReader bfr = new BufferedReader(new FileReader(path));
        HashMap<String, HashSet<String>> vvMap = new HashMap<String, HashSet<String>>();
        String line = null;
        while ((line = bfr.readLine()) != null){

            StringTokenizer stk = new StringTokenizer(line);
            String guid = stk.nextToken();
            HashSet<String> mvset = new HashSet<String>();
            mvset.clear();
            while (stk.hasMoreTokens()){
                mvset.add(stk.nextToken());
            }
            vvMap.put(guid, mvset);
        }
        bfr.close();
        return vvMap;
    }

    public Feature[] getSortedFeatures(String str, int[] featId){

        int movieTagStartIdx = featId[0];
        int likesMovieTagsStart = featId[1];
        int likesStartId = featId[2];
        StringTokenizer stk = new StringTokenizer(str," ,");

        TreeSet<Pair2> set = new TreeSet<Pair2>(new Comparator<Pair2>() {
            public int compare(Pair2 o1, Pair2 o2) {
                int res = o1.getIdx()< o2.getIdx() ? -1 : 1;
                return res;
            }
        });

        HashMap<Integer,Double> midMap = new HashMap<Integer, Double>();

        while(stk.hasMoreTokens()){
            String tmp = stk.nextToken();
            Pair2 p2 = new Pair2(tmp);
            if(p2.getIdx() >= movieTagStartIdx && p2.getIdx() < likesStartId ){
                midMap.put(p2.getIdx(), p2.getScore());
            }
            set.add(p2);
        }
        int deleted = 0;
        int offset = likesMovieTagsStart - movieTagStartIdx;
        Iterator<Map.Entry<Integer, Double>> iter = midMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<Integer, Double>entry = iter.next();
            if(entry.getKey() >= likesMovieTagsStart && entry.getKey() < likesStartId ){
                int keyId = entry.getKey() - offset;
                if(midMap.containsKey(keyId)){
                    midMap.put(entry.getKey(), midMap.get(keyId));
                }else {
                    iter.remove();
                    ++deleted;
                }
            }
        }
        int start = likesMovieTagsStart;
        int end = likesStartId;
        int featNodeNum = set.size();
        int featTotal = featNodeNum - deleted;
        Feature[] feat = new FeatureNode[featTotal];
        int i = 0;

        for(Pair2 p : set){
            if(p.getIdx() >= start && p.getIdx() < end ){
                if(midMap.containsKey(p.getIdx())){
                    feat[i++] = new FeatureNode(p.getIdx(), midMap.get(p.getIdx()));
                }
            }else{
                feat[i++] = new FeatureNode(p.getIdx(), p.getScore());

            }
        }
        return feat;
    }

    public int[] getFeatId(BufferedReader bfr)throws IOException{
        int[] featId = new int[3];
        String line = null;
        int cnt = 0;
        while((line = bfr.readLine()) != null ){
            if(cnt > 0){
                StringTokenizer stk = new StringTokenizer(line, " :");
                stk.nextToken();
                if(cnt == 1){
                    int movTagsStart = Integer.parseInt(stk.nextToken().trim());
                    featId[cnt - 1] = movTagsStart;

                }else if(cnt == 2){
                    int tagLikesStart = Integer.parseInt(stk.nextToken().trim());
                    featId[cnt - 1] = tagLikesStart;

                }else {
                    int likeStart = Integer.parseInt(stk.nextToken().trim());
                    featId[cnt - 1] = likeStart;
                }
            }
            cnt++;
        }
        return featId;
    }


    public static double predScore(Feature[] feature, Model model) {

        double bias = model.getBias();
        double[] weight = model.getFeatureWeights();
        double res = 0.0;
        for(Feature node : feature){
            res += weight[node.getIndex() - 1] * node.getValue();
        }
        if(bias + 1.0 > 0){
            res += bias;
        }
        BigDecimal bd = new BigDecimal(res);
        double valp = Double.parseDouble(bd.setScale(4, BigDecimal.ROUND_HALF_UP).toString());
        return valp;
    }


    public static void main(String[] args)throws IOException{

        String md = args[0];                     //  model
        Model model = Model.load(new File(md));
        int[] label = model.getLabels();

        String featIdPath = args[1];             // featuresId 文件
        BufferedReader bfrId = new BufferedReader(new FileReader(featIdPath));


        String likePath = args[2];              // 用户偏好文件
        String guidPth = args[3];               // guid 文件，只含 guid字符串
        String movieset = args[4];             // 电影特征文件
        String vvhistory = args[5];            // 观看过的历史纪录
        int topk = Integer.parseInt(args[6]);

        String output = args[7];
        BufferedReader bfr = new BufferedReader(new FileReader(likePath));
        BufferedWriter bfw = new BufferedWriter(new FileWriter(output));

        String line = null;
        DereplicatePred dp = new DereplicatePred(guidPth, movieset, vvhistory);

        int[] featid = dp.getFeatId(bfrId);
        bfrId.close();

        HashSet<String> guidset = dp.guids;
        HashMap<String, String> moviemap = dp.movieFeatMap;
        HashMap<String,HashSet<String>> vvmap = dp.vvhistory;

        TreeSet<T2> canset = new TreeSet<T2>(new Comparator<T2>() {
            public int compare(T2 p1, T2 p2){
                int res = p1.getVal() > p2.getVal()? -1: 1;     ///> ：降序  < :  升序
                return res;
            }
        });
        boolean flag = label[0] == 1 ? true: false;
        while ((line = bfr.readLine()) != null) {                          // 读like偏好文件
            StringTokenizer stk = new StringTokenizer(line,"\t");
            String guid = stk.nextToken();

            if(guidset.contains(guid)){
                String likestr = stk.nextToken();
                likestr = likestr.substring(1);
                likestr = likestr.substring(0, likestr.length()-1);

                for(Map.Entry<String, String> entry : moviemap.entrySet()){
                    String featstr = entry.getValue() + "," + likestr;
                    Feature[] fets = dp.getSortedFeatures(featstr, featid);
                    double sc = predScore(fets, model);
                    sc = flag == true ? sc : -sc;
                    if(vvmap.containsKey(guid)){
                        if(! vvmap.get(guid).contains(entry.getKey())){
                            if(canset.size() >= topk){
                                if(sc > canset.last().getVal()){
                                    canset.pollLast();
                                    canset.add(new T2(entry.getKey(), sc));
                                }
                            }else {
                                canset.add(new T2(entry.getKey(), sc));
                            }
                        }
                    }

                }
                String outStr = guid + "\t";
                int cnt = 1;
                for(T2 t2 : canset){
                    if(cnt == canset.size()){
                        outStr += t2.toString() + ":G:206";
                    }else{
                        outStr += t2.toString() + ":G:206,";   //格式输出
                    }
                    cnt++;
                }
                bfw.write(outStr);
                bfw.flush();
                bfw.newLine();
                canset.clear();
            }
        }
        bfr.close();
        bfw.close();
    }

}
