package modelValidate;

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
 * Created by Tsui on 17/9/19.
 * 对用户的个性化推荐集合利用模型排序测试用 step 2
 */
public class PersonRecommend {
    Model model;
    int[] featId;
    HashMap<String, String> movieItemFeatures;
    HashMap<String, String> likeMap;

    public PersonRecommend(String modelPath1, String featIdPath2,
                           String movieFeatPath, String likePath)throws IOException{
        this.featId = getFeatId(featIdPath2);
        this.model = Model.load(new File(modelPath1));
        this.movieItemFeatures = getItemDicts(movieFeatPath);
        this.likeMap = getLikeMap(likePath);
    }
    public int[] getFeatId(String path)throws IOException{
        BufferedReader bfr = new BufferedReader(new FileReader(path));
        int[] res = getBuffFeatId(bfr);
        return res;
    }
    public int[] getBuffFeatId(BufferedReader bfr)throws IOException{

        int[] featId = new int[3];
        String line = null;
        int cnt = 0;
        while((line = bfr.readLine()) != null ){
            if(cnt > 0){
                StringTokenizer stk = new StringTokenizer(line, " :");
                stk.nextToken();
                if(cnt == 1){
                    int movTagsStart = Integer.parseInt(stk.nextToken());
                    featId[cnt - 1] = movTagsStart;

                }else if(cnt == 2){
                    int tagLikesStart = Integer.parseInt(stk.nextToken());
                    featId[cnt - 1] = tagLikesStart;

                }else {
                    int likeStart = Integer.parseInt(stk.nextToken());
                    featId[cnt - 1] = likeStart;
                }
            }
            cnt++;
        }
        return featId;
    }
    public HashMap<String, String> getItemDicts(String path)      // movieItemFeature路径
            throws IOException{
        HashMap<String, String> dict = new HashMap<String, String>();
        BufferedReader bf = new BufferedReader(new FileReader(path));
        String lin = null;
        while((lin=bf.readLine()) != null){
            String[] str = lin.split("\t",-1);
            String itfet = str[1].substring(1);
            itfet = itfet.substring(0,itfet.length() - 1);
            dict.put(str[0], itfet);
        }
        bf.close();
        return dict;
    }

    public HashMap<String, String> getLikeMap(String path)throws IOException{
        HashMap<String, String> likeMp = new HashMap<String, String>();
        BufferedReader bfr = new BufferedReader(new FileReader(path));
        String line = null;
        while((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, "\t");
            String guid = stk.nextToken();
            String like = stk.nextToken();
            like = like.substring(1);
            like = like.substring(0, like.length()-1);
            likeMp.put(guid, like);
        }
        return likeMp;
    }



    public Feature[] getFeature(String str, int[] featId){
        int mvTagsStartId = featId[0];
        int likesMovieTagsStart = featId[1];
        int likeStartId = featId[2];

        StringTokenizer sTok = new StringTokenizer(str," ,");

        TreeSet<Pair2> pairSet = new TreeSet<Pair2>(new Comparator<Pair2>() {
            public int compare(Pair2 t1, Pair2 t2){
                int res = t1.getIdx() < t2.getIdx() ? -1: 1;
                return res;
            }
        });

        HashMap<Integer,Double> midMap = new HashMap<Integer, Double>();  //存储用户label和movieTags交叉重合特征 和 movieTags对应的特征

        while(sTok.hasMoreTokens()){
            String tmp = sTok.nextToken();
            Pair2 p2 = new Pair2(tmp);
            if(p2.getIdx() >= mvTagsStartId && p2.getIdx() < likeStartId ){
                midMap.put(p2.getIdx(), p2.getScore());
            }
            pairSet.add(p2);
        }

        int deleted = 0;
        int offset = likesMovieTagsStart - mvTagsStartId;
        Iterator<Map.Entry<Integer, Double>> iter = midMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<Integer, Double>entry = iter.next();
            if(entry.getKey() >= likesMovieTagsStart && entry.getKey() < likeStartId ){
                int keyId = entry.getKey() - offset;
                if(midMap.containsKey(keyId)){
                    midMap.put(entry.getKey(), midMap.get(keyId));
                }else {
                    iter.remove();                     // 说明占位的特征值在此条数据特征 不存在，删除，并统计
                    ++deleted;
                }
            }
        }

        int featSize = pairSet.size() - deleted;      // 声明Feature 数组大小， 并以此升序填入

        Feature[] feat = new FeatureNode[featSize];
        int i = 0;
        for(Pair2 p : pairSet){
            if(p.getIdx() >= likesMovieTagsStart && p.getIdx() < likeStartId ){
                if(midMap.containsKey(p.getIdx())){
                    feat[i++] = new FeatureNode(p.getIdx(), midMap.get(p.getIdx()));
                }
            }else{
                feat[i++] = new FeatureNode(p.getIdx(), p.getScore());

            }
        }
        return feat;
    }
    public static double predScore(Feature[] feature, Model model, int[] labels) {

        int loc1;
        loc1 = labels[0] == 1 ? 0 : 1;
        double[] res = new double[2];
        Linear.predictProbability(model, feature, res);
        BigDecimal bd = new BigDecimal(res[loc1]);
        double valp = Double.parseDouble(bd.setScale(4, BigDecimal.ROUND_HALF_UP).toString());
        return valp;

    }

    public static void main(String[] args)throws IOException{
        String model = args[0];
        String featId = args[1];

        String movieFeatures = args[2];
        String likes = args[3];

        String personalFile = args[4];
        BufferedReader bfr = new BufferedReader(new FileReader(personalFile));

        int topk = Integer.parseInt(args[5]);

        String outPath = args[6];
        BufferedWriter bfw = new BufferedWriter(new FileWriter(outPath));

        PersonRecommend pr = new PersonRecommend(model, featId, movieFeatures, likes);
        HashMap<String, String> mvMap = pr.movieItemFeatures;
        int[] featid = pr.featId;
        Model md = pr.model;
        int[] label = md.getLabels();
        HashMap<String, String> likeMp = pr.likeMap;

        TreeSet<T2> canset = new TreeSet<T2>(new Comparator<T2>() {
            public int compare(T2 p1, T2 p2){
                int res = p1.getVal() > p2.getVal()? -1: 1;
                return res;
            }
        });

        String line = null;
        while ((line = bfr.readLine()) != null) {

            StringTokenizer stk = new StringTokenizer(line,",\t");

            String guid = stk.nextToken();
            StringBuffer outStr = new StringBuffer();
            outStr.append( guid+"\t");
            canset.clear();

            while(stk.hasMoreTokens()){
                String mvStr = stk.nextToken();
                String mvId = mvStr.substring(0,32);
                if(mvMap.containsKey(mvId) && likeMp.containsKey(guid)){
                    String ftStr = mvMap.get(mvId) + "," + likeMp.get(guid);
                    Feature[] feature = pr.getFeature(ftStr, featid);
                    double sc = predScore(feature, md, label);

                    if(canset.size() >= topk){
                        if(sc > canset.last().getVal()){
                            canset.pollLast();
                            canset.add(new T2(mvId, sc));
                        }
                    }else {
                        canset.add(new T2(mvId, sc));
                    }
                }
            }
            int cnt = 1;
            for(T2 t2 : canset){
                if(cnt == canset.size()){
                    outStr.append(t2.getKey() + ":" +cnt);
                }else {
                    outStr.append(t2.getKey() + ":" +cnt++ + ",");
                }

            }
            bfw.write(outStr.toString());
            bfw.flush();
            bfw.newLine();
        }
        bfr.close();
        bfw.close();

    }

}
