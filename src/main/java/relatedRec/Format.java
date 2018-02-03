package relatedRec;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/11/20.
 */
public class Format {
    public static void main(String[] args)throws IOException{
        BufferedReader bfr = new BufferedReader(new FileReader(args[0]));

        BufferedWriter bfw = new BufferedWriter(new FileWriter(args[1]));
        String line = "";
        while((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, ",\t");
            String first = stk.nextToken();
            int tot = stk.countTokens();
            int cnt = 1;
            String out = first + "\t";
            while (stk.hasMoreTokens()){
                if(cnt == tot){
                    out += stk.nextToken()+":D:104";
                } else {
                    out += stk.nextToken()+":D:104,";
                    cnt++;
                }

            }
            bfw.write(out);
            bfw.flush();
            bfw.newLine();
        }
        bfr.close();
        bfw.close();

    }
}
