package tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 18/1/10.
 *
 */
public class RelatedRecStardOut {
    public static void main(String[] args)throws IOException {
        String input = args[0];
        BufferedReader bfr = new BufferedReader(new FileReader(input));

        BufferedWriter bfw = new BufferedWriter(new FileWriter(args[1]));
        String line = null;
        while((line = bfr.readLine())!= null){
            StringTokenizer stk = new StringTokenizer(line, " \t");

            int tot = stk.countTokens()-1;
            String mvFirst = stk.nextToken();
            String res = mvFirst + "\t";
            int cnt = 1;
            while(stk.hasMoreTokens()){
                String p2 = stk.nextToken();
                int splitidx = p2.indexOf(":");
                String key = p2.substring(0, splitidx);
                double val = Double.parseDouble(p2.substring(splitidx+1));
                double logres = Math.log(val+10)/Math.log(20);

                BigDecimal bgd = new BigDecimal(logres);
                double sctmp = Double.parseDouble(bgd.setScale(4, BigDecimal.ROUND_HALF_UP).toString());
                if(cnt == tot){
                    res += key+":"+sctmp + ":G:207";
                }else {
                    res += key+":"+sctmp + ":G:207,";
                }
                cnt++;
            }
            bfw.write(res);
            bfw.flush();
            bfw.newLine();
        }
        bfr.close();
        bfw.close();
    }
}
