package PersonalRecommend;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import models.Pair2;
import models.ScalePreprocess;

import java.io.*;
import java.util.*;

/**
 * Created by Tsui on 17/8/14.
 * 个性化输出guid的topk候选集合--
 * 输入：
 * 参数0：guid 偏好特征文件
 * 参数1: 模型文件
 * 参数2: 电影自身itemfeatures
 * 参数3: 推选集合阈值设置
 * 参数4：输出文件
 * */

public class LocalModel {

    private Model model;
    HashMap<String, String> itemDict;

    public LocalModel(String path, String modelPath) throws IOException {
        this.itemDict = getItemDicts(path);    //movieItemFeature 集合
        this.model = Model.load(new File(modelPath));
    }
    public HashMap<String, String> getItemDicts(String path)      // movieItemFeature路径
            throws IOException{
        HashMap<String, String> dict = new HashMap<String, String>();
        BufferedReader bf = new BufferedReader(new FileReader(path));
        String lin = "";
        while((lin=bf.readLine()) != null){
            String[] str = lin.split("\t",-1);
            String itfet = str[1].substring(1);
            itfet = itfet.substring(0,itfet.length()-1);
            dict.put(str[0], itfet);
        }
        bf.close();
        return dict;
    }


    public Feature[] getFeature(String str){
        StringTokenizer sTok = new StringTokenizer(str,",");

        TreeSet<Pair2> set = new TreeSet<Pair2>(new Comparator<Pair2>(){
            public int compare(Pair2 p1, Pair2 p2){
                int res = p1.getIdx() < p2.getIdx()? -1: 1;
                return res;
            }
        });
        HashSet<Integer> idSet = new HashSet<Integer>();
        while (sTok.hasMoreTokens()) {
            String tmp = sTok.nextToken().trim();

            Pair2 p = new Pair2(tmp);
            if(!idSet.contains(p.getIdx())){
                set.add(p);
                idSet.add(p.getIdx());
            }

        }
        int size = idSet.size();
        int i = 0;
        Feature[] feat = new FeatureNode[size];
        for(Pair2 p2 : set){
            feat[i++] = new FeatureNode(p2.getIdx(), p2.getScore());
        }
        return feat;
    }

    public static double predScore(Feature[] feature, Model model){
        int[] labels = model.getLabels();
        int loc1;
        loc1 = labels[0] == 1 ? 0 : 1;
        double[] res = new double[2];
        Linear.predictProbability(model, feature, res);
        return res[loc1];
    }

    public static void main(String[] args) throws IOException{
        String likesPath = args[0];    // guid 喜好数据 格式： guid [arr1:v1,arr2:v2...]

        String modelPath = args[1];  // 模型文件

        String moviesPath = args[2];    // 电影itemfeature文件

        int topK = Integer.parseInt(args[3]);  //  候选集阈值topk设置

        String outpath = args[4]; // 输出路径
        File outFile = new File(outpath);
        if(! outFile.exists()){
            outFile.createNewFile();
        }

        LocalModel lm = new LocalModel(moviesPath, modelPath);  // 构造函数 两个文件路径
        Map<String, String> itemDict = lm.itemDict;

        BufferedWriter bw = new BufferedWriter(new FileWriter(outpath));

        BufferedReader bf = new BufferedReader(new FileReader(likesPath));
        String line = "";
        TreeSet<T2> canset = new TreeSet<T2>( new Comparator<T2>() {
            public int compare(T2 p1, T2 p2){
                int res = p1.getVal() > p2.getVal()? -1: 1;
                return res;
            }
        });
        while((line = bf.readLine()) != null){
            int loc = line.indexOf("\t");
            String guid = line.substring(0, loc);
            String likes = line.substring(loc + 2);
            likes = likes.substring(0, likes.length() - 1);

            for(Map.Entry<String, String> entry : itemDict.entrySet()){
                String fet = entry.getValue() + "," + likes;

                Feature[] feat = lm.getFeature(fet);
                double sc = predScore(feat, lm.model);
                if(canset.size() >= topK){
                    if(sc > canset.last().getVal()){
                        canset.pollLast();
                        canset.add(new T2(entry.getKey(), sc));
                    }
                }else {
                    canset.add(new T2(entry.getKey(), sc));
                }
            }
            String persCandi = "";
            for(T2 t: canset){
                persCandi += t.toString()+" ";
            }
            bw.write(guid + "\t" + persCandi);
            bw.flush();
            bw.newLine();
            canset.clear();
        }
        bw.close();
        bf.close();
    }

}
