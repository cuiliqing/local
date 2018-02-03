package modelValidate;

import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/9/28.
 * step 3
 *
 */
public class PersonRecTest {
    HashMap<String, Integer> mvMap ;
    HashMap<String, String> movieTab;

    public PersonRecTest(String Path, String movieTablePath, int row)throws IOException{
        this.mvMap = getMvMap(Path, row);
        this.movieTab = getMovieTab(movieTablePath);
    }

    public HashMap<String, Integer> getMvMap(String path, int row)throws IOException{
        BufferedReader bfr = new BufferedReader(new FileReader(path));
        HashMap<String, Integer> mvMap = new HashMap<String, Integer>();
        String line = null;
        int r = 0;
        while((line = bfr.readLine())!= null){
            ++r;
            if(r == row){
                StringTokenizer stk = new StringTokenizer(line,", \t");
                String guid = stk.nextToken();
                while(stk.hasMoreTokens()){
                    String mvpair = stk.nextToken();
                    String mvkey = mvpair.substring(0,32);
                    int loc = Integer.parseInt(mvpair.substring(33));
                    mvMap.put(mvkey, loc);
                }
            }

        }
        bfr.close();
        return mvMap;
    }
    public HashMap<String, String> getMovieTab(String moviePath)throws IOException{
        BufferedReader bfr = new BufferedReader(new FileReader(moviePath));
        HashMap<String,String>movieTab = new HashMap<String, String>();
        String line = null;
        while((line = bfr.readLine()) != null){
            String[] movieArr = line.split("\t");
            movieTab.put(movieArr[0], movieArr[1]);
        }
        bfr.close();
        return movieTab;
    }

    public static void main(String[] args)throws IOException{


        String sortPath = args[0];               //  model 对个性化推选集排序后文件路径（step 2）
        int row = Integer.parseInt(args[1]);
        BufferedReader bfr = new BufferedReader(new FileReader(sortPath));

        String personRec = args[2];    // step 1 结果
        String movieTablePath = args[3];
        String out = args[4];
        BufferedWriter bfw = new BufferedWriter(new FileWriter(out));

        PersonRecTest pt = new PersonRecTest(personRec, movieTablePath, row);
        HashMap<String,String>movieMp = pt.movieTab;
        String line = null;
        int r = 0;
        while ((line = bfr.readLine()) != null){
            ++r;
            if(r == row){
                StringTokenizer stk = new StringTokenizer(line, ", \t");
                String guid = stk.nextToken();
                String res = guid+"\t";
                while(stk.hasMoreTokens()){
                    String pair = stk.nextToken();
                    String mv = pair.substring(0,32);

                    int loc = Integer.parseInt(pair.substring(33));
                    if(pt.mvMap.containsKey(mv)){
                        if(movieMp.containsKey(mv)){
                            res += movieMp.get(mv) +":"+loc +"#"+pt.mvMap.get(mv)+",";
                        }

                    }
                }
                bfw.write(res);
                bfw.flush();
                bfw.newLine();
            }
        }
        bfw.close();
        bfr.close();
    }

}
