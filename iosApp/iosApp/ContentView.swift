import UIKit
import SwiftUI
import ComposeApp
import FirebaseAuth
import FirebaseCore
import FirebaseFirestore
import GoogleSignIn

// MARK: - Google Auth Coordinator

final class GoogleAuthCoordinator: ObservableObject {
    static let shared = GoogleAuthCoordinator()
    weak var presenter: UIViewController?

    // Using databaseID: "wakesync" as requested (matching Android's getInstance("wakesync"))
    // If you get an error here, ensure you have created a database named "wakesync" in the Firebase Console.
    // If you are using the default database, change this to: Firestore.firestore()
    private var db: Firestore {
        return Firestore.firestore(databaseID: "wakesync")
    }

    func signIn(completion: @escaping (String?) -> Void) {
        guard let presenter = presenter else {
            completion("Unable to present Google Sign-In.")
            return
        }

        guard let clientID = FirebaseApp.app()?.options.clientID else {
            completion("Missing Firebase client ID.")
            return
        }

        let config = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.configuration = config

        GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { [weak self] result, error in
            if let error = error {
                completion(error.localizedDescription)
                return
            }

            guard
                let user = result?.user,
                let idToken = user.idToken?.tokenString
            else {
                completion("Google token not found.")
                return
            }

            let credential = GoogleAuthProvider.credential(
                withIDToken: idToken,
                accessToken: user.accessToken.tokenString
            )

            Auth.auth().signIn(with: credential) { [weak self] _, authError in
                if let authError = authError {
                    completion(authError.localizedDescription)
                } else {
                    // Save basic user info to Firestore after login (same as Android)
                    self?.saveBasicUserInfo(completion: completion)
                }
            }
        }
    }

    private func saveBasicUserInfo(completion: @escaping (String?) -> Void) {
        guard let user = Auth.auth().currentUser else {
            completion(nil)
            return
        }

        let data: [String: Any] = [
            "uid": user.uid,
            "email": user.email ?? "",
            "authDisplayName": user.displayName ?? "",
            "lastLogin": FieldValue.serverTimestamp()
        ]

        db.collection("users").document(user.uid).setData(data, merge: true) { error in
            if let error = error {
                print("DEBUG: saveBasicUserInfo Firestore error: \(error.localizedDescription)")
            }
            // Proceed regardless of Firestore result (same as Android)
            completion(nil)
        }
    }
}

// MARK: - Firestore Profile Bridge (Swift implementation of Kotlin interface)

final class SwiftFirestoreBridge: NSObject, IosFirestoreBridge {
    private var db: Firestore {
        return Firestore.firestore(databaseID: "wakesync")
    }

    // MARK: - Auth
    
    func getCurrentUserUid() -> String? {
        return Auth.auth().currentUser?.uid
    }
    
    func getCurrentUserDisplayName() -> String? {
        return Auth.auth().currentUser?.displayName
    }

    // MARK: - Username Check
    
    func checkUsername(username: String, onResult: @escaping (Bool) -> Void, onError: @escaping (String) -> Void) {
        db.collection("users")
            .whereField("username", isEqualTo: username)
            .getDocuments { snapshot, error in
                if let error = error {
                    print("DEBUG: checkUsername Firestore error: \(error.localizedDescription)")
                    onError(error.localizedDescription)
                } else {
                    let isAvailable = snapshot?.documents.isEmpty ?? true
                    onResult(isAvailable)
                }
            }
    }
    
    // MARK: - Profile
    
    func saveProfile(username: String, avatar: String, goal: String, onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void) {
        guard let user = Auth.auth().currentUser else {
            onError("User not authenticated")
            return
        }

        let data: [String: Any] = [
            "uid": user.uid,
            "email": user.email ?? "",
            "authDisplayName": user.displayName ?? "",
            "username": username,
            "avatarEmoji": avatar,
            "avatar": avatar,
            "goal": goal,
            "setupCompleted": true,
            "streak": 0,
            "wins": 0,
            "losses": 0,
            "soloAlarmStreak": 0,
            "soloAlarmWins": 0,
            "soloAlarmLosses": 0,
            "duoAlarmStreak": 0,
            "duoAlarmWins": 0,
            "duoAlarmLosses": 0,
            "groupAlarmStreak": 0,
            "groupAlarmWins": 0,
            "groupAlarmLosses": 0,
            "createdAt": FieldValue.serverTimestamp()
        ]

        db.collection("users").document(user.uid).setData(data, merge: true) { error in
            if let error = error {
                print("DEBUG: saveProfile Firestore error: \(error.localizedDescription)")
                onError(error.localizedDescription)
            } else {
                onSuccess()
            }
        }
    }
    
    // MARK: - User Document
    
    func getUserDocument(onResult: @escaping (String?) -> Void, onError: @escaping (String) -> Void) {
        guard let user = Auth.auth().currentUser else {
            onError("User not authenticated")
            return
        }
        db.collection("users").document(user.uid).getDocument { snapshot, error in
            if let error = error {
                onError(error.localizedDescription)
                return
            }
            guard let data = snapshot?.data(), snapshot?.exists == true else {
                onResult(nil)
                return
            }
            // Serialize to simple string format: key=value pairs separated by newlines
            let serialized = data.map { "\($0.key)=\($0.value)" }.joined(separator: "\n")
            onResult(serialized)
        }
    }
    
    func setUserDocument(data: [String: Any?], merge: Bool, onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void) {
        guard let user = Auth.auth().currentUser else {
            onError("User not authenticated")
            return
        }
        // Filter out nil values
        let cleanData = data.compactMapValues { $0 }
        if merge {
            db.collection("users").document(user.uid).setData(cleanData, merge: true) { error in
                if let error = error { onError(error.localizedDescription) } else { onSuccess() }
            }
        } else {
            db.collection("users").document(user.uid).setData(cleanData) { error in
                if let error = error { onError(error.localizedDescription) } else { onSuccess() }
            }
        }
    }
    
    func queryUsers(field: String, value: String, onResult: @escaping (String) -> Void, onError: @escaping (String) -> Void) {
        db.collection("users")
            .whereField(field, isEqualTo: value)
            .limit(to: 5)
            .getDocuments { snapshot, error in
                if let error = error {
                    onError(error.localizedDescription)
                    return
                }
                let results = snapshot?.documents.compactMap { doc -> String? in
                    let username = doc.data()["username"] as? String ?? ""
                    let avatar = doc.data()["avatarEmoji"] as? String ?? doc.data()["avatar"] as? String ?? "\ud83d\udc64"
                    let streak = doc.data()["streak"] as? Int ?? 0
                    return "\(doc.documentID)|\(username)|\(avatar)|\(streak)"
                } ?? []
                onResult(results.joined(separator: "\n"))
            }
    }

    // MARK: - Alarm CRUD
    
    func getAlarms(onResult: @escaping (String) -> Void, onError: @escaping (String) -> Void) {
        guard let uid = Auth.auth().currentUser?.uid else { onError("Not authenticated"); return }
        db.collection("users").document(uid).collection("alarms")
            .order(by: "time", ascending: true)
            .getDocuments { snapshot, error in
                if let error = error { onError(error.localizedDescription); return }
                let results = snapshot?.documents.compactMap { doc -> String? in
                    let d = doc.data()
                    let id = doc.documentID
                    let time = d["time"] as? String ?? "00:00"
                    let label = d["label"] as? String ?? "Alarm"
                    let mode = d["mode"] as? String ?? "Solo"
                    let challenge = d["challenge"] as? String ?? "Math"
                    let isEnabled = d["isEnabled"] as? Bool ?? true
                    let isGroup = d["isGroup"] as? Bool ?? false
                    let days = (d["days"] as? [Int])?.map { String($0) }.joined(separator: ",") ?? ""
                    let timestamp = d["timestamp"] as? Int ?? 0
                    let soundUrl = d["soundUrl"] as? String ?? ""
                    let soundName = d["soundName"] as? String ?? "Default"
                    let partnerUid = d["partnerUid"] as? String ?? ""
                    let partnerUsername = d["partnerUsername"] as? String ?? ""
                    let bondName = d["bondName"] as? String ?? ""
                    let mathDifficulty = d["mathDifficulty"] as? String ?? "Medium"
                    return "\(id)|\(time)|\(label)|\(mode)|\(challenge)|\(isEnabled)|\(isGroup)|\(days)|\(timestamp)|\(soundUrl)|\(soundName)|\(partnerUid)|\(partnerUsername)|\(bondName)|\(mathDifficulty)"
                } ?? []
                onResult(results.joined(separator: "\n"))
            }
    }
    
    func addAlarm(data: [String: Any?], onSuccess: @escaping (String) -> Void, onError: @escaping (String) -> Void) {
        guard let uid = Auth.auth().currentUser?.uid else { onError("Not authenticated"); return }
        let cleanData = data.compactMapValues { $0 }
        var ref: DocumentReference?
        ref = db.collection("users").document(uid).collection("alarms").addDocument(data: cleanData) { error in
            if let error = error { onError(error.localizedDescription) } else { onSuccess(ref?.documentID ?? "") }
        }
    }
    
    func updateAlarm(alarmId: String, data: [String: Any?], onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void) {
        guard let uid = Auth.auth().currentUser?.uid else { onError("Not authenticated"); return }
        let cleanData = data.compactMapValues { $0 }
        db.collection("users").document(uid).collection("alarms").document(alarmId).setData(cleanData, merge: true) { error in
            if let error = error { onError(error.localizedDescription) } else { onSuccess() }
        }
    }
    
    func deleteAlarm(alarmId: String, onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void) {
        guard let uid = Auth.auth().currentUser?.uid else { onError("Not authenticated"); return }
        db.collection("users").document(uid).collection("alarms").document(alarmId).delete { error in
            if let error = error { onError(error.localizedDescription) } else { onSuccess() }
        }
    }

    // MARK: - Habit CRUD
    
    func getHabits(onResult: @escaping (String) -> Void, onError: @escaping (String) -> Void) {
        guard let uid = Auth.auth().currentUser?.uid else { onError("Not authenticated"); return }
        db.collection("users").document(uid).collection("habits")
            .getDocuments { snapshot, error in
                if let error = error { onError(error.localizedDescription); return }
                let results = snapshot?.documents.compactMap { doc -> String? in
                    let d = doc.data()
                    let id = doc.documentID
                    let title = d["title"] as? String ?? ""
                    let iconType = d["iconType"] as? String ?? "RUN"
                    let isDone = d["isDone"] as? Bool ?? false
                    let streak = d["streak"] as? Int ?? 0
                    let frequency = d["frequency"] as? String ?? "Daily"
                    let reminderTime = d["reminderTime"] as? String ?? "6:15 AM"
                    let partnerUsername = d["partnerUsername"] as? String ?? ""
                    let bondName = d["bondName"] as? String ?? ""
                    return "\(id)|\(title)|\(iconType)|\(isDone)|\(streak)|\(frequency)|\(reminderTime)|\(partnerUsername)|\(bondName)"
                } ?? []
                onResult(results.joined(separator: "\n"))
            }
    }
    
    func addHabit(data: [String: Any?], onSuccess: @escaping (String) -> Void, onError: @escaping (String) -> Void) {
        guard let uid = Auth.auth().currentUser?.uid else { onError("Not authenticated"); return }
        let cleanData = data.compactMapValues { $0 }
        var ref: DocumentReference?
        ref = db.collection("users").document(uid).collection("habits").addDocument(data: cleanData) { error in
            if let error = error { onError(error.localizedDescription) } else { onSuccess(ref?.documentID ?? "") }
        }
    }
    
    func updateHabit(habitId: String, data: [String: Any?], onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void) {
        guard let uid = Auth.auth().currentUser?.uid else { onError("Not authenticated"); return }
        let cleanData = data.compactMapValues { $0 }
        db.collection("users").document(uid).collection("habits").document(habitId).setData(cleanData, merge: true) { error in
            if let error = error { onError(error.localizedDescription) } else { onSuccess() }
        }
    }
    
    func deleteHabit(habitId: String, onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void) {
        guard let uid = Auth.auth().currentUser?.uid else { onError("Not authenticated"); return }
        db.collection("users").document(uid).collection("habits").document(habitId).delete { error in
            if let error = error { onError(error.localizedDescription) } else { onSuccess() }
        }
    }

    // MARK: - Leaderboard
    
    func getLeaderboard(sortField: String, limit: Int, onResult: @escaping (String) -> Void, onError: @escaping (String) -> Void) {
        let currentUid = Auth.auth().currentUser?.uid
        db.collection("users")
            .order(by: sortField, descending: true)
            .limit(to: limit)
            .getDocuments { snapshot, error in
                if let error = error { onError(error.localizedDescription); return }
                var rank = 1
                let results = snapshot?.documents.compactMap { doc -> String? in
                    let d = doc.data()
                    let username = d["username"] as? String ?? ""
                    let avatar = d["avatarEmoji"] as? String ?? d["avatar"] as? String ?? "\ud83d\udc64"
                    let wins = d[sortField] as? Int ?? d["wins"] as? Int ?? 0
                    let streak = d["streak"] as? Int ?? 0
                    let losses = d["losses"] as? Int ?? 0
                    let isUser = doc.documentID == currentUid
                    let r = rank; rank += 1
                    let score = (wins * 100) + (streak * 10)
                    let isRed = losses > 3 ? 1 : 0
                    return "\(r)|\(isUser ? "YOU" : username)|\(avatar)|\(score)|\(streak)|\(isUser ? 1 : 0)|\(isRed)"
                } ?? []
                onResult(results.joined(separator: "\n"))
            }
    }

    // MARK: - Social Feed
    
    func getFeedPosts(limit: Int, onResult: @escaping (String) -> Void, onError: @escaping (String) -> Void) {
        db.collection("feed")
            .order(by: "createdAt", descending: true)
            .limit(to: limit)
            .getDocuments { snapshot, error in
                if let error = error { onError(error.localizedDescription); return }
                let results = snapshot?.documents.compactMap { doc -> String? in
                    let d = doc.data()
                    let id = doc.documentID
                    let userId = d["userId"] as? String ?? ""
                    let username = d["username"] as? String ?? ""
                    let avatar = d["avatar"] as? String ?? "\ud83d\udc64"
                    let content = d["content"] as? String ?? ""
                    let badgeText = d["badgeText"] as? String ?? ""
                    let badgeColorHex = d["badgeColorHex"] as? String ?? "#00E0FF"
                    let streak = d["streak"] as? Int ?? 0
                    let createdAt = d["createdAt"] as? Int ?? 0
                    let reactionsDict = d["reactions"] as? [String: Int] ?? [:]
                    let reactionsStr = reactionsDict.map { "\($0.key):\($0.value)" }.joined(separator: ",")
                    return "\(id)|\(userId)|\(username)|\(avatar)|\(content)|\(badgeText)|\(badgeColorHex)|\(streak)|\(createdAt)|\(reactionsStr)"
                } ?? []
                onResult(results.joined(separator: "\n"))
            }
    }
    
    func addFeedPost(data: [String: Any?], onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void) {
        let cleanData = data.compactMapValues { $0 }
        db.collection("feed").addDocument(data: cleanData) { error in
            if let error = error { onError(error.localizedDescription) } else { onSuccess() }
        }
    }

    // MARK: - Notifications
    
    func getNotifications(onResult: @escaping (String) -> Void, onError: @escaping (String) -> Void) {
        guard let uid = Auth.auth().currentUser?.uid else { onError("Not authenticated"); return }
        db.collection("users").document(uid).collection("notifications")
            .order(by: "createdAt", descending: true)
            .limit(to: 30)
            .getDocuments { snapshot, error in
                if let error = error { onError(error.localizedDescription); return }
                let results = snapshot?.documents.compactMap { doc -> String? in
                    let d = doc.data()
                    let id = doc.documentID
                    let type = d["type"] as? String ?? ""
                    let message = d["message"] as? String ?? ""
                    let fromUsername = d["fromUsername"] as? String ?? ""
                    let fromUid = d["fromUid"] as? String ?? ""
                    let createdAt = d["createdAt"] as? Int ?? 0
                    let read = d["read"] as? Bool ?? false
                    let punishmentId = d["punishmentId"] as? String ?? ""
                    return "\(id)|\(type)|\(message)|\(fromUsername)|\(fromUid)|\(createdAt)|\(read)|\(punishmentId)"
                } ?? []
                onResult(results.joined(separator: "\n"))
            }
    }
    
    func markNotificationRead(notificationId: String, onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void) {
        guard let uid = Auth.auth().currentUser?.uid else { onError("Not authenticated"); return }
        db.collection("users").document(uid).collection("notifications").document(notificationId)
            .updateData(["read": true]) { error in
                if let error = error { onError(error.localizedDescription) } else { onSuccess() }
            }
    }

    // MARK: - Duo Alarm
    
    func listenToDuoAlarm(alarmId: String, onResult: @escaping (String?) -> Void, onError: @escaping (String) -> Void) {
        db.collection("duo_alarms").document(alarmId)
            .addSnapshotListener { snapshot, error in
                if let error = error { onError(error.localizedDescription); return }
                let winnerUid = snapshot?.data()? ["winnerUid"] as? String
                onResult(winnerUid)
            }
    }
    
    func setDuoAlarmWinner(alarmId: String, winnerUid: String, onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void) {
        db.collection("duo_alarms").document(alarmId)
            .updateData(["winnerUid": winnerUid]) { error in
                if let error = error { onError(error.localizedDescription) } else { onSuccess() }
            }
    }

    // MARK: - Punishment
    
    func writePunishment(punishmentId: String, data: [String: Any?], onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void) {
        guard let uid = Auth.auth().currentUser?.uid else { onError("Not authenticated"); return }
        let cleanData = data.compactMapValues { $0 }
        db.collection("users").document(uid).collection("punishments").document(punishmentId)
            .setData(cleanData, merge: true) { error in
                if let error = error { onError(error.localizedDescription) } else { onSuccess() }
            }
    }

    // MARK: - Push Notifications
    
    func registerDeviceToken(token: String, platform: String, onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void) {
        guard let uid = Auth.auth().currentUser?.uid else { onError("Not authenticated"); return }
        let data: [String: Any] = [
            "fcmToken": token,
            "fcmTokenUpdatedAt": Int(Date().timeIntervalSince1970 * 1000),
            "platform": platform
        ]
        db.collection("users").document(uid).setData(data, merge: true) { error in
            if let error = error {
                onError(error.localizedDescription)
            } else {
                // Also store in tokens subcollection
                db.collection("users").document(uid).collection("tokens").document(token).setData([
                    "token": token,
                    "platform": platform,
                    "createdAt": Int(Date().timeIntervalSince1970 * 1000),
                    "active": true
                ]) { _ in onSuccess() }
            }
        }
    }
    
    func sendPushNotification(toUid: String, title: String, body: String, type: String, data: [String: String], onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void) {
        // Read target user's FCM token
        db.collection("users").document(toUid).getDocument { snapshot, error in
            if let error = error { onError(error.localizedDescription); return }
            guard let fcmToken = snapshot?.data()? ["fcmToken"] as? String, !fcmToken.isEmpty else {
                onSuccess() // No token, silently succeed
                return
            }
            // Write to push_queue for Cloud Function to process
            var pushData: [String: Any] = [
                "toUid": toUid,
                "fcmToken": fcmToken,
                "title": title,
                "body": body,
                "type": type,
                "data": data,
                "createdAt": Int(Date().timeIntervalSince1970 * 1000),
                "processed": false
            ]
            db.collection("push_queue").addDocument(data: pushData) { error in
                if let error = error { onError(error.localizedDescription) } else { onSuccess() }
            }
        }
    }
    
    func getFCMToken(onResult: @escaping (String?) -> Void, onError: @escaping (String) -> Void) {
        // Firebase Messaging automatically provides FCM token after APNs registration
        // Access it through the Firebase Messaging instance
        if let token = Messaging.messaging().fcmToken {
            onResult(token)
        } else {
            // Token not yet available, try to fetch it
            Messaging.messaging().token { token, error in
                if let error = error {
                    onError(error.localizedDescription)
                } else {
                    onResult(token)
                }
            }
        }
    }
}

// MARK: - Compose View

struct ComposeView: UIViewControllerRepresentable {
    @Binding var isProfileCreated: Bool

    func makeUIViewController(context: Context) -> UIViewController {
        let controller = MainViewControllerKt.MainViewController(
            initiallyAuthenticated: Auth.auth().currentUser != nil,
            isProfileCreated: isProfileCreated,
            onGoogleSignInRequested: { completion in
                GoogleAuthCoordinator.shared.signIn { message in
                    completion(message)
                }
            },
            firestoreBridge: SwiftFirestoreBridge()
        )
        GoogleAuthCoordinator.shared.presenter = controller
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        GoogleAuthCoordinator.shared.presenter = uiViewController
    }
}

// MARK: - Content View

struct ContentView: View {
    @State private var isProfileCreated = false
    @State private var isLoading = true

    var body: some View {
        Group {
            if isLoading {
                Color.black.ignoresSafeArea()
                    .onAppear {
                        checkProfileStatus()
                    }
            } else {
                ComposeView(isProfileCreated: $isProfileCreated)
                    .ignoresSafeArea()
            }
        }
    }

    private func checkProfileStatus() {
        guard let user = Auth.auth().currentUser else {
            self.isProfileCreated = false
            self.isLoading = false
            return
        }

        // Check Firestore if profile is already created
        let db = Firestore.firestore(databaseID: "wakesync")
        db.collection("users").document(user.uid).getDocument { snapshot, error in
            if let document = snapshot, document.exists {
                let data = document.data()
                self.isProfileCreated = (data?["setupCompleted"] as? Bool) ?? false
            } else {
                self.isProfileCreated = false
            }
            self.isLoading = false
        }
    }
}
