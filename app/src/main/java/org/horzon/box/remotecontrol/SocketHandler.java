package org.horzon.box.remotecontrol;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

public class SocketHandler {
    private static final String TAG = "SocketHandler" ;
    private float[] dims;

    Socket mSocket;
    InputStream is;

    public SocketHandler(Context context ,Socket socket) {
        mSocket = socket;
        dims = getPixel(context);

        new Thread(new ReceiveCommand(mSocket)).start();

    }

    /** 屏幕截图 */
    private byte[] getBitmapByte(){
        //return qualityZoom(areaZoom(screenShot((int) dims[0], (int) dims[1])));

        Bitmap bitmap = areaZoom(screenShot(CatchScreenService.screenWidth , CatchScreenService.screenHeight));
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteStream);
        bitmap.recycle();
        return byteStream.toByteArray();
    }

    /** 质量缩放 */
    private byte[] qualityZoom(Bitmap bitmap) {
        Bitmap mutable = bitmap.copy(Bitmap.Config.RGB_565, true);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        mutable.compress(Bitmap.CompressFormat.JPEG, 20, byteStream);
        return byteStream.toByteArray();
    }

    /** 面积缩放 */
    private Bitmap areaZoom(Bitmap image) {
        Matrix matrix = new Matrix();
        matrix.setScale(0.5f, 0.5f);
        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }


    private Bitmap screenShot(int width, int height) {
        try {
            return (Bitmap) getSurfaceClass().invoke(null, width, height);
        } catch (Exception e) {
            Log.e(TAG, "screenShot: ", e);
        }
        return null;
    }


    private Method getSurfaceClass(){
        Class<?> surfaceClass;
        Method method = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                surfaceClass = Class.forName("android.view.SurfaceControl");
            } else {
                surfaceClass = Class.forName("android.view.Surface");
            }
            method = surfaceClass.getDeclaredMethod("screenshot", int.class, int.class);
            method.setAccessible(true);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return method;
    }

    private float[] getPixel(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        Display mDisplay = wm.getDefaultDisplay();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mDisplay.getRealMetrics(mDisplayMetrics);
        }
        return new float[]{
                mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels
        };
    }



    private class ReceiveCommand implements Runnable {
        private Socket socket;

        ReceiveCommand(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                byte[] imgDatas = new byte[1] ;
//                Instrumentation inst = new Instrumentation();
                while (socket.isConnected()) {
                    byte[] buff = new byte[128];
                    int len;
                    InputStream bis = socket.getInputStream();
                    OutputStream bos = socket.getOutputStream();
                    while((len = bis.read(buff))!=-1){
                        String commandStr = new String(buff,0,len) ;
                        Log.d(TAG,"recv:"+commandStr);

                        if(!TextUtils.isEmpty(commandStr)&& commandStr.contains("start")){
                            imgDatas = getBitmapByte();
                            bos.write(("length:"+imgDatas.length).getBytes());
                            bos.flush();

                        }
                        if(!TextUtils.isEmpty(commandStr)&& commandStr.contains("send")){
                            bos.write(imgDatas);
                            bos.flush();

                        }

                        /*

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

                        */
                    }


                }

            } catch (IOException e) {
                Log.e(TAG, "run: ", e);
            }
        }
    }

    private void back() {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static byte[] intToBytes(int value) {
        byte[] des = new byte[4];
        des[0] = (byte) (value & 0xff);  // 低位(右边)的8个bit位
        des[1] = (byte) ((value >> 8) & 0xff); //第二个8 bit位
        des[2] = (byte) ((value >> 16) & 0xff); //第三个 8 bit位
        /*
         * (byte)((value >> 24) & 0xFF);
         * value向右移动24位, 然后和0xFF也就是(11111111)进行与运算
         * 在内存中生成一个与 value 同类型的值
         * 然后把这个值强制转换成byte类型, 再赋值给一个byte类型的变量 des[3]
         */
        des[3] = (byte) ((value >> 24) & 0xff); //第4个 8 bit位
        return des;
    }

    static int bytesToInt(byte[] des, int offset) {
        int value;
        value = des[offset] & 0xff
                | ((des[offset + 1] & 0xff) << 8)
                | ((des[offset + 2] & 0xff) << 16)
                | (des[offset + 3] & 0xff) << 24;
        return value;
    }










}
