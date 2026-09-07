import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// Initialize Firebase Admin
admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

// ── Notification Sound / Channel Mapping ───────────────────────────

const ANDROID_CHANNELS: Record<string, string> = {
  alarm_win: "wakesync_alarms",
  alarm_loss: "wakesync_alarms",
  punishment_assigned: "wakesync_punishments",
  proof_submitted: "wakesync_punishments",
  punishment_completed: "wakesync_punishments",
  streak_milestone: "wakesync_social",
  friend_request: "wakesync_social",
  group_invite: "wakesync_social",
  feed_reaction: "wakesync_social",
};

const IOS_SOUNDS: Record<string, string> = {
  alarm_win: "alarm_win.caf",
  alarm_loss: "alarm_loss.caf",
  punishment_assigned: "punishment.caf",
  proof_submitted: "punishment.caf",
  punishment_completed: "punishment.caf",
  streak_milestone: "streak_milestone.caf",
  friend_request: "social.caf",
  group_invite: "social.caf",
  feed_reaction: "social.caf",
};

function getAndroidChannel(type: string): string {
  return ANDROID_CHANNELS[type] || "wakesync_push";
}

function getIOSSound(type: string): string | { critical: number; name: string; volume: number } {
  const soundName = IOS_SOUNDS[type];
  if (soundName) {
    return soundName;
  }
  return "default";
}

/**
 * Trigger: When a new notification is created in users/{uid}/notifications/{notifId}
 * Sends a push notification to the user's device(s)
 */
export const onNotificationCreated = functions.firestore
  .document("users/{userId}/notifications/{notificationId}")
  .onCreate(async (snap, context) => {
    const userId = context.params.userId;
    const notificationData = snap.data();

    const type = notificationData.type || "";
    const message = notificationData.message || "";
    const fromUsername = notificationData.fromUsername || "";

    // Get user's FCM token
    const userDoc = await db.collection("users").doc(userId).get();
    const fcmToken = userDoc.data()?.fcmToken;

    if (!fcmToken) {
      functions.logger.info(`No FCM token for user ${userId}, skipping push`);
      return;
    }

    // Build notification based on type
    let title = "WakeSync";
    let body = message;

    switch (type) {
      case "punishment_assigned":
        title = "🏆 Punishment Assigned";
        body = message || `${fromUsername} assigned you a punishment!`;
        break;
      case "alarm_win":
        title = "⏰ Partner woke up!";
        body = message || `${fromUsername} woke up on time!`;
        break;
      case "alarm_loss":
        title = "😴 Partner missed alarm";
        body = message || `${fromUsername} missed the alarm!`;
        break;
      case "proof_submitted":
        title = "📸 Proof Submitted";
        body = message || `${fromUsername} submitted proof for their punishment`;
        break;
      case "punishment_completed":
        title = "✅ Punishment Complete";
        body = message || `${fromUsername} completed their punishment`;
        break;
      case "streak_milestone":
        title = "🔥 Streak Milestone!";
        body = message || "You hit a new streak milestone!";
        break;
      case "friend_request":
        title = "👋 New Friend";
        body = message || `${fromUsername} wants to be your rival!`;
        break;
      default:
        title = "WakeSync";
        body = message || "You have a new notification";
    }

    // Resolve Android channel + iOS sound from notification type
    const androidChannel = getAndroidChannel(type);
    const iosSound = getIOSSound(type);

    // Send push notification
    const payload: admin.messaging.Message = {
      token: fcmToken,
      notification: {
        title,
        body,
      },
      data: {
        type,
        notificationId: context.params.notificationId,
        fromUsername,
        ...(notificationData.punishmentId
          ? { punishmentId: notificationData.punishmentId }
          : {}),
      },
      android: {
        priority: "high",
        notification: {
          channelId: androidChannel,
          clickAction: "OPEN_ACTIVITY",
        },
      },
      apns: {
        payload: {
          aps: {
            alert: {
              title,
              body,
            },
            sound: iosSound,
            badge: 1,
          },
        },
      },
    };

    try {
      await messaging.send(payload);
      functions.logger.info(
        `Push sent to ${userId} for ${type} notification`
      );
    } catch (error: any) {
      // Token might be stale — remove it
      if (
        error.code === "messaging/registration-token-not-registered" ||
        error.code === "messaging/invalid-registration-token"
      ) {
        functions.logger.info(
          `Removing stale token for user ${userId}`
        );
        await db.collection("users").doc(userId).update({
          fcmToken: admin.firestore.FieldValue.delete(),
        });
      } else {
        functions.logger.error(
          `Failed to send push to ${userId}:`,
          error
        );
      }
    }
  });

/**
 * Trigger: When a document is added to push_queue collection
 * Processes queued push notifications (backup mechanism)
 */
export const onPushQueued = functions.firestore
  .document("push_queue/{pushId}")
  .onCreate(async (snap, context) => {
    const data = snap.data();

    const fcmToken = data.fcmToken;
    const title = data.title || "WakeSync";
    const body = data.body || "";
    const type = data.type || "";
    const pushData = data.data || {};

    if (!fcmToken) {
      functions.logger.info("No FCM token in push queue document, skipping");
      await snap.ref.update({ processed: true, error: "No FCM token" });
      return;
    }

    const androidChannel = getAndroidChannel(type);
    const iosSound = getIOSSound(type);

    const payload: admin.messaging.Message = {
      token: fcmToken,
      notification: {
        title,
        body,
      },
      data: {
        type,
        ...pushData,
      },
      android: {
        priority: "high",
        notification: {
          channelId: androidChannel,
          clickAction: "OPEN_ACTIVITY",
        },
      },
      apns: {
        payload: {
          aps: {
            alert: {
              title,
              body,
            },
            sound: iosSound,
            badge: 1,
          },
        },
      },
    };

    try {
      await messaging.send(payload);
      await snap.ref.update({
        processed: true,
        processedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
      functions.logger.info(`Queued push sent for ${type}`);
    } catch (error: any) {
      await snap.ref.update({
        processed: true,
        error: error.message || "Unknown error",
      });
      functions.logger.error("Failed to send queued push:", error);
    }
  });

/**
 * Scheduled function: Clean up stale FCM tokens (runs daily)
 */
export const cleanupStaleTokens = functions.pubsub
  .schedule("every 24 hours")
  .onRun(async () => {
    const cutoff = Date.now() - 30 * 24 * 60 * 60 * 1000; // 30 days

    const staleTokens = await db
      .collectionGroup("tokens")
      .where("active", "==", true)
      .where("createdAt", "<", cutoff)
      .get();

    const batch = db.batch();
    let count = 0;

    for (const doc of staleTokens.docs) {
      batch.update(doc.ref, { active: false });
      count++;
      if (count % 500 === 0) {
        await batch.commit();
      }
    }

    if (count % 500 !== 0) {
      await batch.commit();
    }

    functions.logger.info(`Cleaned up ${count} stale tokens`);
  });
