
package me.hauvo.thumbnail;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.media.MediaMetadataRetriever;
import android.graphics.Matrix;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.UUID;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;


public class RNThumbnailModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public RNThumbnailModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    public static Bitmap convert(String base64Str) throws IllegalArgumentException
    {
        byte[] decodedBytes = Base64.decode(
                base64Str.substring(base64Str.indexOf(",")  + 1),
                Base64.DEFAULT
        );

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static String convert(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    @Override
    public String getName() {
        return "RNThumbnail";
    }

    @ReactMethod
    public void get(final String filePath, final ReadableMap config, final Promise promise) {
        new Thread(new Runnable() {
            public void run() {
                boolean isLocal = filePath.contains("file://");
                String uri = filePath;
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                if (isLocal) {
                    uri = uri.replace("file://", "");
                    retriever.setDataSource(uri);
                } else {

                    HashMap<String, String> headers = new HashMap(config.getMap("headers").toHashMap());
                    retriever.setDataSource(uri, headers);
                }
                Bitmap image = retriever.getFrameAtTime(config.getInt("timeFrame") * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                try {
                    WritableMap map = Arguments.createMap();

                    map.putString("data", convert(image));
                    map.putDouble("width", image.getWidth());
                    map.putDouble("height", image.getHeight());

                    promise.resolve(map);

                } catch (Exception e) {
                    Log.e("E_RNThumnail_ERROR", e.getMessage());
                    promise.reject("E_RNThumnail_ERROR", e);
                }
            }
        }).start();
    }
}
