package com.litepdf.reader.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.litepdf.reader.R;
import com.litepdf.reader.models.PdfFile;
import com.litepdf.reader.utils.FileUtils;
import com.litepdf.reader.utils.ThumbnailCache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying PDF files in a Grid layout.
 */
public class PdfGridAdapter extends RecyclerView.Adapter<PdfGridAdapter.ViewHolder> {

    public interface OnPdfClickListener {
        void onPdfClick(PdfFile pdfFile);
        void onPdfLongClick(PdfFile pdfFile);
    }

    private final Context context;
    private final List<PdfFile> items;
    private final ThumbnailCache thumbnailCache;
    private OnPdfClickListener listener;

    public PdfGridAdapter(Context context, List<PdfFile> items) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<PdfFile>();
        this.thumbnailCache = ThumbnailCache.getInstance(context);
    }

    public void setOnPdfClickListener(OnPdfClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<PdfFile> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pdf_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final PdfFile pdf = items.get(position);

        holder.txtFileName.setText(pdf.getDisplayName());
        holder.txtFileSize.setText(FileUtils.formatFileSize(pdf.getSize()));

        // Load thumbnail lazily
        thumbnailCache.loadThumbnail(new File(pdf.getPath()), holder.imgThumbnail, R.drawable.ic_pdf_placeholder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onPdfClick(pdf);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onPdfLongClick(pdf);
                    return true;
                }
                return false;
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
        final TextView txtFileSize;

        public ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            txtFileName = (TextView) itemView.findViewById(R.id.txt_file_name);
            txtFileSize = (TextView) itemView.findViewById(R.id.txt_file_size);
        }
    }
}
