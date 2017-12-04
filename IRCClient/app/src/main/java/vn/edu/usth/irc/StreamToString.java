package vn.edu.usth.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Minu'aHYHY on 04-Dec-17.
 */

public class StreamToString {
    public static String iStreamToString(InputStream is1)
    {
        BufferedReader rd = new BufferedReader(new InputStreamReader(is1), 4096);
        String line;
        StringBuilder sb =  new StringBuilder();
        try {
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String contentOfMyInputStream = sb.toString();
        return contentOfMyInputStream;
    }
}
