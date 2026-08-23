from pathlib import Path
import re
root=Path('.')
manifest=root/'app/src/main/AndroidManifest.xml'
text=manifest.read_text()
if 'MarathonAccessibilityService' not in text:
    text=text.replace('<uses-permission android:name="android.permission.VIBRATE" />','<uses-permission android:name="android.permission.VIBRATE" />\n    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />')
    text=text.replace('    <queries>\n','    <queries>\n        <package android:name="com.samsung.android.themedesigner" />\n        <package android:name="com.samsung.android.keyscafe" />\n        <package android:name="com.samsung.android.goodlock" />\n')
    service='''\n        <service\n            android:name=".MarathonAccessibilityService"\n            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"\n            android:exported="true"\n            android:label="Marathon Auto Setup">\n            <intent-filter><action android:name="android.accessibilityservice.AccessibilityService" /></intent-filter>\n            <meta-data android:name="android.accessibilityservice" android:resource="@xml/accessibility_service_config" />\n        </service>\n'''
    text=text.replace('    </application>',service+'    </application>')
manifest.write_text(text)
(root/'app/src/main/res/xml').mkdir(parents=True,exist_ok=True)
(root/'app/src/main/res/values').mkdir(parents=True,exist_ok=True)
(root/'app/src/main/res/xml/accessibility_service_config.xml').write_text('''<?xml version="1.0" encoding="utf-8"?>\n<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android" android:description="@string/accessibility_desc" android:accessibilityEventTypes="typeWindowStateChanged|typeWindowContentChanged|typeViewClicked|typeViewFocused" android:accessibilityFeedbackType="feedbackGeneric" android:notificationTimeout="100" android:canRetrieveWindowContent="true" android:canPerformGestures="true" android:accessibilityFlags="flagReportViewIds|flagRetrieveInteractiveWindows|flagIncludeNotImportantViews" />\n''')
(root/'app/src/main/res/values/strings.xml').write_text('''<?xml version="1.0" encoding="utf-8"?>\n<resources><string name="app_name">Marathon Auto</string><string name="accessibility_desc">Автоматически применяет оформление Marathon в Samsung Theme Park и системных настройках только после нажатия APPLY MARATHON.</string></resources>\n''')
java=root/'app/src/main/java/com/nv/marathontheme'
java.mkdir(parents=True,exist_ok=True)
java.joinpath('MarathonAccessibilityService.java').write_text(r'''package com.nv.marathontheme;
import android.accessibilityservice.AccessibilityService;
import android.content.*;
import android.os.*;
import android.view.accessibility.*;
import android.widget.Toast;
import java.util.*;
public class MarathonAccessibilityService extends AccessibilityService {
 public static final String PREF="marathon_auto",KEY_STAGE="stage"; private final Handler h=new Handler(Looper.getMainLooper()); private long last=0;
 @Override public void onAccessibilityEvent(AccessibilityEvent e){int s=stage();if(s<=0||System.currentTimeMillis()-last<600)return;String p=e.getPackageName()==null?"":e.getPackageName().toString();if(p.equals(getPackageName()))return;h.postDelayed(()->drive(s),350);} @Override public void onInterrupt(){}
 public static void begin(Context c){c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putInt(KEY_STAGE,10).apply();}
 private int stage(){return getSharedPreferences(PREF,MODE_PRIVATE).getInt(KEY_STAGE,0);} private void stage(int s){getSharedPreferences(PREF,MODE_PRIVATE).edit().putInt(KEY_STAGE,s).apply();last=System.currentTimeMillis();}
 private void drive(int s){AccessibilityNodeInfo r=getRootInActiveWindow();if(r==null)return;switch(s){
  case 10:go(r,11,"icon","icons","иконк","значк");break; case 11:go(r,12,"create new","create","создать","новый");break; case 12:go(r,13,"icon pack","iconpack","third party","набор знач","пакет знач");break; case 13:go(r,14,"marathon");break; case 14:go(r,15,"save","download","сохран","скач","готово");break; case 15:if(name(r,"MARATHON ICONS")){stage(16);fb(16,6000);}break; case 16:go(r,17,"marathon icons","marathon");break; case 17:if(click(r,"apply","примен")){stage(20);h.postDelayed(this::themePark,1200);fb(20,8000);}break;
  case 20:go(r,21,"theme","темы","тема");break; case 21:go(r,22,"create new","create","создать","новый");break; case 22:go(r,23,"save","download","сохран","скач","готово");break; case 23:if(name(r,"MARATHON SYSTEM")){stage(24);fb(24,6500);}break; case 24:go(r,25,"marathon system","marathon");break; case 25:if(click(r,"apply","примен")){stage(30);h.postDelayed(this::keys,1200);fb(30,8500);}break;
  case 30:go(r,31,"style your own keyboard","style","оформ","стиль клавиат","свой стиль");break; case 31:if(click(r,"on","вкл","включ")){stage(40);h.postDelayed(this::themePark,1200);fb(40,8000);}break;
  case 40:go(r,41,"quick panel","quick settings","быстр","панел");break; case 41:go(r,42,"create new","create","создать","новый");break; case 42:go(r,43,"save","download","сохран","скач","готово");break; case 43:if(name(r,"MARATHON PANEL")){stage(44);fb(44,6500);}break; case 44:go(r,45,"marathon panel","marathon");break; case 45:if(click(r,"apply","примен"))finish();break;}}
 private void go(AccessibilityNodeInfo r,int n,String...q){if(click(r,q)){stage(n);fb(n,7000);}}
 private void fb(int e,int ms){h.postDelayed(()->{if(stage()!=e)return;if(e<20){stage(20);themePark();}else if(e<30){stage(30);keys();}else if(e<40){stage(40);themePark();}else finish();},ms);}
 private boolean click(AccessibilityNodeInfo r,String...q){AccessibilityNodeInfo n=find(r,q);if(n==null)return false;AccessibilityNodeInfo x=n;for(int i=0;i<5&&x!=null;i++){if(x.isClickable()&&x.performAction(AccessibilityNodeInfo.ACTION_CLICK)){last=System.currentTimeMillis();return true;}x=x.getParent();}return n.performAction(AccessibilityNodeInfo.ACTION_CLICK);}
 private AccessibilityNodeInfo find(AccessibilityNodeInfo n,String...q){if(n==null)return null;String a=(n.getText()==null?"":n.getText())+" "+(n.getContentDescription()==null?"":n.getContentDescription());a=a.toLowerCase(Locale.ROOT);for(String z:q)if(a.contains(z.toLowerCase(Locale.ROOT)))return n;for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo v=find(n.getChild(i),q);if(v!=null)return v;}return null;}
 private AccessibilityNodeInfo edit(AccessibilityNodeInfo n){if(n==null)return null;if(n.isEditable())return n;for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo v=edit(n.getChild(i));if(v!=null)return v;}return null;}
 private boolean name(AccessibilityNodeInfo r,String s){AccessibilityNodeInfo e=edit(r);if(e==null)return click(r,"ok","done","готово","сохранить");Bundle b=new Bundle();b.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,s);e.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,b);h.postDelayed(()->{AccessibilityNodeInfo x=getRootInActiveWindow();if(x!=null)click(x,"ok","done","готово","сохранить");},300);last=System.currentTimeMillis();return true;}
 private void themePark(){launch("com.samsung.android.themedesigner");} private void keys(){launch("com.samsung.android.keyscafe");} private void launch(String p){Intent i=getPackageManager().getLaunchIntentForPackage(p);if(i!=null){i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}else if(p.contains("keyscafe")){stage(40);themePark();}else finish();}
 private void finish(){stage(0);performGlobalAction(GLOBAL_ACTION_HOME);Toast.makeText(this,"MARATHON // SYSTEM ONLINE",Toast.LENGTH_LONG).show();}
}
''')
main=java/'MainActivity.java'
s=main.read_text()
s=s.replace('import android.provider.Settings;','import android.provider.Settings;\nimport android.content.ComponentName;\nimport android.text.TextUtils;')
s=s.replace('static final int REQ_HOME = 90;','static final int REQ_HOME = 90;')
s=s.replace('Button apply=btn("APPLY MARATHON", v -> applyAll()); root.addView(apply);\n        Button preview=btn("PREVIEW WITHOUT SWITCHING HOME", v -> showLauncher()); root.addView(preview);','TextView accessState=txt(isAccessibilityEnabled()?"AUTO SETUP: ENABLED":"AUTO SETUP: REQUIRED",14,true,isAccessibilityEnabled()?Color.rgb(0,110,70):Color.rgb(130,30,20)); accessState.setPadding(0,0,0,dp(8)); root.addView(accessState);\n        Button access=btn("1 // ENABLE AUTO SETUP", v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))); root.addView(access);\n        Button apply=btn("2 // APPLY MARATHON", v -> applyAll()); root.addView(apply);\n        Button preview=btn("PREVIEW WITHOUT SWITCHING HOME", v -> showLauncher()); root.addView(preview);')
s=s.replace('applySystemWallpaper();','applySystemWallpaper();\n        getSharedPreferences(MarathonAccessibilityService.PREF,MODE_PRIVATE).edit().putBoolean("run_after_home",true).apply();',1)
s=s.replace('if (req==REQ_HOME) showLauncher();','if (req==REQ_HOME) startAutoIfReady();')
insert='''\n    private boolean isAccessibilityEnabled(){\n        String enabled=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES); if(enabled==null)return false;\n        String mine=new ComponentName(this,MarathonAccessibilityService.class).flattenToString(); TextUtils.SimpleStringSplitter sp=new TextUtils.SimpleStringSplitter(':');sp.setString(enabled);while(sp.hasNext())if(mine.equalsIgnoreCase(sp.next()))return true;return false;\n    }\n    private void startAutoIfReady(){\n        getSharedPreferences(MarathonAccessibilityService.PREF,MODE_PRIVATE).edit().putBoolean("run_after_home",false).apply();\n        if(isAccessibilityEnabled()){MarathonAccessibilityService.begin(this);Intent i=getPackageManager().getLaunchIntentForPackage("com.samsung.android.themedesigner");if(i!=null){startActivity(i);return;}}showLauncher();\n    }\n'''
s=s.replace('    private void applyLockWallpaper() {',insert+'\n    private void applyLockWallpaper() {')
main.write_text(s)
b=(root/'app/build.gradle').read_text();b=re.sub(r'versionCode\s+\d+','versionCode 21',b);b=re.sub(r'versionName\s+"[^"]+"','versionName "2.1"',b);(root/'app/build.gradle').write_text(b)
print('Applied Marathon Auto v2.1 patch')
