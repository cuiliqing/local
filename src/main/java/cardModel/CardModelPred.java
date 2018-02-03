package cardModel;

import PersonalRecommend.T2;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Model;
import models.Pair2;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by tsui on 17/11/10.
 * @param : 0,model; 1,featIdx; 2,cardFeats; 3,personalLikes; 4,topk
 * @param : 5,output
 **/
public class CardModelPred {
    Model model;
    HashMap<String, String>cardId2FeaturesMp;

    public CardModelPred(String modelPath, String cardId2FeatsPath)
            throws IOException{
        this.model = Model.load(new File(modelPath));
        this.cardId2FeaturesMp = getCardId2FeaturesMp(cardId2FeatsPath);
    }

    public HashMap<String, String> getCardId2FeaturesMp(String cardId2FeatsPath)
            throws IOException{
        BufferedReader bfr = new BufferedReader(new FileReader(cardId2FeatsPath));
        String line = "";
        HashMap<String, String>map = new HashMap<String, String>();
        while ((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, "\t");
            if(stk.countTokens()>0){   //有一个空行干扰
                String cardId = stk.nextToken();
                String cardFeats = stk.nextToken();
                map.put(cardId, cardFeats);
            }
        }
        return map;
    }
    public Feature[] getFeatures(String featStr, int[]featIds){
        int mvTagsStartId = featIds[0];
        int joinFeatStart = featIds[1];
        int likeStartId = featIds[2];
        StringTokenizer sTok = new StringTokenizer(featStr," ,");

        TreeSet<Pair2> pairSet = new TreeSet<Pair2>(new Comparator<Pair2>() {
            public int compare(Pair2 t1, Pair2 t2){
                int res = t1.getIdx() < t2.getIdx() ? -1: 1; //升序
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
        int offset = joinFeatStart - mvTagsStartId;
        Iterator<Map.Entry<Integer, Double>> iter = midMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<Integer, Double>entry = iter.next();
            if(entry.getKey() >= joinFeatStart && entry.getKey() < likeStartId ){
                int keyId = entry.getKey() - offset;
                if(midMap.containsKey(keyId)){
                    //midMap.put(entry.getKey(), midMap.get(keyId));
                    midMap.put(entry.getKey(), 1.0);
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
            if(p.getIdx() >= joinFeatStart && p.getIdx() < likeStartId ){
                if(midMap.containsKey(p.getIdx())){
                    feat[i++] = new FeatureNode(p.getIdx(), midMap.get(p.getIdx()));
                }
            }else{
                feat[i++] = new FeatureNode(p.getIdx(), p.getScore());

            }
        }
        return feat;
    }
    public static double predScore(Feature[] feature, Model model){

        double bias = model.getBias();
        double[] weight = model.getFeatureWeights();

        double res = 0.0;
        for(Feature node : feature){
            res += weight[node.getIndex() - 1] * node.getValue();
        }
        if(bias +1.0 > 0){
            res += bias;
        }
        //res = Math.abs(res - 1.0);
        //Linear.predictProbability(model, feature, res);
        BigDecimal bd = new BigDecimal(res);
        double valp = Double.parseDouble(bd.setScale(4, BigDecimal.ROUND_HALF_UP).toString());
        return valp;

    }

    public int[] getFeatId(BufferedReader bfr)throws IOException{
        int[] featId = new int[3];
        String line = null;
        int cnt = 0;
        while((line = bfr.readLine()) != null ){
            StringTokenizer stk = new StringTokenizer(line, " :");
            stk.nextToken();
            if(cnt == 0){
                int cardTagsStart = Integer.parseInt(stk.nextToken());
                featId[cnt] = cardTagsStart;

            }else if(cnt == 1){
                int joinStart = Integer.parseInt(stk.nextToken());
                featId[cnt] = joinStart;

            }else {
                int likeStart = Integer.parseInt(stk.nextToken());
                featId[cnt] = likeStart;
            }

            cnt++;
        }
        return featId;
    }
    public static void main(String[] args)throws IOException{
        String modelPath = args[0];    //  模型文件
        String fetIdPath = args[1];   // 特征id文件
        BufferedReader bfrId = new BufferedReader(new FileReader(fetIdPath));

        String cardFeatsPath = args[2];    //
        String likesPath = args[3];  //guid 喜好数据 格式： guid [arr1:v1,arr2:v2...]

        int topK = Integer.parseInt(args[4]);  //  候选集阈值topk设置

        String outpath = args[5];            // 输出路径
        File outFile = new File(outpath);
        if(! outFile.exists()){
            outFile.createNewFile();
        }

        CardModelPred cmp = new CardModelPred(modelPath, cardFeatsPath);  //

        int[] label = cmp.model.getLabels();           //  获取model 中label 数组
        int[] featIds = cmp.getFeatId(bfrId);           //---- 获取索引id数组
        bfrId.close();

        Map<String, String> itemDict = cmp.cardId2FeaturesMp;

        BufferedWriter bw = new BufferedWriter(new FileWriter(outpath));
        BufferedReader bf = new BufferedReader(new FileReader(likesPath));

        TreeSet<T2> canset = new TreeSet<T2>(new Comparator<T2>() {
            public int compare(T2 p1, T2 p2){
                int res = p1.getVal() > p2.getVal()? -1: 1;     ///> ：降序  < :  升序
                return res;
            }
        });
        boolean flag = label[0]==1 ? true : false;

        String line = "";
        while((line = bf.readLine()) != null){
            int loc = line.indexOf("\t");
            String cardId = line.substring(0, loc);
            String likes = line.substring(loc + 2);
            likes = likes.substring(0, likes.length() - 1);

            for(Map.Entry<String, String> entry : itemDict.entrySet()){
                String fet = entry.getValue() + "," + likes;

                Feature[] feat = cmp.getFeatures(fet, featIds);

                //--------- 中间结果输出 -----------
                //printMidRes(bfww, feat, guid, entry.getKey());          ///测试用  把测试的几个人的数据按liblinear格式输出 假类别标签注为1.0
                //-------------------------------------

                double sc = predScore(feat, cmp.model);
                sc = flag == true ? sc : -sc;          // 取底部数据最小数据时， 加负号后可按降序排列
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
            bw.write(cardId + "\t" + persCandi);
            bw.flush();
            bw.newLine();
            canset.clear();
        }
        bw.close();
        bf.close();
    }

}

