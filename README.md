# LoopKotlin

Loop is an Android/Kotlin Compose app for personal productivity, habits, goals, journaling, reviews, and daily planning.

## Download

GitHub Pages landing page:

https://yassen0-0.github.io/LoopKotlin/

Latest APK direct download:

https://github.com/Yassen0-0/LoopKotlin/releases/latest/download/Loop-latest.apk

## Current public version

`v0.2.0` is the Firebase Authentication release. It requires a real Firebase account before the main Loop app opens.

`v0.1.0` was an earlier public APK without account login. Do not use it as the current public build.

## Release process

Every public APK must come from a semantic version tag. Do not overwrite old releases; each release keeps its own APK asset.

To publish a new APK:

```bash
git add .
git commit -m "Prepare v0.2.0"
git tag v0.2.0
git push origin main
git push origin v0.2.0
```

GitHub Actions will then:

- verify `app/google-services.json` exists
- verify Firebase Auth dependencies and auth screens exist
- build the Android APK
- create a GitHub Release
- upload a versioned APK asset, for example `Loop-v0.2.0.apk`
- upload `Loop-latest.apk`
- keep the GitHub Pages download button pointing to the latest release asset automatically

The latest APK button points directly to:

https://github.com/Yassen0-0/LoopKotlin/releases/latest/download/Loop-latest.apk

## Firebase Authentication

The app uses real Firebase Authentication:

- Email/password sign up
- Email/password login
- Google Sign-In
- Password reset email
- Auth state listener
- Logout

Unauthenticated users only see the login/signup screens. The main productivity app opens only after Firebase reports a signed-in user.

Required Firebase config:

- `app/google-services.json`
- Firebase Authentication provider: Email/Password enabled
- Firebase Authentication provider: Google enabled
- Android package name: `com.loop.app`

## Version tags

Use semantic version tags:

- `v0.1.0`
- `v0.2.0`
- `v0.3.0`
- `v1.0.0`

## Production signing warning

The current release workflow builds and uploads a debug APK because production release signing is not configured yet. Configure Android release signing before treating APKs as production-ready public builds.

## GitHub Pages setup

After pushing this repository, enable GitHub Pages:

1. Open the repository on GitHub.
2. Go to Settings.
3. Open Pages.
4. Set Source to `Deploy from a branch`.
5. Set Branch to `main`.
6. Set Folder to `/docs`.
7. Save.

The page should become available at:

https://yassen0-0.github.io/LoopKotlin/
