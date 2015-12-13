package co.yishun.onemoment.app.data;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.model.ApiMoment;
import co.yishun.onemoment.app.api.model.QiniuKeyProvider;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.config.Constants;

import static co.yishun.onemoment.app.config.Constants.URL_HYPHEN;

/**
 * Util to create storing paths of videos, images and read information from those file name.
 * <p>
 * You should always give a {@link QiniuKeyProvider} to provide a standard naming part for images and videos.
 * <p>
 * Created by Carlos on 2015/8/21.
 */
public class FileUtil {
    public static final String MOMENT_STORE_DIR = "moment";
    public static final String WORLD_STORE_DIR = "world";
    public static final String THUMB_STORE_DIR = "thumb";

    private static final String TAG = "FileUtil";

    /**
     * Retrieve the corresponding thumbnail path of an {@link QiniuKeyProvider}
     *
     * @param context  to retrieve application private store space.
     * @param provider to provide standard naming part.
     * @param type     of the thumbnail
     * @return path of the thumbnail, it may not exist.
     */
    public static File getThumbnailStoreFile(Context context, QiniuKeyProvider provider, Type type) {
        String dir = THUMB_STORE_DIR;
        final String key = provider.getKey();
//        final String name = key.substring(0, key.lastIndexOf('.'));
        return new File(getMediaStoreDir(context, dir), type.getPrefix(context) + key + type.getSuffix());
    }

    /**
     * Retrieve the video file path of an {@link Video}
     *
     * @param context to retrieve application private store space.
     * @param video   to provide standard naming part.
     * @return path of the Video, it may not exist.
     */
    public static File getWorldVideoStoreFile(Context context, Video video) {
        return new File(getMediaStoreDir(context, WORLD_STORE_DIR), video.getKey());
    }

    /**
     * Retrieve the moment file path.
     *
     * @return file of the moment shot at this time.
     */
    public static File getMomentStoreFile(Context context) {
        File mediaStorageDir = getMediaStoreDir(context, MOMENT_STORE_DIR);
        return getMediaStoreFile(context, mediaStorageDir, Type.SYNCED, null);
    }

    /**
     * Retrieve the moment file path by unix timestamp.
     *
     * @param unixTimeStamp when the moment is shot. Null if use now.
     * @return file of the moment shot at sometime.
     */
    public static File getMomentStoreFile(Context context, long unixTimeStamp) {
        File mediaStorageDir = getMediaStoreDir(context, MOMENT_STORE_DIR);
        return getMediaStoreFile(context, mediaStorageDir, Type.SYNCED, unixTimeStamp);
    }

    /**
     * Retrieve the moment file path by {@link ApiMoment}.
     */
    public static File getMomentStoreFile(Context context, @NonNull ApiMoment apiMoment) {
        File mediaStorageDir = getMediaStoreDir(context, MOMENT_STORE_DIR);
        return getMediaStoreFile(context, mediaStorageDir, Type.SYNCED, apiMoment.getUnixTimeStamp());
    }

    private static File getMediaStoreFile(Context context, File dir, Type type, @Nullable Long unixTimeStamp) {
        if (unixTimeStamp == null) {
            unixTimeStamp = new Date().getTime() / 1000;
        }
        Log.i(TAG, "timestamp: " + unixTimeStamp);
        String time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(unixTimeStamp * 1000);
        Log.i(TAG, "formatted time: " + time);
        return new File(dir, type.getPrefix(context) + time + URL_HYPHEN + unixTimeStamp + type.getSuffix());
    }

    private static File getMediaStoreDir(Context context, @MediaDir String dirType) {
        return context.getDir(dirType, Context.MODE_PRIVATE);
    }

    /**
     * Retrieve unix timestamp from file.
     *
     * @param file to retrieve.
     * @return unix timestamp in filename.
     */
    public static long parseTimeStamp(File file) {
        return parseTimeStamp(file.getPath());
    }

    /**
     * Retrieve unix timestamp from string.
     *
     * @param pathOrFileName to retrieve.
     * @return unix timestamp in filename.
     */
    public static long parseTimeStamp(String pathOrFileName) {
        return Long.parseLong(pathOrFileName.substring(pathOrFileName.lastIndexOf(URL_HYPHEN) + 1, pathOrFileName.lastIndexOf(".")));
    }

    /**
     * return media type by file path.
     * <p>
     * You must ensure the file is one of {@link Type}. Otherwise it may cause wrong result.
     *
     * @param filePath of the media file
     * @return type of the media file
     */
    public static Type whatTypeOf(String filePath) {
        File file = new File(filePath);
        Type type = null;
        if (file.getName().length() > 3) {
            String prefix = file.getName().substring(0, 3);
            switch (prefix) {
                case "LOC":
                    type = Type.LOCAL;
                    break;
                case "VID":
                    type = Type.RECORDED;
                    break;
                case "LAT":
                    type = Type.LARGE_THUMB;
                    break;
                case "MIT":
                    type = Type.MICRO_THUMB;
                    break;
                default:
                    type = Type.SYNCED;
                    break;
            }
        }
        return type;
    }
    
    public static File getCacheFile(Context context, String fileName) {
        return new File(getCacheDirectory(context, false), fileName);
    }

    public static File getVideoCacheFile(Context context) {
        return new File(getCacheDirectory(context, false), "video-" + System.currentTimeMillis() + ".mp4");
    }

    private static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;

        if (preferExternal && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            appCacheDir = getExternalDirectory(context);
        }

        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }

        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            Log.d(FileUtil.class.getName(), "Can't define system cache directory! use " + cacheDirPath);
            appCacheDir = new File(cacheDirPath);
        }

        return appCacheDir;
    }

    private static File getExternalDirectory(Context context) {

        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null && !cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                Log.d(FileUtil.class.getName(), "无法创建SDCard cache");
                return null;
            }

            //try {
            //    new File(cacheDir, ".nomedia").createNewFile();
            //} catch (IOException e) {
            //    Log.d(FileUtil.class.getName(), "无法创建 .nomedia 文件");
            //}
        }

        return cacheDir;
    }

    public static File getExportVideoFile() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/" +
                Environment.DIRECTORY_DCIM + "/yishun");
        if (!dir.exists()) dir.mkdir();
        return new File(dir.getPath(), Constants.EXPORT_VIDEO_PREFIX + Util.unixTimeStamp()+Constants.VIDEO_FILE_SUFFIX);
    }

    /**
     * Copy file from {@code src} to {@code dst}.
     * {@link File#renameTo(File)} cannot move file from internal storage to external storage.
     * This method won't delete the {@code src} file.
     * If you want to move instead of copy, you should delete the {@code src} file.
     *
     * @return true, if the copy success; false, otherwise.
     */
    public static boolean copyFile(File src, File dst) {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inChannel != null)
                    inChannel.close();
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public enum Type {
        SYNCED {
            @Override
            public String getPrefix(Context context) {
                return AccountManager.getAccountId(context) + URL_HYPHEN;
            }

            @Override
            public String getSuffix() {
                return Constants.VIDEO_FILE_SUFFIX;
            }
        },
        RECORDED {
            @Override
            public String getPrefix(Context context) {
                return "VID" + URL_HYPHEN;
            }

            @Override
            public String getSuffix() {
                return Constants.VIDEO_FILE_SUFFIX;
            }
        },

        /**
         * @deprecated User is always login, prefix video name with user id.
         */
        @Deprecated
        LOCAL {
            @Override
            public String getPrefix(Context context) {
                return "LOC" + URL_HYPHEN;
            }

            @Override
            public String getSuffix() {
                return Constants.VIDEO_FILE_SUFFIX;
            }
        },
        LARGE_THUMB {
            @Override
            public String getPrefix(Context context) {
                return "LAT" + URL_HYPHEN;
            }

            @Override
            public String getSuffix() {
                return Constants.THUMB_FILE_SUFFIX;
            }
        },
        MICRO_THUMB {
            @Override
            public String getPrefix(Context context) {
                return "MIT" + URL_HYPHEN;
            }

            @Override
            public String getSuffix() {
                return Constants.THUMB_FILE_SUFFIX;
            }
        };

        public abstract String getPrefix(Context context);

        public abstract String getSuffix();
    }

    @StringDef({MOMENT_STORE_DIR, WORLD_STORE_DIR, THUMB_STORE_DIR})
    public @interface MediaDir {
    }
}
