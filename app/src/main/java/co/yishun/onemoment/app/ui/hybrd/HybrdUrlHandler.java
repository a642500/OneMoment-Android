package co.yishun.onemoment.app.ui.hybrd;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.content.Context;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.api.modelv4.ShareInfo;
import co.yishun.onemoment.app.api.modelv4.World;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.ui.BadgeActivity_;
import co.yishun.onemoment.app.ui.CreateWorldActivity_;
import co.yishun.onemoment.app.ui.HomeContainerActivity_;
import co.yishun.onemoment.app.ui.SettingsActivity_;
import co.yishun.onemoment.app.ui.ShareActivity;
import co.yishun.onemoment.app.ui.ShootActivity_;
import co.yishun.onemoment.app.ui.UserInfoActivity_;
import co.yishun.onemoment.app.ui.WorldVideosActivity_;

/**
 * Created by Jinge on 2016/1/23.
 */
public abstract class HybrdUrlHandler {

    public static final String URL_PREFIX = Constants.APP_URL_PREFIX;
    public static final String FUNC_GET_ACCOUNT_ID = "getAccountId";
    public static final String FUNC_GET_ACCOUNT = "getAccount";
    public static final String FUNC_JUMP = "jump";
    public static final String FUNC_LOG = "log";
    public static final String FUNC_ALERT = "alert";
    public static final String FUNC_CANCEL_AlERT = "cancelAlert";
    public static final String FUNC_FINISH = "finish";
    public static final String FUNC_GET_ENV = "getEnv";
    public static final String FUNC_LOAD = "load";
    public static final String FUNC_GET_BASIC_AUTH_HEADER = "getBasicAuthHeader";
    public static final String FUNC_GET_DIARY = "getDiary";

    private static final String TAG = "HybrdUrlHandler";

    public static String urlDecode(String raw) {
        String result;
        try {
            result = URLDecoder.decode(raw, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            result = "decoder error";
            LogUtil.e(TAG, "decoder error : " + raw);
        }
        return result;
    }

    public static List<String> urlDecode(List<String> rawStrings) {
        List<String> results = new ArrayList<>();
        for (String s : rawStrings) results.add(urlDecode(s));
        return results;
    }

    protected abstract boolean handleInnerUrl(UrlModel urlModel);

    public boolean handleUrl(Context activity, String url) {
        return handleUrl(activity, url, 0, 0);
    }

    /**
     * This can be used in hybrd Activities and fragment, and also can be used with other kind of
     * url, such as when click a banner, jump to a certain world.
     *
     * @param url  : use the same format as hybrd.
     * @param posX : the X position for transition. For example, when you want start {@link
     *             ShootActivity_}, you need to specify {@param posX} and {@param posY}, or the
     *             {@link ShootActivity_} with start from the center of the screen.
     * @return True, if we handle the url.
     */
    public boolean handleUrl(Context context, String url, int posX, int posY) {
        if (url.startsWith(URL_PREFIX)) {
            String json = url.substring(URL_PREFIX.length());
            UrlModel urlModel = new UrlModel();
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
            urlModel.call = jsonObject.get("call").getAsString();
            JsonArray jsonArray = jsonObject.getAsJsonArray("args");
            urlModel.args = new ArrayList<>();
            for (JsonElement element : jsonArray) {
                String elementStr;
                elementStr = element.toString();
                //noinspection ResultOfMethodCallIgnored
                elementStr = elementStr.replace("\\n", "\n");// Gson escape every splash, so restore newline

                if (elementStr.startsWith("\"") && elementStr.endsWith("\""))
                    elementStr = elementStr.substring(1, elementStr.length() - 1);
                urlModel.args.add(elementStr);
            }

            if (TextUtils.equals(urlModel.call, FUNC_JUMP)) {
                return webJumpWithPosition(context, urlModel.args, posX, posY);
            }
            return handleInnerUrl(urlModel);
        }
        return false;
    }

    private boolean webJumpWithPosition(Context context, List<String> args, int posX, int posY) {
        String des = args.get(0);
        if (TextUtils.equals(des, "web")) {
            CommonWebActivity_.intent(context).title(args.get(1)).url(args.get(2)).start();
        } else if (TextUtils.equals(des, "camera")) {
//   don't do here, do in ShootActivity         BaseWebFragment.invalidateWeb();
            World world = new World();
            world.setId(args.get(2));
            world.setName(args.get(1));
            ShootActivity_.intent(context).world(world).forWorld(true).transitionX(posX).transitionY
                    (posY).start();
            //TODO handle permission
        } else if (TextUtils.equals(des, "setting")) {
            SettingsActivity_.intent(context).start();
        } else if (TextUtils.equals(des, "create_world")) {
            BaseWebFragment.invalidateWeb();
            CreateWorldActivity_.intent(context).start();
        } else if (TextUtils.equals(des, "edit")) {
            UserInfoActivity_.intent(context).start();
        } else if (TextUtils.equals(des, "world_square")) {
            World world = new World();
            world.name = args.get(1);
            world.videosNum = Integer.parseInt(args.get(2));
            world._id = args.get(3);
            world.thumbnail = args.get(4);
            WorldVideosActivity_.intent(context).world(world).forWorld(true).start();
        } else if (TextUtils.equals(des, "badge")) {
            BadgeActivity_.intent(context).badgeDetail(args.get(1)).start();
        } else if (TextUtils.equals(des, "world")
                || TextUtils.equals(des, "diary")
                || TextUtils.equals(des, "explore")
                || TextUtils.equals(des, "mine")) {
            HomeContainerActivity_.intent(context).type(des).start();
        } else if (TextUtils.equals(des, "share")) {
            String type = args.get(1);
            // ignore type
            LogUtil.e(TAG, "share type: " + type);

            ShareInfo shareInfo = new ShareInfo();
            shareInfo.title = args.get(2);
            shareInfo.imageUrl = args.get(3);
            shareInfo.link = args.get(4);
            ShareActivity.showShareChooseDialog(context, shareInfo, 0);
        } else {
            LogUtil.e(TAG, "unhandled jump type : " + des);
        }
        return true;
    }

    public static class JumpUrlHandler extends HybrdUrlHandler {

        @Override
        protected boolean handleInnerUrl(UrlModel urlModel) {
            LogUtil.i(TAG, "unknown call type");
            return false;
        }
    }

    class UrlModel {
        String call;
        List<String> args;
    }

}
