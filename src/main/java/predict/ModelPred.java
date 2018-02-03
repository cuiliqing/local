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
 * Created by Tsui on 17/9/2.
 *  注意与类Modelpredv2 区别，
 * 输入：0 模型，1 id索引文件 2 偏好文件， 3 show上某天的guid文件， 4 电影特征文件， 5 topK， 6 输出文件  (7 中间结果数据路径)
 * 改进：getSortedFeatures 内一次遍历获得midMap, 不需从头重新遍历字符串获得midMap;
 */
public class ModelPred {

    HashSet<String> guids ;
    HashMap<String, String>movieFeatMap ;
    public ModelPred(String guidPath1, String moviesPath2)throws IOException{
        this.guids = getGuidSet(guidPath1);
        this.movieFeatMap = getMovieMap(moviesPath2);
    }

    /**
     * function  把电影id ［features : value］预处理，＝》 movieId  id : val id:val ...
     * 存储为HashMap 格式数据
     * */
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

    /**
     *  //获取movie特征和用户偏好特征后的有序特征序列, 已处理好tags和偏好label相同的featuresNode
    * */
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

    /**
     * function: 获取model对此样本（一个电影的特征＋用户偏好特征后的样本）的预测1的概率值
     * 作为 user 选取movieitem的topK的排序依据
     * */
    public static double predScore(Feature[] feature, Model model) {

        //int loc1;
        //loc1 = labels[0] == 1 ? 0 : 1;
        //double[] res = new double[2];
        //Linear.predictProbability(model, feature, res);

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

    /**
     * 读取featuresId文件
     * 获取当前模型中 各个类别的特征key（如 act、direct、movieTags、用户偏好likes）的分界线
     * */
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
    /**
    * function：打印中间结果
     * 格式 guid movieId  id:val id:val ...
     *
    * */
    public static void printMidRes( BufferedWriter bfw, Feature[] fet, String guid, String mvId)throws IOException{
        StringBuffer strBuf = new StringBuffer();
        strBuf.append(guid + " ");
        strBuf.append(mvId + " ");
        for(Feature node : fet){
            int id = node.getIndex();
            double val = node.getValue();
            strBuf.append(id + ":" + val + " ");
        }
        String str = strBuf.toString();
        bfw.write(str);
        bfw.flush();
        bfw.newLine();
    }

    public static void main(String[] args)throws IOException{

        String md = args[0];     // 模型文件
        Model model = Model.load(new File(md));
        int[] label = model.getLabels();

        String featIdPath = args[1];    // 特征id索引文件
        BufferedReader bfrId = new BufferedReader(new FileReader(featIdPath));

        String likePath = args[2];      // 用户偏好
        String guidPth = args[3];       // 用户guid 文件
        String movieset = args[4];       // 电影特征文件
        int topk = Integer.parseInt(args[5]);    // topk阈值设置
        String output = args[6];              // 输出文件路径
        //------------------------//
        //String middleRes = args[7];          // 中间结果数据输出路径
        //BufferedWriter bfwMid = new BufferedWriter(new FileWriter(middleRes));
        //-----------------------//
        BufferedReader bfr = new BufferedReader(new FileReader(likePath));
        BufferedWriter bfw = new BufferedWriter(new FileWriter(output));

        String line = null;
        ModelPred mp = new ModelPred(guidPth, movieset);

        int[] featId = mp.getFeatId(bfrId);
        bfrId.close();

        HashSet<String> guidset = mp.guids;
        HashMap<String, String> moviemap = mp.movieFeatMap;

        TreeSet<T2> canset = new TreeSet<T2>(new Comparator<T2>() {
            public int compare(T2 p1, T2 p2){
                int res = p1.getVal() > p2.getVal()? -1: 1;     ///> ：降序  < :  升序
                return res;
            }
        });
        boolean flag = label[0]==1 ? true : false;
        /**
         *  读user偏好文件 ，每个用户的偏好与索引电影依次匹配，获得score，排序，只取topk个候选集
         * */
        while ((line = bfr.readLine()) != null) {
            StringTokenizer stk = new StringTokenizer(line,"\t");
            String guid = stk.nextToken();

            if(guidset.contains(guid)){
                String likestr = stk.nextToken();
                likestr = likestr.substring(1);
                likestr = likestr.substring(0, likestr.length()-1);

                for(Map.Entry<String, String> entry : moviemap.entrySet()){
                    String featstr = entry.getValue() + "," + likestr;

                    Feature[] fets = mp.getSortedFeatures(featstr, featId);

                    //---------------------- 输出中间结果-----------------//
                   // printMidRes(bfwMid, fets, guid, entry.getKey());
                    //--------------------------------------------------//
                    double sc = predScore(fets, model);            //////////////////////
                    sc = flag == true ? sc : -sc;
                    if(canset.size() >= topk){
                        if(sc > canset.last().getVal()){   // 取top
                            canset.pollLast();
                            canset.add(new T2(entry.getKey(), sc));
                        }
                    }else {
                        canset.add(new T2(entry.getKey(), sc));
                    }

                }
                String outStr = guid + "\t";
                int cnt =1;
                for(T2 t2 : canset){
                    if(cnt == canset.size()){
                        outStr += t2.toString();
                    }else{
                        outStr += t2.toString() +",";
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

        //bfwMid.close();  //中间结果数据打印
    }

}
