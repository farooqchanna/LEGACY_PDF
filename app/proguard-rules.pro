# Add project specific ProGuard rules here.

# Keep PDF viewer classes
-keep class com.github.barteksc.pdfviewer.** { *; }
-keep class com.shockwave.pdfium.** { *; }

# Keep our model classes (used with SQLite)
-keep class com.litepdf.reader.models.** { *; }

# Keep support library
-keep class android.support.** { *; }
-dontwarn android.support.**
