package XgboostModel;

import PersonalRecommend.T2;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import models.Pair2;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by qiguo on 17/12/22.
 */
public class XgbRelatedRecTest {
    HashMap<String, String> mvItemsMp ;
    int[] featidx;
    Booster bst;

    public XgbRelatedRecTest(String mvItemFeatsPath, String featsIdxPath, String ModelPath)
            throws IOException, XGBoostError {
        this.featidx = getFeatsIndices(featsIdxPath);
        this.mvItemsMp = getMoviesMp(mvItemFeatsPath);
        this.bst = XGBoost.loadModel(ModelPath);
    }


    public int[] getFeatsIndices(String path)throws IOException{

        BufferedReader bfr = new BufferedReader(new FileReader(path));
        int cnt = 0;
        int[] feadIdx = new int[2];

        String line = "";
        while ((line = bfr.readLine()) != null){
            if(cnt == 2){
                String[] ftId = line.split(":");
                feadIdx[0] = Integer.parseInt(ftId[1].trim());  // mvTags 起始id
            }
            else if (cnt == 3){
                String[] ftId = line.split(":");
                feadIdx[1] = Integer.parseInt(ftId[1].trim());  // join join交叉起始
            }
            else if(cnt > 3) break;
            cnt++;
        }
        bfr.close();
        return feadIdx;
    }

    public HashMap<String, String> getMoviesMp(String path)throws  IOException{

        HashMap<String, String> moviesMp = new HashMap<String, String>();
        BufferedReader bfr = new BufferedReader(new FileReader(path));

        String line = "";
        while ((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, "\t");
            String mvid = stk.nextToken();
            String mvFeatStr = stk.nextToken();
            mvFeatStr = mvFeatStr.substring(1);
            mvFeatStr = mvFeatStr.substring(0, mvFeatStr.length()-1);

            moviesMp.put(mvid, mvFeatStr);
        }
        bfr.close();
        return moviesMp;

    }

    public static String getSortedFeatStr(String mv1str, String mv2Str, int[] featsIdx){

        int offset = featsIdx[1] - 1;
        int totlen = 2 * offset -1;
        HashMap<Integer, Double> mvMp = new HashMap<Integer, Double>();
        StringTokenizer stk = new StringTokenizer(mv1str, ", ");
        while (stk.hasMoreTokens()) {
            String p = stk.nextToken().trim();
            Pair2 p2 = new Pair2(p);
            mvMp.put(p2.getIdx(), p2.getScore());
        }

        StringTokenizer subStk = new StringTokenizer(mv2Str, ", ");
        while (subStk.hasMoreTokens()) {
            Pair2 p2 = new Pair2(subStk.nextToken().trim());
            if(p2.getIdx() < featsIdx[1]- 2       //上映和时间
                    && mvMp.containsKey(p2.getIdx())){ //&& entry.getKey() >= featIdx[0]
                mvMp.put(p2.getIdx() + offset, 1.0);
            }else if (p2.getIdx() == featsIdx[1] - 1  //把 上映时间 也做交叉特征处理
                    && mvMp.containsKey(p2.getIdx())) {
                mvMp.put(totlen, getDateDiff(p2.getScore(), mvMp.get(p2.getIdx())));
            }
            mvMp.put(p2.getIdx() + totlen, p2.getScore());
        }
        String featStr = getSortedFeatures(mvMp);
        return featStr;
    }

    public static String getSortedFeatures(HashMap<Integer, Double>totalMap){

        TreeSet<Pair2> pairSet = new TreeSet<Pair2>(new Comparator<Pair2>() {
            public int compare(Pair2 t1, Pair2 t2){

                int res = t1.getIdx() < t2.getIdx() ? -1: 1;     //升序
                return res;
            }
        });

        for(Map.Entry<Integer, Double>entry : totalMap.entrySet()){
            Pair2 p2 = new Pair2(entry.getKey(), entry.getValue());
            pairSet.add(p2);
        }
        String out = "";
        for(Pair2 t2 : pairSet){
            out += t2.toString() + " ";
        }
        return out;
    }

    public static double getDateDiff(double date1, double date2){

        double diff = Math.abs(date1 - date2);
        double res = 0.6;
        if(diff <= 0.2){
            res = 1.0;
        }else if (diff < 0.5 && diff > 0.2) {
            res = 0.6;
        }else if (diff >= 0.5 && diff < 0.7) {
            res = 0.3;
        }else if(diff >= 0.7) {
            res = 0.1;
        }
        return res;
    }


    public static void writePredDataFile(String str, BufferedWriter bfw)throws IOException{
        bfw.write(str);
        bfw.flush();
        bfw.newLine();
    }

    public static void main(String[] args)throws IOException,XGBoostError{
        String modelPath = "/Users/qiguo/Documents/XgbRelatedRecTest/XgbrelatedTest/XgRelatedRecModel";//args[0];
        String movieFeatsPath = "/Users/qiguo/Documents/XgbRelatedRecTest/XgbrelatedTest/movieItemFeatures";//args[1];
        String featuresIdxpath = "/Users/qiguo/Documents/XgbRelatedRecTest/XgbrelatedTest/relatedFeatsIdx.txt";//args[2];

        String outPredData = "/Users/qiguo/Documents/XgbRelatedRecTest/XgbrelatedTest/outPredData";//args[3];
        BufferedWriter predBfw = new BufferedWriter(new FileWriter(outPredData));

        int topK = 30;//Integer.parseInt(args[4]);
        String relatedRec = "/Users/qiguo/Documents/XgbRelatedRecTest/XgbrelatedTest/outRec.txt";//args[5];
        BufferedWriter relatedRecbfw = new BufferedWriter(new FileWriter(relatedRec));

        XgbRelatedRecTest XgbRelatedPred = new XgbRelatedRecTest(movieFeatsPath, featuresIdxpath, modelPath);
        HashMap<String, String> mvFeatMp = XgbRelatedPred.mvItemsMp;
        int[] featIdx = XgbRelatedPred.featidx;

        int mvtot = mvFeatMp.size();

        String MovieItemsFeatsTest = "/Users/qiguo/Documents/XgbRelatedRecTest/XgbrelatedTest/movieItemTest";//args[6];
        BufferedReader bfr = new BufferedReader(new FileReader(MovieItemsFeatsTest));
        ArrayList<String> mvidLis = new ArrayList<String>();

        String line = "";
        while ((line = bfr.readLine()) != null){

            StringTokenizer stk = new StringTokenizer(line, "\t");
            String mvid = stk.nextToken();
            mvidLis.add(mvid); //mvid

            String mvFeats = stk.nextToken();
            mvFeats = mvFeats.substring(1);
            mvFeats = mvFeats.substring(0, mvFeats.length()-1);

            for(Map.Entry<String, String>entry : mvFeatMp.entrySet()){

                //0.0后有个空格，格式要求
                String outFeatStr = "0.0 " + getSortedFeatStr(mvFeats, entry.getValue(), featIdx);
                writePredDataFile(outFeatStr, predBfw);
            }
        }
        bfr.close();
        predBfw.close();

        float[][] predScore = XgbRelatedPred.bst.predict(new DMatrix(outPredData));

        TreeSet<T2> canSet = new TreeSet<T2>(new Comparator<T2>() {
            public int compare(T2 o1, T2 o2) {
                int res = o1.getVal() > o2.getVal() ? -1 : 1; //降序
                return res;
            }
        });

        /*-对预测电影进行排序，取topk操作-*/
        for(int i = 0; i < mvidLis.size(); i++ ){
            int mvcnt = i * mvtot;

            for(Map.Entry<String, String> entry : mvFeatMp.entrySet()){
                BigDecimal bgd = new BigDecimal(predScore[mvcnt][0]);
                double res = Double.parseDouble(bgd.setScale(3, BigDecimal.ROUND_HALF_UP).toString());

                if(canSet.size() > topK){   //保留 topk + 1个，（可能要去重推荐为本身的那个电影Item）
                    if(canSet.last().getVal() < res){
                        canSet.pollLast();
                        canSet.add(new T2(entry.getKey(), res));
                    }
                }else {
                    canSet.add(new T2(entry.getKey(), res));
                }
                mvcnt++;
            }

            String relatedRecOut = mvidLis.get(i) + "\t";
            int cnt = 0;
            for(T2 t2 : canSet){
                relatedRecOut += t2.toString()+" ";
                /*
                if(!t2.getKey().equals(mvidLis.get(i)) && cnt < topK){

                    cnt++;
                }*/
            }
            relatedRecbfw.write(relatedRecOut);
            relatedRecbfw.flush();
            relatedRecbfw.newLine();
            canSet.clear();
        }
        relatedRecbfw.close();
    }
}
