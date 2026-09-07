# ── WakeSync ProGuard/R8 Rules ─────────────────────────────────────────
# These rules ensure Firebase, Compose, KMP, and Kotlin coroutines
# work correctly after code shrinking and obfuscation.

# ── Firebase ───────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-keep class com.social.wakesync.FirebaseConfig { *; }

# Firebase Auth - keep user model classes
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.firestore.** { *; }

# ── Kotlin Coroutines ──────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ── Kotlin Serialization (if used) ─────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ── Kotlinx DateTime ───────────────────────────────────────────────────
-keep class kotlinx.datetime.** { *; }
-dontwarn kotlinx.datetime.**

# ── Jetpack Compose ────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ── Compose Material3 ─────────────────────────────────────────────────
-keep class androidx.compose.material3.** { *; }

# ── AndroidX Lifecycle ─────────────────────────────────────────────────
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ── AndroidX Credentials (Google Sign-In) ──────────────────────────────
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**

# ── Ktor (HTTP Client) ────────────────────────────────────────────────
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ── Kamel (Image Loading) ─────────────────────────────────────────────
-keep class io.kamel.** { *; }
-dontwarn io.kamel.**

# ── App Data Classes ───────────────────────────────────────────────────
# Keep all data classes used in Firestore serialization/deserialization
-keep class com.social.wakesync.feature.home.AlarmData { *; }
-keep class com.social.wakesync.feature.home.Habit { *; }
-keep class com.social.wakesync.feature.home.Friend { *; }
-keep class com.social.wakesync.feature.home.SoundMetadata { *; }
-keep class com.social.wakesync.feature.home.LeaderboardUser { *; }
-keep class com.social.wakesync.feature.home.GroupMember { *; }
-keep class com.social.wakesync.feature.home.HomeStats { *; }
-keep class com.social.wakesync.feature.home.UserProfile { *; }
-keep class com.social.wakesync.feature.home.HomeUiState { *; }
-keep class com.social.wakesync.feature.home.MainUiState { *; }
-keep class com.social.wakesync.feature.home.FeedItem { *; }
-keep class com.social.wakesync.feature.home.StoryItem { *; }
-keep class com.social.wakesync.feature.home.ChatItem { *; }
-keep class com.social.wakesync.feature.home.MessageItem { *; }
-keep class com.social.wakesync.feature.home.NotificationItem { *; }
-keep class com.social.wakesync.feature.home.NotificationCategory { *; }
-keep class com.social.wakesync.feature.home.AchievementItem { *; }
-keep class com.social.wakesync.feature.home.GroupMemberSetting { *; }
-keep class com.social.wakesync.feature.home.ProofVerifier { *; }
-keep class com.social.wakesync.feature.home.RivalItem { *; }
-keep class com.social.wakesync.feature.home.PremiumFeatureItem { *; }
-keep class com.social.wakesync.feature.home.ProfileDetails { *; }

# ── Enum Safety ────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Parcelable ─────────────────────────────────────────────────────────
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ── R class ────────────────────────────────────────────────────────────
-keepclassmembers class **.R$* {
    public static <fields>;
}

# ── Remove logging in release ──────────────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}

# ── Keep line numbers for crash reports ────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
