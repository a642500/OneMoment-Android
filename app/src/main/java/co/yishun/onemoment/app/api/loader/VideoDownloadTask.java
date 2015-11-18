package co.yishun.onemoment.app.api.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.data.FileUtil;

/**
 * Created by Jinge on 2015/11/13.
 */
public class VideoDownloadTask extends AsyncTask<Video, Integer, Boolean> {
    private static final String TAG = "VideoDownloadTask";
    private Context mContext;
    private File videoFile;
    private Video video;
    private WeakReference<VideoTask> videoTaskReference;

    public VideoDownloadTask(Context context) {
        mContext = context;
    }

    public VideoDownloadTask(Context context, VideoTask videoTask) {
        mContext = context;
        videoTaskReference = new WeakReference<>(videoTask);
        VideoTaskManager.getInstance().addTask(this);
    }

    @Override
    protected Boolean doInBackground(Video... videos) {
        video = videos[0];
        Log.d(TAG, "start video " + video.fileName);
        videoFile = FileUtil.getWorldVideoStoreFile(mContext, video);

        OkHttpClient httpClient = VideoTaskManager.httpClient;
        Call call = httpClient.newCall(new Request.Builder().url(video.domain + video.fileName).get().build());
        Response response;
        InputStream input = null;
        FileOutputStream output = null;
        try {
            response = call.execute();
            Log.d(TAG, "start net " + video.fileName + " " + this.toString());
            if (response.code() == 200) {
                input = response.body().byteStream();
                output = new FileOutputStream(videoFile);
                long fileLength = response.body().contentLength();

                //OkHttp can't read more than 2048 bytes at a time.
                byte data[] = new byte[2048];
                long total = 0;
                int count;
                Log.d(TAG, "start while " + video.fileName + " " + this.toString());
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        Log.d(TAG, "cancel " + video.fileName + " " + this.toString());
                        input.close();
                        return false;
                    }
                    total += count;
                    output.write(data, 0, count);
                }
                Log.d(TAG, "end while " + video.fileName + " " + this.toString());

                return total == fileLength;
            } else {
                return false;
            }
        } catch (IOException ignore) {
            return false;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.d(TAG, "stop video" + " " + this.toString());
            if (videoTaskReference.get() != null) {
                videoTaskReference.get().getVideo(video);
            }
        } else {
            onCancelled(false);
            Log.e(TAG, "error");
        }
        VideoTaskManager.getInstance().removeTask(this);
    }

    @Override
    protected void onCancelled(Boolean result) {
        super.onCancelled(result);
        Log.d(TAG, "cancel video " + result + " " + this.toString());
        if (videoFile != null && videoFile.exists() && (result == null || !result)) {
            videoFile.delete();
        }
        VideoTaskManager.getInstance().removeTask(this);
    }
}