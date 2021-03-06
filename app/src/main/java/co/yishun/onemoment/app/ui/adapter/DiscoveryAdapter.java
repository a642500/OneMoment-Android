package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.modelv4.World;

/**
 * Created by Jinge on 2016/1/20.
 */
public class DiscoveryAdapter extends AbstractRecyclerViewAdapter<World, DiscoveryAdapter.SimpleViewHolder> {
    private static final String TAG = "DiscoveryAdapter";
    private final String peopleSuffix;

    public DiscoveryAdapter(Context context, OnItemClickListener<World> listener) {
        super(context, listener);
        peopleSuffix = context.getString(R.string.fragment_world_suffix_people_count);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_world_item, parent, false));
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, World item, int position) {
        LogUtil.i(TAG, "on bind: " + item.toString() + ", at " + position);
        holder.numTextView.setVisibility(View.INVISIBLE);
        holder.tagTextView.setVisibility(View.INVISIBLE);

        if (TextUtils.isEmpty(item.thumbnail))
            holder.itemImageView.setImageResource(R.drawable.pic_banner_default);
        else
            Picasso.with(mContext).load(item.thumbnail).placeholder(R.drawable.pic_banner_default).into(holder.itemImageView, new Callback() {
                @Override
                public void onSuccess() {
                    holder.numTextView.setVisibility(View.VISIBLE);
                    holder.tagTextView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError() {

                }
            });
        holder.numTextView.setText(String.format(peopleSuffix, item.videosNum));
        holder.tagTextView.setText(item.name);
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        final ImageView itemImageView;
        final TextView numTextView;
        final TextView tagTextView;
        final TextView likeTextView;

        public SimpleViewHolder(View itemView) {
            super(itemView);
//            ((CardView)itemView).setPreventCornerOverlap(false);
            itemImageView = (ImageView) itemView.findViewById(R.id.itemImageView);
            numTextView = (TextView) itemView.findViewById(R.id.numTextView);
            tagTextView = (TextView) itemView.findViewById(R.id.tagTextView);
            likeTextView = (TextView) itemView.findViewById(R.id.likeTextView);
        }

    }
}
