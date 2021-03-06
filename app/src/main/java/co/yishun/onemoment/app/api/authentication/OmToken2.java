package co.yishun.onemoment.app.api.authentication;

import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.TimeZone;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.config.Constants;
import retrofit.mime.TypedOutput;

/**
 * The first token in Double Token Verification. Created by Carlos on 2015/8/5.
 */
public class OmToken2 implements Token {
    private static final String TAG = "OmToken2";
    final private Token mToken1;
    private final String mValue;
    private final String mOrigin;
    private final String mRaw;
    private final String mKey = Constants.API_KEY;
    private final String mUrl;
    private final String mData;

    OmToken2(Token token1, String url, @Nullable TypedOutput body, long expiredTime) throws IOException {
        mToken1 = token1;
        mRaw = mToken1.origin();
        mUrl = url;

        byte[] data = null;
        if (body != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            body.writeTo(out);
            data = out.toByteArray();
            out.close();
        }
        mData = data == null ? "" : new String(data);
        //        mOrigin = "AC52T575DCV6UPX7K51HZ6J5S1258NZIZ::http://api.yishun.co/v3/account/account/54c7530f7d40b52e24107956::1438940611:Asia/Shanghai";
        mOrigin = Util.joinString("", ":", mRaw, mKey, mUrl, mData, String.valueOf(expiredTime), TimeZone.getDefault().getID());

        byte[] hashcode = Util.sha256(mOrigin);

        mValue = Util.base64(hashcode);

        LogUtil.i(TAG, mValue);
    }


    @Override
    public String value() {
        return mValue;
    }

    @Override
    public String origin() {
        return mOrigin;
    }

    @Override
    public String toString() {
        return "OmToken2{" +
                "mToken1=" + mToken1 +
                ", mValue='" + mValue + '\'' +
                ", mOrigin='" + mOrigin + '\'' +
                ", mRaw='" + mRaw + '\'' +
                ", mKey='" + mKey + '\'' +
                ", mUrl='" + mUrl + '\'' +
                ", mData=" + mData +
                '}';
    }


}