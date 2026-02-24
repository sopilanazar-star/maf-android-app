# 1. Твої правила для WebView (щоб не було помилок хрому)
-dontwarn org.chromium.**
-keep class android.webkit.** { *; }
-keep class androidx.webkit.** { *; }

# 2. Правила для моделей даних (щоб JSON завантажувався коректно)
-keep class ua.lviv.maf.TournamentRow { *; }
-keep class ua.lviv.maf.NewsModel { *; }
-keep class ua.lviv.maf.DateModel { *; }
-keep class ua.lviv.maf.AppConfig { *; }

# 3. Правила для бібліотек (OkHttp, Glide, Retrofit)
-keepattributes Signature, InnerClasses, EnclosingMethod
-keep class com.squareup.okhttp3.** { *; }
-keep class com.github.bumptech.glide.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
