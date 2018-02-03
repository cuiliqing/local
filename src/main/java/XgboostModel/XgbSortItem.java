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
 * Created by qiguo on 18/1/24.
 */
public class XgbSortItem {
    HashMap<String, String> movieFeatsMp ;
    int[] featidx;
    Booster bst;

    public XgbSortItem(String mvItemFeatsPath, String featsIdxPath, String ModelPath)
            throws IOException, XGBoostError {
        this.featidx = getFeatsIndices(featsIdxPath);
        this.movieFeatsMp = getMoviesMp(mvItemFeatsPath);
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
        String modelPath = args[0];  // xgboost模型
        String movieFeatsPath = args[1];  // movieItemfeatures 文件
        String featuresIdxpath = args[2];  // relatedFeaturesIdx.txt索引文件

        String relatedIn = args[3];            //召回后的数据，输入
        int topK = Integer.parseInt(args[4]);  //阈值
        String relatedRec = args[5];          // 输出
        BufferedWriter relatedRecbfw = new BufferedWriter(new FileWriter(relatedRec));

        XgbSortItem XgbRelatedPred = new XgbSortItem(movieFeatsPath, featuresIdxpath, modelPath);
        HashMap<String, String> mvFeatMp = XgbRelatedPred.movieFeatsMp;
        int[] featIdx = XgbRelatedPred.featidx;

        TreeSet<T2> canSet = new TreeSet<T2>(new Comparator<T2>() {
            public int compare(T2 o1, T2 o2) {
                int res = o1.getVal() > o2.getVal() ? -1 : 1; //降序
                return res;
            }
        });

        BufferedReader bfr = new BufferedReader(new FileReader(relatedIn)); // 召回后的相关推荐
        String line = "";
        while ((line = bfr.readLine()) != null){

            StringTokenizer stk = new StringTokenizer(line, "\t");
            String mvid = stk.nextToken();

            String mvSetStr = stk.nextToken();
            StringTokenizer secstk = new StringTokenizer(mvSetStr, ",");

            HashSet<String> canSetMvid = new HashSet<String>();

            while (secstk.hasMoreTokens()){
                String mvtmp = secstk.nextToken().trim();
                String mvIdTmp = mvtmp.substring(0, 32);

                if(mvFeatMp.containsKey(mvIdTmp) && mvFeatMp.containsKey(mvid)){
                    String mv1FeatStr = mvFeatMp.get(mvid);

                    DMatrix pred = getPredDMtrix(mv1FeatStr, mvFeatMp.get(mvIdTmp), featIdx);
                    float[][] predScore = XgbRelatedPred.bst.predict(pred,true);
                    BigDecimal bgd = new BigDecimal(predScore[0][0]);
                    double res = 10.0 + Double.parseDouble(bgd.setScale(3, BigDecimal.ROUND_HALF_UP).toString());

                    if(!canSetMvid.contains(mvIdTmp)){  // 去重
                        canSetMvid.add(mvIdTmp);
                        if(canSet.size() >= topK){   //保留 topk个，
                            if(canSet.last().getVal() < res){
                                canSet.pollLast();
                                canSet.add(new T2(mvIdTmp, res));
                            }
                        }else {
                            canSet.add(new T2(mvIdTmp, res));
                        }
                    }
                }
            }
            String relatedRecOut = mvid + "\t";
            int tot = canSet.size();
            int cnt = 1;
            for(T2 t2 : canSet){
                if(cnt < tot){
                    relatedRecOut += t2.toString()+":E:103,";
                }else {
                    relatedRecOut += t2.toString()+":E:103";
                }
                cnt++;
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
