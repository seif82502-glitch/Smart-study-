package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class SmartStudyRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun upsertUserProfile_and_lessonProgress_succeedsForAuthenticatedOwner() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = SmartStudyRepository(firestore)

        val profileRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertUserProfile(
                displayName = "Alice Egyptian Student",
                selectedGradeId = "prep_1",
                selectedTrackId = "general",
                academicYear = "2026-2027",
                preferredLang = "AR"
            )
        }
        assertTrue(profileRes.isSuccess)

        val lessonId = "lesson_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        val progressRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.setLessonProgress(
                lessonId = lessonId,
                subjectId = "prep1_sci_en",
                gradeId = "prep_1",
                completed = true,
                masteryScore = 92,
                weakTopic = false
            )
        }
        assertTrue(progressRes.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeLessonProgress(uid).first { list -> list.any { it.lessonId == lessonId } }
        }
        assertTrue(emitted.any { it.lessonId == lessonId && it.completed })
    }

    @Test
    fun saveSummary_and_observe_succeedsForOwner() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = SmartStudyRepository(firestore)

        val summaryRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveSummary(
                lessonId = "prep1_sci_l1",
                lessonTitle = "تركيب الذرة",
                subjectName = "العلوم",
                summaryType = "SHORT",
                content = "النواة موجبة والإلكترونات سالبة.",
                language = "AR",
                origin = "AI_GENERATED"
            )
        }
        assertTrue(summaryRes.isSuccess)
        val summaryId = summaryRes.getOrThrow()

        val summaries = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeSavedSummaries(uid).first { list -> list.any { it.id == summaryId } }
        }
        assertTrue(summaries.any { it.id == summaryId })
    }

    @Test
    fun crossUserRead_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = SmartStudyRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.upsertUserProfile("Alice", "prep_1", "general", "2026-2027", "AR").getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = SmartStudyRepository(firestore)
        var captured: FirebaseFirestoreException? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeUserProfile(aliceUid).first()
            }
        } catch (e: Exception) {
            captured = e.unwrapFirestoreException()
        }
        assertNotNull(captured)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, captured?.code)
    }

    @Test
    fun unauthenticatedQuery_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = SmartStudyRepository(firestore)
        var captured: FirebaseFirestoreException? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observePublishedCurriculumVersions().first()
            }
        } catch (e: Exception) {
            captured = e.unwrapFirestoreException()
        }
        assertNotNull(captured)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, captured?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice.student@smartstudy.eg"
        const val BOB_EMAIL = "bob.student@smartstudy.eg"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3500L
    }
}
