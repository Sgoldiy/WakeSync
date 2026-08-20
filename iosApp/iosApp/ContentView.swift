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
            "avatar": avatar,
            "goal": goal,
            "setupCompleted": true,
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
