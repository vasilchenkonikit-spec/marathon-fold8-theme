package com.nv.marathontheme;

import android.accessibilityservice.AccessibilityService;
import android.content.*;
import android.os.*;
import android.view.accessibility.*;
import android.widget.Toast;
import java.util.*;

public class MarathonAccessibilityService extends AccessibilityService {
    private SharedPreferences prefs;
    private Handler handler;
    private long lastAction=0L;

    @Override protected void onServiceConnected(){
        super.onServiceConnected();
        prefs=getSharedPreferences(SetupActivity.PREF,MODE_PRIVATE);
        handler=new Handler(Looper.getMainLooper());
    }
    @Override public void onInterrupt(){}

    @Override public void onAccessibilityEvent(AccessibilityEvent event){
        if(prefs==null || !prefs.getBoolean("setup_active",false))return;
        if(System.currentTimeMillis()-lastAction<350)return;
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(this::drive,220);
    }

    private void drive(){
        if(prefs==null || !prefs.getBoolean("setup_active",false))return;
        AccessibilityNodeInfo root=getRootInActiveWindow();
        String stage=prefs.getString("stage","ensure_home");
        long age=System.currentTimeMillis()-prefs.getLong("stage_time",System.currentTimeMillis());

        if("ensure_home".equals(stage)){
            if(root!=null && clickAny(root,"Главный экран One UI","One UI Home")){
                setStage("home_confirm");return;
            }
            if(age>2500){SetupActivity.launchWallpaperPicker(this);setStage("wallpaper_picker");}
            return;
        }

        if("home_confirm".equals(stage)){
            if(root!=null && clickAny(root,"По умолчанию","Set as default","Default")){
                handler.postDelayed(()->SetupActivity.launchWallpaperPicker(this),700);
                setStage("wallpaper_picker");return;
            }
            if(age>2200){SetupActivity.launchWallpaperPicker(this);setStage("wallpaper_picker");}
            return;
        }

        if("wallpaper_picker".equals(stage)){
            if(root!=null && clickAny(root,"Установить обои","Set wallpaper","Применить","Apply")){
                setStage("wallpaper_scope");return;
            }
            if(age>4500){launchThemePark();}
            return;
        }

        if("wallpaper_scope".equals(stage)){
            if(root!=null && clickAny(root,"Главный экран и экран блокировки","Home screen and lock screen","Оба","Both")){
                handler.postDelayed(this::launchThemePark,900);return;
            }
            if(age>2600){launchThemePark();}
            return;
        }

        if("themepark".equals(stage)){
            if(root!=null && clickAny(root,"Icon","Icons","Иконки","Значки")){setStage("icon_tab");return;}
            if(age>4500){setStage("icon_tab");}
            return;
        }

        if("icon_tab".equals(stage)){
            if(root!=null && (clickAny(root,"Create new","Create","New","Создать","Добавить") || clickTopRightButton(root))){setStage("icon_editor");return;}
            if(age>4500){setStage("icon_editor");}
            return;
        }

        if("icon_editor".equals(stage)){
            if(root!=null && clickAny(root,"Icon pack","Icon Pack","Пакет значков","Пакет иконок","Third party")){setStage("pack_picker");return;}
            if(root!=null && clickAny(root,"Icon","Иконка")){setStage("pack_picker");return;}
            if(age>5000){setStage("pack_picker");}
            return;
        }

        if("pack_picker".equals(stage)){
            if(root!=null && clickContains(root,"Marathon")){setStage("theme_save");return;}
            if(age>5000){setStage("theme_save");}
            return;
        }

        if("theme_save".equals(stage)){
            if(root!=null && clickAny(root,"Save","Сохранить","Download","Скачать","Готово","Done")){setStage("theme_apply");return;}
            if(age>4500){setStage("theme_apply");}
            return;
        }

        if("theme_apply".equals(stage)){
            if(root!=null && clickAny(root,"Apply","Применить")){
                handler.postDelayed(this::launchKeysCafe,900);return;
            }
            if(age>3800){launchKeysCafe();}
            return;
        }

        if("keyscafe".equals(stage)){
            if(root!=null && clickAny(root,"Style your own keyboard","Style your keyboard","Оформить свою клавиатуру","Стиль клавиатуры")){setStage("keyscafe_editor");return;}
            if(age>4500){setStage("keyscafe_editor");}
            return;
        }

        if("keyscafe_editor".equals(stage)){
            if(root!=null && (clickAny(root,"Create new","Create","Создать","Добавить") || clickTopRightButton(root))){setStage("keyscafe_apply");return;}
            if(age>4500){setStage("keyscafe_apply");}
            return;
        }

        if("keyscafe_apply".equals(stage)){
            if(root!=null && clickAny(root,"Apply","Применить","Save","Сохранить")){
                handler.postDelayed(this::launchClockFace,700);return;
            }
            if(age>4200){launchClockFace();}
            return;
        }

        if("clockface".equals(stage)){
            if(root!=null && clickAny(root,"Lock screen","Экран блокировки")){setStage("clockface_apply");return;}
            if(age>4000){setStage("clockface_apply");}
            return;
        }

        if("clockface_apply".equals(stage)){
            if(root!=null && clickAny(root,"Apply","Применить","Done","Готово","Save","Сохранить")){
                handler.postDelayed(this::requestTickTick,700);return;
            }
            if(age>4200){requestTickTick();}
            return;
        }

        if("ticktick_pin".equals(stage)){
            if(root!=null && clickAny(root,"Добавить","Add","Добавить на главный экран","Add to Home screen")){
                finishSetup();return;
            }
            if(age>6500){finishSetup();}
        }
    }

    private void launchThemePark(){
        setStage("themepark");
        if(!SetupActivity.launchPackage(this,SetupActivity.THEME_PARK)){
            SetupActivity.launchPackage(this,SetupActivity.GOOD_LOCK);
        }
    }

    private void launchKeysCafe(){
        setStage("keyscafe");
        if(!SetupActivity.launchPackage(this,SetupActivity.KEYS_CAFE)){
            launchClockFace();
        }
    }

    private void launchClockFace(){
        setStage("clockface");
        if(!SetupActivity.launchPackage(this,SetupActivity.CLOCK_FACE)){
            if(!SetupActivity.launchPackage(this,SetupActivity.LOCK_STAR)) requestTickTick();
        }
    }

    private void requestTickTick(){
        if(SetupActivity.requestTickTickWidget(this)){
            setStage("ticktick_pin");
        }else finishSetup();
    }

    private void finishSetup(){
        prefs.edit().putString("stage","done").putBoolean("setup_active",false).apply();
        Toast.makeText(this,"MARATHON v6 setup complete",Toast.LENGTH_LONG).show();
        performGlobalAction(GLOBAL_ACTION_HOME);
    }

    private void setStage(String s){
        prefs.edit().putString("stage",s).putLong("stage_time",System.currentTimeMillis()).apply();
        lastAction=System.currentTimeMillis();
    }

    private boolean clickAny(AccessibilityNodeInfo root,String...labels){
        for(String s:labels)if(clickText(root,s,false))return true;return false;
    }
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
        for(int i=0;i<5&&x!=null;i++,x=x.getParent())if(x.isClickable()&&x.performAction(AccessibilityNodeInfo.ACTION_CLICK)){lastAction=System.currentTimeMillis();return true;}
        return false;
    }

    private boolean clickTopRightButton(AccessibilityNodeInfo root){
        int sw=getResources().getDisplayMetrics().widthPixels,sh=getResources().getDisplayMetrics().heightPixels;
        ArrayDeque<AccessibilityNodeInfo> q=new ArrayDeque<>();q.add(root);
        android.graphics.Rect r=new android.graphics.Rect();
        while(!q.isEmpty()){
            AccessibilityNodeInfo n=q.removeFirst();n.getBoundsInScreen(r);
            if(n.isClickable()&&r.centerX()>sw*0.70&&r.centerY()<sh*0.24&&r.width()<sw*0.30){
                if(n.performAction(AccessibilityNodeInfo.ACTION_CLICK)){lastAction=System.currentTimeMillis();return true;}
            }
            for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo c=n.getChild(i);if(c!=null)q.add(c);}
        }
        return false;
    }
}
