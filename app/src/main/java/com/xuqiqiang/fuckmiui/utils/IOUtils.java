package com.xuqiqiang.fuckmiui.utils;

import android.os.Build;
import android.os.CancellationSignal;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.Executor;

public class IOUtils {
    private static final String TAG = "IOUtils";

    public static Thread writeStreamToStringBuilder(StringBuilder builder,
        InputStream inputStream) {
        Thread t = new Thread(() -> {
            try {
                char[] buf = new char[1024];
                int len;
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                while ((len = reader.read(buf)) > 0) {
                    builder.append(buf, 0, len);
                }

                reader.close();
            } catch (Exception e) {
                Log.wtf(TAG, e);
            }
        });
        t.start();
        return t;
    }

    public static long copy(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        return copy(in, out, null, null, null);
    }

    public static long copy(@NonNull InputStream in, @NonNull OutputStream out, @Nullable
        CancellationSignal signal, @Nullable Executor executor,
        @Nullable ProgressListener listener) throws IOException {
        return Build.VERSION.SDK_INT >= 29 && in instanceof FileInputStream
            && out instanceof FileOutputStream ? copy(
            ((FileInputStream) in).getFD(), ((FileOutputStream) out).getFD(), signal, executor,
            listener) : copyInternalUserspace(in, out, signal, executor, listener);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static long copy(@NonNull FileDescriptor in, @NonNull FileDescriptor out,
        @Nullable CancellationSignal signal, @Nullable Executor executor, @Nullable
        ProgressListener listener) throws IOException {
        android.os.FileUtils.ProgressListener progressListener;
        if (listener != null) {
            Objects.requireNonNull(listener);
            progressListener = listener::onProgress;
        } else {
            progressListener = null;
        }

        return android.os.FileUtils.copy(in, out, signal, executor, progressListener);
    }

    private static long copyInternalUserspace(InputStream in, OutputStream out,
        CancellationSignal signal, Executor executor, ProgressListener listener)
        throws IOException {
        long progress = 0L;
        byte[] buffer = new byte[8192];

        int t;
        while ((t = in.read(buffer)) != -1) {
            out.write(buffer, 0, t);
            progress += t;
            if (signal != null) {
                signal.throwIfCanceled();
            }
            if (listener != null) {
                if (executor != null) {
                    long finalProgress = progress;
                    executor.execute(() -> listener.onProgress(finalProgress));
                } else {
                    listener.onProgress(progress);
                }
            }
        }

        return progress;
    }

    public static boolean downloadFile(String uri, String savePath, ProgressListener listener) {
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL url = new URL(uri);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();

            int fileSize = conn.getContentLength();
            if (fileSize <= 0) throw new RuntimeException("Unknown file size");
            is = conn.getInputStream();
            if (is == null) throw new RuntimeException("stream is null");
            copy(is, new FileOutputStream(savePath), null, null, listener);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
        return false;
    }

    public interface ProgressListener {
        void onProgress(long progress);
    }
}
