package com.example.domain

/**
 * Bilingual Arabic/English Search Engine & Curriculum RAG Context Retriever.
 * Normalizes Arabic characters (alef variants, ta marbuta, ya/alef maqsura, tashkeel)
 * so students can search naturally in either Arabic or English.
 */
object CurriculumSearchEngine {

    private val DIACRITICS_REGEX = Regex("[\\u064B-\\u065F\\u0670]")

    fun normalizeArabicAndLower(input: String): String {
        return input
            .replace(DIACRITICS_REGEX, "")
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ة', 'ه')
            .replace('ى', 'ي')
            .lowercase()
            .trim()
    }

    fun search(
        query: String,
        gradeIdFilter: String? = null,
        subjectIdFilter: String? = null,
        semesterFilter: Int? = null,
        unitIdFilter: String? = null,
        extraAssessments: List<OfficialAssessmentItem> = emptyList()
    ): List<SearchHit> {
        val normalizedQuery = normalizeArabicAndLower(query)
        val tokens = normalizedQuery.split(Regex("\\s+")).filter { it.length >= 2 }

        val hits = mutableListOf<SearchHit>()

        // 1. Search Lessons & Units
        for (subject in OfficialEgyptianCurriculumCatalog.subjects) {
            if (gradeIdFilter != null && subject.gradeId != gradeIdFilter) continue
            if (subjectIdFilter != null && subject.id != subjectIdFilter) continue
            if (semesterFilter != null && subject.semester != semesterFilter) continue

            for (unit in subject.units) {
                if (unitIdFilter != null && unit.id != unitIdFilter) continue
                for (chapter in unit.chapters) {
                    for (lesson in chapter.lessons) {
                        val corpus = normalizeArabicAndLower(
                            listOf(
                                subject.nameAr, subject.nameEn,
                                unit.titleAr, unit.titleEn,
                                chapter.titleAr, chapter.titleEn,
                                lesson.titleAr, lesson.titleEn,
                                lesson.contentAr, lesson.contentEn,
                                lesson.formulasAndDefinitionsAr.joinToString(" "),
                                lesson.formulasAndDefinitionsEn.joinToString(" "),
                                lesson.exercisesAr.joinToString(" "),
                                lesson.exercisesEn.joinToString(" ")
                            ).joinToString(" ")
                        )
                        val matchScore = if (normalizedQuery.isBlank()) 1 else scoreMatch(corpus, normalizedQuery, tokens)
                        if (matchScore > 0) {
                            hits += SearchHit(
                                id = lesson.id,
                                kind = "LESSON",
                                titleAr = lesson.titleAr,
                                titleEn = lesson.titleEn,
                                subtitleAr = "${subject.nameAr} • ${unit.titleAr}",
                                subtitleEn = "${subject.nameEn} • ${unit.titleEn}",
                                gradeId = subject.gradeId,
                                subjectId = subject.id,
                                semester = subject.semester,
                                lesson = lesson,
                                score = matchScore
                            )
                        }
                    }
                }
            }
        }

        // 2. Search Official Assessments
        val allAssessments = OfficialEgyptianCurriculumCatalog.officialAssessments + extraAssessments
        for (assessment in allAssessments) {
            if (gradeIdFilter != null && assessment.gradeId != gradeIdFilter) continue
            if (subjectIdFilter != null && assessment.subjectId != subjectIdFilter) continue
            if (semesterFilter != null && assessment.semester != semesterFilter) continue

            val corpus = normalizeArabicAndLower(
                listOf(
                    assessment.titleAr, assessment.titleEn,
                    assessment.subjectNameAr, assessment.subjectNameEn,
                    assessment.periodLabelAr, assessment.periodLabelEn,
                    assessment.sampleQuestionsAr.joinToString(" "),
                    assessment.sampleQuestionsEn.joinToString(" ")
                ).joinToString(" ")
            )
            val matchScore = if (normalizedQuery.isBlank()) 1 else scoreMatch(corpus, normalizedQuery, tokens)
            if (matchScore > 0) {
                hits += SearchHit(
                    id = assessment.id,
                    kind = "ASSESSMENT",
                    titleAr = assessment.titleAr,
                    titleEn = assessment.titleEn,
                    subtitleAr = "${assessment.assessmentType.labelAr} • ${assessment.periodLabelAr}",
                    subtitleEn = "${assessment.assessmentType.labelEn} • ${assessment.periodLabelEn}",
                    gradeId = assessment.gradeId,
                    subjectId = assessment.subjectId,
                    semester = assessment.semester,
                    assessment = assessment,
                    score = matchScore
                )
            }
        }

        return hits.sortedByDescending { it.score }
    }

    /**
     * Retrieves grounded curriculum excerpts for Firebase AI Logic RAG prompts.
     */
    fun retrieveRagContext(
        query: String,
        gradeId: String,
        subjectId: String?,
        selectedLesson: LessonSpec?,
        publishedCloudVersions: List<CurriculumVersionRecord>,
        isArabic: Boolean
    ): Pair<String, List<String>> {
        val citations = mutableListOf<String>()
        val sb = StringBuilder()

        if (selectedLesson != null) {
            val title = if (isArabic) selectedLesson.titleAr else selectedLesson.titleEn
            val content = if (isArabic) selectedLesson.contentAr else selectedLesson.contentEn
            val formulas = if (isArabic) selectedLesson.formulasAndDefinitionsAr else selectedLesson.formulasAndDefinitionsEn
            val objectives = if (isArabic) selectedLesson.objectivesAr else selectedLesson.objectivesEn
            val originLabel = if (isArabic) {
                if (isArabic) ContentOrigin.OFFICIAL_MOE.labelAr else ContentOrigin.OFFICIAL_MOE.labelEn
            } else {
                selectedLesson.englishOrigin.labelEn
            }
            sb.appendLine("[Source 1 | $title | Origin: $originLabel | URL: ${selectedLesson.officialSourceUrl}]")
            sb.appendLine("Objectives: ${objectives.joinToString("; ")}")
            sb.appendLine("Official Content: $content")
            sb.appendLine("Key Rules/Formulas: ${formulas.joinToString("; ")}")
            sb.appendLine()
            citations += "$title (${selectedLesson.officialSourceUrl})"
        }

        val searchResults = search(
            query = query,
            gradeIdFilter = gradeId,
            subjectIdFilter = subjectId
        ).filter { it.lesson != null && it.lesson.id != selectedLesson?.id }.take(3)

        searchResults.forEachIndexed { idx, hit ->
            val l = hit.lesson ?: return@forEachIndexed
            val title = if (isArabic) l.titleAr else l.titleEn
            val content = if (isArabic) l.contentAr else l.contentEn
            val formulas = if (isArabic) l.formulasAndDefinitionsAr else l.formulasAndDefinitionsEn
            sb.appendLine("[Source ${idx + 2} | $title | URL: ${l.officialSourceUrl}]")
            sb.appendLine(content)
            if (formulas.isNotEmpty()) sb.appendLine("Formulas: ${formulas.joinToString("; ")}")
            sb.appendLine()
            citations += "$title (${l.officialSourceUrl})"
        }

        // Also include any Admin-published Firestore CurriculumVersions for this grade/subject
        val matchingCloud = publishedCloudVersions.filter {
            it.status == "PUBLISHED" &&
                it.gradeId == gradeId &&
                (subjectId == null || it.subjectId == subjectId)
        }.take(2)

        matchingCloud.forEachIndexed { idx, ver ->
            sb.appendLine("[Cloud Published MOE Version v${ver.versionNumber} | URL: ${ver.sourceUrl}]")
            sb.appendLine(ver.extractedUnitsSummary.take(1800))
            sb.appendLine()
            citations += "MOE v${ver.versionNumber} (${ver.sourceUrl})"
        }

        return sb.toString() to citations.distinct()
    }

    private fun scoreMatch(corpus: String, fullQuery: String, tokens: List<String>): Int {
        var score = 0
        if (corpus.contains(fullQuery)) score += 10
        for (t in tokens) {
            if (corpus.contains(t)) score += 3
        }
        return score
    }
}
