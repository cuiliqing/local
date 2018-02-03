package XgboostModel;

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qiguo on 17/12/21.
 */
public class XgbRelatedRecCV {
    HashMap<String, Object> Param;
    public XgbRelatedRecCV(){
        this.Param = getParam();
    }

    public static HashMap getParam(){
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("eta", 0.01);
        param.put("max_depth", 4);
        param.put("eval_metric", "auc"); // auc, rmse
        param.put("silent", 0);
        param.put("objective", "reg:logistic");
        //param.put("objective", "binary:logistic");
        param.put("lambda", 10);
        //param.put("gamma", 0.1);
        param.put("colsample_bytree", 0.7);
        //param.put("min_child_weight", 5);
        //param.put("max_delta_step", 1.0);
        param.put("subsample", 0.7);
        //param.put("seed", 100);

        return param;
    }

    public static void main(String[] args)throws XGBoostError,IOException {

        String trainPath = args[0];
        String testPath = args[1];
        String modelInfo = args[2];

        DMatrix trainMtr = new DMatrix(trainPath);
        DMatrix testMtr = new DMatrix(testPath);
        Map watches = new HashMap<String, DMatrix>();
        watches.put("train", trainMtr);
        watches.put("test", testMtr);

        int round = 1000;
        int Cv = 5;
        XgbRelatedRecCV xgbstCV = new XgbRelatedRecCV();

        //Booster booster = XGBoost.train(trainMtr, xgbstCV.Param, round, watches, null, null);
        String[] metricsArr = new String[1];
        metricsArr[0] = "auc";
        String[] boostInfos = XGBoost.crossValidation(trainMtr, xgbstCV.Param, round, Cv, metricsArr,null,null);
        //booster.saveModel(modelPath);

        BufferedWriter bfw = new BufferedWriter(new FileWriter(modelInfo));
        for(int i = 0; i < modelInfo.length(); i++){
            bfw.write(boostInfos[0]);
            bfw.flush();
            bfw.newLine();
        }
        bfw.close();
    }
}
