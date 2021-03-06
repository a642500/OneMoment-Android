package co.yishun.onemoment.app.api.loader;

import android.os.AsyncTask;
import android.support.v4.util.Pair;

import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.api.modelv4.VideoProvider;

/**
 * Created by Jinge on 2015/11/13.
 */
public class VideoTaskManager {
    public static final OkHttpClient httpClient = new OkHttpClient();
    private static final String TAG = "VideoTaskManager";

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;
    private static final BlockingQueue<Runnable> poolQueue =
            new LinkedBlockingQueue<Runnable>(128);
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "VideoTaskManager #" + mCount.getAndIncrement());
        }
    };
    public static final Executor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, poolQueue, sThreadFactory);

    private static VideoTaskManager instance;
    private List<AsyncTask> asyncTaskList = new ArrayList<>();
    private List<Pair<LoaderTask, VideoProvider[]>> taskMap = new ArrayList<>();

    public static VideoTaskManager getInstance() {
        synchronized (VideoTaskManager.class) {
            if (instance == null) {
                instance = new VideoTaskManager();
            }
        }
        return instance;
    }

    public void executeTask(LoaderTask task, VideoProvider... params) {
        asyncTaskList.add(task);
        LogUtil.d(TAG, asyncTaskList.size() + "");

        if (poolQueue.size() >= 96) {
            LogUtil.d(TAG, "pool size over");
//            taskMap.put(task, params);
            taskMap.add(new Pair<>(task, params));
        } else {
            task.executeOnExecutor(executor, params);
        }
    }

    public void removeTask(LoaderTask task) {
        asyncTaskList.remove(task);
        LogUtil.d(TAG, asyncTaskList.size() + "");
        for (int i = 0; i < taskMap.size(); ) {
            if (taskMap.get(i).first == task) {
                taskMap.remove(i);
                break;
            } else i++;
        }
        if (poolQueue.size() < 96) {
            LogUtil.d(TAG, "pool size ok");
            if (taskMap.size() > 0) {
                Pair<LoaderTask, VideoProvider[]> e = taskMap.get(0);
                taskMap.remove(0);
                executeTask(e.first, e.second);
            }
        }
    }

//    public void addTask(AsyncTask task) {
//        asyncTaskList.add(task);
//        Log.d(TAG, asyncTaskList.size() + "");
//        if (asyncTaskList.size() >= 96) {
//            Log.e(TAG, "size error");
//        }
//    }
//
//    public void removeTask(AsyncTask task) {
//        asyncTaskList.remove(task);
//        Log.d(TAG, asyncTaskList.size() + "");
//    }

    public void quit() {
        for (AsyncTask asyncTask : asyncTaskList) {
            asyncTask.cancel(true);
        }
        asyncTaskList.clear();
    }

}
