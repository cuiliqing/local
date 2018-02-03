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
 * Created by qiguo on 17/11/27.
 */
public class RelatedRecAreaTagsFullShow {
    HashMap<String, String> movItemSet;
    Model model;

    public RelatedRecAreaTagsFullShow(String movPth, String modelPth)throws IOException {
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
        int[] feadIdx = new int[3];   //
        while((line = bfr.readLine()) != null){
            if(cnt == 1){
                String[] ftId = line.split(":");
                feadIdx[0] = Integer.parseInt(ftId[1].trim());  // area 起始id
            }else if(cnt == 2){
                String[] ftId = line.split(":");
                feadIdx[1] = Integer.parseInt(ftId[1].trim());  // mvTags 起始id
            }
            else if (cnt == 3){
                String[] ftId = line.split(":");
                feadIdx[2] = Integer.parseInt(ftId[1].trim());  // join join起始
            }
            else if(cnt > 3) break;
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


    public Feature[] getFeatures(String str1, String str2, int[] featIdx, HashMap<String, String>mvitemsMp){

        HashMap<Integer, Double>ftmp1 = getMvFeatsMp(mvitemsMp.get(str1));
        HashMap<Integer, Double>ftmp2 = getMvFeatsMp(mvitemsMp.get(str2));

        int offset = featIdx[2] - featIdx[0];    //设置交叉特征区域段
        int firstSegTotLen = featIdx[2] + offset -3; //offset*2 - 2;

        HashSet<Pair2> set = new HashSet<Pair2>();
        for(Map.Entry<Integer,Double> entry : ftmp2.entrySet()){
            if(entry.getKey() < featIdx[2] - 2 && entry.getKey() >= featIdx[0]  //只交叉area 和 tags
                    && ftmp1.containsKey(entry.getKey())){
                ftmp1.put(entry.getKey() + offset, 1.0);
                //set.add(new Pair2(entry.getKey(), entry.getValue()));
            }
            ftmp1.put(entry.getKey() + firstSegTotLen, entry.getValue());
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

        RelatedRecAreaTagsFullShow rm = new RelatedRecAreaTagsFullShow(movSetPth, modelPth);
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
