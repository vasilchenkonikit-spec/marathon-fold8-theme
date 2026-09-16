package com.nv.marathontheme;

import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.graphics.*;
import android.os.*;
import java.util.Random;

public class MarathonWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine(){return new EngineImpl();}

    class EngineImpl extends Engine implements Runnable {
        final Handler handler=new Handler(Looper.getMainLooper());
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        final Random rng=new Random(93);
        final float[] particles=new float[54];
        Bitmap inner,cover;
        boolean visible=false;
        long start=System.currentTimeMillis();

        EngineImpl(){
            inner=BitmapFactory.decodeResource(getResources(),R.drawable.wallpaper_inner_4k);
            cover=BitmapFactory.decodeResource(getResources(),R.drawable.wallpaper_cover_4k);
            for(int i=0;i<particles.length;i++)particles[i]=rng.nextFloat();
        }

        @Override public void onVisibilityChanged(boolean v){visible=v;if(v)drawFrame();else handler.removeCallbacks(this);}
        @Override public void onSurfaceChanged(SurfaceHolder h,int f,int w,int he){drawFrame();}
        @Override public void onSurfaceDestroyed(SurfaceHolder h){visible=false;handler.removeCallbacks(this);}
        @Override public void run(){drawFrame();}

        private void drawFrame(){
            Canvas c=null;
            try{
                SurfaceHolder holder=getSurfaceHolder();
                c=holder.lockCanvas();
                if(c==null)return;
                int w=c.getWidth(),h=c.getHeight();
                Bitmap src=((float)w/(float)h)>0.88f?inner:cover;
                c.drawColor(Color.BLACK);

                long t=System.currentTimeMillis()-start;
                float pulse=(float)Math.sin(t/5200.0);
                float base=Math.max((float)w/src.getWidth(),(float)h/src.getHeight());
                float scale=base*(1.018f+0.0045f*pulse);
                float x=(w-src.getWidth()*scale)/2f+(float)Math.sin(t/7600.0)*w*0.006f;
                float y=(h-src.getHeight()*scale)/2f+(float)Math.cos(t/8800.0)*h*0.004f;
                Matrix m=new Matrix();m.postScale(scale,scale);m.postTranslate(x,y);
                paint.setAlpha(255);c.drawBitmap(src,m,paint);

                paint.setStrokeWidth(Math.max(1f,w/1100f));
                paint.setColor(Color.argb(22,214,255,0));
                float scan=(t/24f)%h;c.drawLine(0,scan,w,scan,paint);

                paint.setColor(Color.argb(45,214,255,0));
                for(int i=0;i<18;i++){
                    float px=particles[i*3]*w;
                    float py=(particles[i*3+1]*h+t*(0.0025f+particles[i*3+2]*0.006f))%h;
                    c.drawCircle(px,py,0.7f+particles[i*3+2]*1.4f,paint);
                }
            }finally{
                if(c!=null)getSurfaceHolder().unlockCanvasAndPost(c);
            }
            handler.removeCallbacks(this);
            if(visible)handler.postDelayed(this,50);
        }
    }
}
