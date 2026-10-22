package ca.kallistesoft.hunt.core.model

/** Official study manual the questions are written from, keyed by the file name used in `sourceRef`. */
data class Manual(val titles: Map<Lang, String>, val url: String) {
    fun title(lang: Lang): String = titles[lang] ?: titles.getValue(Lang.FR)
}

val manuals: Map<String, Manual> = mapOf(
    "cfsc_fr.pdf" to Manual(
        mapOf(Lang.FR to "Manuel de l'étudiant CCSMAF (GRC, 2014)", Lang.EN to "CFSC student manual (RCMP, 2014)"),
        "https://fedecp.com/media/2186/manuel-ccsmaf-francais-comprenant-la-section-sur-les-armes-a-feu-a-autorisation-restreinte.pdf",
    ),
    "icaf_fr.pdf" to Manual(
        mapOf(Lang.FR to "Manuel ICAF (FédéCP, 2020)", Lang.EN to "ICAF manual (FédéCP, 2020)"),
        "https://fedecp.com/media/7535/icaf-fr-3-08-2020.pdf",
    ),
    "ab_hunting_2026.pdf" to Manual(
        mapOf(
            Lang.FR to "Guide des règlements de chasse de l'Alberta 2026",
            Lang.EN to "2026 Alberta Guide to Hunting Regulations",
        ),
        "https://www.albertaregulations.ca/2026-Alberta-Hunting-Regulations.pdf",
    ),
)

/** A readable source line and a link that opens the manual at the right PDF page. */
data class SourceRef(val label: String, val url: String) {
    companion object {
        private val pattern = Regex("""^(\S+) p\.(\d+)$""")

        /** Parses `"cfsc_fr.pdf p.61"`; null for an unknown manual or another format. */
        fun parse(ref: String, lang: Lang): SourceRef? {
            val match = pattern.matchEntire(ref.trim()) ?: return null
            val (file, page) = match.destructured
            val manual = manuals[file] ?: return null
            return SourceRef("${manual.title(lang)}, p. $page", "${manual.url}#page=$page")
        }
    }
}
