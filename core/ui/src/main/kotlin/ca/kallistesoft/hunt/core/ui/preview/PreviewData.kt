package ca.kallistesoft.hunt.core.ui.preview

import ca.kallistesoft.hunt.core.model.Exam
import ca.kallistesoft.hunt.core.model.ImageCredit
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.Question
import ca.kallistesoft.hunt.core.model.QuestionText
import ca.kallistesoft.hunt.core.ui.ExamSummary

/** Realistic sample data for previews and screenshot tests (taken from the bundled banks). */
object PreviewData {
    val pal = Exam(
        id = "PAL",
        names = mapOf(Lang.FR to "Examen PPA (CCSMAF, armes sans restriction)", Lang.EN to "PAL exam (CFSC, non-restricted)"),
        scopes = listOf("federal"),
        shortNames = mapOf(Lang.FR to "PPA", Lang.EN to "PAL"),
    )
    val icaf = Exam(
        id = "ICAF",
        names = mapOf(Lang.FR to "Examen de chasse · Québec", Lang.EN to "Hunting exam · Québec"),
        scopes = listOf("hunter-common", "QC"),
        shortNames = mapOf(Lang.FR to "Examen de chasse", Lang.EN to "Hunting exam"),
    )
    val abhe = Exam(
        id = "ABHE",
        names = mapOf(Lang.FR to "Examen de chasse · Alberta", Lang.EN to "Hunting exam · Alberta"),
        scopes = listOf("hunter-common", "AB"),
        shortNames = mapOf(Lang.FR to "Examen de chasse", Lang.EN to "Hunting exam"),
    )
    val photoGame = Exam(
        id = "ID",
        names = mapOf(Lang.FR to "Qu'est-ce que c'est?", Lang.EN to "What is it?"),
        scopes = listOf("wildlife-id"),
        questionCount = 20,
        minutes = 15,
    )

    val palSummary = ExamSummary(pal, questionCount = 303, readiness = 0.62f, weakCount = 7)
    val icafSummary = ExamSummary(icaf, questionCount = 241, readiness = 0.18f, weakCount = 0)
    val abheSummary = ExamSummary(abhe, questionCount = 285, readiness = 0f, weakCount = 0)
    val photoGameSummary = ExamSummary(photoGame, questionCount = 66, readiness = 0.4f, weakCount = 3)

    val question = Question(
        id = "federal-ann-def-001-q1",
        scope = "federal",
        chapter = "ann-def",
        texts = mapOf(
            Lang.FR to QuestionText(
                q = "Que faut-il présenter pour acheter des munitions au Canada lorsqu'on est résident?",
                choices = listOf(
                    "Une carte d'identité avec photo seulement",
                    "Un permis d'armes à feu valide",
                    "Un certificat du chasseur provincial",
                    "Une preuve d'adhésion à un club de tir",
                ),
                explanation = "Toute personne qui veut acheter des munitions au Canada doit détenir un permis d'armes à feu valide.",
            ),
            Lang.EN to QuestionText(
                q = "What must a resident show to buy ammunition in Canada?",
                choices = listOf(
                    "Photo ID only",
                    "A valid firearms licence",
                    "A provincial hunter certificate",
                    "Proof of shooting club membership",
                ),
                explanation = "Anyone buying ammunition in Canada must hold a valid firearms licence.",
            ),
        ),
        answerIndex = 1,
        sourceRef = "cfsc_fr.pdf p.341",
        sourceUrl = "https://laws-lois.justice.gc.ca/eng/acts/F-11.6/FullText.html",
    )

    val photoQuestion = Question(
        id = "id-white-tailed-deer",
        scope = "wildlife-id",
        chapter = "hoofed",
        texts = mapOf(
            Lang.FR to QuestionText(
                q = "Quel est cet animal?",
                choices = listOf("Cerf mulet", "Cerf de Virginie", "Wapiti", "Antilocapre d'Amérique"),
                explanation = "Cerf brun-roux au ventre blanc; sa large queue se dresse comme un drapeau quand il fuit.",
                habitat = "Sud du Canada. Forêts mixtes, lisières, terres agricoles et boisés de ferme.",
                confuse = "le cerf mulet, aux grandes oreilles et à la queue mince à bout noir.",
            ),
            Lang.EN to QuestionText(
                q = "What animal is this?",
                choices = listOf("Mule deer", "White-tailed deer", "Elk", "Pronghorn"),
                explanation = "Reddish-brown deer with a white belly; its broad tail is raised like a flag when it runs.",
                habitat = "Southern Canada. Mixed forests, forest edges, farmland and woodlots.",
                confuse = "the mule deer, with large ears and a thin black-tipped tail.",
            ),
        ),
        answerIndex = 1,
        sourceUrl = "https://www.hww.ca/wildlife/mammals/white-tailed-deer/",
        image = "wildlife/2ff1851c66ea.jpg",
        imageCredit = ImageCredit("USDA photo by Scott Bauer", "Public domain", "https://commons.wikimedia.org/wiki/File:White-tailed_deer.jpg"),
        imageAspect = 640f / 486f,
    )
}
