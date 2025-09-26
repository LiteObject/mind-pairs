# Google Play Deployment Guide

## Why Manual Upload is Required for the First Time

### 1. Initial App Setup in Play Console
- Before you can automate uploads (via GitHub Actions), your app needs to exist in the Google Play Console
- The first upload is part of the initial app setup process, which includes filling out store listing details, content ratings, pricing, etc.
- You can't fully complete this setup without an initial AAB or APK

### 2. Establishing the App's Identity
- The first upload registers your app's package name and initial signing key with Google Play
- Google Play uses this first upload to verify that you own the package name and to set up App Signing by Google Play

### 3. App Signing by Google Play (Highly Recommended)
When you upload your first AAB, Google Play will prompt you to enroll in "App Signing by Google Play."

**How it works:**
- You sign your app with an upload key (which you keep private)
- When you upload the AAB, Google Play verifies it with your upload key, then re-signs your app with the app signing key that Google manages

**Benefits:**
- **Security**: If your upload key is ever compromised, you can request Google to reset it without affecting users who already have your app
- **AAB Requirement**: App Signing by Google Play is required to use Android App Bundles, which offer significant size savings for users
- **Future-proof**: If you managed your own app signing key and lost it, you'd never be able to update your app again

### 4. Testing the Build and Signing Process
Manually building and signing your AAB for the first time helps you ensure that your local build environment, keystore, and signing configurations are all working correctly before you try to automate it.

## Step-by-Step Manual Upload Process

### Step 1: Develop Your App
Get your app to a state where it's ready for at least an initial internal test or alpha release.

### Step 2: Generate a Signing Keystore Using Android Studio
1. Open Android Studio and load your project
2. Go to **Build > Generate Signed Bundle / APK...**
3. Select **Android App Bundle** and click **Next**
4. Under **Key store path**, click **Create new...**
5. Fill in the keystore details:
   - **Key store path**: Choose a secure location (e.g., `C:\Users\Owner\source\repos\...\...\Keys\***.jks`)
   - **Password**: Create a strong password
   - **Key alias**: Choose a meaningful alias (e.g., `mindpairs-key`)
   - **Key password**: Create a strong password for the key
   - **Validity (years)**: 25-30 years (recommended)
   - **Certificate details**: Fill in your information
6. Click **OK**
7. **Important**: Store this keystore file and its passwords securely and back them up

### Step 3: Build the Signed Android App Bundle (AAB)
1. In the "Generate Signed Bundle / APK" dialog, enter:
   - Key store password
   - Key alias
   - Key password
2. Choose a destination folder for your AAB
3. Select **release** as the Build Type
4. Click **Finish**
5. Android Studio will build your signed AAB file (e.g., `app-release.aab`)

### Step 4: Set Up Your App in Google Play Console
1. Go to [Google Play Console](https://play.google.com/console)
2. Click **Create app**
3. Fill in app details:
   - **App name**: "Mind Pairs"
   - **Default language**: English (United States)
   - **App or game**: Game
   - **Free or paid**: Free
4. Accept Play Console Developer Policy
5. Click **Create app**

### Step 5: Complete App Setup Requirements
Navigate through the dashboard tasks and complete:

**App Setup:**
- App access
- Ads (declare if your app contains ads)
- Content rating
- Target audience and content
- Data safety

**Store Presence:**
- Main store listing (descriptions, screenshots, etc.)

### Step 6: Create Your First Release
1. In the left menu, under **Release**, go to **Testing > Internal testing** (recommended for first release)
2. Click **Create new release**
3. **Important**: You'll be prompted to enroll in **App Signing by Google Play** - follow the instructions and enroll

### Step 7: Upload Your AAB
1. Drag and drop your signed `.aab` file into the "App bundles" section or use the upload button
2. Google Play will process and verify your upload

### Step 8: Enter Release Details
1. Add a **release name** (e.g., "1.0.0-initial")
2. Add **release notes** describing what's new in this version
3. Review any warnings or suggestions from Google Play

### Step 9: Review and Roll Out
1. Click **Save**
2. Click **Review release**
3. Address any errors or critical warnings
4. If everything looks good, click **Start rollout to Internal testing**

## After the First Manual Upload

Once your app is set up in the Play Console and App Signing by Google Play is configured, you can set up automated CI/CD pipeline using GitHub Actions for subsequent releases.

### Set up GitHub Secrets
In your GitHub repository settings, add these secrets:
- `KEYSTORE_FILE`: Base64 encoded keystore file
- `KEYSTORE_PASSWORD`: Your keystore password
- `KEY_ALIAS`: Your key alias
- `KEY_PASSWORD`: Your key password
- `SERVICE_ACCOUNT_JSON`: Google Play Console service account JSON

### Automated Deployment
Your CI/CD pipeline will use the upload key you configured to sign the AABs it builds, and Google Play will handle the final app signing with their managed key.

## Important Notes
- The package name `com.liteobject.mindpairs` in your app must match exactly in Google Play Console
- Keep your keystore file safe and backed up - you cannot update your app without it
- The first upload must always be done manually through the web interface
- Service account must have "Release Manager" role in Google Play Console
- App Signing by Google Play is highly recommended for security and flexibility

## Command Line Alternative for Building AAB

If you prefer command line over Android Studio:

```bash
# Navigate to your project directory
cd C:\Users\Owner\source\repos\...\...\MindPairs

# Build release AAB (make sure signing config is in build.gradle.kts)
.\gradlew bundleRelease

# The AAB will be created at:
# app\build\outputs\bundle\release\app-release.aab
```

## Troubleshooting
- **Keystore errors**: Verify keystore file path, password, and alias are correct
- **Signing errors**: Ensure keystore is in JKS format (not PKCS12)
- **Upload errors**: Check that package name matches between app and Play Console
- **App Signing issues**: Follow Google Play's App Signing setup wizard carefully

## Converting PKCS12 to JKS (if needed)
```bash
keytool -importkeystore -srckeystore your-keystore.p12 -srcstoretype PKCS12 -destkeystore release-keystore.jks -deststoretype JKS
```
