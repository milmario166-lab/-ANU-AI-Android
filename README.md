# ANU AI — Android starter

A first Android Studio starter for the ANU AI personal assistant.

## What works in this first version
- Futuristic dark/pink dashboard UI
- Home, Chat, Voice, Create and Tools navigation
- Local demo chat responses
- Android speech recognition launched when the user taps the microphone
- Android Text-to-Speech replies (uses the installed English India voice where available)
- Microphone permission requested only when voice input is tapped

## What is not connected yet
This is a functional UI/voice starter, not a full JARVIS system. Cloud AI, live web search, image generation, persistent memory, reminders, device automation and wake-word listening need additional integrations and testing.

## Open in Android Studio
1. Install Android Studio (a recent stable version).
2. Extract `ANU_AI_Android.zip`.
3. In Android Studio choose **Open** and select the extracted `ANU_AI_Android` folder.
4. Allow Gradle sync. Use JDK 17.
5. Connect an Android phone with USB debugging enabled, or create an emulator.
6. Press **Run**.

## Build an APK
In Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
The debug APK is normally created at:
`app/build/outputs/apk/debug/app-debug.apk`

## Package
`com.anu.ai`

## Next development milestone
Add a FastAPI backend and connect a hosted LLM using a server-side API key. Never put a private API key in the Android app.
