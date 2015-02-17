package hackerspacepardubice.cz.hspswitcher;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


public class HSPSwitcher extends ActionBarActivity {

    private final Handler resultHandler = new Handler(){

        private boolean isOnline = false;

        @Override
        public void handleMessage(Message msg) {
            TextView currentState = (TextView)findViewById(R.id.textCurrentState);
            TextView currentInfo = (TextView)findViewById(R.id.textCurrentInfo);
            ImageView imageCurrentState = (ImageView)findViewById(R.id.imageCurrentState);

            ImageButton changer = (ImageButton)findViewById(R.id.buttonInside);

            if(currentState!=null){ //When activity is in background, context is null.
                short state =  msg.getData().getShort(getString(R.string.SPACE_API_OPEN_KEY));
                if(state == GetHSPStateTask.STATE_UNKNOWN){
                    currentState.setText(getString(R.string.HSP_STATE_NULL));
                    imageCurrentState.setImageResource(R.drawable.unknown);
                }else{
                    if(state == GetHSPStateTask.STATE_OFF){
                        currentState.setText(getString(R.string.HSP_STATE_OFF));
                        imageCurrentState.setImageResource(R.drawable.closed);
                        changer.setImageResource(R.drawable.toggle_off);
                        isOnline = false;
                    }else{
                        currentState.setText(getString(R.string.HSP_STATE_ON));
                        imageCurrentState.setImageResource(R.drawable.open);
                        changer.setImageResource(R.drawable.toggle_on);
                        isOnline = true;
                    }
                    currentInfo.setText(msg.getData().getString(getString(R.string.SPACE_API_INFO_KEY)));

                    changer.setClickable(true);
                    changer.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){

                            EditText editNewInfo = (EditText)findViewById(R.id.newInfoText);

                            ChangeHSPStateTask changeHSPStateTask = new ChangeHSPStateTask(!isOnline, editNewInfo.getText().toString(), resultHandler, getResources());
                            changeHSPStateTask.start();

                            editNewInfo.setText("");
                        }
                    });

                    ImageView twitter = (ImageView)findViewById(R.id.twitterEnable);
                    twitter.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            if(!ChangeHSPStateTask.sendToTwitter){
                                ChangeHSPStateTask.sendToTwitter = true;
                                ((ImageView)view).setImageResource(R.drawable.tweet_enabled);
                            }else{
                                ChangeHSPStateTask.sendToTwitter = false;
                                ((ImageView)view).setImageResource(R.drawable.tweet_disabled);
                            }
                        }
                    });
                }
            }

            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hspswitcher);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SwitcherFragment())
                    .commit();
        }

        GetHSPStateTask getState= new GetHSPStateTask(getString(R.string.HSP_STATE_URL), resultHandler, getResources());
        getState.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hspswitcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.app_name))
                    .setMessage("Version: "+getString(R.string.app_version)+" \nBy: Hackerspace Pardubice")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();

            return true;
        }else if (id == R.id.action_settings) {
            Intent displaySettings = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(displaySettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
