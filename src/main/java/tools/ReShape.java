package tools;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/9/25.
 */
public class ReShape {
    public static void main(String[] args)throws IOException{
        String path = args[0];
        BufferedReader bfr = new BufferedReader(new FileReader(path));

        String path2 = args[1];
        BufferedWriter bfw = new BufferedWriter(new FileWriter(path2));

        String line = null;
        while ((line = bfr.readLine()) != null){

            StringBuffer newLin = new StringBuffer();
            StringTokenizer stk = new StringTokenizer(line);
            int tot = stk.countTokens();
            int cnt = 0;
            while(stk.hasMoreTokens() && (cnt < tot-1)){
                String str = stk.nextToken();
                newLin.append(str + " ");
                cnt++;
            }
            String lin = newLin.toString();

            bfw.write(lin);
            bfw.flush();
            bfw.newLine();
        }
        bfw.close();
        bfr.close();
    }
}
