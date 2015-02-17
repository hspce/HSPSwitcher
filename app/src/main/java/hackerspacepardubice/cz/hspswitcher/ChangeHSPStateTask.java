package hackerspacepardubice.cz.hspswitcher;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Mytrin
 */
public class ChangeHSPStateTask extends Thread {

    public static boolean sendToTwitter = false;

    private static final String LOG_TAG = "ChangeHSPStateTask";

    private final boolean state;
    private final String info;
    private final Resources resources;

    private final Handler resultHandler;

    private HttpURLConnection urlConnection;

    public ChangeHSPStateTask(boolean state, String info, Handler resultHandler, Resources config){
        this.state=state;
        this.info=info;
        resources=config;
        this.resultHandler=resultHandler;
    }

    @Override
    public void run(){

        try{
            //SpaceAPI
            Uri builtUri = Uri.parse(resources.getString(R.string.SPACE_API_BASE)).buildUpon()
                    .appendQueryParameter(resources.getString(R.string.SPACE_API_KEY_TAG), resources.getString(R.string.SPACE_API_KEY))
                    .appendQueryParameter(resources.getString(R.string.SPACE_API_SENSORS_TAG), "")
                    .build();

            String sensorsBase= resources.getString(R.string.SPACE_API_STATE_GROUP);
            //Still better then not working sensors={"xx":aa, "yy":b}
            String sensorsValueForState = "{"+sensorsBase+ ":{"+resources.getString(R.string.SPACE_API_OPEN_TAG) + ":" + state +"}}";
            String sensorsValueForLastChange = "{"+sensorsBase+ ":{"+resources.getString(R.string.SPACE_API_TIMESTAMP_TAG) + ":" + System.currentTimeMillis() / 1000L +"}}";
            String sensorsValueForMessage = "{"+sensorsBase+ ":{"+resources.getString(R.string.SPACE_API_MESSAGE_TAG) + ":" + "\""+info+"\""+"}}";

            URL url = new URL(builtUri.toString()+sensorsValueForState);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            pushURL(url);
            pushURL(new URL(builtUri.toString()+sensorsValueForLastChange));
            pushURL(new URL(builtUri.toString()+sensorsValueForMessage));

            //ThingSpeak
            if(sendToTwitter){
                builtUri = Uri.parse(resources.getString(R.string.THINGSPEAK_API_BASE)).buildUpon()
                        .appendQueryParameter(resources.getString(R.string.THINGSPEAK_API_TAG), resources.getString(R.string.THINGSPEAK_API_KEY))
                        .appendQueryParameter(resources.getString(R.string.THINGSPEAK_STATUS_TAG), info)
                        .build();

                url = new URL(builtUri.toString());
                pushURL(url);
            }

        }catch(Exception e){
            Log.e(LOG_TAG, "Error changing state: ", e);
        }
        cleanConnections();

        //Refresh
        GetHSPStateTask getNewState= new GetHSPStateTask(resources.getString(R.string.HSP_STATE_URL), resultHandler, resources);
        getNewState.start();
    }

    private void pushURL(URL url) throws Exception{
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.connect();
        urlConnection.getResponseMessage(); //forces connection to actually send the params, does not send otherwise
        urlConnection.disconnect();
    }

    private void cleanConnections(){
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }
}
