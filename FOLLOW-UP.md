# Follow-up: console setup

Steps to do outside the code, in the Firebase, AdMob and Play consoles. They use the application ID
`ca.kallistesoft.hunt`, the one registered in Play Console (it can never change); debug builds are
`ca.kallistesoft.hunt.debug`.

- [1. Firebase project (feature flags)](#1-firebase-project-feature-flags)
- [2. AdMob (ads)](#2-admob-ads)
- [3. Play Console](#3-play-console)
- [4. Possible next step: questions from an online API](#4-possible-next-step-questions-from-an-online-api)

---

## 1. Firebase project (feature flags)

The app reads its feature flags from Firebase Remote Config. Until these steps are done it runs with every flag at
its default (on).

### Create the project

1. Open the [Firebase console](https://console.firebase.google.com/) with the KallisteSoft Google account
   (contact@kallistesoft.ca) and click **Create a project**.
2. Name it **BetterHunter**. Accept the project ID Firebase proposes (or set `betterhunter-kallistesoft`); it can't be
   changed later.
3. **Google Analytics:** turn it off. The app doesn't use it, and leaving it off keeps the Play data safety form as
   it is. (It can be enabled later if Remote Config conditions on audiences are ever needed.)
4. Click **Create project**, then **Continue**.

### Register the two Android apps

5. On the project overview, click the **Android** icon (**Add app**).
6. **Android package name:** `ca.kallistesoft.hunt`. **App nickname:** `BetterHunter`. Leave the SHA-1 empty (Remote
   Config doesn't need it). Click **Register app**.
7. Skip the download and the SDK steps for now (the SDK is already in the code): click **Next** until **Continue to
   console**.
8. Add a second Android app the same way: package `ca.kallistesoft.hunt.debug`, nickname `BetterHunter Dev`.
9. Open **Project settings** (gear icon) → **General** → **Your apps**, select either app and click
   **google-services.json**. This file now describes both apps.

### Give the file to the build

10. Put the file at `app/google-services.json`. It holds identifiers, not secrets (access is restricted to the
    registered package names), so it can be committed to this private repo. Commit it, or keep it local and rely on
    the CI secret below.
11. For CI: on GitHub, **kallistesoft/betterHunter-android** → **Settings** → **Secrets and variables** → **Actions**
    → **New repository secret**. Name `GOOGLE_SERVICES_JSON`, value: the whole content of the file.
12. Build once (`./gradlew assembleDebug`, or push and let CI run). With the file present the Google Services plugin is
    applied; if this first build fails on that plugin, tell me: its version (in `gradle/libs.versions.toml`) may need
    a bump for AGP 9.

### Create the flags

13. In the console: **Run** (left menu) → **Remote Config** → **Create configuration**.
14. Add one parameter per flag, **Data type: Boolean**, **Default value: true**, then **Save**:

    | Parameter name | Description |
    |---|---|
    | `photo_of_the_day` | Photo of the day on the dashboard |
    | `recognition_games` | "Can you recognise them?" hub and games |
    | `report_question` | "Report a problem" on each question |

15. Click **Publish changes**.

### Check it works

16. Install the debug build, open the app, then in Remote Config set `photo_of_the_day` to `false` and publish. The
    photo of the day disappears within seconds (real-time updates), or at the latest when the app comes back to the
    foreground. Set it back to `true`.

### Using flags later

- **Turn a feature off for everyone:** set its parameter to `false` and publish. No release needed.
- **Debug only:** **Add new** → **Conditional value** → condition *App* is `ca.kallistesoft.hunt.debug`.
- **Gradual rollout:** conditional value with *User in random percentile* ≤ 10 %, then raise it.
- **New flag:** ask for it in code (one line in `FeatureFlag.kt`), then create the parameter here with the same name.
  Parameters the app doesn't know are ignored, and flags missing in the console keep their in-app default.

---

## 2. AdMob (ads)

Play Console already declares that the app contains ads (AdMob) and uses the advertising ID: the first release will
include them. The ads aren't in the app yet: these steps create everything AdMob needs, and give the IDs that go into the code. The planned placement, as in
the Flutter prototype, is one **interstitial between a finished test and its results**, never during a question.

### Create the account

1. Go to [AdMob](https://admob.google.com/) with the KallisteSoft Google account and sign up: country **Canada**,
   time zone, currency **CAD**, accept the terms.
2. **Payments** (left menu): add the business name and address, then the bank account once AdMob asks for it (payments
   start when earnings reach the threshold). Fill in the **tax information** AdMob asks for (as a Canadian company,
   usually the W-8BEN-E form).

### Add the app

3. **Apps** → **Add app** → platform **Android**.
4. *Is the app listed on a supported app store?*
   - Not published yet: **No**. Name it **BetterHunter**. Once the app is live on Play, open **App settings** →
     **Add app store details** and link `ca.kallistesoft.hunt`. AdMob only serves real ads once the app is linked
     and reviewed.
   - Already published: **Yes**, search `ca.kallistesoft.hunt`.
5. Leave **user metrics** off (it needs Firebase Analytics, which we don't use).
6. Note the **App ID**: `ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY`.

### Create the ad unit

7. In the app: **Ad units** → **Add ad unit** → **Interstitial**.
8. Name: `Quiz finished`. Open **Advanced settings** → **Frequency capping** → **1 impression per 3 minutes per user**
   (so a user doing short practice runs isn't shown an ad after each one).
9. **Create ad unit** and note the **ad unit ID**: `ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ`.

### app-ads.txt

10. **Apps** → **View all apps** → **app-ads.txt** column → **How to set up app-ads.txt**: copy the line, which looks
    like `google.com, pub-XXXXXXXXXXXXXXXX, DIRECT, f08c47fec0942fa0`.
11. Publish it at **https://kallistesoft.ca/app-ads.txt** (the root of the developer website given in the Play
    listing; not on a subdomain). Plain text, one line per ad network.
12. AdMob checks the file after the app is linked to Play; verification can take a few days. Without it, ads are
    limited.

### Privacy and consent

13. **Privacy & messaging** → **European regulations** → **Create message**: choose the app, the languages
    **English** and **French**, the privacy policy URL `https://kallistesoft.ca/en/privacy/`, then **Publish**. It is
    shown only to users in the EEA, the UK and Switzerland. It's needed even for a Canadian app if Play distribution
    includes those countries.
14. **US state regulations** → create and publish a message the same way (shown only in the relevant US states).
15. Update the privacy policy pages (FR and EN) to mention AdMob, the advertising ID and the consent choice.

### Ad content

16. **Blocking controls** → **Content** → **Maximum ad content rating**: **PG** (the app's IARC rating is 3+/10+).
17. **Sensitive categories**: block at least **Gambling & betting**, **Dating** and **Get rich quick**.

### Test without risking the account

18. **Settings** → **Test devices** → **Add test device**: your phone, with its advertising ID (Android settings →
    Google → Ads). Never tap real ads in your own app.
19. Debug builds will use Google's sample IDs, which always serve test ads:
    App ID `ca-app-pub-3940256099942544~3347511713`, interstitial `ca-app-pub-3940256099942544/1033173712`.

### Hand over

20. Send the **App ID** (step 6) and the **interstitial ad unit ID** (step 9). The code work then is: add the
    Google Mobile Ads SDK and the UMP consent SDK, the App ID in the manifest, the ad unit per build type (test IDs in
    debug), the consent form at launch, and the interstitial after a finished test. An `ads_enabled` Remote Config
    flag can switch ads off without a release.

---

## 3. Play Console

The app **BetterHunter: Species & Rules** (`ca.kallistesoft.hunt`) already has its listing, content rating and
declarations (see `store/play-console.md` in the hunt repo). Left to do:

1. **Upload key:** create the keystore (`keytool -genkeypair -v -keystore release.jks -alias upload -keyalg RSA
   -keysize 4096 -validity 10000`), keep it and its passwords safe (password manager plus an offline copy), then set
   `keystore.properties` locally and the CI secrets `BH_KEYSTORE_BASE64`, `BH_KEYSTORE_PASSWORD`, `BH_KEY_ALIAS`,
   `BH_KEY_PASSWORD` (see the README).
2. **First release:** **Test and release** → **Testing** → **Internal testing** → create a release and upload the AAB
   from CI (`app-release.aab`, signed). Add testers by email, then promote to closed testing and production.
3. **Force update file:** publish `https://hunt.kallistesoft.ca/android/update.json` with `{"minVersionCode": 0}`
   (and the same for `update-debug.json`) so the URL exists before the first release.
4. **Before releasing:** check the Play Console declarations against what the release really does (below).

### Before releasing: Play Console vs. the app

The declarations were entered on 2026-10-21, before the native app existed. When the release candidate is ready,
compare them with what that build really does, and fix whichever side is wrong.

1. **List what the release does:**
   - Upload the AAB to internal testing, then **Test and release** → **App bundle explorer**: note its permissions
     (look for `AD_ID`, `INTERNET`, location) and the SDKs Play detects (AdMob, Firebase…).
   - Note what leaves the device: servers contacted, identifiers sent, anything typed by the user. Today: the update
     check (hunt.kallistesoft.ca), Firebase Remote Config (installation ID) and, once added, AdMob.
2. **Compare with Play Console**, **Policy and programs** → **App content**:
   - **Ads** and **Advertising ID**: Yes only if the AdMob SDK and `AD_ID` are in the bundle.
   - **Data safety**: every data type sent off the device is declared, with the right *collected / shared*,
     *required / optional* and purposes (Remote Config's installation ID: app functionality; AdMob: Google's AdMob
     guidance); nothing is declared that the app doesn't do.
   - **Target audience**, **Content rating**, **App access**: still true for this version.
3. **Privacy policy** (FR and EN on kallistesoft.ca) describes the same things: data kept on the device, the servers
   contacted and why, ads and consent if present, contact address.
4. **Fix and record:** update the declarations or the app, save, and note the answers given (date and version) in the
   hunt repo's `store/play-console.md` so the next release starts from them.

---

## 4. Possible next step: questions from an online API

Not started; to decide. Today the questions ship inside the app, so a corrected question or a new season's rules need
a release. Serving them online lets the app update its content by itself.

### Recommended: a static API on the GoDaddy site

The hunt repo already deploys `hunt.kallistesoft.ca` to GoDaddy on every push. The same deploy can publish the
approved questions as files, which the app reads like an API. No database, no server code, nothing to secure or back
up, and the review workflow stays (only approved questions are published).

```
https://hunt.kallistesoft.ca/api/v1/manifest.json      {"version": 12, "format": 1, "minAppVersion": 3, "sha256": "…"}
https://hunt.kallistesoft.ca/api/v1/content-12.json    exam recipes and questions
https://hunt.kallistesoft.ca/api/v1/wildlife/<id>.jpg  photos
```

**Hunt repo (publishing):**

1. `python -m qgen bundle` also writes the content pack (exams and approved questions) and the manifest: content
   version, format version, oldest app versionCode that can read it, SHA-256 of the pack.
2. The GoDaddy deploy workflow copies `api/v1/` with the web app. Packs are never overwritten: a change is a new
   version, so an app never reads a half-uploaded file.

**App (reading):**

1. Questions move to **Room** (`core:data`): exams, questions and their texts, replaced in one transaction per
   content version.
2. A **WorkManager** job (daily, and at launch) reads the manifest. If its version is newer and its format is one the
   app knows, it downloads the pack, checks the SHA-256 and installs it. On any failure the app keeps its questions.
3. A **pack still ships in the APK**, so the app works on first launch and with no network (hunters study offline).
4. **Photos** are downloaded with Coil and kept in its disk cache; the bundled ones stay as a fallback.
5. **Progress** is unchanged: it's keyed by question ID, so a corrected question keeps its history and a removed one
   simply leaves the readiness count.
6. **Emergency:** a Remote Config parameter `content_min_version` (Firebase, section 1) makes the app sync at once
   when a wrong rule must be corrected quickly.

Only `ContentRepository` gets a new implementation; screens and ViewModels don't change.

### Later, if needed: a database and a small backend

Worth it only for features a static API can't offer:

- an admin page to edit questions in a browser,
- accounts, with progress synced across devices or shared with an instructor,
- question reports and statistics stored on a server (most missed questions),
- content chosen by the server for each user.

Options, with what each means to run:

| Option | Fits | Cost and care |
|---|---|---|
| GoDaddy shared hosting: PHP + MySQL | Admin page, simple API | Included in the plan; you own security (protected admin, prepared SQL), updates and backups |
| Firebase: Firestore + Cloud Functions + Auth | Accounts and sync, already in the project | No server to maintain; billed per read and write beyond the free quota |

Keep the API versioned (`/api/v1/…`) and the content format stable: a backend can later serve the same manifest and
pack from a database, and the app won't need to change.

### Store and privacy impact

- Static API: the app downloads public files and sends nothing about the user. The Play data safety form doesn't
  change.
- Accounts or server-side reports: declare the collected data (email, user IDs, app activity) in data safety and in
  the privacy policy before releasing.

