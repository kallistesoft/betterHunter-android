package ca.kallistesoft.hunt.core.flags

/**
 * Features that can be switched on or off remotely, without a release. [key] is the Firebase Remote Config parameter
 * (Boolean); [defaultValue] applies until the console sets a value, and when Firebase isn't configured or reachable.
 */
enum class FeatureFlag(val key: String, val defaultValue: Boolean) {
    /** The "What is it?" photo on the dashboard. */
    PHOTO_OF_THE_DAY("photo_of_the_day", true),

    /** The "Can you recognise them?" hub and its games. */
    RECOGNITION_GAMES("recognition_games", true),

    /** "Report a problem" on each question. */
    REPORT_QUESTION("report_question", true),
}
