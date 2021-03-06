package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collection;

import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2015/11/2.
 */
public class TagSearchAdapter extends AbstractRecyclerViewAdapter<String, TagSearchAdapter.TagSearchViewHolder> {
    private int fixedSize;

    public TagSearchAdapter(Context context, OnItemClickListener<String> listener) {
        super(context, listener);
    }

    public void replaceItem(int position, String item) {
        if (position >= mItems.size() || TextUtils.equals(mItems.get(position), item)) {
            return;
        }
        mItems.remove(position);
        mItems.add(position, item);
        notifyItemChanged(position);
    }

    public boolean addFixedItems(Collection<? extends String> collection) {
        if (fixedSize > 0) {
            mItems.clear();
        }
        boolean re = mItems.addAll(collection);
        fixedSize = collection.size();
        notifyDataSetChanged();
        return re;
    }

    @Override
    public boolean addAll(Collection<? extends String> collection) {
        if (fixedSize > 0) {
            mItems.subList(fixedSize, mItems.size()).clear();
        } else {
            mItems.clear();
        }
        boolean re = mItems.addAll(collection);
        notifyDataSetChanged();
        return re;
    }

    @Override
    public void onBindViewHolder(TagSearchViewHolder holder, String item, int position) {
        holder.itemTextView.setText(item);
    }

    @Override
    public TagSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TagSearchViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_item_tag_search, parent, false));
    }

    public class TagSearchViewHolder extends RecyclerView.ViewHolder {
        final TextView itemTextView;

        public TagSearchViewHolder(View itemView) {
            super(itemView);
            itemTextView = (TextView) itemView.findViewById(R.id.itemTextView);
        }
    }
}
