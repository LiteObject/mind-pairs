# Google Play Deployment Guide

## The Issue
The error "Package not found: com.example.mindpairs" occurs because Google Play Console requires the first release of any app to be uploaded manually through the web interface. Automated uploads via GitHub Actions only work for subsequent releases.

## Solution Steps

### Step 1: Create a Release Keystore (if you haven't already)
```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias your-key-alias
```

### Step 2: Build a Release AAB Locally
1. Open terminal in your project root
2. Run: `./gradlew bundleRelease`
3. The AAB will be created at: `app/build/outputs/bundle/release/app-release.aab`

### Step 3: Manual Upload to Google Play Console
1. Go to [Google Play Console](https://play.google.com/console)
2. Click "Create app"
3. Fill in app details:
   - App name: "Mind Pairs"
   - Default language: English (United States)
   - App or game: Game
   - Free or paid: Free
4. Accept Play Console Developer Policy
5. Create the app
6. Go to "Release" > "Testing" > "Internal testing"
7. Click "Create new release"
8. Upload your AAB file (`app-release.aab`)
9. Add release notes
10. Review and rollout to internal testing

### Step 4: Set up GitHub Secrets
In your GitHub repository settings, add these secrets:
- `KEYSTORE_FILE`: Base64 encoded keystore file
- `KEYSTORE_PASSWORD`: Your keystore password
- `KEY_ALIAS`: Your key alias
- `KEY_PASSWORD`: Your key password
- `SERVICE_ACCOUNT_JSON`: Google Play Console service account JSON

### Step 5: Test Automated Deployment
After the manual upload is successful, your GitHub Actions workflow will work for subsequent releases.

## Important Notes
- The package name `com.example.mindpairs` in your app must match exactly in Google Play Console
- Keep your keystore file safe - you cannot update your app without it
- The first upload must always be done manually through the web interface
- Service account must have "Release Manager" role in Google Play Console

## Troubleshooting
If you get keystore errors, verify:
1. Keystore file is properly base64 encoded
2. Password and alias are correct
3. Keystore is in JKS format (not PKCS12)

## Converting PKCS12 to JKS (if needed)
```bash
keytool -importkeystore -srckeystore your-keystore.p12 -srcstoretype PKCS12 -destkeystore release-keystore.jks -deststoretype JKS
```
