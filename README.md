# 🍵 Kombucha Finder — Android App

Searches **Zepto & Blinkit** for Kombucha, compares prices, and adds the cheapest one to your cart — all from one native Android app. **No USB, no PC, no Android Studio needed.**

---

## 📱 How to Get the App on Your Phone (Free, No USB)

### Step 1 — Upload to GitHub (one-time, 2 min)

1. Go to **https://github.com** → Sign up free if you don't have an account
2. Click **"New repository"** → name it `kombucha-finder` → set to **Private** → click **Create**
3. On the next screen, click **"uploading an existing file"**
4. Drag and drop **all the files from this zip** into the uploader
5. Click **"Commit changes"** — this triggers the auto-build!

### Step 2 — Download Your APK (built in the cloud, ~3 min)

1. Go to your repo → click the **"Actions"** tab at the top
2. You'll see a workflow run called **"Build Kombucha Finder APK"** — wait for the ✅ green tick
3. Click on the workflow run → scroll down to **"Artifacts"**
4. Click **"KombuchaFinder-APK"** → it downloads a zip with your APK inside

### Step 3 — Install on Your Android Phone

1. **Send the APK to your phone** — email it to yourself, upload to Google Drive, or WhatsApp it
2. Open the APK on your phone
3. If prompted: **Settings → Install unknown apps → Allow**
4. Tap Install → Open → Done! 🎉

> 💡 **Every time you want to rebuild** (e.g. after editing the code), just push changes to GitHub and repeat Step 2.

---

## 🍵 How the App Works

1. Tap **"Find Best Kombucha Price"**
2. The app opens Zepto & Blinkit in the background and searches for kombucha
3. JavaScript extracts prices from both sites automatically
4. The cheaper option is highlighted with your savings shown
5. Tap **"Add Best Price to Cart"** — it clicks the Add button for you

---

## 🔧 Customize the Search Product

To search for something other than kombucha, edit `app/src/main/java/com/kombuchafinder/MainActivity.java` and change these two lines:

```java
zeptoWebView.loadUrl("https://www.zeptonow.com/search?query=kombucha");
blinkitWebView.loadUrl("https://blinkit.com/s/?q=kombucha");
```

Replace `kombucha` with anything — `green tea`, `oats`, `eggs`, etc.

---

## ⚠️ Notes

- **Internet required** — the app loads live Zepto & Blinkit websites
- **Log in via the WebViews** — scroll down in the app to see both sites live; log into your accounts for accurate cart access
- **Add to Cart fallback** — if auto-add fails (sites update their UI), just tap manually in the WebView shown at the bottom
- The APK artifact on GitHub is available for **30 days** — download it and keep it safe

---

## 📁 Project Structure

```
KombuchaFinder/
├── .github/workflows/
│   └── build-apk.yml          ← GitHub Actions: auto-builds APK on push
├── app/src/main/
│   ├── java/com/kombuchafinder/
│   │   └── MainActivity.java  ← All logic: WebView + JS injection + price compare
│   ├── res/layout/
│   │   └── activity_main.xml  ← UI layout
│   └── AndroidManifest.xml
├── gradle/wrapper/
│   └── gradle-wrapper.properties
├── gradlew
├── build.gradle
└── settings.gradle
```
