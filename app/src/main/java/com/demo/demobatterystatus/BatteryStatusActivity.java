/**
Author : Adriano Alves
Date   : Mar 8 2016
objective : Application to give status of battery
            practice of Broadcast and animation
 **/


package com.demo.demobatterystatus;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.content.ContextCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class BatteryStatusActivity extends Activity
{
    BroadcastReceiver innerReceiver;
    TextView tvTech, tvCharger, tvIcon, tvLevel,tvVolt, tvAmps, tvTemp ,tvHealth;
    ImageView imageView;
    AnimationDrawable animation;
    private final int NOTIFY_BATTERY_ID = 100;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.battery_activity_layout);

        tvTech = (TextView)findViewById(R.id.tv_tech);
        tvCharger = (TextView)findViewById(R.id.tv_charger);
        tvIcon = (TextView)findViewById(R.id.tv_battery_icon);
        imageView = (ImageView)findViewById(R.id.iv_icon_animation);
        tvLevel = (TextView)findViewById(R.id.tv_level);
        tvVolt = (TextView)findViewById(R.id.tv_volt);
        tvAmps = (TextView)findViewById(R.id.tv_amp);
        tvTemp = (TextView)findViewById(R.id.tv_temp);
        tvHealth = (TextView)findViewById(R.id.tv_health);

        /******* BroadcastReceiver ********/
        innerReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String actionName = intent.getAction();

                /********* custom icon **********/
                Integer[] imagesIDs = probeId(R.drawable.class, "level");

                int levelIconId = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, -1);


                switch(actionName)
                {
                    case Intent.ACTION_POWER_DISCONNECTED:
                        mkToast(context, "POWER DISCONNECTED");
                        break;
                    case Intent.ACTION_POWER_CONNECTED:
                        mkToast(context, "POWER CONNECTED");
                        //notifyBattery(NOTIFY_BATTERY_ID);
                        break;
                    case Intent.ACTION_BATTERY_CHANGED:

                        /***** Technology type ******/
                        String tech = "Technology Type : "+intent.getStringExtra(BatteryManager
                                .EXTRA_TECHNOLOGY);
                        tvTech.setText(tech);

                        /******** Level ******/
                        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int maxLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
                        int intPercent = (level * 100)/maxLevel;
                        String strPercent = "Battery Level : "+String.valueOf(intPercent)+" %";
                        tvLevel.setText(strPercent);

                        /******** VOLTS ********/
                        float volt = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,-1)/1000;
                        String strVolts = "Voltage : "+ String.valueOf(volt)+"V";
                        tvVolt.setText(strVolts);

                        /******** AMPS ********/
                        int amps = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                        String strAmps = "Amperes : "+String.valueOf(amps)+"A";
                        tvAmps.setText(strAmps);

                        /******** TEMP ********/
                        float temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)/10;
                        String strTemp = "Temperature : "+String.valueOf(temp)+"°C";
                        tvTemp.setText(strTemp);

                        /******** HEALTH ******/
                        String strHealth = "Health: "+stringBatteryHealth(intent);
                        tvHealth.setText(strHealth);

                        /******* icon *******/
                        //int levelIconId = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL,-1);
                        LevelListDrawable levelListDrawable = (LevelListDrawable)ContextCompat.getDrawable(context,levelIconId);
                        //BitmapDrawable levelListDrawable = (BitmapDrawable)ContextCompat.getDrawable(context,levelIconId);


                        /********* Charger Type ********/
                        String strChargerType = "Charger Type: "+typeOfCharger(intent);
                        tvCharger.setText(strChargerType);

                        if(levelListDrawable!=null)
                        {
                            levelListDrawable.setLevel(level);
                            tvIcon.setBackground(levelListDrawable);
                        }

                        animation = mkBatteryAnimation(imagesIDs,intPercent);

                        if(isCharging(intent))
                        {
                            animation = mkBatteryAnimation(imagesIDs,intPercent);
                            imageView.setBackground(animation);
                            animation.start();


                        }
                        else if (isBatteryFull(intent))
                        {
                            animation.stop();
                            imageView.setBackgroundResource(imagesIDs[getImageIndex(intPercent)]);
                        }
                        else
                        {
                            //if (animation.isRunning() && animation != null)
                            animation.stop();
                            imageView.setBackgroundResource(imagesIDs[getImageIndex(intPercent)]);
                        }

                        break;
                }
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////
    public void notifyBattery(int id)
    {
        long[] vibrate = new long[]{150l,300l,150l,300l,150l,300l};
        Context context = getBaseContext();
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Intent i = new Intent(context,BatteryStatusActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context,0,i,0);
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setTicker("TICKER TEST")
                .setWhen(System.currentTimeMillis())
                .setContentText("CONTENT TEXT")
                .setContentTitle("CONTENT TITLE")
                .setVibrate(vibrate)
                .setContentIntent(pi);
        Notification notification = builder.build();
        manager.notify(id, notification);
    }
    //////////////////////////////////////////////////////////////////////

    @Override
    protected void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        //filter.addAction(BatteryManager.ACTION_CHARGING); // api >= 23
        registerReceiver(innerReceiver, filter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(innerReceiver);
    }
    /************* mkToast *************/
    public void mkToast(Context c,String msg )
    {
        Toast.makeText(c,msg,Toast.LENGTH_SHORT).show();
    }
    /****************** probe *****************/
    public Integer[] probeId(Class c, String search)
    {
        ArrayList<Integer> arrayList = new ArrayList<>();
        try
        {
            Field[] fields = c.getFields();

            for(Field f : fields)
            {
                if(f.toString().contains(search))
                {
                    arrayList.add(f.getInt(null));
                }
            }
        }
        catch(Exception e)
        {
            Log.e("TAG-ERROR","@PROBE_METHOD",e);
        }
        return(arrayList.toArray(new Integer[arrayList.size()]));
    }
    /****************** mkBatteryAnimation *******************/
    public AnimationDrawable mkBatteryAnimation(Integer[] images, int percent)
    {
        int index = getImageIndex(percent);

        AnimationDrawable anim = new AnimationDrawable();
        if(index != 4)
        {
            anim.addFrame(ContextCompat.getDrawable(getBaseContext(), images[index]),550);
            anim.addFrame(ContextCompat.getDrawable(getBaseContext(), images[index+1]),550);
            anim.setOneShot(false);
        }
        return anim;
    }
    /************* getImageIndex ***********/
    public int getImageIndex(int percentage)
    {
        int index = 0;
        if(percentage >= 25 && percentage < 50 ) index = 1;
        else if(percentage >= 50 && percentage < 75 ) index = 2;
        else if(percentage >= 75 && percentage < 100) index = 3;
        else if(percentage == 100) index = 4;
        return index;
    }
    /************ typeOfCharger **************/
    public String typeOfCharger(Intent intent)
    {
        int type = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,-1);
        String strCharger = "UNPLUGGED";

        switch(type)
        {
            case BatteryManager.BATTERY_PLUGGED_AC:
                strCharger = "AC CHARGER";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                strCharger = "USB CHARGER";
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                strCharger = "WIRELESS CHARGER";
                break;
        }
        return strCharger;
    }
    /************* stringBatteryHealth ************/
    public String stringBatteryHealth(Intent intent)
    {
        int status = intent.getIntExtra(BatteryManager.EXTRA_HEALTH,-1);
        String strHealth = "UNAVAILABLE";

        switch(status)
        {
            case BatteryManager.BATTERY_HEALTH_COLD:
                strHealth = "COLD";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                strHealth = "GOOD";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                strHealth = "DEAD";
                break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                strHealth = "UNKNOWN";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                strHealth = "UNSPECIFIED FAILURE";
                break;
        }
        return strHealth;
    }
    /********* isCharging *************/
    public boolean isCharging(Intent intent)
    {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
        return(status==BatteryManager.BATTERY_STATUS_CHARGING);
    }
    /********* isBatteryPresent ********/
    public boolean isBatteryPresent(Intent intent)
    {
        return(intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT,true));
    }
    /************ isBatteryFull *********/
    public boolean isBatteryFull(Intent intent)
    {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
        return(status==BatteryManager.BATTERY_STATUS_FULL);
    }
    /************** isBatteryOverheat *********/
    public boolean isBatteryOverheat(Intent intent)
    {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
        return(status==BatteryManager.BATTERY_HEALTH_OVERHEAT);
    }
    /*************** isBatteryOverVoltage **********/
    public boolean isBatteryOverVoltage(Intent intent)
    {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
        return(status==BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE);
    }
}
