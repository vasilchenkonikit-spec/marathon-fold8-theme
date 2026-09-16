package com.nv.marathontheme;

import android.app.*;
import android.app.WallpaperManager;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.graphics.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class SetupActivity extends Activity {
    private static final int ACID = Color.rgb(205,255,0);
    private static final int BG = Color.rgb(5,8,6);
    private static final String ONEUI = "com.sec.android.app.launcher";
    private TextView status;
    private SharedPreferences prefs;

    @Override public void onCreate(Bundle b){ super.onCreate(b); prefs=getSharedPreferences("marathon_v5",MODE_PRIVATE); buildUi(); }
    @Override protected void onResume(){ super.onResume(); if(prefs.getBoolean("waiting_accessibility",false) && isAccessibilityEnabled()) beginSetup(); else refreshStatus(); }

    private void buildUi(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(28),dp(34),dp(28),dp(24)); root.setBackgroundColor(BG);
        TextView title=text("MARATHON // FOLD 8\nONE UI INSTALLER v5.1",34,ACID,true); root.addView(title);
        TextView sub=text("Keeps Samsung One UI Home, your current pages, widget positions, swipe-up app drawer and Samsung search. Existing widgets stay native and clickable.",16,Color.WHITE,false); sub.setPadding(0,dp(18),0,dp(24)); root.addView(sub);
        status=text("",15,Color.LTGRAY,false); root.addView(status);
        Button install=button("INSTALL MARATHON"); install.setOnClickListener(v->startInstall()); root.addView(install,new LinearLayout.LayoutParams(-1,dp(58)));
        Space sp=new Space(this); root.addView(sp,new LinearLayout.LayoutParams(1,dp(14)));
        Button home=button("RESTORE ONE UI HOME"); home.setOnClickListener(v->restoreOneUiHome()); root.addView(home,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView note=text("v5.1 never becomes your launcher and never replaces Samsung/TickTick widgets with fake panels. TickTick widgets, weather, clock, calendar and other existing widgets remain their original apps.",13,Color.GRAY,false); note.setPadding(0,dp(22),0,0); root.addView(note);
        setContentView(root); refreshStatus();
    }

    private TextView text(String s,int size,int color,boolean bold){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return t; }
    private Button button(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(16); b.setAllCaps(false); return b; }
    private int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }

    private void startInstall(){
        prefs.edit().putBoolean("install_requested",true).apply();
        if(!isAccessibilityEnabled()){
            prefs.edit().putBoolean("waiting_accessibility",true).apply();
            status.setText("1/3 Enable Marathon Auto Setup once. After that the installer continues automatically.");
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return;
        }
        beginSetup();
    }

    private void beginSetup(){
        prefs.edit().putBoolean("waiting_accessibility",false).putBoolean("setup_active",true).apply();
        if(!isOneUiDefault()){
            prefs.edit().putString("stage","home_default").apply();
            status.setText("2/3 Restoring Samsung One UI Home as the default launcher…");
            try{ startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); }
            catch(Exception e){ restoreOneUiHome(); }
            return;
        }
        continueVisualSetup();
    }

    void continueVisualSetup(){
        prefs.edit().putString("stage","themepark").apply();
        applyWallpaper();
        status.setText("2/3 One UI Home preserved. Applying Marathon wallpaper and icon layer…");
        if(!launchPackage("com.samsung.android.themedesigner") && !launchPackage("com.samsung.android.goodlock")){
            prefs.edit().putString("stage","themepark_missing").putBoolean("setup_active",false).apply();
            status.setText("Theme Park/Good Lock is not installed. One UI Home and all widgets were preserved.");
        }
    }

    private void applyWallpaper(){
        try{
            Bitmap home=BitmapFactory.decodeResource(getResources(),R.drawable.wallpaper_inner);
            WallpaperManager.getInstance(this).setBitmap(home,null,true,WallpaperManager.FLAG_SYSTEM);
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

    private boolean isOneUiDefault(){
        try{
            Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_HOME);
            ResolveInfo r=getPackageManager().resolveActivity(i,0);
            return r!=null && r.activityInfo!=null && ONEUI.equals(r.activityInfo.packageName);
        }catch(Exception e){ return false; }
    }

    private void restoreOneUiHome(){
        try{
            Intent i=getPackageManager().getLaunchIntentForPackage(ONEUI);
            if(i!=null){ i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); }
            if(!isOneUiDefault()) startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        }catch(Exception e){ try{ startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); }catch(Exception ignored){} }
    }

    private void refreshStatus(){
        if(status==null)return;
        String stage=prefs.getString("stage","idle");
        if("done".equals(stage)) status.setText("Installed. One UI Home, Samsung gestures/search and all native widgets are preserved.");
        else if(!isOneUiDefault()) status.setText("One UI Home is not currently the default launcher. INSTALL MARATHON will restore it first.");
        else if(isAccessibilityEnabled()) status.setText("Ready. One UI Home is active and Marathon Auto Setup permission is enabled.");
        else status.setText("Ready. One UI Home will remain the launcher.");
    }
}
