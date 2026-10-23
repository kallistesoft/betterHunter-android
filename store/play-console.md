# Google Play Console: BetterHunter

Account: KallisteSoft (organization), login contact@kallistesoft.ca, developer ID 5567153248845759571.
App: "BetterHunter: Species & Rules", package `ca.kallistesoft.hunt`, app ID 4974570275446446704.
Created 2026-10-21: default language en-CA, App, Free, Play App Signing.

## Store listing (draft)
- en-CA and fr-CA: name, short and full description, icon, feature graphic and 5 screenshots each.
  The texts are in `listing.md`; the images are in this folder (`feature-en.png`, `feature-fr.png`, `screens/`).
- **To replace before release:** the icon (upload `icon-512.png`, the new buck logo, from `design/logo/`), the 5
  screenshots per language (they show the Flutter prototype; take new ones from this app) and the feature graphics
  (they show the old icon), and remove the RPAL / CRFSC mentions from the descriptions (see `listing.md`).
- Category: Education. Contact: contact@kallistesoft.ca, https://kallistesoft.ca

## App content declarations (all done 2026-10-21)
| Declaration | Answer |
|---|---|
| Privacy policy | https://kallistesoft.ca/en/privacy/ |
| Ads | Yes (AdMob, **not integrated yet**) |
| App access | No restrictions, no sign-in |
| Content rating (IARC) | Violence against animals implied in text; rare references to drugs/alcohol; primarily educational. Result: ESRB 10+, PEGI 3, USK 6, ClassInd L, IARC 3+ |
| Target audience | 18+ only ("block minors" option left unticked) |
| Government app | No |
| Financial features | None |
| Health apps | None (first-aid questions are teaching content) |
| Advertising ID | Yes: advertising/marketing, analytics, fraud prevention/security/compliance |

### Data safety
The app itself collects nothing; everything declared comes from the AdMob SDK
([Google's guidance](https://developers.google.com/admob/android/privacy/play-data-disclosure)).
- Collects or shares data: Yes. Encrypted in transit: Yes. Account creation: none, no external sign-in.
- Data types: approximate location (from IP), app interactions, diagnostics, device or other IDs.
- For each type: collected and shared, not ephemeral, required.
  Purposes for both collection and sharing: analytics, advertising or marketing, fraud prevention/security/compliance.

**Keep in sync:** if the app ships without AdMob, change Ads and Advertising ID to "No" and Data safety to
"no data collected". If another SDK is added (analytics, crash reporting…), update Data safety.

## Still to do
1. AdMob: the account is created by the owner; then integrate the SDK, add app-ads.txt and a consent form (UMP).
2. Upload keystore and a signed release AAB.
3. Internal or closed testing track, then production.
4. ~~Launcher icons~~ done: the app's icon is the buck logo, a vector adaptive icon in the app
   (`app/src/main/res/drawable/ic_launcher_foreground.xml`); the store's 512 px icon is `icon-512.png`.

## Console tips
- A floating Adobe PDF extension button covers the bottom-right buttons; Save/Next can be clicked another way.
- Open declarations from App content → overview; guessed URLs redirect to the home page.
