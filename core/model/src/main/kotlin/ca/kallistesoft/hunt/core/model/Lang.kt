package ca.kallistesoft.hunt.core.model

/** Content language. The question banks are written in French first, with an English translation. */
enum class Lang(val code: String) {
    FR("fr"),
    EN("en");

    companion object {
        /** French for any French locale (fr, fr-CA, fr-FR…), English otherwise. */
        fun forLanguage(language: String?): Lang = if (language == "fr") FR else EN
    }
}
