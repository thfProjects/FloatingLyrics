package com.ayyyyyyylmao.lyrics;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import static android.content.ContentValues.TAG;

/**
 * Created by marko on 1/7/2017.
 */

public class MusicListeningService extends Service {

    WindowManager windowManager;
    WindowManager.LayoutParams params;
    WindowManager.LayoutParams params2;
    WindowManager.LayoutParams params3;
    View lyricsview;
    LayoutInflater layoutInflater;
    ListView lyricslst;
    TextView albumname;
    View circlebutton;
    View delete;
    GestureDetector gestureDetector;
    GestureDetector fullscreenGestureDetector;
    Intent intent;
    PendingIntent pendingIntent;
    int screenwidth;
    int screenheight;
    int x;
    int y;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    ImageButton scalebutton;
    int lyricswidth;
    int lyricsheight;
    boolean fullscreen;

    static {
        HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3SocketFactory());

    }

    private View.OnTouchListener fullscreenlistener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return fullscreenGestureDetector.onTouchEvent(event);
        }
    };

    private View.OnTouchListener lyricscaleListener = new View.OnTouchListener() {
        int initialX;
        int initialY;
        float initialTouchX;
        float initialTouchY;
        int initialwidth;
        int initialheight;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    initialwidth = params2.width;
                    initialheight = params2.height;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    initialX = params2.x;
                    initialY = params2.y;
                case MotionEvent.ACTION_MOVE:
                    params2.width = initialwidth + (int) (initialTouchX - event.getRawX());
                    params2.height = initialheight + (int) (event.getRawY() - initialTouchY);
                    params2.x = initialX + (initialwidth - params2.width);

                    lyricswidth = params2.width;
                    lyricsheight = params2.height;

                    windowManager.updateViewLayout(lyricsview, params2);
            }
            return false;
        }
    };

    private View.OnTouchListener lyricdraglistener = new View.OnTouchListener() {


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(gestureDetector.onTouchEvent(event))return true;
            if(event.getAction() == MotionEvent.ACTION_UP){
                if(lyricsview.isShown()){
                    if(params2.x < 0){
                        params2.x = 0;
                    }
                    if(params2.y > screenheight - lyricsview.getHeight() - getStatusBarHeight()){
                        params2.y = screenheight - lyricsview.getHeight() - getStatusBarHeight();
                    }
                    if(params2.y < circlebutton.getHeight() + getStatusBarHeight()){
                        params2.y = circlebutton.getHeight() + getStatusBarHeight();
                    }
                    params.x = params2.x + lyricsview.getWidth() - circlebutton.getWidth();
                    params.y = params2.y - circlebutton.getHeight() - 20;
                }else {
                    int centerx = params.x + circlebutton.getWidth()/2;
                    int centery = params.y + circlebutton.getHeight()/2;

                    int deletecenterx = params3.x + delete.getWidth()/2;
                    int deletecentery = params3.y + delete.getHeight()/2;

                    int xdistance = Math.abs(centerx - deletecenterx);
                    int ydistance = Math.abs(centery - deletecentery);

                    if(Math.sqrt(xdistance*xdistance + ydistance*ydistance) < 70){
                        windowManager.removeView(circlebutton);
                        windowManager.removeView(lyricsview);
                        windowManager.removeView(delete);
                        params.x = x;
                        params.y = y;
                        params2.x = params.x - lyricsview.getWidth() + circlebutton.getWidth();
                        params2.y = params.y + circlebutton.getHeight() + 20;
                        lyricsview.setVisibility(View.VISIBLE);
                        stopForeground(true);
                    }else if((params.x + circlebutton.getWidth()/2) < screenwidth/2){
                        params.x = 0;
                    }else {
                        params.x = screenwidth - circlebutton.getWidth();
                    }

                    delete.setVisibility(View.GONE);
                }
                windowManager.updateViewLayout(circlebutton, params);
                windowManager.updateViewLayout(lyricsview, params2);
                return true;
            }
            return false;
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");
            Log.v("tag", artist + ":" + album + ":" + track);
            albumname.setText(artist + " - " + track);
            getLyrics(artist, album, track);
            if(!circlebutton.isShown()) {
                windowManager.addView(lyricsview, params2);
                windowManager.addView(circlebutton, params);
                windowManager.addView(delete, params3);
            }
            Notification notification = new Notification.Builder(MusicListeningService.this)
                    .setContentTitle("lyrics shown")
                    .setContentText("")
                    .setSmallIcon(R.drawable.circle)
                    .setContentIntent(pendingIntent)
                    .setTicker("")
                    .build();

            startForeground(6654, notification);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        intent = new Intent(MusicListeningService.this, StartActivity.class);
        pendingIntent = PendingIntent.getActivity(MusicListeningService.this, 0, intent, 0);

        Toast.makeText(MusicListeningService.this, "service started", Toast.LENGTH_LONG).show();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        screenwidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenheight = Resources.getSystem().getDisplayMetrics().heightPixels;

        lyricswidth = (screenwidth/3)*2;
        lyricsheight = screenheight/2;
        int circleheight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());

        int x = screenwidth - circleheight - 50;
        int y = 50;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = x;
        params.y = y;

        params2 = new WindowManager.LayoutParams(
                lyricswidth,
                lyricsheight,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params2.gravity = Gravity.TOP | Gravity.LEFT;
        params2.x = params.x - lyricswidth + circleheight;
        params2.y = params.y + circleheight + 20;

        params3 = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params3.gravity = Gravity.TOP | Gravity.LEFT;
        params3.x = screenwidth/2 - circleheight/2;
        params3.y = screenheight - circleheight - 120;

        gestureDetector = new GestureDetector(MusicListeningService.this, new LyricsGesturelistener());
        fullscreenGestureDetector = new GestureDetector(MusicListeningService.this, new FullscreenGestureListener());

        fullscreen = false;

        layoutInflater =(LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        lyricsview = layoutInflater.inflate(R.layout.lyricstext, null);

        lyricslst = (ListView) lyricsview.findViewById(R.id.list);
        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.lyrics_list_item, arrayList);
        lyricslst.setAdapter(adapter);
        lyricslst.setDivider(null);

        albumname = (TextView) lyricsview.findViewById(R.id.albumname);
        albumname.setOnTouchListener(fullscreenlistener);

        scalebutton = (ImageButton) lyricsview.findViewById(R.id.scalebutton);
        scalebutton.setOnTouchListener(lyricscaleListener);

        circlebutton = layoutInflater.inflate(R.layout.circlebutton, null);
        circlebutton.setOnTouchListener(lyricdraglistener);

        delete = layoutInflater.inflate(R.layout.circledelete, null);
        delete.setVisibility(View.GONE);


        IntentFilter iF = new IntentFilter();

        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.lge.music.metachanged");
        iF.addAction("com.htc.music.metachanged");
        iF.addAction("com.spotify.music.metadatachanged");
        iF.addAction("fm.last.android.metachanged");
        iF.addAction("com.sec.android.app.music.metachanged");
        iF.addAction("com.nullsoft.winamp.metachanged");
        iF.addAction("com.amazon.mp3.metachanged");
        iF.addAction("com.miui.player.metachanged");
        iF.addAction("com.real.IMP.metachanged");
        iF.addAction("com.sony.music.metachanged");
        iF.addAction("com.rdio.android.metachanged");
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        iF.addAction("com.andrew.apollo.metachanged");

        registerReceiver(mReceiver, iF);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if (circlebutton.isShown()) {
            windowManager.removeView(circlebutton);
            windowManager.removeView(lyricsview);
            windowManager.removeView(delete);
        }
        Toast.makeText(MusicListeningService.this, "service stopped", Toast.LENGTH_LONG).show();
    }

    class FullscreenGestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onDown(MotionEvent event){
            super.onDown(event);

            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event){
            super.onDoubleTap(event);

            if(!fullscreen){
                params2.width = screenwidth;
                params2.height = screenheight - getStatusBarHeight();

                windowManager.updateViewLayout(lyricsview, params2);

                scalebutton.setVisibility(View.GONE);
                circlebutton.setVisibility(View.GONE);

                fullscreen = true;
            }else {
                params2.width = lyricswidth;
                params2.height = lyricsheight;

                windowManager.updateViewLayout(lyricsview, params2);

                scalebutton.setVisibility(View.VISIBLE);
                circlebutton.setVisibility(View.VISIBLE);

                fullscreen = false;
            }

            return true;
        }
    }

    class LyricsGesturelistener extends GestureDetector.SimpleOnGestureListener{

        int initialX;
        int initialY;
        float initialTouchX;
        float initialTouchY;

        @Override
        public boolean onDown(MotionEvent event){
            super.onDown(event);

            initialX = params.x;
            initialY = params.y;
            initialTouchX = event.getRawX();
            initialTouchY = event.getRawY();

            if(!lyricsview.isShown()){
                delete.setVisibility(View.VISIBLE);
            }

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event){
            super.onSingleTapConfirmed(event);
            if (lyricsview.isShown()) {
                lyricsview.setVisibility(View.GONE);

                x = params.x;
                y = params.y;

                if((params.x + circlebutton.getWidth()/2) < screenwidth/2){
                    params.x = 0;
                }else {
                    params.x = screenwidth - circlebutton.getWidth();
                }
            }
            else {
                lyricsview.setVisibility(View.VISIBLE);

                params.x = x;
                params.y = y;

                params2.x = params.x - lyricsview.getWidth() + circlebutton.getWidth();
                params2.y = params.y + circlebutton.getHeight() + 20;
            }
            windowManager.updateViewLayout(circlebutton, params);
            windowManager.updateViewLayout(lyricsview, params2);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY){
            super.onScroll(e1, e2, distanceX, distanceY);

            float moveX = e2.getRawX();
            float moveY = e2.getRawY();

            params.x = initialX + (int) (moveX - initialTouchX);
            params.y = initialY + (int) (moveY - initialTouchY);

            params2.x = params.x - lyricsview.getWidth() + circlebutton.getWidth();
            params2.y = params.y + circlebutton.getHeight() + 20;

            windowManager.updateViewLayout(circlebutton, params);
            windowManager.updateViewLayout(lyricsview,params2);

            return true;
        }
    }

    public void getLyrics(String artist, String album, String track){

        final String originaltrack = track;
        String originalartist = artist;
        String originalalbum = album;

        artist = artist.toLowerCase();
        artist = artist.replace(" ", "-");
        album = album.toLowerCase();
        album = album.replace(" ", "-");
        track = track.toLowerCase();
        track = track.replace(" ", "-");

        String metrolyricsurl = "http://www.metrolyrics.com/" + track.replace(".", "") + "-lyrics-" + artist.replace(".", "") + ".html";
        String songlyricsurl = "http://www.songlyrics.com/" + artist.replace("’", "-") + "/" + track.replace("’", "-") + "-lyrics/";
        String darklyricsurl = "http://www.darklyrics.com/lyrics/" + artist.replace(".", "") + "/" + album.replace("-", "") + ".html";
        String lyricstranslateurl = "http://www.lyricstranslate.com/" + "en/" + artist + "-" + track + "-lyrics.html";
        String geniusurl = "http://www.genius.com/" + artist.replace(" ", "-") + "-" + track.replace(" ", "-").replace("’", "") + "-lyrics";

        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());

        class GetLyricsTask extends AsyncTask <String, Void, String>{
            @Override
            protected String doInBackground(String... params){
                Document doc = null;
                Elements lyrics = null;
                String text = null;

                String useragant = System.getProperty("http.agent");

                try {
                    doc = Jsoup.connect(params[0]).get();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if(doc!=null){
                    doc.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
                    doc.select("br").append("\\n");
                    doc.select("p").prepend("\\n\\n");

                    lyrics = doc.select("div[id=lyrics-body-text]");
                    text = lyrics.text();
                    doc = null;
                }

                if(text!="" && text!=null && !text.equals("\\n\\n")){
                    return text;
                }else try{
                    doc = Jsoup.connect(params[1]).get();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(doc!=null){
                    doc.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
                    doc.select("br").append("\\n");
                    doc.select("p").prepend("\\n\\n");

                    lyrics = doc.select("p[id=songLyricsDiv]");
                    text = lyrics.text();

                    doc = null;
                }
                if(text!="" && text!=null && !text.equals("\\n\\n") && !text.contains("We do not have the lyrics for")){
                    return text;
                }else try {
                    doc = Jsoup.connect(params[2]).userAgent(useragant).get();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(doc!=null){
                    doc.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
                    doc.select("br").append("\\n");
                    doc.select("p").prepend("\\n\\n");

                    lyrics = doc.select("div[class=lyrics]");
                    text = lyrics.text();

                    if(text.indexOf(WordUtils.capitalize(originaltrack))!=-1){
                        text = text.substring(text.indexOf(". " + WordUtils.capitalize(originaltrack)));
                        if(originaltrack.length() + 6 < text.indexOf("\\n \\n \\n", 2)){
                            text = text.substring(originaltrack.length() + 6, text.indexOf("\\n \\n \\n", 2));
                        }else text = "";
                    }else text = "";

                    doc = null;
                }
                if(text!="" && text!=null && !text.equals("\\n\\n") && !text.contains("We do not have the lyrics for")){
                    return text;
                }else try {
                    doc = Jsoup.connect(params[3]).get();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(doc!=null){
                    doc.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
                    doc.select("br").append("\\n");
                    doc.select("p").prepend("\\n\\n");

                    lyrics = doc.select("div[class=song-node-text]");
                    text = lyrics.text();
                    text = text.substring(0, text.indexOf("Submitted by"));

                    doc = null;
                }
                if(text!="" && text!=null && !text.equals("\\n\\n") && !text.contains("We do not have the lyrics for")){
                    return text;
                }else try {
                    doc = Jsoup.connect(params[4]).get();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(doc!=null){
                    doc.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
                    doc.select("br").append("\\n");
                    doc.select("p").prepend("\\n\\n");

                    lyrics = doc.select("div[class=lyrics]");
                    text = lyrics.text();
                }
                if(text!="" && text!=null && !text.equals("\\n\\n") && !text.contains("We do not have the lyrics for")){
                    return text;
                }else return "";
            }

            @Override
            protected void onPostExecute(String result) {
                if(result == ""){
                    result = "no lyrics found :c";
                }
                arrayList.clear();
                arrayList.add(result.replace("\\n", System.getProperty("line.separator")));

                adapter.notifyDataSetInvalidated();
                adapter.notifyDataSetChanged();

                super.onPostExecute(result);
            }

        }

        new GetLyricsTask().execute(metrolyricsurl, songlyricsurl, darklyricsurl, lyricstranslateurl, geniusurl);

    }

    public class NullHostNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            Log.i("RestUtilImpl", "Approving certificate for " + hostname);
            return true;
        }

    }



    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


}
