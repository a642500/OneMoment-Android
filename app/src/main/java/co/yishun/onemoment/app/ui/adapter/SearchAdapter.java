package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collection;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;

/**
 * Created on 2015/10/19.
 */
public class SearchAdapter
        extends AbstractRecyclerViewAdapter<WorldTag, SearchAdapter.SimpleViewHolder> {
    private static final String TAG = "SearchAdapter";
    private final String PeopleSuffix;
    private final Drawable[] mTagDrawable;

    public SearchAdapter(Context context, OnItemClickListener<WorldTag> listener) {
        super(context, listener);
        Resources resource = context.getResources();
        mTagDrawable = new Drawable[]{
                resource.getDrawable(R.drawable.ic_me_tag_time),
                resource.getDrawable(R.drawable.ic_me_tag_location),
                resource.getDrawable(R.drawable.ic_me_tag_msg)
        };
        PeopleSuffix = context.getString(R.string.fragment_world_suffix_people_count);
    }

    @Override
    public boolean addAll(Collection<? extends WorldTag> collection) {
        mItems.clear();
        boolean re = mItems.addAll(collection);
        notifyDataSetChanged();
        return re;
    }

    @Override
    public void onBindViewHolder(SearchAdapter.SimpleViewHolder holder, WorldTag item, int position) {
        Picasso.with(mContext).load(item.domain + item.thumbnail).placeholder(R.drawable.pic_world_default).into(holder.itemImageView);
        holder.numTextView.setText(String.format(PeopleSuffix, item.videosCount));
        holder.tagTextView.setText(item.name);
        holder.likeTextView.setText(String.valueOf(item.likeCount));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = getDrawableByType(item.type);
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, mContext.getResources().getColor(R.color.colorAccent));
            holder.tagTextView.setCompoundDrawablesWithIntrinsicBounds(getDrawableByType(item.type), null, null, null);

            drawable = holder.likeTextView.getCompoundDrawables()[0];
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, mContext.getResources().getColor(R.color.textColorPrimary));
            holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        } else
            holder.tagTextView.setCompoundDrawablesWithIntrinsicBounds(getDrawableByType(item.type), null, null, null);
    }

    private Drawable getDrawableByType(String type) {
        switch (type) {
            case "time":
                return mTagDrawable[0];
            case "location":
                return mTagDrawable[1];
            default:
                return mTagDrawable[2];
        }
    }

    @Override
    public SearchAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_item_search, parent, false));
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        TextView numTextView;
        TextView tagTextView;
        TextView likeTextView;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            itemImageView = (ImageView) itemView.findViewById(R.id.itemImageView);
            numTextView = (TextView) itemView.findViewById(R.id.numTextView);
            tagTextView = (TextView) itemView.findViewById(R.id.tagTextView);
            likeTextView = (TextView) itemView.findViewById(R.id.likeTextView);
        }
    }
}
