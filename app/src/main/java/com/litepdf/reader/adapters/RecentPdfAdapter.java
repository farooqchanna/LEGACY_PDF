package com.litepdf.reader.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.litepdf.reader.R;
import com.litepdf.reader.models.RecentFile;
import com.litepdf.reader.utils.ThumbnailCache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for the horizontal Recent Files section.
 */
public class RecentPdfAdapter extends RecyclerView.Adapter<RecentPdfAdapter.ViewHolder> {

    public interface OnRecentClickListener {
        void onRecentClick(RecentFile recentFile);
    }

    private final Context context;
    private final List<RecentFile> items;
    private final ThumbnailCache thumbnailCache;
    private OnRecentClickListener listener;

    public RecentPdfAdapter(Context context, List<RecentFile> items) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<RecentFile>();
        this.thumbnailCache = ThumbnailCache.getInstance(context);
    }

    public void setOnRecentClickListener(OnRecentClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<RecentFile> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pdf_recent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final RecentFile file = items.get(position);

        holder.txtFileName.setText(file.getDisplayName());
        if (file.getLastPage() > 0) {
            holder.txtLastPage.setText(context.getString(R.string.page_indicator_format, file.getDisplayLastPage(), 0).replace(" / 0", ""));
        } else {
            holder.txtLastPage.setText("");
        }

        thumbnailCache.loadThumbnail(new File(file.getFilePath()), holder.imgThumbnail, R.drawable.ic_pdf_placeholder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRecentClick(file);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgThumbnail;
        final TextView txtFileName;
        final TextView txtLastPage;

        public ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            txtFileName = (TextView) itemView.findViewById(R.id.txt_file_name);
            txtLastPage = (TextView) itemView.findViewById(R.id.txt_last_page);
        }
    }
}
