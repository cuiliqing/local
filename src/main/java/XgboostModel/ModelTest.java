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
 * Created by qiguo on 18/1/23.
 */
public class ModelTest {

    HashMap<String, Object> Param;
    public ModelTest(){
        this.Param = getParam();
    }

    public static HashMap getParam(){
        HashMap<String, Object> param = new HashMap<String, Object>();
        //param.put("booster", "gblinear");
        param.put("eta", 0.01);
        param.put("max_depth", 3);
        param.put("eval_metric", "auc"); // auc, rmse
        param.put("silent", 0);
        param.put("objective", "reg:logistic");
        //param.put("objective", "binary:logistic");
        param.put("lambda", 5);
        //param.put("alpha", 10);
        //param.put("gamma", 1.0); //0.1
        param.put("colsample_bytree", 0.3); //0.7
        //param.put("min_child_weight", 0.5);
        //param.put("max_delta_step", 0.5);
        param.put("subsample", 0.7);
        //param.put("seed", 10);

        return param;
    }

    public static void main(String[] args)throws XGBoostError,IOException {

        String trainPath = args[0];
        String testPath = args[1];
        String modelPath = args[2];

        DMatrix trainMtr = new DMatrix(trainPath);
        DMatrix testMtr = new DMatrix(testPath);
        Map watches = new HashMap<String, DMatrix>();
        watches.put("train", trainMtr);
        watches.put("test", testMtr);

        int round = 5000; // 1000

        ModelTest xgbst = new ModelTest();

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
        String[] res = booster.getModelDump(null, true);  // ，

        BufferedWriter bfw = new BufferedWriter(new FileWriter(featuresStr));
        for(String s : res){
            bfw.write(s);
            bfw.flush();
            bfw.newLine();
        }
        bfw.close();

    }
}
