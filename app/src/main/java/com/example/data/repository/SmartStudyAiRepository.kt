package com.example.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.domain.GeneratedPracticeExam
import com.example.domain.PracticeQuestion
import com.example.domain.SummaryType
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

class SmartStudyAiRepository {

    private val tutorModel by lazy {
        Firebase.ai.generativeModel(modelName = "gemini-3.8-flash")
    }

    private val examSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Title of the AI practice exam"),
            "questions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "number" to Schema.integer(description = "1-based question number"),
                        "type" to Schema.string(description = "MCQ, TRUE_FALSE, COMPLETE, WRITTEN, or MATH"),
                        "prompt" to Schema.string(description = "Question prompt text"),
                        "options" to Schema.array(
                            items = Schema.string(),
                            description = "Answer options for MCQ or TRUE_FALSE; empty list for written/math"
                        ),
                        "correctAnswer" to Schema.string(description = "Exact correct answer"),
                        "explanation" to Schema.string(description = "Step-by-step curriculum explanation")
                    )
                ),
                description = "List of practice exam questions"
            )
        )
    )

    private val structuredExamModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = examSchema
            }
        )
    }

    fun streamLessonSummary(
        lessonTitle: String,
        subjectName: String,
        gradeName: String,
        summaryType: SummaryType,
        ragContext: String,
        citations: List<String>,
        isArabic: Boolean
    ): Flow<String> = flow {
        val langInstruction = if (isArabic) {
            "Respond in clear, structured Egyptian educational Arabic (العربية الفصحى المبسطة)."
        } else {
            "Respond in clear, structured academic English."
        }
        val modeInstruction = when (summaryType) {
            SummaryType.SHORT -> "Provide a concise executive summary (5-7 bullet points)."
            SummaryType.DETAILED -> "Provide a thorough, well-structured lesson explanation covering every concept."
            SummaryType.KEY_POINTS -> "List the most important exam-critical takeaways."
            SummaryType.DEFINITIONS -> "Extract and explain all key scientific/academic definitions and terms."
            SummaryType.FORMULAS -> "List all rules, laws, formulas, units, and worked examples."
            SummaryType.MIND_MAP -> "Create a hierarchical text-based Mind Map (branches & sub-branches) of the lesson."
            SummaryType.REVISION_NOTES -> "Create quick pre-exam revision flashcards and common mistake warnings."
            SummaryType.OBJECTIVES -> "Map each expected Ministry learning objective to concrete mastery checks."
        }
        val prompt = """
            You are Smart Study, an AI Educational Assistant specialized in the Egyptian Ministry of Education (MOE) curriculum.
            Grade: $gradeName | Subject: $subjectName | Lesson: $lessonTitle
            Task: $modeInstruction
            $langInstruction

            Strict Grounding Rules:
            1. Rely strictly on the Official Curriculum Excerpts below when stating curriculum facts.
            2. Clearly separate your output into two headings:
               - Official Curriculum Basis (citing the lesson/source)
               - AI Study Notes & Clarification
            3. Do NOT invent official Ministry claims outside the provided excerpts.

            Official Curriculum Excerpts:
            ${ragContext.ifBlank { "No matching curriculum excerpt provided." }}

            Available Citations: ${citations.joinToString(" | ")}
        """.trimIndent()

        val sb = StringBuilder()
        withTimeout(60_000L) {
            tutorModel.generateContentStream(prompt).collect { chunk ->
                val piece = chunk.text.orEmpty()
                if (piece.isNotEmpty()) {
                    sb.append(piece)
                    emit(sb.toString())
                }
            }
        }
        if (sb.isBlank()) {
            throw IllegalStateException("AI returned an empty summary.")
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        Log.e(TAG, "streamLessonSummary failed", e)
        throw e
    }

    fun streamTutorReply(
        question: String,
        gradeName: String,
        subjectName: String,
        ragContext: String,
        citations: List<String>,
        conversationHistory: List<Pair<String, String>>,
        isArabic: Boolean
    ): Flow<String> = flow {
        val langInstruction = if (isArabic) {
            "Reply in clear, encouraging educational Arabic suitable for $gradeName."
        } else {
            "Reply in clear, encouraging academic English suitable for $gradeName."
        }
        val historyBlock = conversationHistory.takeLast(6).joinToString("\n") { (role, msg) ->
            "$role: $msg"
        }
        val prompt = """
            You are an interactive Egyptian Ministry of Education AI Study Tutor.
            Grade: $gradeName | Subject: $subjectName
            $langInstruction

            Strict Grounding & Pedagogy Rules:
            1. Ground official curriculum statements strictly in the Curriculum Context below and cite the lesson/source.
            2. If the student's question goes beyond the provided official excerpt or no excerpt matches, state clearly what is from the official curriculum vs general educational guidance, and never fabricate an official MOE statement.
            3. Explain concepts simply with examples adapted to $gradeName, and end with 1 short check-for-understanding follow-up question.

            Curriculum Context:
            ${ragContext.ifBlank { "No direct curriculum excerpt matched this query." }}

            Citations: ${citations.joinToString(" | ")}

            Recent Chat History:
            $historyBlock

            Student Question:
            $question
        """.trimIndent()

        val sb = StringBuilder()
        withTimeout(60_000L) {
            tutorModel.generateContentStream(prompt).collect { chunk ->
                val piece = chunk.text.orEmpty()
                if (piece.isNotEmpty()) {
                    sb.append(piece)
                    emit(sb.toString())
                }
            }
        }
        if (sb.isBlank()) {
            throw IllegalStateException("AI tutor returned an empty response.")
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        Log.e(TAG, "streamTutorReply failed", e)
        throw e
    }

    fun streamProblemSolution(
        problemText: String,
        problemImage: Bitmap?,
        gradeName: String,
        subjectName: String,
        ragContext: String,
        isArabic: Boolean
    ): Flow<String> = flow {
        val langInstruction = if (isArabic) {
            "Solve and explain step by step in clear Arabic."
        } else {
            "Solve and explain step by step in clear English."
        }
        val instructionPrompt = """
            You are an Egyptian Curriculum Problem Solver for $gradeName ($subjectName).
            $langInstruction

            Required Structure:
            1. Subject & Topic Identification
            2. Official Curriculum Rules / Formulas Used (grounded in context below where applicable)
            3. Step-by-Step Solution (explaining why each step is necessary)
            4. Final Answer
            5. One Similar Practice Question for the student to try

            Curriculum Context:
            ${ragContext.ifBlank { "Use standard Egyptian $gradeName $subjectName curriculum methods and state clearly when providing general academic explanation." }}

            Student Problem:
            ${problemText.ifBlank { "Please read and solve the problem shown in the uploaded image." }}
        """.trimIndent()

        val sb = StringBuilder()
        withTimeout(60_000L) {
            val flowStream = if (problemImage != null) {
                tutorModel.generateContentStream(
                    content {
                        image(problemImage)
                        text(instructionPrompt)
                    }
                )
            } else {
                tutorModel.generateContentStream(instructionPrompt)
            }
            flowStream.collect { chunk ->
                val piece = chunk.text.orEmpty()
                if (piece.isNotEmpty()) {
                    sb.append(piece)
                    emit(sb.toString())
                }
            }
        }
        if (sb.isBlank()) {
            throw IllegalStateException("AI problem solver returned an empty response.")
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        Log.e(TAG, "streamProblemSolution failed", e)
        throw e
    }

    suspend fun generatePracticeExam(
        gradeName: String,
        subjectName: String,
        lessonTitle: String,
        difficulty: String,
        questionCount: Int,
        questionTypes: List<String>,
        ragContext: String,
        isArabic: Boolean
    ): GeneratedPracticeExam {
        val langInstruction = if (isArabic) {
            "Write all questions, options, correct answers, and explanations in Arabic."
        } else {
            "Write all questions, options, correct answers, and explanations in English."
        }
        val prompt = """
            Generate an Egyptian curriculum AI Practice Exam (NOT an official Ministry exam).
            Grade: $gradeName
            Subject: $subjectName
            Topic/Lesson: $lessonTitle
            Difficulty: $difficulty
            Number of Questions: $questionCount
            Allowed Question Types: ${questionTypes.joinToString(", ")}
            $langInstruction

            Base the questions strictly on this curriculum context:
            ${ragContext.ifBlank { "Standard Egyptian $gradeName $subjectName syllabus for $lessonTitle." }}

            For MCQ questions, include 4 clear options. For TRUE_FALSE, include 2 options (True/False or صح/خطأ).
            For COMPLETE, WRITTEN, or MATH questions, set options to an empty array [].
        """.trimIndent()

        val response = withTimeout(60_000L) {
            structuredExamModel.generateContent(prompt)
        }
        val rawJson = response.text ?: throw IllegalStateException("AI returned empty exam JSON.")
        val root = JSONObject(rawJson)
        val title = root.optString("title", "$subjectName Practice Exam")
        val arr = root.getJSONArray("questions")
        val questions = (0 until arr.length()).map { idx ->
            val obj = arr.getJSONObject(idx)
            val optsArr = obj.optJSONArray("options")
            val opts = if (optsArr != null) {
                (0 until optsArr.length()).map { optsArr.getString(it) }
            } else emptyList()
            PracticeQuestion(
                number = obj.optInt("number", idx + 1),
                type = obj.optString("type", "MCQ"),
                prompt = obj.getString("prompt"),
                options = opts,
                correctAnswer = obj.getString("correctAnswer"),
                explanation = obj.getString("explanation")
            )
        }
        return GeneratedPracticeExam(
            title = title,
            gradeName = gradeName,
            subjectName = subjectName,
            difficulty = difficulty,
            questions = questions
        )
    }

    suspend fun extractCurriculumSummaryFromPdfBitmap(
        pageBitmap: Bitmap,
        subjectName: String,
        gradeName: String,
        isArabic: Boolean
    ): String {
        val prompt = if (isArabic) {
            "استخرج النص التعليمي والعناوين والوحدات والدروس والأهداف الموجودة في صفحة الكتاب المدرسي الرسمية المرفقة للصف ($gradeName) مادة ($subjectName) بدقة دون اختلاق."
        } else {
            "Accurately extract the educational units, chapters, lessons, definitions, and objectives from this official textbook page for $gradeName ($subjectName) without fabricating."
        }
        val response = withTimeout(60_000L) {
            tutorModel.generateContent(
                content {
                    image(pageBitmap)
                    text(prompt)
                }
            )
        }
        return response.text ?: throw IllegalStateException("Empty extraction output.")
    }

    private companion object {
        const val TAG = "SmartStudyAI"
    }
}
