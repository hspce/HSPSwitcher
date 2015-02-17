package hackerspacepardubice.cz.hspswitcher;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Mytrin
 */
public class GetHSPStateTask extends Thread {

    private static final String LOG_TAG = "GetHSPStateTask";

    public static short STATE_UNKNOWN = 2;
    public static short STATE_ON = 1;
    public static short STATE_OFF = 0;

    private HttpURLConnection urlConnection;
    private BufferedReader reader;

    private final String url;
    private final Handler resultHandler;

    private final String STATE_KEY;
    private final String OPEN_KEY;
    private final String INFO_KEY;
    private final String TIMESTAMP_KEY;

    public GetHSPStateTask(String url, Handler resultHandler, Resources config){
        this.url = url;
        this.resultHandler = resultHandler;

        STATE_KEY = config.getString(R.string.SPACE_API_STATE_KEY);
        OPEN_KEY = config.getString(R.string.SPACE_API_OPEN_KEY);
        INFO_KEY = config.getString(R.string.SPACE_API_INFO_KEY);
        TIMESTAMP_KEY = config.getString(R.string.SPACE_API_TIMESTAMP_KEY);
    }

    @Override
    public void run(){
        String result= obtainData(url);
        cleanConnections();

        Message resultMessage = resultHandler.obtainMessage();


        if(result == null){
            fillFailedMessage(resultMessage);
        }

        parseState(result, resultMessage);
        resultHandler.sendMessage(resultMessage);
    }

    private String obtainData(String urlString){
        try{
            //Open connection
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            //Get Data
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }

            return buffer.toString();
        }catch(Exception e){
            Log.e(LOG_TAG, "Error when opening connection", e);
            return null;
        }
    }

    private void cleanConnections(){
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (final IOException e) {
                Log.e(LOG_TAG, "Error closing stream", e);
            }
        }
    }

    private void parseState(String dataJSON, Message msg){
        try{
            JSONObject data = new JSONObject(dataJSON);
            JSONObject state =  data.getJSONObject(STATE_KEY);

            Boolean stateHS = state.getBoolean(OPEN_KEY);
            if(stateHS){
                msg.getData().putShort(OPEN_KEY, STATE_ON);
            }else{
                msg.getData().putShort(OPEN_KEY, STATE_OFF);
            }

            msg.getData().putString(INFO_KEY, state.getString(INFO_KEY));
            msg.getData().putLong(TIMESTAMP_KEY, state.getLong(TIMESTAMP_KEY));
        }catch(Exception e) {
            Log.e(LOG_TAG, "Error parsing JSON", e);
            fillFailedMessage(msg);
        }
    }

    private void fillFailedMessage(Message msg){
        msg.getData().putShort(OPEN_KEY, STATE_UNKNOWN);
        msg.getData().putString(INFO_KEY, "");
        msg.getData().putLong(TIMESTAMP_KEY, -1);
    }
}