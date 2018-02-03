package XgboostModel;

import PersonalRecommend.T2;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import models.Pair2;

import java.io.*;
import java.util.*;

/**
 * Created by qiguo on 17/12/23.
 */
public class XgboostPred1DMtrixTest {
    Booster bst;
    HashMap<String, String> moviesFeatMp;

    public XgboostPred1DMtrixTest(String modelPath, String movieItemPath)throws IOException, XGBoostError {

        this.moviesFeatMp = getMoviesFeatMp(movieItemPath);
        this.bst = XGBoost.loadModel(modelPath);

    }

    public static int[] getFeatIdx(BufferedReader bfr)throws IOException{
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

                }else if(cnt == 2){  // join 交叉
                    int tagLikesStart = Integer.parseInt(stk.nextToken());
                    featId[cnt - 1] = tagLikesStart;

                }else {    // 偏好
                    int likeStart = Integer.parseInt(stk.nextToken());
                    featId[cnt - 1] = likeStart;
                }
            }
            cnt++;
        }
        return featId;
    }

    public HashMap<String, String>getMoviesFeatMp(String moviePtemPath)throws IOException{
        HashMap<String, String> mvFeatsMap = new HashMap<String, String>();
        BufferedReader bfr = new BufferedReader(new FileReader(moviePtemPath));
        String line = "";
        while((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, "\t");
            String mvid = stk.nextToken();
            String mvFeats = stk.nextToken();
            mvFeats = mvFeats.substring(1);
            mvFeats = mvFeats.substring(0, mvFeats.length()-1);
            mvFeatsMap.put(mvid, mvFeats);
        }
        bfr.close();

        return mvFeatsMap;
    }

    public static DMatrix getPredMatrix(String str, int[] featIdx)throws IOException, XGBoostError{
        int mvTagsStartId = featIdx[0];
        int likesMovieTagsStart = featIdx[1];
        int likeStartId = featIdx[2];

        StringTokenizer sTok = new StringTokenizer(str," ,");

        TreeSet<Pair2> pairSet = new TreeSet<Pair2>(new Comparator<Pair2>() {
            public int compare(Pair2 t1, Pair2 t2){
                int res = t1.getIdx() < t2.getIdx() ? -1: 1;  //升序
                return res;
            }
        });

        HashMap<Integer, Double> midMap = new HashMap<Integer, Double>();  //存储用户label和movieTags交叉重合特征 和 movieTags对应的特征

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
                if(!midMap.containsKey(keyId)){
                    iter.remove();                     // 说明占位的特征值在此条数据特征 不存在，删除，并统计
                    ++deleted;
                }
            }
        }

        int featSize = pairSet.size() - deleted;      // 声明Feature 数组大小， 并以此升序填入

        float[] featData = new float[featSize];
        int indices[] = new int[featSize];
        //StringBuffer strbuf = new StringBuffer();
        //strbuf.append("0.0 ");
        int i = 0, j = 0;
        for(Pair2 p : pairSet){
            if(p.getIdx() >= likesMovieTagsStart && p.getIdx() < likeStartId ){
                if(midMap.containsKey(p.getIdx())){
                    //strbuf.append(new Pair2(p.getIdx(), midMap.get(p.getIdx()).floatValue()).toString());
                    //strbuf.append(" ");
                    indices[j++] = p.getIdx();
                    featData[i++] = midMap.get(p.getIdx()).floatValue();
                }
            }else{
                //strbuf.append(p.toString());
                //strbuf.append(" ");
                indices[j++] = p.getIdx();
                featData[i++] = (float) p.getScore();
            }
        }
        long[] header = new long[2];
        header[0] = 0;
        header[1] = featData.length;

        return new DMatrix(header, indices, featData, DMatrix.SparseType.CSR);
    }

    public static void main(String[] args)throws IOException, XGBoostError{

        String modelPath = "/Users/qiguo/Documents/XgbTest/XgModel" ;//args[0];
        String movieFeats = "/Users/qiguo/Documents/XgbTest/movieItemFeatures"  ;//args[1];
        String featIdxPath = "/Users/qiguo/Documents/XgbTest/featuresIdx.txt" ;//args[2];
        String psnLikesPath = "/Users/qiguo/Documents/XgbTest/qiguolikes.txt" ;//args[3];

        BufferedWriter outpredBfw = new BufferedWriter(new FileWriter("/Users/qiguo/Documents/XgbTest/testMatrixOut.txt"));

        BufferedReader idxBfr = new BufferedReader(new FileReader(featIdxPath));
        int[] featIdx = getFeatIdx(idxBfr);
        idxBfr.close();

        XgboostPred1DMtrixTest XgbPred = new XgboostPred1DMtrixTest(modelPath, movieFeats);
        HashMap<String, String> movieMp = XgbPred.moviesFeatMp;

        TreeSet<T2> canSet = new TreeSet<T2>(new Comparator<T2>() {
            public int compare(T2 o1, T2 o2) {
                int res = o1.getVal() > o2.getVal() ? -1 : 1;
                return res;
            }
        });
        int topk = 200;

        //ArrayList<String> guidlis = new ArrayList<String>();
        BufferedReader likesBfr = new BufferedReader(new FileReader(psnLikesPath));
        String line = "";
        while ((line = likesBfr.readLine()) != null){
            int loc = line.indexOf("\t");
            String guid = line.substring(0, loc);
            //guidlis.add(guid);
            String likes = line.substring(loc + 2);
            likes = likes.substring(0, likes.length() - 1);

            for(Map.Entry<String, String> entry : movieMp.entrySet()){
                //System.out.println(guid + "\t" + entry.getKey());
                String mvStrPsnlike = entry.getValue() + "," + likes;

                DMatrix sam = getPredMatrix(mvStrPsnlike, featIdx);
                float[][] pred = XgbPred.bst.predict(sam, false);

                if(canSet.size() >= topk){
                    if(canSet.last().getVal() < pred[0][0]){
                        canSet.pollLast();
                        canSet.add(new T2(entry.getKey(), Double.parseDouble(pred[0][0]+"")));
                    }
                }else {
                    canSet.add(new T2(entry.getKey(), Double.parseDouble(pred[0][0]+"")));
                }
            }

            String out = guid + "\t";
            for(T2 t2: canSet){
                out += t2.toString() +" ";
            }
            outpredBfw.write(out);
            outpredBfw.flush();
            outpredBfw.newLine();

            canSet.clear();
        }
        outpredBfw.close();
        likesBfr.close();

    }
}
