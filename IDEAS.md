# Product ideas: two parts and monetization

Thinking out loud, not a plan: nothing here is decided or scheduled. Written 2026-10-22.

## The idea: two parts in one app

1. **Prep:** exam preparation (what the app does today: PAL, provincial hunter education, photo ID).
2. **In the field:** tools for the season, once the licence is in hand.

Why it helps: an exam-prep app is used for a few weeks and then deleted once the exam is passed. A field part gives a
reason to keep the app all season and to come back every year.

The current structure already fits: the dashboard's two hub cards become "Prep" and "In the field", each part can be
its own feature module, and feature flags can switch features on, off or behind a gate remotely.

## Prep: one assessment a day, a rewarded ad for more

One free assessment a day; watching a rewarded ad unlocks another one, with no limit.

**For it:**

- Rewarded ads pay the best per view (often several times an interstitial), and the user chooses to watch: it feels
  fair, not intrusive.
- "No limit with an ad" never blocks someone cramming the week before the exam, when the app matters most to them.

**To decide:**

- **What counts as an assessment.** Gate only the **mock exams** (the timed, exam-like value). Keep practice, review of
  mistakes and the photo of the day free, so learning is never blocked; otherwise users may leave before seeing the
  value.
- **Offline.** A rewarded ad needs the network; hunters often study without it. If no ad can load, let the extra exam
  through rather than lock the user out.
- **The daily counter** can be bypassed (changing the phone's clock). Acceptable at this scale; not worth a server.
- **Don't stack ad formats.** With rewarded ads gating mock exams, the interstitial after a test becomes too much:
  one format per flow.
- **Play policy:** the reward is clearly stated before the ad and always granted after it.

## In the field: features with ads, a full unlock removes them

**For it:** a one-time full unlock matches hunters' use (every season, for years), and a recurring tool justifies a
higher price than exam prep alone.

**Possible features:** season dates and rules by zone, legal shooting hours (sunrise and sunset by location), weather
and wind, harvest and tag log, gear checklists, species ID (exists), offline maps and waypoints, calls.

**What it changes:**

- **Accuracy of regulations is the biggest risk.** A wrong season date or shooting hour can cost someone a fine. Same
  review discipline as the questions, an "always check the official regulations" notice, and online content updates
  (FOLLOW-UP.md, section 4) since rules change every year.
- **Privacy:** location, a harvest log or photos make it a different app: location permission, new Data safety and
  privacy policy answers, and the log stored in Room. Offline maps are a project of their own.

## Monetization

### One purchase for both parts

A single **BetterHunter Pro** purchase (no ads, unlimited mock exams, all field features) is easier to understand and
to sell than two unlocks, and someone who paid during prep is already a customer when the season starts. A cheaper
prep-only unlock can come later if exam students won't pay for field features. Keep both free parts genuinely useful:
ratings matter in a niche this small.

Paying goes through **Google Play Billing** (required for digital features); Google keeps 15% of the first million
dollars a year. Play restores purchases on the user's other devices with the same Google account; a server is only
needed for strict purchase verification or sharing across platforms.

Paid also means expectations: people paying for an unofficial study aid expect accuracy, so the disclaimer and the
question review matter even more.

### Order of magnitude (rough estimates)

**Ads:** revenue ≈ daily active users × ads per user per day × eCPM ÷ 1,000. Canada is a good ad market; an
interstitial earns roughly a few dollars to ~$15 CAD per thousand views, a banner under $1 (and clutters the quiz).

| Daily active users | Ads per day (1.5 each) | eCPM | Per month |
|---|---|---|---|
| 300 | 450 | ~$8 | ~$100 CAD |
| 1,000 | 1,500 | ~$8 | ~$350 CAD |
| 5,000 | 7,500 | ~$8 | ~$1,800 CAD |

What lowers it for this app: short use per user (until the exam), strong seasonality (courses peak before the fall
seasons), a niche audience (tens of thousands of people preparing each year, not millions), and some advertisers
excluding firearm-related content (lower fill and eCPM than average).

**One-time unlock** ($4.99 to $9.99 CAD): often 2 to 5% of active users buy when the free part is useful. For example
1,000 new users a month × 4% × $6.99 ≈ $240 CAD a month after Google's share. Together with ads for free users, it
usually beats ads alone.

**Realistic expectation:** ads cover hosting and accounts with something left; the paid unlock and the field part (use
all season, every year) are what could make it a real income.
