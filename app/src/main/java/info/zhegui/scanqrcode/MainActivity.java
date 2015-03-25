package info.zhegui.scanqrcode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;


public class MainActivity extends ActionBarActivity {
    private final static String TAG = "MainActivity";

    private TextView tv;
    private final int WHAT_DECODED = 101;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_DECODED:
                    Result result = (Result) msg.obj;
                    tv.setText(result.getText());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String path = "/sdcard/hello_feixun.png";

        ImageView iv = (ImageView) findViewById(R.id.iv);
        iv.setImageBitmap(BitmapFactory.decodeFile(path));
        Button btn = (Button) findViewById(R.id.btn);
        tv = (TextView) findViewById(R.id.tv);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    public void run() {
                        Result result = decode(path);
                        if (result != null) {
                            Message msg = mHandler.obtainMessage(WHAT_DECODED, result);
                            msg.sendToTarget();
                        }
                    }
                }.start();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private static Result decode(String path) {


        Bitmap imageBitmap = BitmapFactory.decodeFile(path);
        if (imageBitmap == null) {
            Log.e(TAG, "Couldn't open " + path);
            return null;
        }

        MultiFormatReader reader = new MultiFormatReader();
        reader.setHints(null);
        // Try to get in a known state before starting the benchmark
        System.gc();

        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);

        boolean success;
        Result result = null;
        // Using this call instead of getting the time should eliminate a lot of variability due to
        // scheduling and what else is happening in the system.
        long now = Debug.threadCpuTimeNanos();
        try {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            result = reader.decodeWithState(bitmap);
            success = true;
        } catch (ReaderException ignored) {
            success = false;
        }


        return result;
    }


}
