# LoopKotlin

Loop is an Android/Kotlin Compose app for personal productivity, habits, goals, journaling, reviews, and daily planning.

## Download

GitHub Pages landing page:

https://yassen0-0.github.io/LoopKotlin/

Latest APK direct download:

https://github.com/Yassen0-0/LoopKotlin/releases/latest/download/Loop-latest.apk

## Release process

Every public APK should come from a semantic version tag. Do not overwrite old releases; each release keeps its own APK asset.

To publish a new APK:

```bash
git add .
git commit -m "Prepare v0.2.0"
git tag v0.2.0
git push origin main
git push origin v0.2.0
```

GitHub Actions will then:

- build the Android APK
- create a GitHub Release
- upload a versioned APK asset, for example `Loop-v0.2.0-debug.apk`
- upload `Loop-latest.apk`
- keep the GitHub Pages download button pointing to the latest release asset automatically

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
