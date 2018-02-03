package XgboostModel;

import ml.dmlc.xgboost4j.java.*;

import java.io.IOException;
import java.util.*;

/**
 * Created by Tsui on 17/12/19.
 */
public class XgbModel {

    HashMap<String, Object> Param;
    public XgbModel(){
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

        int round = 200; //500

        XgbModel xgbst = new XgbModel();

        Booster booster = XGBoost.train(trainMtr, xgbst.Param, round, watches, null, null);

        booster.saveModel(modelPath);

    }

}
