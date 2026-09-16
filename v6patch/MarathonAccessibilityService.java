package com.nv.marathontheme;

import android.accessibilityservice.AccessibilityService;
import android.content.*;
import android.graphics.Rect;
import android.os.*;
import android.view.accessibility.*;
import android.widget.Toast;
import java.util.*;

public class MarathonAccessibilityService extends AccessibilityService {
    private SharedPreferences prefs;
    private Handler handler;
    private long lastAction=0L;
    private final Runnable watchdog=new Runnable(){
        @Override public void run(){
            try{ if(prefs!=null && prefs.getBoolean("setup_active",false)) drive(); }catch(Exception ignored){}
            if(handler!=null) handler.postDelayed(this,700);
        }
    };

    @Override protected void onServiceConnected(){
        super.onServiceConnected();
        prefs=getSharedPreferences(SetupActivity.PREF,MODE_PRIVATE);
        handler=new Handler(Looper.getMainLooper());
        handler.postDelayed(watchdog,500);
    }
    @Override public void onInterrupt(){}
    @Override public void onDestroy(){ if(handler!=null) handler.removeCallbacksAndMessages(null); super.onDestroy(); }

    @Override public void onAccessibilityEvent(AccessibilityEvent event){
        if(prefs==null || !prefs.getBoolean("setup_active",false))return;
        if(System.currentTimeMillis()-lastAction<250)return;
        handler.postDelayed(this::drive,180);
    }

    private void drive(){
        if(prefs==null || !prefs.getBoolean("setup_active",false))return;
        AccessibilityNodeInfo root=getRootInActiveWindow();
        String stage=prefs.getString("stage","ensure_home");
        long age=System.currentTimeMillis()-prefs.getLong("stage_time",System.currentTimeMillis());

        if("ensure_home".equals(stage)){
            if(root!=null && clickAny(root,"Главный экран One UI","One UI Home")){setStage("home_confirm");return;}
            if(age>2500){SetupActivity.launchWallpaperPicker(this);setStage("wallpaper_picker");}
            return;
        }
        if("home_confirm".equals(stage)){
            if(root!=null && clickAny(root,"По умолчанию","Set as default","Default")){
                handler.postDelayed(()->SetupActivity.launchWallpaperPicker(this),700);setStage("wallpaper_picker");return;
            }
            if(age>2200){SetupActivity.launchWallpaperPicker(this);setStage("wallpaper_picker");}
            return;
        }
        if("wallpaper_picker".equals(stage)){
            if(root!=null && clickAny(root,"Установить обои","Set wallpaper","Применить","Apply")){setStage("wallpaper_scope");return;}
            if(age>4200){launchThemePark();}
            return;
        }
        if("wallpaper_scope".equals(stage)){
            if(root!=null && clickAny(root,"Главный экран и экран блокировки","Home screen and lock screen","Оба","Both")){
                setStage("wallpaper_done");handler.postDelayed(this::launchThemePark,900);return;
            }
            if(age>2600){launchThemePark();}
            return;
        }
        if("wallpaper_done".equals(stage)){
            if(age>900){launchThemePark();}
            return;
        }

        if("themepark".equals(stage)){
            if(root!=null && clickAny(root,"Icon","Icons","Иконки","Значки")){setStage("icon_tab");return;}
            if(age>3800){setStage("icon_tab");}
            return;
        }
        if("icon_tab".equals(stage)){
            if(root!=null && (clickAny(root,"Create new","Create","New","Создать","Добавить") || clickTopRightButton(root))){setStage("icon_editor");return;}
            if(age>4500){setStage("icon_editor");}
            return;
        }
        if("icon_editor".equals(stage)){
            if(root!=null && clickAny(root,"Iconpack","Icon pack","Icon Pack","Пакет значков","Пакет иконок","Набор значков","Third party")){
                setStage("pack_picker");return;
            }
            if(root!=null && clickAny(root,"Icon","Иконка")){lastAction=System.currentTimeMillis();return;}
            return;
        }
        if("pack_picker".equals(stage)){
            if(root!=null && (clickContains(root,"Marathon Fold 8") || clickContains(root,"Marathon"))){setStage("theme_save");return;}
            if(age>4200 && root!=null && clickAny(root,"Iconpack","Icon pack","Пакет значков","Пакет иконок")){
                setStage("pack_picker");return;
            }
            return;
        }
        if("theme_save".equals(stage)){
            if(root!=null && (clickAny(root,"Save","Сохранить","Download","Скачать","Готово","Done") || clickTopRightButton(root))){
                setStage("theme_name");return;
            }
            return;
        }
        if("theme_name".equals(stage)){
            if(root!=null){
                setFirstEditableText(root,"MARATHON 93");
                if(clickAny(root,"OK","ОК","Save","Сохранить","Done","Готово")){setStage("theme_list");return;}
            }
            if(age>5500){setStage("theme_list");}
            return;
        }
        if("theme_list".equals(stage)){
            if(root!=null && clickContains(root,"MARATHON 93")){setStage("theme_apply");return;}
            if(root!=null && clickAny(root,"Apply","Применить")){
                setStage("theme_applied");handler.postDelayed(this::launchKeysCafe,900);return;
            }
            if(age>5500){setStage("theme_apply");}
            return;
        }
        if("theme_apply".equals(stage)){
            if(root!=null && clickAny(root,"Apply","Применить")){
                setStage("theme_applied");handler.postDelayed(this::launchKeysCafe,900);return;
            }
            if(age>7000){launchKeysCafe();}
            return;
        }
        if("theme_applied".equals(stage)){
            if(age>900){launchKeysCafe();}
            return;
        }

        if("keyscafe".equals(stage)){
            if(root!=null && clickAny(root,"Style your own keyboard","Style your keyboard","Оформить свою клавиатуру","Стиль клавиатуры")){setStage("keyscafe_editor");return;}
            if(age>5000){setStage("keyscafe_editor");}
            return;
        }
        if("keyscafe_editor".equals(stage)){
            if(root!=null && (clickAny(root,"Create new","Create","Создать","Добавить") || clickTopRightButton(root))){setStage("keyscafe_apply");return;}
            if(age>5200){setStage("keyscafe_apply");}
            return;
        }
        if("keyscafe_apply".equals(stage)){
            if(root!=null && (clickAny(root,"Apply","Применить","Save","Сохранить","Done","Готово") || clickTopRightButton(root))){
                setStage("keyboard_done");handler.postDelayed(this::launchClockFace,800);return;
            }
            if(age>6500){launchClockFace();}
            return;
        }
        if("keyboard_done".equals(stage)){
            if(age>800){launchClockFace();}
            return;
        }

        if("clockface".equals(stage)){
            if(root!=null && clickAny(root,"Lock screen","Экран блокировки")){setStage("clockface_apply");return;}
            if(age>4500){setStage("clockface_apply");}
            return;
        }
        if("clockface_apply".equals(stage)){
            if(root!=null && (clickAny(root,"Apply","Применить","Done","Готово","Save","Сохранить") || clickTopRightButton(root))){
                setStage("clock_done");handler.postDelayed(this::requestTickTick,800);return;
            }
            if(age>6000){requestTickTick();}
            return;
        }
        if("clock_done".equals(stage)){
            if(age>800){requestTickTick();}
            return;
        }

        if("ticktick_pin".equals(stage)){
            if(root!=null && clickAny(root,"Добавить","Add","Добавить на главный экран","Add to Home screen")){finishSetup();return;}
            if(age>9000){finishSetup();}
        }
    }

    private void launchThemePark(){
        setStage("themepark");
        if(!SetupActivity.launchPackage(this,SetupActivity.THEME_PARK)) SetupActivity.launchPackage(this,SetupActivity.GOOD_LOCK);
    }
    private void launchKeysCafe(){
        setStage("keyscafe");
        if(!SetupActivity.launchPackage(this,SetupActivity.KEYS_CAFE)) launchClockFace();
    }
    private void launchClockFace(){
        setStage("clockface");
        if(!SetupActivity.launchPackage(this,SetupActivity.CLOCK_FACE)){
            if(!SetupActivity.launchPackage(this,SetupActivity.LOCK_STAR)) requestTickTick();
        }
    }
    private void requestTickTick(){ if(SetupActivity.requestTickTickWidget(this)) setStage("ticktick_pin"); else finishSetup(); }
    private void finishSetup(){
        prefs.edit().putString("stage","done").putBoolean("setup_active",false).apply();
        Toast.makeText(this,"MARATHON v6.2 setup complete",Toast.LENGTH_LONG).show();
        performGlobalAction(GLOBAL_ACTION_HOME);
    }
    private void setStage(String s){
        prefs.edit().putString("stage",s).putLong("stage_time",System.currentTimeMillis()).apply();
        lastAction=System.currentTimeMillis();
    }

    private boolean clickAny(AccessibilityNodeInfo root,String...labels){ for(String s:labels)if(clickText(root,s,false))return true;return false; }
    private boolean clickContains(AccessibilityNodeInfo root,String label){return clickText(root,label,true);}
    private boolean clickText(AccessibilityNodeInfo n,String label,boolean contains){
        if(n==null)return false;
        CharSequence t=n.getText(),d=n.getContentDescription();
        if(match(t,label,contains)||match(d,label,contains))return clickNodeOrParent(n);
        for(int i=0;i<n.getChildCount();i++)if(clickText(n.getChild(i),label,contains))return true;
        return false;
    }
    private boolean match(CharSequence cs,String label,boolean contains){
        if(cs==null)return false;
        String a=cs.toString().trim().toLowerCase(Locale.ROOT),b=label.toLowerCase(Locale.ROOT);
        return contains?a.contains(b):a.equals(b);
    }
    private boolean clickNodeOrParent(AccessibilityNodeInfo n){
        AccessibilityNodeInfo x=n;
        for(int i=0;i<6&&x!=null;i++,x=x.getParent()) if(x.isClickable()&&x.performAction(AccessibilityNodeInfo.ACTION_CLICK)){lastAction=System.currentTimeMillis();return true;}
        return false;
    }
    private boolean setFirstEditableText(AccessibilityNodeInfo n,String value){
        if(n==null)return false;
        if(n.isEditable()){
            Bundle b=new Bundle();b.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,value);
            if(n.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,b)){lastAction=System.currentTimeMillis();return true;}
        }
        for(int i=0;i<n.getChildCount();i++)if(setFirstEditableText(n.getChild(i),value))return true;
        return false;
    }
    private boolean clickTopRightButton(AccessibilityNodeInfo root){
        int sw=getResources().getDisplayMetrics().widthPixels,sh=getResources().getDisplayMetrics().heightPixels;
        ArrayDeque<AccessibilityNodeInfo> q=new ArrayDeque<>();q.add(root);Rect r=new Rect();AccessibilityNodeInfo best=null;int bestX=-1;
        while(!q.isEmpty()){
            AccessibilityNodeInfo n=q.removeFirst();n.getBoundsInScreen(r);
            if(n.isClickable()&&r.centerX()>sw*0.68&&r.centerY()<sh*0.25&&r.width()<sw*0.34&&r.centerX()>bestX){best=n;bestX=r.centerX();}
            for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo c=n.getChild(i);if(c!=null)q.add(c);}
        }
        if(best!=null && best.performAction(AccessibilityNodeInfo.ACTION_CLICK)){lastAction=System.currentTimeMillis();return true;}
        return false;
    }
}
