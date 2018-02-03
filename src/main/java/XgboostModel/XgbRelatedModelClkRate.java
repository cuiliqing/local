package XgboostModel;

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qiguo on 17/12/26.
 *
 */
public class XgbRelatedModelClkRate {
    HashMap<String, Object> Param;
    public XgbRelatedModelClkRate(){
        this.Param = getParam();
    }

    public static HashMap getParam(){
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("eta", 0.01);
        param.put("max_depth", 3);
        param.put("eval_metric", "auc"); // auc, rmse
        param.put("silent", 0);
        param.put("objective", "reg:logistic");
        //param.put("objective", "binary:logistic");
        param.put("lambda", 5);
        //param.put("alpha", 10);
        //param.put("gamma", 1.0); //0.1
        param.put("colsample_bytree", 0.7); //0.7
        //param.put("min_child_weight", 0.5);
        //param.put("max_delta_step", 2);
        param.put("subsample", 0.7);

        param.put("seed", 10);

        return param;
    }

    public static void main(String[] args)throws XGBoostError,IOException {

        String trainPath = args[0];   // 训练集
        String testPath = args[1];   // 测试集
        String modelPath = args[2];  //  输出的模型文件名

        DMatrix trainMtr = new DMatrix(trainPath);
        DMatrix testMtr = new DMatrix(testPath);
        Map watches = new HashMap<String, DMatrix>();
        watches.put("train", trainMtr);
        watches.put("test", testMtr);

        int round = 800; // 700 1000  //  树的个数

        XgbRelatedModelClkRate xgbst = new XgbRelatedModelClkRate();

        Booster booster = XGBoost.train(trainMtr, xgbst.Param, round, watches, null, null);
        booster.saveModel(modelPath);
        //int Cv = 5;
        //String[] metricsArr = new String[1];
        //metricsArr[0] = "auc";
        //String[] boostInfos = XGBoost.crossValidation(trainMtr, xgbst.Param, round, Cv, metricsArr,null,null);
        //for (int i = 0; i< boostInfos.length; i++ ){
          //  System.out.println(boostInfos[i]);
        //}

        String featuresStr = args[3];
        String[] res = booster.getModelDump(null, true);  // 返回的模型树信息 保存到txt格式文件

        BufferedWriter bfw = new BufferedWriter(new FileWriter(featuresStr));
        for(String s : res){
            bfw.write(s);
            bfw.flush();
            bfw.newLine();
        }
        bfw.close();

    }
}
