package test;

import PersonalRecommend.T2;
import models.Pair2;

import java.io.*;
import java.nio.Buffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qiguo on 17/9/20.
 */
public class JsonTest {

    public static void main(String[] args)throws IOException{
        String path = "Hans Horn";
        StringTokenizer stk = new StringTokenizer(path, ",");
        while (stk.hasMoreTokens()){
            String key = stk.nextToken().trim();
            System.out.println(key);
        }
    }
}
