package com.nv.marathontheme;

import android.app.*;
import android.app.WallpaperManager;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class SetupActivity extends Activity {
    private static final int ACID = Color.rgb(205,255,0);
    private static final int BG = Color.rgb(5,8,6);
    private TextView status;
    private SharedPreferences prefs;

    @Override public void onCreate(Bundle b){ super.onCreate(b); prefs=getSharedPreferences("marathon_v5",MODE_PRIVATE); buildUi(); }
    @Override protected void onResume(){ super.onResume(); if(prefs.getBoolean("waiting_accessibility",false) && isAccessibilityEnabled()) beginSetup(); else refreshStatus(); }

    private void buildUi(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(28),dp(34),dp(28),dp(24)); root.setBackgroundColor(BG);
        TextView title=text("MARATHON // FOLD 8\nONE UI INSTALLER v5",34,ACID,true); root.addView(title);
        TextView sub=text("Keeps Samsung One UI Home, your pages, widgets, swipe-up app drawer and search. The installer only applies Marathon visual layers.",16,Color.WHITE,false); sub.setPadding(0,dp(18),0,dp(24)); root.addView(sub);
        status=text("",15,Color.LTGRAY,false); root.addView(status);
        Button install=button("INSTALL MARATHON"); install.setOnClickListener(v->startInstall()); root.addView(install,new LinearLayout.LayoutParams(-1,dp(58)));
        Space sp=new Space(this); root.addView(sp,new LinearLayout.LayoutParams(1,dp(14)));
        Button home=button("RETURN TO ONE UI HOME"); home.setOnClickListener(v->openOneUiHome()); root.addView(home,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView note=text("v5 does not replace the launcher and does not create fake widgets. Existing Samsung/TickTick widgets remain real and clickable.",13,Color.GRAY,false); note.setPadding(0,dp(22),0,0); root.addView(note);
        setContentView(root); refreshStatus();
    }

    private TextView text(String s,int size,int color,boolean bold){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return t; }
    private Button button(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(16); b.setAllCaps(false); return b; }
    private int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }

    private void startInstall(){
        prefs.edit().putBoolean("install_requested",true).apply();
        if(!isAccessibilityEnabled()){
            prefs.edit().putBoolean("waiting_accessibility",true).apply();
            status.setText("1/3 Enable Marathon Auto Setup once. Android requires this confirmation.");
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return;
        }
        beginSetup();
    }

    private void beginSetup(){
        prefs.edit().putBoolean("waiting_accessibility",false).putBoolean("setup_active",true).putString("stage","themepark").apply();
        applyWallpaper();
        status.setText("2/3 Wallpaper applied. Opening Theme Park for automatic icon styling…");
        if(!launchPackage("com.samsung.android.themedesigner") && !launchPackage("com.samsung.android.goodlock")){
            prefs.edit().putString("stage","themepark_missing").apply();
            status.setText("Theme Park/Good Lock is not installed. One UI Home has been preserved.");
        }
    }

    private void applyWallpaper(){
        try{
            Bitmap home=BitmapFactory.decodeResource(getResources(),R.drawable.wallpaper_inner);
            WallpaperManager wm=WallpaperManager.getInstance(this);
            wm.setBitmap(home,null,true,WallpaperManager.FLAG_SYSTEM);
        }catch(Exception ignored){}
    }

    private boolean launchPackage(String pkg){
        try{ Intent i=getPackageManager().getLaunchIntentForPackage(pkg); if(i==null)return false; i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); return true; }catch(Exception e){return false;}
    }

    private boolean isAccessibilityEnabled(){
        String enabled=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if(enabled==null)return false;
        String id=getPackageName()+"/"+MarathonAccessibilityService.class.getName();
        return enabled.toLowerCase(Locale.ROOT).contains(id.toLowerCase(Locale.ROOT));
    }

    private void openOneUiHome(){
        Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_HOME); i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i);
    }

    private void refreshStatus(){
        if(status==null)return;
        String stage=prefs.getString("stage","idle");
        if("done".equals(stage)) status.setText("Installed. Samsung One UI Home remains active; native gestures and widgets are untouched.");
        else if(isAccessibilityEnabled()) status.setText("Ready. Marathon Auto Setup permission is enabled.");
        else status.setText("Ready. One UI Home will not be replaced.");
    }
}
