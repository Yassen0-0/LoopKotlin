# Runtime QA Handoff

## Current Status

Firebase Auth release `v0.2.0` is build-ready but not runtime-verified or published.

Do not create the `v0.2.0` tag or publish a GitHub Release until the app is installed and the auth gate is tested on a real Android device or a stable emulator.

## Verified Locally

- `app/google-services.json` exists.
- Firebase project ID is `loop-f6424`.
- Android package is `com.loop.app`.
- `./gradlew clean build --stacktrace` completed successfully with JDK 17 and the workspace Gradle cache.
- `./gradlew assembleDebug --stacktrace` completed successfully.
- Debug APK metadata reports:
  - `applicationId`: `com.loop.app`
  - `versionCode`: `2`
  - `versionName`: `0.2.0`
- Lint report: `No issues found.`

## APK Artifacts

Debug APK:

```bash
/home/yassen/Downloads/my space/figma/LoopKotlin/app/build/outputs/apk/debug/app-debug.apk
```

Current size: `18M`

Unsigned release APK:

```bash
/home/yassen/Downloads/my space/figma/LoopKotlin/app/build/outputs/apk/release/app-release-unsigned.apk
```

Current size: `12M`

## Runtime Blocker

No Android target is currently available.

Observed:

```bash
adb devices
```

Result:

```bash
List of devices attached
```

The available AVD is `arete_api35`, but it failed to boot twice with emulator exit code `139`.

## GitHub Blocker

GitHub CLI is not authenticated.

Observed:

```bash
gh auth status
```

Result:

```bash
The token in default is invalid.
```

Re-authenticate before pushing:

```bash
gh auth login -h github.com
```

## Required Runtime QA

Run this on a real Android phone or a stable emulator.

1. Confirm a device is connected:

```bash
adb devices -l
```

Expected: one device shows `device`, not `offline` or `unauthorized`.

2. Install the debug APK:

```bash
adb install -r "/home/yassen/Downloads/my space/figma/LoopKotlin/app/build/outputs/apk/debug/app-debug.apk"
```

3. Clear app data:

```bash
adb shell pm clear com.loop.app
```

4. Launch:

```bash
adb shell am start -n com.loop.app/.MainActivity
```

5. Verify unauthenticated state:

- The first visible app screen must be Login/Signup.
- The main Loop planner screens must not be visible before sign-in.
- The screen must show Email, Password, Sign in, Create account, Continue with Google, and Forgot password controls.

6. Verify Email/Password signup:

- Create a new Firebase account with a test email and password.
- After success, the app enters the main Loop app.
- Force stop and relaunch; the signed-in session remains active.

7. Verify logout:

- Open Settings.
- Tap Log out.
- App returns to Login/Signup.
- Force stop and relaunch; app still shows Login/Signup.

8. Verify Email/Password login:

- Sign in again with the same test account.
- App enters the main Loop app.

9. Verify password reset UI:

- Log out.
- Enter the test email.
- Tap Forgot password.
- A password reset success or Firebase error message is shown clearly.

10. Verify Google Sign-In:

- Tap Continue with Google.
- Complete Google account selection.
- App enters the main Loop app after Firebase accepts the credential.

11. Verify UID-separated local storage:

- Sign in as account A.
- Create a visible task or habit.
- Log out.
- Sign in as account B.
- Confirm account A's local task or habit is not visible.
- Log out and sign back in as account A.
- Confirm account A's data is still visible.

12. Capture evidence:

```bash
adb exec-out screencap -p > loop-auth-login.png
adb logcat -d -t 500 > loop-auth-logcat.txt
```

## Publish Only After Runtime QA Passes

After runtime QA passes and GitHub CLI is authenticated:

```bash
git add .github/workflows/android-release.yml README.md RUNTIME_QA_HANDOFF.md build.gradle.kts app/build.gradle.kts app/google-services.json app/src/main/java/com/loop/app/MainActivity.kt app/src/main/java/com/loop/app/ui/LoopApp.kt app/src/main/java/com/loop/app/ui/LoopLocalStore.kt app/src/main/java/com/loop/app/ui/auth docs/index.html docs/styles.css
git commit -m "Add Firebase Auth release gate"
git tag v0.2.0
/usr/bin/git push origin main
/usr/bin/git push origin v0.2.0
```

Then verify:

- GitHub Actions passes for `v0.2.0`.
- Release `v0.2.0` exists.
- Release assets include `Loop-v0.2.0.apk` and `Loop-latest.apk`.
- Latest download URL downloads the new APK:

```text
https://github.com/Yassen0-0/LoopKotlin/releases/latest/download/Loop-latest.apk
```
