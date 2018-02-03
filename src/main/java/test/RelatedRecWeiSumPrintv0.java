package test;

import PersonalRecommend.T2;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Model;
import models.Pair2;

import java.io.*;
import java.util.*;

/**
 * Created by qiguo on 17/11/16.
 */
public class RelatedRecWeiSumPrintv0 {

    public static void main(String[] args)throws IOException {
        RelatedRecWeiSumPrintv0 rms = new RelatedRecWeiSumPrintv0();

        String firstMvPath = "/Users/qiguo/Documents/relatedRec/first.txt";
        String secMvPath = "/Users/qiguo/Documents/relatedRec/tmp.txt";

        String modelpth = "/Users/qiguo/Documents/relatedRec/relatedModel";
        String outpath = "/Users/qiguo/Documents/relatedRec/out.txt";
        BufferedWriter bfw = new BufferedWriter(new FileWriter(outpath));

        Model model = Model.load(new File(modelpth));
        double[] wei = model.getFeatureWeights();
        HashMap<String, String> secMvMp = rms.getmvMap(secMvPath);
        BufferedReader bfr = new BufferedReader(new FileReader(firstMvPath));
        String firstMv = "";
        String firstFtsStr = "";

        String line = "";
        while((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, "\t");
            firstMv = stk.nextToken();
            firstFtsStr = stk.nextToken();
            firstFtsStr = firstFtsStr.substring(1);
            firstFtsStr = firstFtsStr.substring(0, firstFtsStr.length()-1);

            TreeSet<T2> canset = new TreeSet<T2>(new Comparator<T2>() {
                public int compare(T2 o1, T2 o2) {
                    int res = o1.getVal() > o2.getVal() ? -1 : 1;
                    return res;
                }
            });

            for (Map.Entry<String, String> entry : secMvMp.entrySet()){
                Feature[] feat = rms.getFeatures(firstFtsStr, entry.getValue());
                bfw.write(firstMv + "\t" +entry.getKey()); bfw.newLine();
                double res = 0.0;

                for(int i = 0; i < feat.length; i++){
                    res += wei[feat[i].getIndex() - 1 ] * feat[i].getValue();
                    String out = feat[i].getIndex() + "\t" + wei[feat[i].getIndex()-1] +"\t"+ feat[i].getValue() + "\t"
                            + wei[feat[i].getIndex() - 1]*feat[i].getValue() + "\t" + res;
                    bfw.write(out);
                    bfw.flush();
                    bfw.newLine();
                    System.out.println(res);
                }

            }

        }
        bfr.close();
        bfw.close();
    }

    public HashMap<Integer, Double> mvId2FeaturesMp(String mvFtStr){
        HashMap<Integer, Double> mvFtsMp = new HashMap<Integer, Double>();
        StringTokenizer stk = new StringTokenizer(mvFtStr, ", ");
        while(stk.hasMoreTokens()){
            Pair2 p2 = new Pair2(stk.nextToken());
            mvFtsMp.put(p2.getIdx(), p2.getScore());
        }
        return mvFtsMp;
    }

    public Feature[] getFeatures(String str1, String str2){

        HashMap<Integer, Double>ftmp1 = mvId2FeaturesMp(str1);
        HashMap<Integer, Double>ftmp2 = mvId2FeaturesMp(str2);
        int[] featIdx ={15829, 24147};                 // tagStartId, joinStartId
        int offset = featIdx[1] - 1;
        int firstMvTotLen =  offset*2 - 2;//featIdx[2] + offset - 3 ;   ///featIdx[1] - 3

        //HashSet<Pair2> set = new HashSet<Pair2>();
        for(Map.Entry<Integer,Double> entry : ftmp2.entrySet()){
            if(entry.getKey() < featIdx[1] - 2 //&& entry.getKey() >= featIdx[0]
                    && ftmp1.containsKey(entry.getKey())){
                ftmp1.put(entry.getKey() + offset, 1.0);
                //set.add(new Pair2(entry.getKey(), entry.getValue()));
            }
            ftmp1.put(entry.getKey() + firstMvTotLen, entry.getValue());
        }

        Feature[] feats = new FeatureNode[ftmp1.size()];
        TreeSet<Pair2> pairset = new TreeSet<Pair2>(new Comparator<Pair2>() {
            public int compare(Pair2 o1, Pair2 o2) {
                int res = o1.getIdx() < o2.getIdx() ? -1 : 1;   //升序
                return res;
            }
        });

        for(Map.Entry<Integer, Double> entry : ftmp1.entrySet()){
            pairset.add(new Pair2(entry.getKey(), entry.getValue()));
        }

        int i = 0;
        for( Pair2 p2 : pairset){
            feats[i++] = new FeatureNode(p2.getIdx(), p2.getScore());
        }
        return feats;
    }


    public HashMap<String, String> getmvMap(String path)throws IOException{
        HashMap<String, String> mvMp = new HashMap<String, String>();

        BufferedReader bfr2 = new BufferedReader(new FileReader(path));
        String line2 = "";
        while((line2 = bfr2.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line2, "\t");
            String secid = stk.nextToken();
            String secStr = stk.nextToken();
            secStr = secStr.substring(1);
            secStr = secStr.substring(0, secStr.length()-1);
            mvMp.put(secid, secStr);
        }
        bfr2.close();
        return mvMp;
    }

}
