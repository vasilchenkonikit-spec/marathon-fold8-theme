from pathlib import Path
import re
root=Path('.')
java=root/'app/src/main/java/com/nv/marathontheme'
(java).mkdir(parents=True,exist_ok=True)

(root/'app/src/main/AndroidManifest.xml').write_text('''<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.SET_WALLPAPER" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <queries>
        <package android:name="com.samsung.android.themedesigner" />
        <package android:name="com.samsung.android.keyscafe" />
        <package android:name="com.samsung.android.goodlock" />
        <intent><action android:name="org.adw.launcher.THEMES" /></intent>
    </queries>
    <application android:allowBackup="true" android:icon="@drawable/icon_marathon" android:label="Marathon Fold 8 Theme" android:theme="@style/AppTheme">
        <activity android:name=".MainActivity" android:exported="true" android:screenOrientation="unspecified">
            <intent-filter><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.LAUNCHER"/></intent-filter>
            <intent-filter><action android:name="org.adw.launcher.THEMES"/></intent-filter>
            <intent-filter><action android:name="com.novalauncher.THEME"/></intent-filter>
            <intent-filter><action android:name="com.anddoes.launcher.THEME"/></intent-filter>
        </activity>
        <service android:name=".MarathonWallpaperService" android:permission="android.permission.BIND_WALLPAPER" android:exported="true">
            <intent-filter><action android:name="android.service.wallpaper.WallpaperService"/></intent-filter>
            <meta-data android:name="android.service.wallpaper" android:resource="@xml/wallpaper"/>
        </service>
        <service android:name=".MarathonAccessibilityService" android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE" android:exported="true" android:label="Marathon Auto Setup">
            <intent-filter><action android:name="android.accessibilityservice.AccessibilityService"/></intent-filter>
            <meta-data android:name="android.accessibilityservice" android:resource="@xml/accessibility_service_config"/>
        </service>
    </application>
</manifest>''')

(root/'app/src/main/res/xml').mkdir(parents=True,exist_ok=True)
(root/'app/src/main/res/xml/wallpaper.xml').write_text('''<?xml version="1.0" encoding="utf-8"?><wallpaper xmlns:android="http://schemas.android.com/apk/res/android" android:description="@string/wallpaper_desc"/>''')
(root/'app/src/main/res/xml/accessibility_service_config.xml').write_text('''<?xml version="1.0" encoding="utf-8"?><accessibility-service xmlns:android="http://schemas.android.com/apk/res/android" android:description="@string/accessibility_desc" android:accessibilityEventTypes="typeWindowStateChanged|typeWindowContentChanged|typeViewClicked" android:accessibilityFeedbackType="feedbackGeneric" android:notificationTimeout="120" android:canRetrieveWindowContent="true" android:canPerformGestures="true" android:accessibilityFlags="flagReportViewIds|flagRetrieveInteractiveWindows"/>''')
(root/'app/src/main/res/values/strings.xml').write_text('''<?xml version="1.0" encoding="utf-8"?><resources><string name="app_name">Marathon Fold 8 Theme</string><string name="wallpaper_desc">Marathon Fold 8 animated wallpaper</string><string name="accessibility_desc">Helps apply Marathon settings in Samsung Good Lock after explicit permission.</string></resources>''')

java.joinpath('MainActivity.java').write_text(r'''package com.nv.marathontheme;
import android.app.*;import android.app.WallpaperManager;import android.content.*;import android.graphics.*;import android.os.*;import android.provider.Settings;import android.text.TextUtils;import android.view.*;import android.widget.*;import java.io.IOException;
public class MainActivity extends Activity{
 final int ACID=Color.rgb(214,255,0),BG=Color.rgb(5,8,6); LinearLayout root;
 @Override public void onCreate(Bundle b){super.onCreate(b);build();}
 void build(){ScrollView sv=new ScrollView(this);sv.setBackgroundColor(BG);root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(22),dp(28),dp(22),dp(42));sv.addView(root);setContentView(sv);
  root.addView(txt("MARATHON // FOLD 8",31,true,ACID));root.addView(txt("PRODUCTION THEME INSTALLER v3.0\nOne UI / Good Lock companion",13,false,Color.LTGRAY));
  ImageView hero=new ImageView(this);hero.setImageResource(R.drawable.wallpaper_cover);hero.setScaleType(ImageView.ScaleType.CENTER_CROP);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(220));hp.setMargins(0,dp(18),0,dp(18));root.addView(hero,hp);
  add("1 // ANIMATED WALLPAPER",v->live());add("2 // STATIC LOCK FALLBACK",v->staticLock());add("3 // THEME PARK / ICON PACK",v->open("com.samsung.android.themedesigner"));add("4 // KEYS CAFE / KEYBOARD",v->open("com.samsung.android.keyscafe"));add("5 // GOOD LOCK",v->open("com.samsung.android.goodlock"));add("6 // AUTO APPLY",v->auto());
  root.addView(txt("KEYBOARD PRESET\nBackground #050806  •  Keys #121612  •  Text #F1F2EA  •  Accent #D6FF00\n\nSamsung system apps are themed through the icon pack. Other apps keep their identity inside the common technical container when supported by Theme Park. Widgets are real system widgets placed over clean wallpaper, never painted into the background.",13,false,Color.rgb(205,210,200)));}
 TextView txt(String s,int sp,boolean bold,int c){TextView v=new TextView(this);v.setText(s);v.setTextColor(c);v.setTextSize(sp);if(bold)v.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);v.setPadding(0,dp(5),0,dp(10));return v;}
 void add(String s,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setTextColor(BG);b.setTextSize(14);b.setAllCaps(false);b.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);b.setBackgroundColor(ACID);b.setOnClickListener(l);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(54));p.setMargins(0,0,0,dp(10));root.addView(b,p);} int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
 void live(){try{Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,MarathonWallpaperService.class));startActivity(i);}catch(Exception e){startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));}}
 void staticLock(){try{Bitmap bm=BitmapFactory.decodeResource(getResources(),R.drawable.wallpaper_cover);WallpaperManager.getInstance(this).setBitmap(bm,null,true,WallpaperManager.FLAG_LOCK);Toast.makeText(this,"Static lock wallpaper applied",Toast.LENGTH_LONG).show();}catch(IOException e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}}
 void open(String pkg){Intent i=getPackageManager().getLaunchIntentForPackage(pkg);if(i!=null){startActivity(i);return;}try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("samsungapps://ProductDetail/"+pkg)));}catch(Exception e){Toast.makeText(this,"Install module from Good Lock",Toast.LENGTH_LONG).show();}}
 boolean accessOn(){String s=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);if(s==null)return false;String mine=new ComponentName(this,MarathonAccessibilityService.class).flattenToString();TextUtils.SimpleStringSplitter sp=new TextUtils.SimpleStringSplitter(':');sp.setString(s);while(sp.hasNext())if(mine.equalsIgnoreCase(sp.next()))return true;return false;}
 void auto(){if(!accessOn()){Toast.makeText(this,"Enable Marathon Auto Setup, return, then tap AUTO APPLY again",Toast.LENGTH_LONG).show();startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));return;}MarathonAccessibilityService.begin(this);open("com.samsung.android.themedesigner");}
}''')

java.joinpath('MarathonWallpaperService.java').write_text(r'''package com.nv.marathontheme;
import android.service.wallpaper.WallpaperService;import android.view.SurfaceHolder;import android.graphics.*;import android.os.*;import java.util.Random;
public class MarathonWallpaperService extends WallpaperService{
 @Override public Engine onCreateEngine(){return new E();}
 class E extends Engine implements Runnable{final Handler h=new Handler(Looper.getMainLooper());Bitmap cover,inner;boolean visible;long start=System.currentTimeMillis();Paint p=new Paint(3);Random rng=new Random(93);float[] dots=new float[36];E(){cover=BitmapFactory.decodeResource(getResources(),R.drawable.wallpaper_cover);inner=BitmapFactory.decodeResource(getResources(),R.drawable.wallpaper_inner);for(int i=0;i<dots.length;i++)dots[i]=rng.nextFloat();}
  @Override public void onVisibilityChanged(boolean v){visible=v;if(v)draw();else h.removeCallbacks(this);}@Override public void onSurfaceChanged(SurfaceHolder s,int f,int w,int he){draw();}@Override public void onSurfaceDestroyed(SurfaceHolder s){visible=false;h.removeCallbacks(this);}@Override public void run(){draw();}
  void draw(){SurfaceHolder sh=getSurfaceHolder();Canvas c=null;try{c=sh.lockCanvas();if(c==null)return;int w=c.getWidth(),he=c.getHeight();Bitmap b=((float)w/he>.72f)?inner:cover;c.drawColor(Color.BLACK);long t=System.currentTimeMillis()-start;float pulse=(float)Math.sin(t/4200.0);float extra=1.035f+.008f*pulse;float base=Math.max((float)w/b.getWidth(),(float)he/b.getHeight());float sc=base*extra,x=(w-b.getWidth()*sc)/2f+(float)Math.sin(t/6500.0)*w*.012f,y=(he-b.getHeight()*sc)/2f+(float)Math.cos(t/7200.0)*he*.008f;Matrix m=new Matrix();m.postScale(sc,sc);m.postTranslate(x,y);p.setAlpha(255);c.drawBitmap(b,m,p);p.setColor(Color.argb(35,214,255,0));p.setStrokeWidth(Math.max(1f,w/700f));float sy=(t/18f)%he;c.drawLine(0,sy,w,sy,p);p.setColor(Color.argb(70,214,255,0));for(int i=0;i<12;i++){float px=dots[i*3]*w,py=(dots[i*3+1]*he+t*(.006f+dots[i*3+2]*.015f))%he;c.drawCircle(px,py,1.2f+dots[i*3+2]*2.2f,p);}}finally{if(c!=null)sh.unlockCanvasAndPost(c);}h.removeCallbacks(this);if(visible)h.postDelayed(this,33);}
 }
}''')

java.joinpath('MarathonAccessibilityService.java').write_text(r'''package com.nv.marathontheme;
import android.accessibilityservice.AccessibilityService;import android.content.*;import android.os.*;import android.view.accessibility.*;import android.widget.Toast;import java.util.*;
public class MarathonAccessibilityService extends AccessibilityService{static final String P="marathon_auto",S="stage";final Handler h=new Handler(Looper.getMainLooper());long last;static void begin(Context c){c.getSharedPreferences(P,Context.MODE_PRIVATE).edit().putInt(S,10).apply();}int st(){return getSharedPreferences(P,MODE_PRIVATE).getInt(S,0);}void st(int s){getSharedPreferences(P,MODE_PRIVATE).edit().putInt(S,s).apply();last=System.currentTimeMillis();}
 @Override public void onAccessibilityEvent(AccessibilityEvent e){if(st()==0||System.currentTimeMillis()-last<500)return;h.postDelayed(this::drive,300);}@Override public void onInterrupt(){}
 void drive(){AccessibilityNodeInfo r=getRootInActiveWindow();if(r==null)return;int s=st();if(s==10&&click(r,"icon","icons","икон","значк")){st(11);return;}if(s==11&&click(r,"create new","create","создать","новый")){st(12);return;}if(s==12&&click(r,"icon pack","iconpack","third party","пакет","набор знач")){st(13);return;}if(s==13&&click(r,"marathon")){st(14);return;}if(s==14&&click(r,"save","download","сохран","скач","готово")){st(0);Toast.makeText(this,"MARATHON ICONS ready. Apply and continue with keyboard preset.",Toast.LENGTH_LONG).show();}}
 boolean click(AccessibilityNodeInfo n,String...q){AccessibilityNodeInfo f=find(n,q);if(f==null)return false;for(int i=0;i<6&&f!=null;i++,f=f.getParent())if(f.isClickable()&&f.performAction(AccessibilityNodeInfo.ACTION_CLICK)){last=System.currentTimeMillis();return true;}return false;}AccessibilityNodeInfo find(AccessibilityNodeInfo n,String...q){if(n==null)return null;String s=((n.getText()==null?"":n.getText())+" "+(n.getContentDescription()==null?"":n.getContentDescription())).toLowerCase(Locale.ROOT);for(String z:q)if(s.contains(z.toLowerCase(Locale.ROOT)))return n;for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo x=find(n.getChild(i),q);if(x!=null)return x;}return null;}
}''')

b=(root/'app/build.gradle').read_text();b=re.sub(r'versionCode\s+\d+','versionCode 30',b);b=re.sub(r'versionName\s+"[^"]+"','versionName "3.0"',b);(root/'app/build.gradle').write_text(b)
print('v3 production patch applied')
