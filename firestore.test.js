const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const nowTs = () => new Date();

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_seif";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("Unauthenticated user: cannot read or write user profiles or curricula", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("curriculum_versions").where("status", "==", "PUBLISHED").get()
  );
  await assertFails(
    unauthDb.collection("official_assessments").where("isOfficialVerified", "==", true).get()
  );
});

test("Student user: can create and read own profile and progress, blocked from Bob's", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@student.eg", email_verified: true }).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      selectedGradeId: "prep_1",
      selectedTrackId: "general",
      academicYear: "2026-2027",
      preferredLang: "AR",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  // Shadow update with ghost field must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      selectedGradeId: "sec_3",
      role: "ADMIN",
      updatedAt: nowTs(),
    })
  );

  // Cross-user access must fail
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("lesson_progress").get()
  );
});

test("Student user: can create and query own lesson_progress and saved_summaries", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("lesson_progress").doc("lesson_1").set({
      userId: ALICE_UID,
      lessonId: "lesson_1",
      subjectId: "prep1_sci_ar",
      gradeId: "prep_1",
      completed: true,
      masteryScore: 95,
      weakTopic: false,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("lesson_progress").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_summaries").doc("sum_1").set({
      userId: ALICE_UID,
      lessonId: "lesson_1",
      lessonTitle: "النظام البيئي",
      subjectName: "العلوم",
      summaryType: "SHORT",
      content: "ملخص رسمي مبسط للدرس.",
      language: "AR",
      origin: "AI_GENERATED",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
});

test("Spoofed unverified admin email cannot write official curricula or read audit logs", async () => {
  const spoofDb = testEnv.authenticatedContext("attacker_1", {
    email: "seif82502@gmail.com",
    email_verified: false,
  }).firestore();

  await assertFails(spoofDb.collection("admin_audit_logs").get());
  await assertFails(
    spoofDb.collection("curriculum_versions").doc("ver_1").set({
      gradeId: "prep_1",
      subjectId: "prep1_sci_ar",
      academicYear: "2026-2027",
      semester: 1,
      language: "AR",
      origin: "OFFICIAL_MOE",
      versionNumber: 1,
      status: "PUBLISHED",
      sourceUrl: "https://studentbooks.moe.gov.eg/Books",
      fileSha256: "abc123",
      extractedUnitsSummary: "Unit 1",
      createdByUid: "attacker_1",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
});

test("Verified admin can stage, review, publish curricula and log audit actions; students can only read PUBLISHED", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "seif82502@gmail.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("curriculum_versions").doc("ver_staged").set({
      gradeId: "sec_1",
      subjectId: "sec1_integrated_sci",
      academicYear: "2026-2027",
      semester: 1,
      language: "AR",
      origin: "OFFICIAL_MOE",
      versionNumber: 1,
      status: "PENDING_REVIEW",
      sourceUrl: "https://studentbooks.moe.gov.eg/Books",
      fileSha256: "sha256_xyz",
      extractedUnitsSummary: "الوحدة الأولى: النظام البيئي المائي",
      createdByUid: ADMIN_UID,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // Unfiltered query without status == PUBLISHED must fail for student
  await assertFails(aliceDb.collection("curriculum_versions").get());
  // Pending version direct read must fail for student
  await assertFails(aliceDb.collection("curriculum_versions").doc("ver_staged").get());

  // Admin approves -> PUBLISHED
  await assertSucceeds(
    adminDb.collection("curriculum_versions").doc("ver_staged").update({
      status: "PUBLISHED",
      updatedAt: nowTs(),
    })
  );

  // Now student can query PUBLISHED versions
  await assertSucceeds(
    aliceDb.collection("curriculum_versions").where("status", "==", "PUBLISHED").get()
  );
  // Student still cannot mutate or delete published version
  await assertFails(
    aliceDb.collection("curriculum_versions").doc("ver_staged").delete()
  );
});
