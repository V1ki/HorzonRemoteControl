package org.horzon.box.remotecontrol;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class CatchScreenService extends Service {


    private static final String TAG = "CatchScreenService";

    public static int screenWidth;
    public static int screenHeight;
    public static int pixelformat;

    public CatchScreenService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        try {
            final ServerSocket serverSocket = new ServerSocket(33445);

            new Thread() {
                @Override
                public void run() {
                    while (true) {

                        Socket socket = null;
                        try {
                            socket = serverSocket.accept();

                            new SocketHandler(getApplicationContext(),socket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }.start();

            final ServerSocket controlSocket = new ServerSocket(33446);

            new Thread(){
                @Override
                public void run() {

                    while (true) {
                        Socket socket = null;
                        try {
                            socket = controlSocket.accept();

                            while (socket.isConnected()) {
                                byte[] buff = new byte[128];
                                int len;
                                InputStream bis = socket.getInputStream();
                                Instrumentation inst = new Instrumentation();
                                while((len = bis.read(buff))!=-1){
                                    String commandStr = new String(buff,0,len) ;
                                    Log.d(TAG,"recv:"+commandStr);


                                    if(!TextUtils.isEmpty(commandStr)&& commandStr.startsWith("touch:")){

                                        int lastIndex = 0 ;

                                        int index = 0 ;
                                        while((index =  commandStr.indexOf("touch:",lastIndex))  != -1) {

                                            String[] touchEvent = commandStr.substring(index + 6).split(",");
                                            if(touchEvent.length >= 3 ) {
                                                int touchType = Integer.parseInt(touchEvent[0]);
                                                float touchX = Float.parseFloat(touchEvent[1]);
                                                float touchY = Float.parseFloat(touchEvent[2]);

                                                inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                                                        touchType, touchX, touchY, 0));
                                            }
                                            else{
                                                break;
                                            }
                                            lastIndex = index + 1;
                                        }


                                    }

                                }


                            }

                        } catch (Exception e) {
                            Log.e(TAG, "run: ", e);
                        }

                    }

                }
            }.start();



        } catch (Exception e) {
            e.printStackTrace();
        }

    }





    private Bitmap getGraphics() throws Exception {
//        FileInputStream buf = new FileInputStream(new File("/dev/graphics/fb0"));
//        screenWidth = 1280 ;
//
//        PixelFormat localPixelFormat1 = new PixelFormat();
//        PixelFormat.getPixelFormatInfo(pixelformat, localPixelFormat1);
//
//        int deepth = localPixelFormat1.bytesPerPixel;// 位深
//        byte[] piex = new byte[screenHeight * screenWidth * deepth];// 像素
//        Log.d(TAG, "getGraphics: deepth:"+deepth + " -- piex:"+piex.length);
//        DataInputStream dStream = new DataInputStream(buf);
//        dStream.readFully(piex);
//        int[] colors = new int[screenHeight * screenWidth];
        // 将rgb转为色值
       /* for (int i = 0; i < piex.length; i += 2) {
            colors[i / 2] = (int) 0xff000000 +
            (int) (((piex[i + 1]) << (16)) & 0x00f80000) +
            (int) (((piex[i + 1]) << 13) & 0x0000e000) +
            (int) (((piex[i]) << 5) & 0x00001A00) +
            (int) (((piex[i]) << 3) & 0x000000f8);
        }*/
       /*int r,g,b ,a;
        for (int j = 0; j < piex.length; j+= deepth) {
            r = piex[j]&0xff;
            g = piex[j+1]&0xff;
            b = piex[j+2]&0xff;
            a = piex[j+3]&0xff;
            colors[j/deepth] = (r << 16) | (g << 8) | b |(0xff000000);
        }*/
//        for (int m = 0; m < colors.length; m++) {
//            int b = (piex[m * 4] & 0xFF);
//            int g = (piex[m * 4 + 1] & 0xFF);
//            int r = (piex[m * 4 + 2] & 0xFF);
//            int a = (piex[m * 4 + 3] & 0xFF);
//            colors[m] = (a << 24) + (r << 16) + (g << 8) + b;
//        }

        //return Bitmap.createBitmap(colors, screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
        Bitmap b = (Bitmap) Class.forName("android.view.SurfaceControl").getDeclaredMethod("screenshot", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(0), Integer.valueOf(0)});
// 得到屏幕bitmap

        return b;


    }


}
