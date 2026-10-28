package com.litepdf.reader.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thumbnail manager for PDF files.
 * Provides two-level caching (Memory LRU Cache + Disk Cache) and async generation
 * using native PdfiumCore with RGB_565 configuration for low-memory Android 4.x devices.
 */
public class ThumbnailCache {

    private static final int THUMB_WIDTH = 150;
    private static final int THUMB_HEIGHT = 200;

    private static ThumbnailCache sInstance;

    private final Context context;
    private final LruCache<String, Bitmap> memoryCache;
    private final File diskCacheDir;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface ThumbnailCallback {
        void onThumbnailLoaded(Bitmap bitmap);
        void onError();
    }

    public static synchronized ThumbnailCache getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ThumbnailCache(context.getApplicationContext());
        }
        return sInstance;
    }

    private ThumbnailCache(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newFixedThreadPool(2);

        // Memory cache size: 1/8th of available memory
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = Math.max(1024, maxMemory / 8);

        this.memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // Return size in kilobytes
                return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
            }
        };

        // Disk cache directory: /cache/thumbnails
        this.diskCacheDir = new File(context.getCacheDir(), "thumbnails");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
    }

    /**
     * Loads a thumbnail for the specified PDF file into the ImageView asynchronously.
     */
    public void loadThumbnail(final File pdfFile, final ImageView imageView, final int placeholderResId) {
        if (pdfFile == null || !pdfFile.exists()) {
            if (imageView != null && placeholderResId != 0) {
                imageView.setImageResource(placeholderResId);
            }
            return;
        }

        final String cacheKey = getCacheKey(pdfFile);

        // 1. Check Memory Cache
        Bitmap memBitmap = memoryCache.get(cacheKey);
        if (memBitmap != null && !memBitmap.isRecycled()) {
            if (imageView != null) {
                imageView.setImageBitmap(memBitmap);
            }
            return;
        }

        // Set placeholder while loading
        if (imageView != null && placeholderResId != 0) {
            imageView.setImageResource(placeholderResId);
            imageView.setTag(cacheKey);
        }

        final WeakReference<ImageView> imageViewRef = new WeakReference<ImageView>(imageView);

        // 2. Load asynchronously from disk or generate
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = getOrGenerateBitmap(pdfFile, cacheKey);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ImageView iv = imageViewRef.get();
                        if (iv != null && bitmap != null && !bitmap.isRecycled()) {
                            Object tag = iv.getTag();
                            if (tag != null && tag.equals(cacheKey)) {
                                iv.setImageBitmap(bitmap);
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Retrieves thumbnail from disk or generates a new one.
     */
    private Bitmap getOrGenerateBitmap(File pdfFile, String cacheKey) {
        // Check disk cache first
        File diskFile = new File(diskCacheDir, cacheKey + ".png");
        if (diskFile.exists() && diskFile.lastModified() >= pdfFile.lastModified()) {
            Bitmap diskBitmap = BitmapFactory.decodeFile(diskFile.getAbsolutePath());
            if (diskBitmap != null) {
                memoryCache.put(cacheKey, diskBitmap);
                return diskBitmap;
            }
        }

        // Generate using PdfiumCore
        Bitmap generated = renderFirstPage(pdfFile);
        if (generated != null) {
            // Put into memory cache
            memoryCache.put(cacheKey, generated);

            // Save to disk cache
            saveBitmapToDisk(generated, diskFile);
            return generated;
        }

        return null;
    }

    /**
     * Renders page 0 of the PDF using native PdfiumCore.
     */
    private Bitmap renderFirstPage(File pdfFile) {
        ParcelFileDescriptor fd = null;
        PdfiumCore pdfiumCore = null;
        PdfDocument pdfDoc = null;

        try {
            fd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfiumCore = new PdfiumCore(context);
            pdfDoc = pdfiumCore.newDocument(fd);

            int pageCount = pdfiumCore.getPageCount(pdfDoc);
            if (pageCount <= 0) {
                return null;
            }

            pdfiumCore.openPage(pdfDoc, 0);

            int docWidth = pdfiumCore.getPageWidthPoint(pdfDoc, 0);
            int docHeight = pdfiumCore.getPageHeightPoint(pdfDoc, 0);

            // Calculate scaled dimensions maintaining aspect ratio
            int targetWidth = THUMB_WIDTH;
            int targetHeight = THUMB_HEIGHT;

            if (docWidth > 0 && docHeight > 0) {
                float aspect = (float) docWidth / (float) docHeight;
                if (aspect > 1.0f) {
                    targetHeight = (int) (targetWidth / aspect);
                } else {
                    targetWidth = (int) (targetHeight * aspect);
                }
            }

            targetWidth = Math.max(1, targetWidth);
            targetHeight = Math.max(1, targetHeight);

            // RGB_565 uses 2 bytes per pixel (half of ARGB_8888) to save RAM on Android 4.x
            Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565);
            pdfiumCore.renderPageBitmap(pdfDoc, bitmap, 0, 0, 0, targetWidth, targetHeight);

            return bitmap;
        } catch (Throwable e) {
            return null;
        } finally {
            try {
                if (pdfiumCore != null && pdfDoc != null) {
                    pdfiumCore.closeDocument(pdfDoc);
                }
            } catch (Exception ignored) {
            }
            try {
                if (fd != null) {
                    fd.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void saveBitmapToDisk(Bitmap bitmap, File diskFile) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(diskFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fos);
            fos.flush();
        } catch (IOException ignored) {
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Generates a stable MD5 cache key from file path and last modified time.
     */
    private String getCacheKey(File file) {
        String input = file.getAbsolutePath() + "_" + file.lastModified();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }

    /**
     * Clears both memory cache and disk cache.
     */
    public void clearCache() {
        memoryCache.evictAll();
        File[] files = diskCacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }
}
