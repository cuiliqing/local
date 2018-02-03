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
 * Created by Tsui on 17/12/21.
 *
 */
public class XgbRelatedRecPred {
    HashMap<String, String>mvItemsMp ;
    int[] featidx;
    Booster bst;

    public XgbRelatedRecPred(String mvItemFeatsPath, String featsIdxPath, String ModelPath)
            throws IOException, XGBoostError{
        this.featidx = getFeatsIndices(featsIdxPath);
        this.mvItemsMp = getMoviesMp(mvItemFeatsPath);
        this.bst = XGBoost.loadModel(ModelPath);
    }


    public int[] getFeatsIndices(String path)throws IOException{

        BufferedReader bfr = new BufferedReader(new FileReader(path));
        int cnt = 0;
        int[] featIdx = new int[2];

        String line = "";
        while ((line = bfr.readLine()) != null){
            if(cnt == 2){
                String[] ftId = line.split(":");
                featIdx[0] = Integer.parseInt(ftId[1].trim());  // mvTags 起始id
            }
            else if (cnt == 3){
                String[] ftId = line.split(":");
                featIdx[1] = Integer.parseInt(ftId[1].trim());  // join join交叉起始
            }
            cnt++;
        }
        bfr.close();
        return featIdx;
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

    public static DMatrix getPredDMtrix(String mv1str, String mv2Str, int[] featsIdx) throws XGBoostError{

        int offset = featsIdx[1] - 1;
        int totlen = 2 * offset - 1; //第一部电影＋交叉特征的总长度

        TreeSet<Pair2> pairSet = new TreeSet<Pair2>(new Comparator<Pair2>() {
            public int compare(Pair2 t1, Pair2 t2){

                int res = t1.getIdx() < t2.getIdx() ? -1: 1;     //升序
                return res;
            }
        });

        HashMap<Integer, Double> mvMp = new HashMap<Integer, Double>();
        StringTokenizer stk = new StringTokenizer(mv1str, ", ");
        while (stk.hasMoreTokens()) {
            String p = stk.nextToken().trim();
            Pair2 p2 = new Pair2(p);
            mvMp.put(p2.getIdx(), p2.getScore());
            pairSet.add(p2);
        }

        StringTokenizer subStk = new StringTokenizer(mv2Str, ", ");
        while (subStk.hasMoreTokens()) {
            Pair2 p2 = new Pair2(subStk.nextToken().trim());
            if(p2.getIdx() < featsIdx[1] - 2       //上映和时间
                    && mvMp.containsKey(p2.getIdx())){ //&& entry.getKey() >= featIdx[0]
                pairSet.add(new Pair2(p2.getIdx() + offset, 1.0));
            }else if (p2.getIdx() == featsIdx[1] - 1  //把 上映时间 也做交叉特征处理
                    && mvMp.containsKey(p2.getIdx())) {
                pairSet.add(new Pair2(totlen, getDateDiff(p2.getScore(), mvMp.get(p2.getIdx()))));
            }
            pairSet.add(new Pair2(p2.getIdx() + totlen, p2.getScore()));
        }


        int featSize = pairSet.size();
        long[] header = {0, featSize};
        int[] indices = new int[featSize];
        float[] featVal = new float[featSize];
        int i = 0;
        int j = 0;
        for(Pair2 p2 : pairSet){
            indices[i++] = p2.getIdx();
            featVal[j++] = (float)p2.getScore();
        }

        return new DMatrix(header, indices, featVal, DMatrix.SparseType.CSR);
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


    public static void main(String[] args)throws IOException, XGBoostError{
        String modelPath = args[0];  // 模型文件
        String movieFeatsPath = args[1];   // 电影特征文件 movieItemFeatures
        String featuresIdxpath = args[2];  // 特征id索引文件

        int topK = Integer.parseInt(args[3]);  // 设定阈值topk
        String relatedRec = args[4];
        BufferedWriter relatedRecbfw = new BufferedWriter(new FileWriter(relatedRec));   // 输出

        XgbRelatedRecPred XgbRelatedPred = new XgbRelatedRecPred(movieFeatsPath, featuresIdxpath, modelPath);
        HashMap<String, String> mvFeatMp = XgbRelatedPred.mvItemsMp;
        int[] featIdx = XgbRelatedPred.featidx;

        TreeSet<T2> canSet = new TreeSet<T2>(new Comparator<T2>() {
            public int compare(T2 o1, T2 o2) {
                int res = o1.getVal() > o2.getVal() ? -1 : 1; //降序
                return res;
            }
        });

        BufferedReader bfr = new BufferedReader(new FileReader(movieFeatsPath));
        String line = "";
        while ((line = bfr.readLine()) != null){

            StringTokenizer stk = new StringTokenizer(line, "\t");
            String mvid = stk.nextToken();

            String mvFeats = stk.nextToken();
            mvFeats = mvFeats.substring(1);
            mvFeats = mvFeats.substring(0, mvFeats.length()-1);

            for(Map.Entry<String, String>entry : mvFeatMp.entrySet()){
                if(!entry.getKey().equals(mvid)){

                    DMatrix pred = getPredDMtrix(mvFeats, entry.getValue(), featIdx);
                    float[][] predScore = XgbRelatedPred.bst.predict(pred,true);
                    BigDecimal bgd = new BigDecimal(predScore[0][0]);
                    double res = Double.parseDouble(bgd.setScale(3, BigDecimal.ROUND_HALF_UP).toString());

                    if(canSet.size() >= topK){   //保留 topk个，
                        if(canSet.last().getVal() < res){
                            canSet.pollLast();
                            canSet.add(new T2(entry.getKey(), res));
                        }
                    }else {
                        canSet.add(new T2(entry.getKey(), res));
                    }
                }
            }

            String relatedRecOut = mvid + "\t";
            for(T2 t2 : canSet){
                relatedRecOut += t2.toString()+" ";
            }
            relatedRecbfw.write(relatedRecOut);
            relatedRecbfw.flush();
            relatedRecbfw.newLine();
            canSet.clear();
        }
        bfr.close();
        relatedRecbfw.close();
    }
}
