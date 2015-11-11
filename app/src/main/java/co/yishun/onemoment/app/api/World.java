package co.yishun.onemoment.app.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import java.util.List;

import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.api.model.Banner;
import co.yishun.onemoment.app.api.model.Seed;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.api.model.VideoTag;
import co.yishun.onemoment.app.api.model.WorldTag;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Carlos on 2015/8/8.
 */
public interface World {
    @GET("/world/banners")
    List<Banner> getBanners(@Query("limit") int bannerNumLimit);

    @FormUrlEncoded
    @POST("/world/like/video/{video_id}")
    ApiModel likeVideo(@Path("video_id") @NonNull String videoId, @Field("account_id") @NonNull String userId);

    @FormUrlEncoded
    @POST("/world/unlike/video/{video_id}")
    ApiModel unlikeVideo(@Path("video_id") @NonNull String videoId, @Field("account_id") @NonNull String userId);

    @GET("/world/tags")
    List<WorldTag> getWorldTagList(@Query("limit") int limit, @Query("ranking") @Nullable String ranking, @Query("sort") @Nullable
    @Sort String sort);

    @GET("/world/videos/liked")
    List<TagVideo> getLikedVideos(@Query("account_id") @NonNull String userId, @Query("offset") int offset, @Query("limit") int limit);

    @GET("/world/tags/joined")
    List<WorldTag> getJoinedWorldTags(@Query("account_id") @NonNull String userId, @Query("type") @NonNull @WorldTag.Type String type, @Query("offset") int offset, @Query("limit") int limit);

    @GET("/world/videos")
    List<TagVideo> getVideoOfTag(@Query("tag_name") @NonNull String tagName, @Query("offset") int offset, @Query("limit") int limit
            , @Query("account_id") @Nullable String userId, @Query("seed") @Nullable Seed seed);

    @GET("/world/private_videos")
    List<TagVideo> getPrivateVideoOfTag(@Query("tag_name") @NonNull String tagName, @Query("offset") int offset, @Query("limit") int limit
            , @Query("account_id") @Nullable String userId);

    @GET("/world/tag/suggest")
    List<WorldTag> getSuggestedTagName(@Query("words") @NonNull String tagName);

    @POST("/world/video")
    @FormUrlEncoded
    Video addVideoToWorld(@Field("account_id") @NonNull String userId, @Field("type") @NonNull @Video.Type String type,
                          @Field("filename") @NonNull String fileName, @Field("tags") @NonNull String tags
    );

    @StringDef({"recommend", "time"})
    @interface Sort {
    }

    class Util {
        public static String getTagsJson(@NonNull List<VideoTag> tags) {
            return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(tags);
        }
    }
}


