package PersonalRecommend;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import models.Pair2;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by qiguo on 17/10/18.
 */
public class ModelPredProb {
    Model model;
    HashMap<String, String> itemDict;
    public ModelPredProb(String movieFeatsPath, String modelPath) throws IOException {
        this.itemDict = getItemDicts(movieFeatsPath);    //movieItemFeature 集合
        this.model = Model.load(new File(modelPath));
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

    public static double predScore(Feature[] feature, Model model, int[] labels){
        int loc1;
        loc1 = labels[0] == 1 ? 0 : 1;
        double[] res = new double[2];
        //Linear.predictProbability(model, feature, res);
        /*
        double bias = model.getBias();
        double[] weight = model.getFeatureWeights();
        double res = 0.0;
        for(Feature node : feature){
            res += weight[node.getIndex() - 1] * node.getValue();
        }
        if(bias +1.0 > 0){
            res += bias;
        }
        */

        Linear.predictProbability(model, feature, res);
        BigDecimal bd = new BigDecimal(res[loc1]);
        double valp = Double.parseDouble(bd.setScale(4, BigDecimal.ROUND_HALF_UP).toString());
        return valp;

    }
    public HashMap<Integer, Double> joinMap(String featStr, int movieTagStartIdx, int likesMovieTagsStart, int likesStartId){
        StringTokenizer stk = new StringTokenizer(featStr, " ,");
        stk.nextToken(); //lab

        HashMap<Integer, Double>tmpMap = new HashMap<Integer, Double>();
        while (stk.hasMoreTokens()) {
            String pair = stk.nextToken().trim();
            Pair2 tup = new Pair2(pair);
            if(tup.getIdx() >= movieTagStartIdx && tup.getIdx() <= likesStartId - 1){
                tmpMap.put(tup.getIdx(), tup.getScore());
            }
        }


        int offset = likesMovieTagsStart - movieTagStartIdx;
        int deleted = 0;
        Iterator<Map.Entry<Integer, Double>> iter = tmpMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<Integer, Double>entry = iter.next();
            if(entry.getKey() >= likesMovieTagsStart && entry.getKey() <= likesStartId - 1){
                int keyId = entry.getKey() - offset;
                if(tmpMap.containsKey(keyId)){
                    tmpMap.put(entry.getKey(), tmpMap.get(keyId));
                }else {
                    iter.remove();
                    ++deleted;
                }
            }
        }
        return tmpMap;
    }

    public static void printMidRes(BufferedWriter bfw, Feature[] fet, String guid, String mvId)throws IOException{
        StringBuffer strBuf = new StringBuffer();
        strBuf.append(guid + " ");
        strBuf.append(mvId + " ");
        for(Feature node : fet){
            int id = node.getIndex();
            double val = node.getValue();
            strBuf.append(id + ":" + val + " ");   ///////
        }
        String str = strBuf.toString();
        bfw.write(str);
        bfw.flush();
        bfw.newLine();
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


    public static void main(String[] args) throws IOException{
        String modelPath = args[0];    //  模型文件

        String fetIdPath = args[1];   // 特征id文件
        BufferedReader bfrId = new BufferedReader(new FileReader(fetIdPath));

        String likesPath = args[2];  //guid 喜好数据 格式： guid [arr1:v1,arr2:v2...]

        String moviesPath = args[3];    // 电影itemfeature文件

        int topK = Integer.parseInt(args[4]);  //  候选集阈值topk设置

        String outpath = args[5];            // 输出路径


        File outFile = new File(outpath);
        if(! outFile.exists()){
            outFile.createNewFile();
        }

        //String midPath = args[6];         //中间数据输出文件
        //BufferedWriter bfww = new BufferedWriter(new FileWriter(midPath));

        ModelPredProb modelPred = new ModelPredProb(moviesPath, modelPath);  // 构造函数 两个文件路径

        int[] label = modelPred.model.getLabels();           //  获取model 中label 数组
        int[] featIds = modelPred.getFeatId(bfrId);           //---- 获取索引id数组
        bfrId.close();

        Map<String, String> itemDict = modelPred.itemDict;

        BufferedWriter bw = new BufferedWriter(new FileWriter(outpath));
        BufferedReader bf = new BufferedReader(new FileReader(likesPath));
        String line = null;

        TreeSet<T2> canset = new TreeSet<T2>(new Comparator<T2>() {
            public int compare(T2 p1, T2 p2){
                int res = p1.getVal() > p2.getVal()? -1: 1;     ///> ：降序  < :  升序
                return res;
            }
        });
        boolean flag = label[0]==1 ? true : false;
        while((line = bf.readLine()) != null){
            int loc = line.indexOf("\t");
            String guid = line.substring(0, loc);
            String likes = line.substring(loc + 2);
            likes = likes.substring(0, likes.length() - 1);

            for(Map.Entry<String, String> entry : itemDict.entrySet()){
                String fet = entry.getValue() + "," + likes;

                Feature[] feat = modelPred.getFeature(fet, featIds);

                //--------- 中间结果输出 -----------
                //printMidRes(bfww, feat, guid, entry.getKey());          ///测试用  把测试的几个人的数据按liblinear格式输出 假类别标签注为1.0

                //-------------------------------------

                double sc = predScore(feat, modelPred.model, label);
                //sc = flag == true ? sc : -sc;          // 取底部数据最小数据时， 加负号后可按降序排列
                if(canset.size() >= topK){
                    if(sc > canset.last().getVal()){   //降序 取top
                        canset.pollLast();
                        canset.add(new T2(entry.getKey(), sc));
                    }
                }else {
                    canset.add(new T2(entry.getKey(), sc));
                }
            }

            String persCandi = "";
            int cnt = 1;
            for(T2 t: canset){
                if(cnt == canset.size()){
                    persCandi += t.toString();
                }else {
                    persCandi += t.toString()+",";  ////////////////
                }
                cnt++;
            }
            bw.write(guid + "\t" + persCandi);
            bw.flush();
            bw.newLine();
            canset.clear();
        }
        bw.close();
        bf.close();
        //bfww.close();
    }


}
