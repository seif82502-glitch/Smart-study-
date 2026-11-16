package com.example.domain

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class Stage(val nameAr: String, val nameEn: String) {
    PREPARATORY("المرحلة الإعدادية", "Preparatory Stage"),
    SECONDARY_GENERAL("الثانوية العامة", "General Secondary Stage"),
    SECONDARY_BACCALAUREATE("البكالوريا المصرية", "Egyptian Baccalaureate")
}

enum class LanguageCode { AR, EN }

enum class ContentOrigin(val labelAr: String, val labelEn: String) {
    OFFICIAL_MOE("منهج رسمي معتمد — وزارة التربية والتعليم", "Official MOE Curriculum"),
    AI_TRANSLATED("ترجمة مساعدة بالذكاء الاصطناعي (ليست وثيقة رسمية)", "AI-Translated Study Aid (Not Official MOE)"),
    AI_GENERATED("محتوى تعليمي مولّد بالذكاء الاصطناعي", "AI-Generated Educational Aid")
}

enum class VersionStatus(val labelAr: String, val labelEn: String) {
    PENDING_REVIEW("بانتظار المراجعة", "Pending Review"),
    PUBLISHED("منشور ومعتمد", "Published"),
    ARCHIVED("نسخة مؤرشفة", "Archived"),
    REJECTED("مرفوض", "Rejected")
}

enum class AssessmentType(val labelAr: String, val labelEn: String) {
    WEEKLY("تقييم أسبوعي", "Weekly Assessment"),
    MONTHLY("تقييم شهري", "Monthly Assessment"),
    GENERAL("تقييم عام / أداءات صفية", "General & Classroom Assessment"),
    MODEL_EXAM("نموذج استرشادي", "Model Exam"),
    REVISION("مراجعة نهائية", "Revision Material"),
    QUESTION_BANK("بنك أسئلة رسمي", "Official Question Bank"),
    EXAM_SPECIFICATION("مواصفات الورقة الامتحانية", "Exam Paper Specification")
}

enum class AiMode {
    SUMMARIZER,
    PROBLEM_SOLVER,
    STUDY_TUTOR,
    EXAM_GENERATOR
}

enum class SummaryType(val labelAr: String, val labelEn: String) {
    SHORT("ملخص قصير", "Short Summary"),
    DETAILED("ملخص تفصيلي", "Detailed Summary"),
    KEY_POINTS("أهم النقاط", "Important Points"),
    DEFINITIONS("التعريفات والمصطلحات", "Key Definitions"),
    FORMULAS("القوانين والمعادلات", "Rules & Formulas"),
    MIND_MAP("خريطة ذهنية", "Mind Map Outline"),
    REVISION_NOTES("ملاحظات المراجعة", "Revision Notes"),
    OBJECTIVES("نواتج التعلم المتوقعة", "Learning Objectives")
}

data class GradeSpec(
    val id: String,
    val stage: Stage,
    val levelNumber: Int,
    val nameAr: String,
    val nameEn: String,
    val tracks: List<TrackSpec>
)

data class TrackSpec(
    val id: String,
    val nameAr: String,
    val nameEn: String
)

data class SubjectSpec(
    val id: String,
    val gradeId: String,
    val trackIds: List<String>,
    val semester: Int, // 1 or 2
    val nameAr: String,
    val nameEn: String,
    val hasOfficialArabic: Boolean,
    val hasOfficialEnglish: Boolean,
    val officialMoePortalUrl: String,
    val officialBookPdfUrlAr: String?,
    val officialBookPdfUrlEn: String?,
    val publicationYear: String = "2026-2027",
    val isAddedToGpa: Boolean = true,
    val units: List<UnitSpec>
)

data class UnitSpec(
    val id: String,
    val order: Int,
    val titleAr: String,
    val titleEn: String,
    val englishOrigin: ContentOrigin,
    val chapters: List<ChapterSpec>
)

data class ChapterSpec(
    val id: String,
    val order: Int,
    val titleAr: String,
    val titleEn: String,
    val lessons: List<LessonSpec>
)

data class LessonSpec(
    val id: String,
    val order: Int,
    val titleAr: String,
    val titleEn: String,
    val objectivesAr: List<String>,
    val objectivesEn: List<String>,
    val contentAr: String,
    val contentEn: String,
    val formulasAndDefinitionsAr: List<String>,
    val formulasAndDefinitionsEn: List<String>,
    val exercisesAr: List<String>,
    val exercisesEn: List<String>,
    val revisionSummaryAr: String,
    val revisionSummaryEn: String,
    val officialSourceUrl: String,
    val publicationDate: String,
    val englishOrigin: ContentOrigin
)

data class OfficialAssessmentItem(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val gradeId: String,
    val subjectId: String,
    val subjectNameAr: String,
    val subjectNameEn: String,
    val academicYear: String,
    val semester: Int,
    val periodLabelAr: String,
    val periodLabelEn: String,
    val assessmentType: AssessmentType,
    val language: LanguageCode,
    val sourceUrl: String,
    val pdfDirectUrl: String?,
    val publishedDateLabel: String,
    val isOfficialVerified: Boolean = true,
    val sampleQuestionsAr: List<String> = emptyList(),
    val sampleQuestionsEn: List<String> = emptyList()
)

data class SearchHit(
    val id: String,
    val kind: String, // "LESSON" or "ASSESSMENT" or "SUBJECT"
    val titleAr: String,
    val titleEn: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val gradeId: String,
    val subjectId: String,
    val semester: Int,
    val lesson: LessonSpec? = null,
    val assessment: OfficialAssessmentItem? = null,
    val score: Int = 0
)

data class PracticeQuestion(
    val number: Int,
    val type: String, // MCQ, TRUE_FALSE, COMPLETE, WRITTEN, MATH
    val prompt: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

data class GeneratedPracticeExam(
    val title: String,
    val gradeName: String,
    val subjectName: String,
    val difficulty: String,
    val questions: List<PracticeQuestion>
)

// --- Firestore Models (with @DocumentId and defaults for every property) ---

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val selectedGradeId: String = "prep_1",
    val selectedTrackId: String = "general",
    val academicYear: String = "2026-2027",
    val preferredLang: String = "AR",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LessonProgress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val lessonId: String = "",
    val subjectId: String = "",
    val gradeId: String = "",
    val completed: Boolean = false,
    val masteryScore: Long = 0L,
    val weakTopic: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SavedSummary(
    @DocumentId val id: String = "",
    val userId: String = "",
    val lessonId: String = "",
    val lessonTitle: String = "",
    val subjectName: String = "",
    val summaryType: String = "SHORT",
    val content: String = "",
    val language: String = "AR",
    val origin: String = "AI_GENERATED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AiConversation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subjectId: String = "",
    val lessonId: String = "",
    val title: String = "",
    val lastMessage: String = "",
    val messageCount: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PracticeExamRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val gradeId: String = "",
    val subjectId: String = "",
    val difficulty: String = "Medium",
    val questionCount: Long = 5L,
    val score: Long = 0L,
    val isAiGenerated: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CurriculumVersionRecord(
    @DocumentId val id: String = "",
    val gradeId: String = "",
    val subjectId: String = "",
    val academicYear: String = "2026-2027",
    val semester: Long = 1L,
    val language: String = "AR",
    val origin: String = "OFFICIAL_MOE",
    val versionNumber: Long = 1L,
    val status: String = "PENDING_REVIEW",
    val sourceUrl: String = "",
    val fileSha256: String = "",
    val extractedUnitsSummary: String = "",
    val createdByUid: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class OfficialAssessmentRecord(
    @DocumentId val id: String = "",
    val titleAr: String = "",
    val titleEn: String = "",
    val gradeId: String = "",
    val subjectId: String = "",
    val academicYear: String = "2026-2027",
    val semester: Long = 1L,
    val periodLabel: String = "",
    val assessmentType: String = "WEEKLY",
    val language: String = "AR",
    val sourceUrl: String = "",
    val publishedDateLabel: String = "",
    val isOfficialVerified: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CurriculumDiffRecord(
    @DocumentId val id: String = "",
    val versionId: String = "",
    val subjectId: String = "",
    val gradeId: String = "",
    val addedTopics: String = "",
    val removedTopics: String = "",
    val modifiedLessons: String = "",
    val summary: String = "",
    val reviewed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MoeSyncLogRecord(
    @DocumentId val id: String = "",
    val checkedUrl: String = "",
    val status: String = "",
    val details: String = "",
    val checkedByUid: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AdminAuditLogRecord(
    @DocumentId val id: String = "",
    val adminUid: String = "",
    val action: String = "",
    val targetId: String = "",
    val details: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
