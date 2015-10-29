package co.yishun.onemoment.app.ui.play;

import android.util.Log;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import co.yishun.library.OnemomentPlayerView;
import co.yishun.library.resource.BaseVideoResource;
import co.yishun.library.resource.LocalVideo;
import co.yishun.library.resource.TaggedVideo;
import co.yishun.library.resource.VideoResource;
import co.yishun.library.tag.BaseVideoTag;
import co.yishun.library.tag.VideoTag;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.api.model.Seed;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.VideoUtil;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * Created on 2015/10/28.
 */
@EFragment(R.layout.fragment_play_world)
public class PlayWorldFragment extends BaseFragment implements OnemomentPlayerView.OnIndexChangeListener {
    private World mWorld = OneMomentV3.createAdapter().create(World.class);
    private List<TagVideo> tagVideos = new ArrayList<>();
    private int index;
    @FragmentArg
    WorldTag worldTag;
    @ViewById
    OnemomentPlayerView videoPlayView;
    @ViewById
    TextView voteCountTextView;

    private Seed seed;
    private int offset = 0;

    @Background
    void getData() {
        List<TagVideo> videos = mWorld.getVideoOfTag(worldTag.name, offset, 10, AccountHelper.getUserInfo(this.getActivity())._id, seed);
        if (videos.size() == 0) {
            return;
        }
        offset += videos.size();
        seed = videos.get(0).seed;
        getData();

        for (TagVideo oneVideo : videos) {
            File fileSynced = FileUtil.getWorldVideoStoreFile(this.getActivity(), oneVideo);
            if (fileSynced.exists()) {
                addVideo(oneVideo, fileSynced);
            } else {
                downloadVideo(oneVideo, fileSynced);
            }
        }
    }

    @AfterViews
    void setupView() {
        videoPlayView.setIndexChangeListener(this);
        getData();
    }

    void addVideo(TagVideo tagVideo, File fileSynced) {
        VideoResource videoResource = new LocalVideo(new BaseVideoResource(), fileSynced.getPath());
        List<VideoTag> tags = new LinkedList<VideoTag>();
        for (int i = 0; i < tagVideo.tags.size(); i++) {
            tags.add(new BaseVideoTag(tagVideo.tags.get(i).name, tagVideo.tags.get(i).x / 100f, tagVideo.tags.get(i).y / 100f));
        }
        videoResource = new TaggedVideo(videoResource, tags);
        videoPlayView.addVideoResource(videoResource);
        tagVideos.add(tagVideo);

        try {
            videoPlayView.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Background
    void downloadVideo(TagVideo tagVideo, File fileSynced) {
        try {
            OkHttpClient httpClient = new OkHttpClient();
            Call call = httpClient.newCall(new Request.Builder().url(tagVideo.domain + tagVideo.fileName).get().build());
            Response response = null;
            response = call.execute();

            if (response.code() == 200) {
                InputStream input = null;
                FileOutputStream output = null;
                try {
                    input = response.body().byteStream();
                    output = new FileOutputStream(fileSynced);
                    long fileLength = response.body().contentLength();

                    byte data[] = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        if (fileLength > 0) // only if total length is known
                            output.write(data, 0, count);
                    }
                    addVideo(tagVideo, fileSynced);
                } catch (IOException ignore) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Click(R.id.videoPlayView)
    void videoClick() {
        if (videoPlayView.isPlaying()) {
            videoPlayView.pause();
        } else {
            videoPlayView.start();
        }
    }

    @Override
    @UiThread
    public void indexChangeTo(int index) {
        this.index = index;
        if (tagVideos.get(index).liked) {
            voteCountTextView.setTextAppearance(this.getActivity(), android.R.style.TextAppearance_DeviceDefault_Small_Inverse);
        } else {
            voteCountTextView.setTextAppearance(this.getActivity(), android.R.style.TextAppearance_DeviceDefault_Small);
        }
        voteCountTextView.setText(tagVideos.get(index).likeNum + "");
    }


    @Click(R.id.voteCountTextView)
    @Background
    void voteClick() {
        ApiModel model;
        if (tagVideos.get(index).liked) {
            model = mWorld.unlikeVideo(tagVideos.get(index)._id, AccountHelper.getUserInfo(this.getActivity())._id);
        } else {
            model = mWorld.likeVideo(tagVideos.get(index)._id, AccountHelper.getUserInfo(this.getActivity())._id);
        }
        if (model.code == 1) {
            tagVideos.get(index).liked = !tagVideos.get(index).liked;
            voteSuccess();
        }
    }

    @UiThread
    void voteSuccess() {
        if (tagVideos.get(index).liked) {
            tagVideos.get(index).likeNum++;
            voteCountTextView.setTextAppearance(this.getActivity(), android.R.style.TextAppearance_DeviceDefault_Small_Inverse);
            voteCountTextView.setText(tagVideos.get(index).likeNum + "");
        } else {
            tagVideos.get(index).likeNum--;
            voteCountTextView.setTextAppearance(this.getActivity(), android.R.style.TextAppearance_DeviceDefault_Small);
            voteCountTextView.setText(tagVideos.get(index).likeNum + "");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (videoPlayView != null) {
            videoPlayView.stop();
        }
    }
}
