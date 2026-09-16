package com.nv.marathontheme;

import android.app.*;
import android.appwidget.*;
import android.app.WallpaperManager;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.os.*;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import java.io.IOException;
import java.util.*;

public class SetupActivity extends Activity {
    static final String PREF = "marathon_v6";
    static final String ONE_UI = "com.sec.android.app.launcher";
    static final String THEME_PARK = "com.samsung.android.themedesigner";
    static final String KEYS_CAFE = "com.samsung.android.keyscafe";
    static final String GOOD_LOCK = "com.samsung.android.goodlock";
    static final String CLOCK_FACE = "com.samsung.android.app.clockface";
    static final String LOCK_STAR = "com.samsung.systemui.lockstar";
    static final int ACID = Color.rgb(214,255,0);
    static final int BG = Color.rgb(5,8,6);

    private SharedPreferences prefs;
    private TextView status;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences(PREF,MODE_PRIVATE);
        buildUi();
    }

    @Override protected void onResume(){
        super.onResume();
        if(prefs.getBoolean("waiting_accessibility",false) && isAccessibilityEnabled()) beginSetup();
        else refreshStatus();
    }

    private void buildUi(){
        ScrollView scroll=new ScrollView(this);
        scroll.setBackgroundColor(BG);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(26),dp(32),dp(26),dp(34));
        scroll.addView(root);
        setContentView(scroll);

        root.addView(text("MARATHON // FOLD 8",32,ACID,true));
        root.addView(text("COMPLETE ONE UI INSTALLER v6.0",18,Color.WHITE,true));
        TextView info=text("ONE APK. One UI Home stays intact. Native swipe-up, app drawer, Samsung search and all existing widgets remain available. The package contains wallpapers, live wallpaper, icon pack and setup automation.",15,Color.LTGRAY,false);
        info.setPadding(0,dp(16),0,dp(20)); root.addView(info);

        status=text("",15,Color.WHITE,false);
        status.setPadding(0,0,0,dp(16)); root.addView(status);

        Button install=new Button(this);
        install.setText("INSTALL EVERYTHING");
        install.setTextSize(17);
        install.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        install.setAllCaps(false);
        install.setTextColor(BG);
        install.setBackgroundColor(ACID);
        install.setOnClickListener(v->startInstall());
        root.addView(install,new LinearLayout.LayoutParams(-1,dp(62)));

        TextView details=text("Included: adaptive Marathon live wallpaper for folded/unfolded screens, 4K static wallpaper fallback, Marathon system icon pack, Theme Park automation, Keys Cafe hand-off, Lock screen/ClockFace hand-off, One UI restore protection and TickTick widget pin request. Android/Samsung confirmation dialogs may still appear once because apps cannot grant those permissions to themselves.",13,Color.GRAY,false);
        details.setPadding(0,dp(20),0,0); root.addView(details);
    }

    private TextView text(String s,int size,int color,boolean bold){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return t;
    }
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}

    private void startInstall(){
        prefs.edit().putBoolean("install_requested",true).apply();
        if(!isAccessibilityEnabled()){
            prefs.edit().putBoolean("waiting_accessibility",true).putString("stage","permission").apply();
            status.setText("1/6 Enable Marathon Auto Setup once. Returning to this app continues automatically.");
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return;
        }
        beginSetup();
    }

    private void beginSetup(){
        prefs.edit().putBoolean("waiting_accessibility",false).putBoolean("setup_active",true).putString("stage","ensure_home").apply();
        applyStaticFallback();
        if(!isOneUiHomeDefault()){
            status.setText("2/6 Restoring Samsung One UI Home before visual setup…");
            try{ startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); }
            catch(Exception e){ launchOneUi(); launchWallpaperPicker(this); }
        }else{
            status.setText("2/6 One UI Home preserved. Opening Marathon live wallpaper…");
            launchWallpaperPicker(this);
        }
    }

    static void launchWallpaperPicker(Context c){
        c.getSharedPreferences(PREF,MODE_PRIVATE).edit().putString("stage","wallpaper_picker").apply();
        try{
            Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(c,MarathonWallpaperService.class));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);
        }catch(Exception e){
            try{ Intent i=new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); c.startActivity(i); }catch(Exception ignored){}
        }
    }

    private void applyStaticFallback(){
        try{
            WallpaperManager wm=WallpaperManager.getInstance(this);
            Bitmap inner=BitmapFactory.decodeResource(getResources(),R.drawable.wallpaper_inner_4k);
            Bitmap cover=BitmapFactory.decodeResource(getResources(),R.drawable.wallpaper_cover_4k);
            wm.setBitmap(inner,null,true,WallpaperManager.FLAG_SYSTEM);
            wm.setBitmap(cover,null,true,WallpaperManager.FLAG_LOCK);
        }catch(Exception ignored){}
    }

    boolean isOneUiHomeDefault(){
        try{
            Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_HOME);
            ResolveInfo r=getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY);
            return r!=null && r.activityInfo!=null && ONE_UI.equals(r.activityInfo.packageName);
        }catch(Exception e){return false;}
    }

    static void launchOneUi(Context c){
        try{
            Intent i=c.getPackageManager().getLaunchIntentForPackage(ONE_UI);
            if(i!=null){i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);c.startActivity(i);return;}
        }catch(Exception ignored){}
        Intent h=new Intent(Intent.ACTION_MAIN); h.addCategory(Intent.CATEGORY_HOME); h.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); c.startActivity(h);
    }

    static boolean launchPackage(Context c,String pkg){
        try{
            Intent i=c.getPackageManager().getLaunchIntentForPackage(pkg);
            if(i==null)return false; i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); c.startActivity(i); return true;
        }catch(Exception e){return false;}
    }

    static boolean requestTickTickWidget(Context c){
        if(Build.VERSION.SDK_INT<26)return false;
        try{
            AppWidgetManager m=AppWidgetManager.getInstance(c);
            if(!m.isRequestPinAppWidgetSupported())return false;
            AppWidgetProviderInfo best=null, fallback=null;
            PackageManager pm=c.getPackageManager();
            for(AppWidgetProviderInfo p:m.getInstalledProviders()){
                String pkg=p.provider.getPackageName().toLowerCase(Locale.ROOT);
                if(!pkg.contains("ticktick"))continue;
                if(fallback==null)fallback=p;
                String label="";
                try{CharSequence cs=p.loadLabel(pm);if(cs!=null)label=cs.toString().toLowerCase(Locale.ROOT);}catch(Exception ignored){}
                if(label.contains("calendar")||label.contains("month")||label.contains("календар")||label.contains("agenda")){best=p;break;}
            }
            if(best==null)best=fallback;
            if(best==null)return false;
            c.getSharedPreferences(PREF,MODE_PRIVATE).edit().putString("stage","ticktick_pin").apply();
            return m.requestPinAppWidget(best.provider,null,null);
        }catch(Exception e){return false;}
    }

    private boolean isAccessibilityEnabled(){
        String enabled=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if(enabled==null)return false;
        String mine=new ComponentName(this,MarathonAccessibilityService.class).flattenToString();
        TextUtils.SimpleStringSplitter split=new TextUtils.SimpleStringSplitter(':'); split.setString(enabled);
        while(split.hasNext())if(mine.equalsIgnoreCase(split.next()))return true;
        return false;
    }

    private void refreshStatus(){
        if(status==null)return;
        String stage=prefs.getString("stage","idle");
        if("done".equals(stage)) status.setText("Installed. One UI Home remains the launcher. Marathon visual layer is active.");
        else if("permission".equals(stage)) status.setText("Waiting for Marathon Auto Setup permission.");
        else if(prefs.getBoolean("setup_active",false)) status.setText("Setup is running: "+stage);
        else status.setText("Ready. This build does not replace Samsung One UI Home.");
    }
}
