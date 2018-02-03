package relatedRec;

import PersonalRecommend.T2;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Model;
import models.Pair2;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by tsui on 17/11/15.
 * @param: 0: movieitemFeatures; 1: model; 2: featuresIdx.txt 3: topk;
 *  output 4:out
 */
public class RelatedModelRec {
    HashMap<String, String>movItemSet;
    Model model;

    public RelatedModelRec(String movPth, String modelPth)throws IOException{
        this.model = Model.load(new File(modelPth));
        this.movItemSet = getMovItemSet(movPth);
    }

    public HashMap<String, String>getMovItemSet(String pth)throws IOException{
        HashMap<String, String>mvItemMap = new HashMap<String, String>();

        BufferedReader bfr = new BufferedReader(new FileReader(pth));
        String line = "";
        while ((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, "\t");
            String mvId = stk.nextToken();
            String mvFeatStr = stk.nextToken();
            mvFeatStr = mvFeatStr.substring(1);
            mvFeatStr = mvFeatStr.substring(0, mvFeatStr.length()-1);
            mvItemMap.put(mvId, mvFeatStr);
        }
        bfr.close();
        return mvItemMap;
    }

    public int[] getFeaturesIdx(String featIdxpth)throws IOException{
        BufferedReader bfr = new BufferedReader(new FileReader(featIdxpth));
        String line = "";
        int cnt = 0;
        int[] feadIdx = new int[2];
        while((line = bfr.readLine()) != null){
            if(cnt == 1){
                String[] ftId = line.split(":");
                feadIdx[0] = Integer.parseInt(ftId[1].trim());  // mvTags 起始id
            }
            else if (cnt == 2){
                String[] ftId = line.split(":");
                feadIdx[1] = Integer.parseInt(ftId[1].trim());  // join join起始
            }
            else if(cnt > 2) break;
            cnt++;
        }
        bfr.close();
        return feadIdx;
    }

    public HashMap<Integer, Double> getMvFeatsMp(String str){
        HashMap<Integer, Double> mvFtsMp = new HashMap<Integer, Double>();
        StringTokenizer stk = new StringTokenizer(str, ", ");
        while(stk.hasMoreTokens()){
            Pair2 p2 = new Pair2(stk.nextToken());
            mvFtsMp.put(p2.getIdx(), p2.getScore());
        }
        return mvFtsMp;
    }

    public double setPosTagsLevel(int size){
        double res = 0.0;
        if(size < 9 && size > 6){
            res = 20.0;
        }else if (size <=6 && size > 4) {
            res = 15.0;
        }else if (size >2 && size <= 4) {
            res = 10.0;
        }else if (size >=1 && size < 3) {
            res = 5.0;
        }
        return res;
    }

    public Feature[] getFeatures(String str1, String str2, int[] featIdx, HashMap<String, String>mvitemsMp){

        HashMap<Integer, Double>ftmp1 = getMvFeatsMp(mvitemsMp.get(str1));
        HashMap<Integer, Double>ftmp2 = getMvFeatsMp(mvitemsMp.get(str2));
        int offset = featIdx[1] - featIdx[0];
        int firstMvTotLen = featIdx[1] + offset - 3;

        HashSet<Pair2> set = new HashSet<Pair2>();
        for(Map.Entry<Integer,Double> entry : ftmp2.entrySet()){
            if(entry.getKey() < featIdx[1] - 2 && entry.getKey() >= featIdx[0]  // 交叉特征区域段
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

    public static double predScore(Model model, Feature[] feats){
        double bias = model.getBias();
        double[] weight = model.getFeatureWeights();

        double res = 0.0;
        for(Feature node : feats){
            res += weight[node.getIndex() - 1] * node.getValue();
        }
        if(bias +1.0 > 0){
            res += bias;
        }
        BigDecimal bd = new BigDecimal(res);
        double valp = Double.parseDouble(bd.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        return valp;
    }

    public static void main(String[] args)throws IOException{
       String movSetPth = args[0];
       String modelPth = args[1];

       RelatedModelRec rm = new RelatedModelRec(movSetPth, modelPth);
       int[] featidx = rm.getFeaturesIdx(args[2]);
       int[] label = rm.model.getLabels();

       int topk = Integer.parseInt(args[3]);
       String out = args[4];
       BufferedWriter bfw = new BufferedWriter(new FileWriter(out));

       TreeSet<T2> topKSet = new TreeSet<T2>(new Comparator<T2>() {
           public int compare(T2 o1, T2 o2) {
               int res = o1.getVal() > o2.getVal() ? -1 : 1;            //降序
               return res;
           }
       });

       int cnt = 0;
       boolean flag = label[0]==1 ? true : false;

       for(Map.Entry<String, String> entry : rm.movItemSet.entrySet()){
           String mvid = entry.getKey();
           topKSet.clear();
           cnt++;
           if(cnt > 50) break;
           for(Map.Entry<String, String> inentry : rm.movItemSet.entrySet()){
               String secmv = inentry.getKey();

               if(! mvid.equals(secmv)){
                   Feature[] feats = rm.getFeatures(mvid, secmv, featidx, rm.movItemSet);
                   double res = predScore(rm.model, feats);
                   BigDecimal bd = new BigDecimal(res);
                   res = Double.parseDouble(bd.setScale(4, BigDecimal.ROUND_HALF_UP).toString());
                   res = flag == true ? res : -res;

                   if(topKSet.size() >= topk){
                       if(res > topKSet.last().getVal()){
                           topKSet.pollLast();
                           topKSet.add(new T2(secmv, res));
                       }
                   }else {
                       topKSet.add(new T2(secmv, res));
                   }
               }
           }

           String outStr = mvid + "\t";
           for(T2 t2 : topKSet){
               outStr += t2.toString()+",";
           }
           bfw.write(outStr);
           bfw.flush();
           bfw.newLine();
       }
       bfw.close();
    }

}
