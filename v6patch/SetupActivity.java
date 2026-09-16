package com.nv.marathontheme;

import android.app.*;
import android.appwidget.*;
import android.app.WallpaperManager;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class SetupActivity extends Activity {
    static final String PREF = "marathon_v7";
    static final String THEME_PARK = "com.samsung.android.themedesigner";
    static final String KEYS_CAFE = "com.samsung.android.keyscafe";
    static final String GOOD_LOCK = "com.samsung.android.goodlock";
    static final String CLOCK_FACE = "com.samsung.android.app.clockface";
    static final String LOCK_STAR = "com.samsung.systemui.lockstar";
    static final String TICKTICK = "com.ticktick.task";
    static final int ACID = Color.rgb(214,255,0);
    static final int BG = Color.rgb(5,8,6);
    static final int PANEL = Color.rgb(18,22,18);

    private SharedPreferences prefs;
    private LinearLayout root;
    private int step;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences(PREF,MODE_PRIVATE);
        step=prefs.getInt("step",0);
        render();
    }

    @Override protected void onResume(){
        super.onResume();
        step=prefs.getInt("step",step);
        render();
    }

    private void render(){
        ScrollView scroll=new ScrollView(this);
        scroll.setBackgroundColor(BG);
        root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24),dp(28),dp(24),dp(32));
        scroll.removeAllViews();
        scroll.addView(root);
        setContentView(scroll);

        root.addView(text("MARATHON // FOLD 8",30,ACID,true));
        root.addView(text("STABLE ONE UI INSTALLER v7.0",17,Color.WHITE,true));
        addSpace(10);
        TextView safe=text("One UI Home остаётся штатным. Свайп вверх, поиск Samsung, список приложений, папки и твоя текущая раскладка виджетов не меняются.",14,Color.LTGRAY,false);
        root.addView(safe);
        addSpace(18);

        if(step<=0) welcome();
        else if(step==1) wallpaperStep();
        else if(step==2) iconStep();
        else if(step==3) keyboardStep();
        else if(step==4) lockStep();
        else if(step==5) tickTickStep();
        else finishStep();
    }

    private void welcome(){
        addProgress("0 / 5");
        addCard("Одна стабильная установка", "Я убрал автоклики Accessibility. Они и были причиной зависаний в Theme Park. Теперь приложение само открывает нужный модуль Samsung, а ты подтверждаешь только то, что Android/Samsung не разрешают сделать стороннему APK. После каждого модуля возвращаешься сюда и жмёшь одну кнопку «ГОТОВО, ДАЛЬШЕ».\n\nВ комплекте: Marathon-обои, live wallpaper, Marathon icon pack, настройки клавиатуры, блокировки и TickTick. Твоя раскладка One UI не перестраивается.");
        addPrimary("НАЧАТЬ УСТАНОВКУ", ()->{ setStep(1); applyStaticWallpapers(); render(); });
    }

    private void wallpaperStep(){
        addProgress("1 / 5  ОБОИ");
        addCard("Обои уже встроены в APK", "4K-версия применяется автоматически как статический резерв. Для адаптивного варианта Fold нажми кнопку ниже и один раз подтверди системное «Установить обои». Больше здесь ничего настраивать не нужно.");
        addPrimary("ОТКРЫТЬ LIVE WALLPAPER", ()->launchWallpaperPicker(this));
        addSecondary("ГОТОВО, ДАЛЬШЕ", ()->{setStep(2);render();});
        addTertiary("Повторно применить 4K-обои", this::applyStaticWallpapers);
    }

    private void iconStep(){
        addProgress("2 / 5  ИКОНКИ");
        addCard("Theme Park: только этот короткий блок", "1. Нажми «ОТКРЫТЬ THEME PARK».\n2. Внизу выбери Icon.\n3. Нажми + / Create new.\n4. В редакторе нажми Iconpack.\n5. Выбери Marathon Fold 8 / Marathon 93.\n6. Нажми значок сохранения справа сверху, название: MARATHON 93.\n7. Открой сохранённый пакет и нажми Apply.\n8. Вернись сюда.\n\nЭто единственный надёжный способ на твоей версии Theme Park. Никаких автоматических кликов и зависаний.");
        addPrimary("ОТКРЫТЬ THEME PARK", ()->{ if(!launchPackage(this,THEME_PARK)) launchPackage(this,GOOD_LOCK); });
        addSecondary("ГОТОВО, ДАЛЬШЕ", ()->{setStep(3);render();});
    }

    private void keyboardStep(){
        addProgress("3 / 5  КЛАВИАТУРА");
        addCard("Keys Cafe", "Нажми «ОТКРЫТЬ KEYS CAFE» -> Style your own keyboard -> создай/открой стиль и примени Marathon-схему:\n\nфон: #050806\nклавиши: #121612\nакцент: #D6FF00\nтекст: #F1F2EA\n\nНажми Apply и вернись сюда. Если клавиатуру сейчас менять не хочешь, этот шаг можно пропустить.");
        addPrimary("ОТКРЫТЬ KEYS CAFE", ()->{ if(!launchPackage(this,KEYS_CAFE)) launchPackage(this,GOOD_LOCK); });
        addSecondary("ГОТОВО, ДАЛЬШЕ", ()->{setStep(4);render();});
        addTertiary("ПРОПУСТИТЬ", ()->{setStep(4);render();});
    }

    private void lockStep(){
        addProgress("4 / 5  ЭКРАН БЛОКИРОВКИ");
        addCard("ClockFace / LockStar", "Твоя цель здесь только визуальная: крупные технические часы, минимум лишних элементов, чёрный + acid lime. Нажми кнопку, примени вариант для экрана блокировки и вернись сюда. One UI Home это не затрагивает.");
        addPrimary("ОТКРЫТЬ CLOCKFACE / LOCKSTAR", ()->{ if(!launchPackage(this,CLOCK_FACE)) if(!launchPackage(this,LOCK_STAR)) launchPackage(this,GOOD_LOCK); });
        addSecondary("ГОТОВО, ДАЛЬШЕ", ()->{setStep(5);render();});
        addTertiary("ПРОПУСТИТЬ", ()->{setStep(5);render();});
    }

    private void tickTickStep(){
        addProgress("5 / 5  TICKTICK");
        addCard("Настоящий виджет TickTick", "Я не заменяю твой домашний экран и не двигаю существующие виджеты. Кнопка ниже вызывает штатное Android-подтверждение настоящего виджета TickTick. Если свободного места нет или ты хочешь поставить его в конкретное место, открой TickTick/виджеты One UI и поставь его вручную. Нажатия по нему будут работать как обычно.");
        addPrimary("ДОБАВИТЬ ВИДЖЕТ TICKTICK", ()->{
            boolean ok=requestTickTickWidget(this);
            if(!ok){ Toast.makeText(this,"Виджет TickTick не найден. Открой TickTick и добавь его через штатное меню виджетов One UI.",Toast.LENGTH_LONG).show(); launchPackage(this,TICKTICK); }
        });
        addSecondary("ЗАВЕРШИТЬ", ()->{setStep(6);render();});
        addTertiary("НЕ МЕНЯТЬ МОИ ВИДЖЕТЫ", ()->{setStep(6);render();});
    }

    private void finishStep(){
        addProgress("ГОТОВО");
        addCard("Marathon установлен поверх One UI", "Штатный Samsung launcher не заменён. Свайп вверх, Samsung Search и список приложений остаются системными. Установщик можно оставить для повторного доступа к настройкам или удалить после завершения: применённые обои/настройки Theme Park останутся.");
        addPrimary("НА ГЛАВНЫЙ ЭКРАН", ()->{ Intent h=new Intent(Intent.ACTION_MAIN); h.addCategory(Intent.CATEGORY_HOME); startActivity(h); });
        addSecondary("НАЧАТЬ ЗАНОВО", ()->{setStep(1);applyStaticWallpapers();render();});
    }

    private void addProgress(String s){
        TextView t=text(s,14,ACID,true); t.setPadding(0,0,0,dp(10)); root.addView(t);
    }

    private void addCard(String title,String body){
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18),dp(18),dp(18),dp(18));
        GradientDrawable gd=new GradientDrawable();
        gd.setColor(PANEL); gd.setCornerRadius(dp(14)); gd.setStroke(dp(1),ACID);
        card.setBackground(gd);
        card.addView(text(title,20,Color.WHITE,true));
        TextView b=text(body,15,Color.LTGRAY,false); b.setPadding(0,dp(10),0,0); card.addView(b);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,dp(16));
        root.addView(card,lp);
    }

    private void addPrimary(String label,Runnable r){ addButton(label,ACID,BG,r); }
    private void addSecondary(String label,Runnable r){ addButton(label,Color.WHITE,BG,r); }
    private void addTertiary(String label,Runnable r){ addButton(label,PANEL,Color.LTGRAY,r); }

    private void addButton(String label,int bg,int fg,Runnable r){
        Button b=new Button(this);
        b.setText(label); b.setTextSize(16); b.setAllCaps(false); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        b.setTextColor(fg);
        GradientDrawable gd=new GradientDrawable(); gd.setColor(bg); gd.setCornerRadius(dp(8));
        if(bg==PANEL) gd.setStroke(dp(1),Color.DKGRAY);
        b.setBackground(gd); b.setOnClickListener(v->r.run());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58)); lp.setMargins(0,0,0,dp(10));
        root.addView(b,lp);
    }

    private TextView text(String s,int size,int color,boolean bold){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return t;
    }
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private void addSpace(int v){ Space s=new Space(this); root.addView(s,new LinearLayout.LayoutParams(1,dp(v))); }
    private void setStep(int s){ step=s; prefs.edit().putInt("step",s).apply(); }

    static void launchWallpaperPicker(Context c){
        try{
            Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(c,MarathonWallpaperService.class));
            c.startActivity(i);
        }catch(Exception e){
            try{ c.startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)); }
            catch(Exception ignored){ Toast.makeText(c,"Не удалось открыть системный выбор обоев",Toast.LENGTH_LONG).show(); }
        }
    }

    private void applyStaticWallpapers(){
        try{
            WallpaperManager wm=WallpaperManager.getInstance(this);
            Bitmap inner=BitmapFactory.decodeResource(getResources(),R.drawable.wallpaper_inner_4k);
            Bitmap cover=BitmapFactory.decodeResource(getResources(),R.drawable.wallpaper_cover_4k);
            wm.setBitmap(inner,null,true,WallpaperManager.FLAG_SYSTEM);
            wm.setBitmap(cover,null,true,WallpaperManager.FLAG_LOCK);
            Toast.makeText(this,"4K Marathon-обои применены",Toast.LENGTH_SHORT).show();
        }catch(Exception e){ Toast.makeText(this,"Не удалось применить статические обои автоматически",Toast.LENGTH_LONG).show(); }
    }

    static boolean launchPackage(Context c,String pkg){
        try{
            Intent i=c.getPackageManager().getLaunchIntentForPackage(pkg);
            if(i==null)return false;
            c.startActivity(i); return true;
        }catch(Exception e){return false;}
    }

    static boolean requestTickTickWidget(Context c){
        if(Build.VERSION.SDK_INT<26)return false;
        try{
            AppWidgetManager m=AppWidgetManager.getInstance(c);
            if(!m.isRequestPinAppWidgetSupported())return false;
            AppWidgetProviderInfo best=null,fallback=null;
            PackageManager pm=c.getPackageManager();
            for(AppWidgetProviderInfo p:m.getInstalledProviders()){
                String pkg=p.provider.getPackageName().toLowerCase(Locale.ROOT);
                if(!pkg.contains("ticktick"))continue;
                if(fallback==null)fallback=p;
                String label="";
                try{CharSequence cs=p.loadLabel(pm); if(cs!=null)label=cs.toString().toLowerCase(Locale.ROOT);}catch(Exception ignored){}
                if(label.contains("calendar")||label.contains("month")||label.contains("agenda")||label.contains("календар")){best=p;break;}
            }
            if(best==null)best=fallback;
            return best!=null && m.requestPinAppWidget(best.provider,null,null);
        }catch(Exception e){return false;}
    }
}
