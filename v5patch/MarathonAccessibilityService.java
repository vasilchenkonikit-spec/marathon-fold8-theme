package com.nv.marathontheme;

import android.accessibilityservice.*;
import android.content.*;
import android.os.*;
import android.view.accessibility.*;
import java.util.*;

public class MarathonAccessibilityService extends AccessibilityService {
    private SharedPreferences prefs;
    private Handler handler;
    private long lastAction=0L;

    @Override protected void onServiceConnected(){ super.onServiceConnected(); prefs=getSharedPreferences("marathon_v5",MODE_PRIVATE); handler=new Handler(Looper.getMainLooper()); }
    @Override public void onInterrupt(){}

    @Override public void onAccessibilityEvent(AccessibilityEvent event){
        if(prefs==null || !prefs.getBoolean("setup_active",false)) return;
        if(System.currentTimeMillis()-lastAction<500) return;
        CharSequence p=event.getPackageName(); if(p==null)return;
        String pkg=p.toString();
        if(pkg.contains("themedesigner") || pkg.contains("goodlock")) handleThemePark();
    }

    private void handleThemePark(){
        AccessibilityNodeInfo root=getRootInActiveWindow(); if(root==null)return;
        String stage=prefs.getString("stage","themepark");
        boolean acted=false;
        if("themepark".equals(stage)){
            acted=clickAny(root,"Icon","Icons","Иконки","Значки");
            if(acted) setStage("icon_tab");
        } else if("icon_tab".equals(stage)){
            acted=clickAny(root,"Create new","Create","New","Создать","Добавить");
            if(!acted) acted=clickTopRightButton(root);
            if(acted) setStage("icon_editor");
        } else if("icon_editor".equals(stage)){
            acted=clickAny(root,"Icon pack","Icon Pack","Пакет значков","Пакет иконок");
            if(!acted) acted=clickAny(root,"Icon","Иконка");
            if(acted) setStage("pack_picker");
        } else if("pack_picker".equals(stage)){
            acted=clickContains(root,"Marathon");
            if(acted) setStage("save_theme");
        } else if("save_theme".equals(stage)){
            acted=clickAny(root,"Save","Сохранить","Apply","Применить");
            if(acted){ setStage("done"); prefs.edit().putBoolean("setup_active",false).apply(); handler.postDelayed(this::goHome,1200); }
        }
        if(acted) lastAction=System.currentTimeMillis();
    }

    private void setStage(String s){ prefs.edit().putString("stage",s).apply(); }

    private boolean clickAny(AccessibilityNodeInfo root,String... labels){ for(String s:labels) if(clickText(root,s,false)) return true; return false; }
    private boolean clickContains(AccessibilityNodeInfo root,String label){ return clickText(root,label,true); }

    private boolean clickText(AccessibilityNodeInfo n,String label,boolean contains){
        if(n==null)return false;
        CharSequence t=n.getText(), d=n.getContentDescription();
        if(match(t,label,contains) || match(d,label,contains)) return clickNodeOrParent(n);
        for(int i=0;i<n.getChildCount();i++) if(clickText(n.getChild(i),label,contains)) return true;
        return false;
    }

    private boolean match(CharSequence cs,String label,boolean contains){
        if(cs==null)return false; String a=cs.toString().trim().toLowerCase(Locale.ROOT), b=label.toLowerCase(Locale.ROOT);
        return contains ? a.contains(b) : a.equals(b);
    }

    private boolean clickNodeOrParent(AccessibilityNodeInfo n){
        AccessibilityNodeInfo x=n;
        for(int i=0;i<4 && x!=null;i++,x=x.getParent()) if(x.isClickable()) return x.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        return false;
    }

    private boolean clickTopRightButton(AccessibilityNodeInfo root){
        int sw=getResources().getDisplayMetrics().widthPixels, sh=getResources().getDisplayMetrics().heightPixels;
        ArrayDeque<AccessibilityNodeInfo> q=new ArrayDeque<>(); q.add(root);
        android.graphics.Rect r=new android.graphics.Rect();
        while(!q.isEmpty()){
            AccessibilityNodeInfo n=q.removeFirst(); n.getBoundsInScreen(r);
            if(n.isClickable() && r.centerX()>sw*0.72 && r.centerY()<sh*0.22 && r.width()<sw*0.25) return n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            for(int i=0;i<n.getChildCount();i++){ AccessibilityNodeInfo c=n.getChild(i); if(c!=null)q.add(c); }
        }
        return false;
    }

    private void goHome(){ performGlobalAction(GLOBAL_ACTION_HOME); }
}
