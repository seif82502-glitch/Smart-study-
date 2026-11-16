package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.domain.AdminAuditLogRecord
import com.example.domain.AiConversation
import com.example.domain.CurriculumDiffRecord
import com.example.domain.CurriculumVersionRecord
import com.example.domain.LessonProgress
import com.example.domain.MoeSyncLogRecord
import com.example.domain.OfficialAssessmentRecord
import com.example.domain.PracticeExamRecord
import com.example.domain.SavedSummary
import com.example.domain.UserProfile
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SmartStudyRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    private fun <T> Flow<T>.withFirestoreErrorHandling(
        operationType: OperationType,
        path: String,
        uid: String? = auth.currentUser?.uid
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val ex = firestoreError ?: (error as? Exception)
        if (ex != null && (uid == null || auth.currentUser?.uid == uid)) {
            handleFirestoreError(ex, operationType, path)
        }
        throw error
    }

    // ==================== USER PROFILE ====================

    fun observeUserProfile(userId: String = requireUserId()): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .withFirestoreErrorHandling(OperationType.GET, path, userId)
        )
    }

    suspend fun upsertUserProfile(
        displayName: String,
        selectedGradeId: String,
        selectedTrackId: String,
        academicYear: String,
        preferredLang: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snap = docRef.get().await()
        if (!snap.exists()) {
            val payload = mapOf(
                "userId" to uid,
                "displayName" to displayName.ifBlank { "Student" }.take(120),
                "selectedGradeId" to selectedGradeId,
                "selectedTrackId" to selectedTrackId,
                "academicYear" to academicYear,
                "preferredLang" to preferredLang,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }
        } else {
            val updates = mapOf(
                "displayName" to displayName.ifBlank { "Student" }.take(120),
                "selectedGradeId" to selectedGradeId,
                "selectedTrackId" to selectedTrackId,
                "academicYear" to academicYear,
                "preferredLang" to preferredLang,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.update(updates).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        }
    }

    // ==================== LESSON PROGRESS ====================

    fun observeLessonProgress(userId: String = requireUserId()): Flow<List<LessonProgress>> = flow {
        val path = "users/$userId/lesson_progress"
        emitAll(
            db.collection("users").document(userId).collection("lesson_progress")
                .snapshots()
                .map { qs ->
                    qs.toObjects(LessonProgress::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .withFirestoreErrorHandling(OperationType.LIST, path, userId)
        )
    }

    suspend fun setLessonProgress(
        lessonId: String,
        subjectId: String,
        gradeId: String,
        completed: Boolean,
        masteryScore: Int,
        weakTopic: Boolean
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("lesson_progress").document(lessonId)
        val snap = docRef.get().await()
        if (!snap.exists()) {
            val payload = mapOf(
                "userId" to uid,
                "lessonId" to lessonId,
                "subjectId" to subjectId,
                "gradeId" to gradeId,
                "completed" to completed,
                "masteryScore" to masteryScore.coerceIn(0, 100),
                "weakTopic" to weakTopic,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }
        } else {
            val updates = mapOf(
                "subjectId" to subjectId,
                "gradeId" to gradeId,
                "completed" to completed,
                "masteryScore" to masteryScore.coerceIn(0, 100),
                "weakTopic" to weakTopic,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.update(updates).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        }
    }

    // ==================== SAVED SUMMARIES ====================

    fun observeSavedSummaries(userId: String = requireUserId()): Flow<List<SavedSummary>> = flow {
        val path = "users/$userId/saved_summaries"
        emitAll(
            db.collection("users").document(userId).collection("saved_summaries")
                .snapshots()
                .map { qs ->
                    qs.toObjects(SavedSummary::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .withFirestoreErrorHandling(OperationType.LIST, path, userId)
        )
    }

    suspend fun saveSummary(
        lessonId: String,
        lessonTitle: String,
        subjectName: String,
        summaryType: String,
        content: String,
        language: String,
        origin: String = "AI_GENERATED"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val id = "sum_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("saved_summaries").document(id)
        val payload = mapOf(
            "userId" to uid,
            "lessonId" to lessonId,
            "lessonTitle" to lessonTitle.take(300),
            "subjectName" to subjectName.take(150),
            "summaryType" to summaryType.take(64),
            "content" to content.take(20000),
            "language" to language,
            "origin" to origin,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteSavedSummary(summaryId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("saved_summaries").document(summaryId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // ==================== AI CONVERSATIONS & PRACTICE EXAMS ====================

    fun observeConversations(userId: String = requireUserId()): Flow<List<AiConversation>> = flow {
        val path = "users/$userId/conversations"
        emitAll(
            db.collection("users").document(userId).collection("conversations")
                .snapshots()
                .map { qs ->
                    qs.toObjects(AiConversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .withFirestoreErrorHandling(OperationType.LIST, path, userId)
        )
    }

    suspend fun saveConversationSnapshot(
        subjectId: String,
        lessonId: String,
        title: String,
        lastMessage: String,
        messageCount: Int
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val id = "conv_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("conversations").document(id)
        val payload = mapOf(
            "userId" to uid,
            "subjectId" to subjectId.ifBlank { "general" },
            "lessonId" to lessonId.ifBlank { "general" },
            "title" to title.take(200),
            "lastMessage" to lastMessage.take(4000),
            "messageCount" to messageCount.coerceIn(1, 500),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    fun observePracticeExams(userId: String = requireUserId()): Flow<List<PracticeExamRecord>> = flow {
        val path = "users/$userId/practice_exams"
        emitAll(
            db.collection("users").document(userId).collection("practice_exams")
                .snapshots()
                .map { qs ->
                    qs.toObjects(PracticeExamRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .withFirestoreErrorHandling(OperationType.LIST, path, userId)
        )
    }

    suspend fun savePracticeExamScore(
        title: String,
        gradeId: String,
        subjectId: String,
        difficulty: String,
        questionCount: Int,
        score: Int
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val id = "exam_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("practice_exams").document(id)
        val payload = mapOf(
            "userId" to uid,
            "title" to title.take(300),
            "gradeId" to gradeId,
            "subjectId" to subjectId,
            "difficulty" to difficulty.take(32),
            "questionCount" to questionCount.coerceIn(1, 50),
            "score" to score.coerceIn(0, 100),
            "isAiGenerated" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // ==================== PUBLISHED CURRICULUM & ASSESSMENTS ====================

    fun observePublishedCurriculumVersions(): Flow<List<CurriculumVersionRecord>> = flow {
        val path = "curriculum_versions"
        emitAll(
            db.collection(path)
                .whereEqualTo("status", "PUBLISHED")
                .snapshots()
                .map { qs ->
                    qs.toObjects(CurriculumVersionRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .withFirestoreErrorHandling(OperationType.LIST, path)
        )
    }

    fun observeAllCurriculumVersionsForAdmin(): Flow<List<CurriculumVersionRecord>> = flow {
        val path = "curriculum_versions"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { qs ->
                    qs.toObjects(CurriculumVersionRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .withFirestoreErrorHandling(OperationType.LIST, path)
        )
    }

    fun observeVerifiedAssessments(): Flow<List<OfficialAssessmentRecord>> = flow {
        val path = "official_assessments"
        emitAll(
            db.collection(path)
                .whereEqualTo("isOfficialVerified", true)
                .snapshots()
                .map { qs ->
                    qs.toObjects(OfficialAssessmentRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .withFirestoreErrorHandling(OperationType.LIST, path)
        )
    }

    // ==================== ADMIN GOVERNANCE ====================

    fun observeCurriculumDiffsForAdmin(): Flow<List<CurriculumDiffRecord>> = flow {
        val path = "curriculum_diffs"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { qs ->
                    qs.toObjects(CurriculumDiffRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .withFirestoreErrorHandling(OperationType.LIST, path)
        )
    }

    fun observeMoeSyncLogsForAdmin(): Flow<List<MoeSyncLogRecord>> = flow {
        val path = "moe_sync_logs"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { qs ->
                    qs.toObjects(MoeSyncLogRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .withFirestoreErrorHandling(OperationType.LIST, path)
        )
    }

    fun observeAdminAuditLogs(): Flow<List<AdminAuditLogRecord>> = flow {
        val path = "admin_audit_logs"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { qs ->
                    qs.toObjects(AdminAuditLogRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .withFirestoreErrorHandling(OperationType.LIST, path)
        )
    }

    suspend fun stageCurriculumVersionAndDiff(
        gradeId: String,
        subjectId: String,
        academicYear: String,
        semester: Int,
        language: String,
        origin: String,
        sourceUrl: String,
        fileSha256: String,
        extractedUnitsSummary: String,
        addedTopics: String,
        removedTopics: String,
        modifiedLessons: String,
        diffSummary: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val versionId = "ver_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val verRef = db.collection("curriculum_versions").document(versionId)
        val verPayload = mapOf(
            "gradeId" to gradeId,
            "subjectId" to subjectId,
            "academicYear" to academicYear,
            "semester" to semester.coerceIn(0, 2),
            "language" to language,
            "origin" to origin,
            "versionNumber" to 2,
            "status" to "PENDING_REVIEW",
            "sourceUrl" to sourceUrl.take(1000),
            "fileSha256" to fileSha256.take(128),
            "extractedUnitsSummary" to extractedUnitsSummary.take(30000),
            "createdByUid" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            verRef.set(verPayload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, verRef.path)
            throw e
        }

        val diffId = "diff_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val diffRef = db.collection("curriculum_diffs").document(diffId)
        val diffPayload = mapOf(
            "versionId" to versionId,
            "subjectId" to subjectId,
            "gradeId" to gradeId,
            "addedTopics" to addedTopics.take(10000),
            "removedTopics" to removedTopics.take(10000),
            "modifiedLessons" to modifiedLessons.take(10000),
            "summary" to diffSummary.take(5000),
            "reviewed" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            diffRef.set(diffPayload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, diffRef.path)
            throw e
        }

        logAdminAction("STAGE_CURRICULUM_VERSION", versionId, "Staged $subjectId ($gradeId) pending review")
        versionId
    }

    suspend fun approveAndPublishVersion(versionId: String, diffId: String?): Result<Unit> = runCatching {
        val verRef = db.collection("curriculum_versions").document(versionId)
        try {
            verRef.update(
                mapOf(
                    "status" to "PUBLISHED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, verRef.path)
            throw e
        }
        if (!diffId.isNullOrBlank()) {
            val diffRef = db.collection("curriculum_diffs").document(diffId)
            try {
                diffRef.update(
                    mapOf(
                        "reviewed" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, diffRef.path)
            }
        }
        logAdminAction("PUBLISH_CURRICULUM_VERSION", versionId, "Approved and published version $versionId")
    }

    suspend fun rejectCurriculumVersion(versionId: String, diffId: String?): Result<Unit> = runCatching {
        val verRef = db.collection("curriculum_versions").document(versionId)
        try {
            verRef.update(
                mapOf(
                    "status" to "REJECTED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, verRef.path)
            throw e
        }
        if (!diffId.isNullOrBlank()) {
            val diffRef = db.collection("curriculum_diffs").document(diffId)
            try {
                diffRef.update(
                    mapOf(
                        "reviewed" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, diffRef.path)
            }
        }
        logAdminAction("REJECT_CURRICULUM_VERSION", versionId, "Rejected staged version $versionId")
    }

    suspend fun publishOfficialAssessment(
        titleAr: String,
        titleEn: String,
        gradeId: String,
        subjectId: String,
        academicYear: String,
        semester: Int,
        periodLabel: String,
        assessmentType: String,
        language: String,
        sourceUrl: String,
        publishedDateLabel: String
    ): Result<String> = runCatching {
        val id = "asmt_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("official_assessments").document(id)
        val payload = mapOf(
            "titleAr" to titleAr.take(300),
            "titleEn" to titleEn.take(300),
            "gradeId" to gradeId,
            "subjectId" to subjectId,
            "academicYear" to academicYear,
            "semester" to semester.coerceIn(1, 2),
            "periodLabel" to periodLabel.take(100),
            "assessmentType" to assessmentType,
            "language" to language,
            "sourceUrl" to sourceUrl.take(1000),
            "publishedDateLabel" to publishedDateLabel.take(64),
            "isOfficialVerified" to sourceUrl.contains("moe.gov.eg", ignoreCase = true),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
        logAdminAction("PUBLISH_ASSESSMENT", id, "Published MOE assessment: $titleAr")
        id
    }

    suspend fun recordMoeSyncLog(checkedUrl: String, status: String, details: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val id = "sync_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("moe_sync_logs").document(id)
        val payload = mapOf(
            "checkedUrl" to checkedUrl.take(1000),
            "status" to status.take(64),
            "details" to details.take(4000),
            "checkedByUid" to uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    private suspend fun logAdminAction(action: String, targetId: String, details: String) {
        val uid = requireUserId()
        val id = "aud_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("admin_audit_logs").document(id)
        val payload = mapOf(
            "adminUid" to uid,
            "action" to action.take(120),
            "targetId" to targetId.take(128),
            "details" to details.take(4000),
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
    }
}
