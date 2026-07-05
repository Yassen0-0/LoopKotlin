# Runtime QA Handoff

## Current Status

Task 2 is BLOCKED.

Build verification has passed and a debug APK is available, but runtime acceptance is still pending. This app must not be marked runtime-verified until it is installed and tested on a real Android device or a stable emulator.

## Debug APK

APK path:

```bash
/home/yassen/Downloads/my space/figma/LoopKotlin/app/build/outputs/apk/debug/app-debug.apk
```

Package:

```bash
com.loop.app
```

Main activity:

```bash
com.loop.app/.MainActivity
```

## Physical Android Device Install Instructions

1. On the Android phone, enable Developer options.
2. Enable USB debugging.
3. Connect the phone to this machine with a USB cable.
4. Accept the USB debugging prompt on the phone.
5. From `/home/yassen/Downloads/my space/figma/LoopKotlin`, confirm the device is connected:

```bash
adb devices -l
```

Expected result: one connected device shows `device`, not `offline` or `unauthorized`.

6. Install the debug APK:

```bash
adb install -r "/home/yassen/Downloads/my space/figma/LoopKotlin/app/build/outputs/apk/debug/app-debug.apk"
```

7. Confirm Android can resolve the app activity:

```bash
adb shell cmd package resolve-activity --brief com.loop.app
```

Expected result includes:

```bash
com.loop.app/.MainActivity
```

8. Clear app data before first-launch testing:

```bash
adb shell pm clear com.loop.app
```

9. Launch the app:

```bash
adb shell am start -n com.loop.app/.MainActivity
```

10. Capture launch evidence:

```bash
adb exec-out screencap -p > loop-today-launch.png
adb logcat -d -t 500 > loop-launch-logcat.txt
```

11. For persistence checks, force stop and relaunch:

```bash
adb shell am force-stop com.loop.app
adb shell am start -n com.loop.app/.MainActivity
```

## Manual QA Checklist

Use this checklist on a real Android device or a stable emulator. Record Pass, Fail, or Blocked for each item and attach screenshots or logcat evidence for failures.

### App Launch

- Install succeeds with `adb install -r`.
- Activity resolves with `adb shell cmd package resolve-activity --brief com.loop.app`.
- App launches with `adb shell am start -n com.loop.app/.MainActivity`.
- No immediate crash appears in logcat.
- First screen is visible and interactive.

### First-Launch Onboarding

- After `adb shell pm clear com.loop.app`, app opens to first-launch setup.
- Optional name field accepts input.
- Skip works.
- Start works.
- No fake personal data appears for a new install.
- After onboarding, relaunch does not show onboarding again.

### Home / Today Screen

- Today screen loads after onboarding.
- Empty state is visible when no tasks or habits exist.
- Quick add actions open the correct creation flows.
- Today task count and habit count update after creating and toggling items.
- Today content remains readable in portrait orientation.

### Tasks CRUD

- Create a task with title, optional details, and date.
- Edit the task title/details/date.
- Toggle task completion.
- Cancel an edit and confirm original data remains unchanged.
- Delete confirmation can be cancelled.
- Delete confirmation removes the task.
- Force stop and relaunch; saved tasks and toggles persist.

### Habits CRUD

- Create a habit.
- Edit the habit title.
- Toggle habit completion.
- Cancel an edit and confirm original data remains unchanged.
- Delete confirmation can be cancelled.
- Delete confirmation removes the habit.
- Force stop and relaunch; saved habits and toggles persist.

### Goals CRUD

- Create a goal with title, progress, target, and unit.
- Edit goal fields.
- Invalid target is rejected.
- Cancel an edit and confirm original data remains unchanged.
- Delete confirmation can be cancelled.
- Delete confirmation removes the goal.
- Force stop and relaunch; saved goals persist.

### Journal CRUD

- Create a journal entry with date and content.
- Edit the entry date and content.
- Blank content is rejected.
- Cancel an edit and confirm original data remains unchanged.
- Delete confirmation can be cancelled.
- Delete confirmation removes the entry.
- Force stop and relaunch; saved entries persist.

### Reviews Saving

- Open More, then Reviews.
- Start review opens a visible review form.
- Blank required review fields are rejected.
- Save review with wins and next focus.
- Saved review appears in Reviews.
- Cancel a second review and confirm no extra review is saved.
- Force stop and relaunch; saved review persists.

### Settings Persistence

- Open More, then Settings.
- Change profile name.
- Change theme to Light, Dark, and System.
- Change language to Arabic and back to English.
- Force stop and relaunch after each setting change.
- Confirm selected settings persist after relaunch.
- Reset local data clears user-created content.

### Arabic / RTL

- Switch language to Arabic from Settings.
- Layout direction changes to RTL.
- Main navigation, More list, forms, back controls, and calendar controls mirror correctly.
- Arabic copy appears across Today, Tasks, Habits, Insights, More, Goals, Journal, Calendar, Deen, Search, Reviews, and Settings.
- Directional icons point correctly in RTL.
- No unintended English copy remains except product branding or technical values.

### Dark Mode / Light Mode

- Set theme to Light and inspect all main screens.
- Set theme to Dark and inspect all main screens.
- Set theme to System and confirm it follows device theme.
- Text, buttons, cards, inputs, toggles, and dividers remain readable in both light and dark modes.
- Theme choice persists after force stop and relaunch.

### Navigation

- Bottom navigation switches between Today, Tasks, Habits, Insights, and More.
- More opens Goals, Journal, Calendar, Deen, Search, Reviews, and Settings.
- Calendar previous/next day controls work.
- Search can find task details, habits, journal entries, goals, and reviews.
- No navigation route lands on a blank or wrong screen.

### Back Behavior

- Top back returns from each More sub-screen to More or the immediate prior route.
- Android system back behaves consistently with top back.
- Back from an open sheet asks for discard confirmation when there are unsaved changes.
- Back/cancel from delete confirmation leaves data unchanged.
- Repeated back presses do not crash the app.

### 5-Minute No-Crash Usage Test

- Start logcat capture before the test:

```bash
adb logcat -c
adb logcat > loop-5-minute-test-logcat.txt
```

- Use the app continuously for at least 5 minutes.
- Navigate through every main and More screen.
- Create, edit, toggle, cancel, and delete at least one task and one habit.
- Create and save one journal entry, one goal, and one review.
- Change theme and language at least once.
- Force stop and relaunch once during the test.
- Confirm no crash dialog appears.
- Stop logcat and inspect for fatal exceptions or process death.

## Acceptance Rule

Task 2 remains BLOCKED until the APK is installed and the checklist above passes on a real physical Android device or a stable emulator. Build success alone is not runtime acceptance.
