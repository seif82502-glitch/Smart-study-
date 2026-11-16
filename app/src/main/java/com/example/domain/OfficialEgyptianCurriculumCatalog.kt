package com.example.domain

/**
 * Local bundled catalog of the Official Egyptian Ministry of Education (MOE)
 * 2026-2027 curriculum structure across Preparatory (1-3), Secondary General (1-3),
 * and Egyptian Baccalaureate tracks, anchored to https://moe.gov.eg/ &
 * https://studentbooks.moe.gov.eg/ & https://ellibrary.moe.gov.eg/.
 */
object OfficialEgyptianCurriculumCatalog {

    const val MOE_BOOKS_PORTAL = "https://studentbooks.moe.gov.eg/Books"
    const val MOE_ASSESSMENTS_PORTAL = "https://studentbooks.moe.gov.eg/Performance_Assessments/"
    const val MOE_BACCALAUREATE_PORTAL = "https://ellibrary.moe.gov.eg/EgyptianBaccalaureate/"
    const val MOE_ELEARNING_PORTAL = "https://moe.gov.eg/ar/elearning"

    val academicYears = listOf("2026-2027", "2025-2026")

    val grades: List<GradeSpec> = listOf(
        GradeSpec(
            id = "prep_1",
            stage = Stage.PREPARATORY,
            levelNumber = 1,
            nameAr = "الصف الأول الإعدادي",
            nameEn = "First Preparatory Grade",
            tracks = listOf(
                TrackSpec("general", "عربي (مدارس رسمية)", "Arabic Schools"),
                TrackSpec("languages", "لغات (مدارس رسمية لغات)", "Language Schools (English)")
            )
        ),
        GradeSpec(
            id = "prep_2",
            stage = Stage.PREPARATORY,
            levelNumber = 2,
            nameAr = "الصف الثاني الإعدادي",
            nameEn = "Second Preparatory Grade",
            tracks = listOf(
                TrackSpec("general", "عربي (مدارس رسمية)", "Arabic Schools"),
                TrackSpec("languages", "لغات (مدارس رسمية لغات)", "Language Schools (English)")
            )
        ),
        GradeSpec(
            id = "prep_3",
            stage = Stage.PREPARATORY,
            levelNumber = 3,
            nameAr = "الصف الثالث الإعدادي",
            nameEn = "Third Preparatory Grade (Prep Certificate)",
            tracks = listOf(
                TrackSpec("general", "عربي (الشهادة الإعدادية)", "Arabic Schools"),
                TrackSpec("languages", "لغات (الشهادة الإعدادية)", "Language Schools (English)")
            )
        ),
        GradeSpec(
            id = "sec_1",
            stage = Stage.SECONDARY_GENERAL,
            levelNumber = 1,
            nameAr = "الصف الأول الثانوي",
            nameEn = "First Secondary Grade",
            tracks = listOf(
                TrackSpec("general", "ثانوي عام — عربي", "General Secondary (Arabic)"),
                TrackSpec("languages", "ثانوي عام — لغات", "General Secondary (Languages)"),
                TrackSpec("bacc_y1", "البكالوريا المصرية — السنة التأسيسية", "Egyptian Baccalaureate (Year 1)")
            )
        ),
        GradeSpec(
            id = "sec_2",
            stage = Stage.SECONDARY_GENERAL,
            levelNumber = 2,
            nameAr = "الصف الثاني الثانوي",
            nameEn = "Second Secondary Grade",
            tracks = listOf(
                TrackSpec("sci_general", "الشعبة العلمية", "Scientific Track"),
                TrackSpec("lit", "الشعبة الأدبية", "Literary Track"),
                TrackSpec("bacc_med", "البكالوريا: الطب وعلوم الحياة", "Baccalaureate: Medicine & Life Sciences"),
                TrackSpec("bacc_eng", "البكالوريا: الهندسة وعلوم الحاسب", "Baccalaureate: Engineering & CS")
            )
        ),
        GradeSpec(
            id = "sec_3",
            stage = Stage.SECONDARY_GENERAL,
            levelNumber = 3,
            nameAr = "الصف الثالث الثانوي (الثانوية العامة)",
            nameEn = "Third Secondary Grade (Thanaweya Amma)",
            tracks = listOf(
                TrackSpec("sci_bio", "علمي علوم", "Scientific — Biology & Sciences"),
                TrackSpec("sci_math", "علمي رياضة", "Scientific — Mathematics"),
                TrackSpec("lit", "أدبي", "Literary Track")
            )
        )
    )

    val subjects: List<SubjectSpec> = listOf(
        // ==================== FIRST PREPARATORY (الصف الأول الإعدادي) ====================
        SubjectSpec(
            id = "prep1_ar_lang",
            gradeId = "prep_1",
            trackIds = listOf("general", "languages"),
            semester = 1,
            nameAr = "اللغة العربية",
            nameEn = "Arabic Language",
            hasOfficialArabic = true,
            hasOfficialEnglish = false,
            officialMoePortalUrl = MOE_BOOKS_PORTAL,
            officialBookPdfUrlAr = MOE_BOOKS_PORTAL,
            officialBookPdfUrlEn = null,
            units = listOf(
                UnitSpec(
                    id = "prep1_ar_u1",
                    order = 1,
                    titleAr = "الوحدة الأولى: الهوية وبناء الإنسان",
                    titleEn = "Unit 1: Identity & Character Building",
                    englishOrigin = ContentOrigin.AI_TRANSLATED,
                    chapters = listOf(
                        ChapterSpec(
                            id = "prep1_ar_u1_c1",
                            order = 1,
                            titleAr = "النصوص والقراءة والنحو",
                            titleEn = "Reading, Texts & Grammar",
                            lessons = listOf(
                                LessonSpec(
                                    id = "prep1_ar_l1",
                                    order = 1,
                                    titleAr = "همزة القطع وألف الوصل",
                                    titleEn = "Hamzat Al-Qat' and Alif Al-Wasl",
                                    objectivesAr = listOf(
                                        "التمييز بين همزة القطع وألف الوصل نطقًا وكتابةً",
                                        "تطبيق القاعدة في الأسماء والأفعال الثلاثية والخماسية والسداسية",
                                        "تصويب الأخطاء الإملائية الشائعة في التعبير الكتابي"
                                    ),
                                    objectivesEn = listOf(
                                        "Distinguish Hamzat Al-Qat' from Alif Al-Wasl in pronunciation and writing",
                                        "Apply rules to nouns and 3-, 5-, and 6-letter verbs",
                                        "Correct common spelling mistakes in writing"
                                    ),
                                    contentAr = "همزة القطع: همزة متحركة تظهر في النطق دائمًا في أول الكلام وفي وصله وتُكتب (أ / إ / آ). وتوجد في: جميع الحروف ما عدا (أل التعريف)، وجميع الأسماء ما عدا الأسماء التسعة، وماضي الثلاثي والرباعي ومصدرهما.\nألف الوصل: ألف تُنطق في ابتداء الكلام وتسقط في وصله وتُكتب (ا) دون همزة، وتوجد في: (أل) التعريف، وأمر الثلاثي، وماضي وأمر ومصدر الخماسي والسداسي، والأسماء التسعة (اسم، ابن، ابنة، امرؤ، امرأة، اثنان، اثنتان، ايم الله، أيمن الله).",
                                    contentEn = "Hamzat Al-Qat' is always pronounced at the start and middle of speech (written أ/إ/آ). It appears in all particles except 'Al-', all nouns except the 9 specific nouns, and past/infinitive forms of 3- and 4-letter verbs. Alif Al-Wasl is pronounced only at the start of speech (written ا without hamza) and appears in 'Al-', 3-letter imperative verbs, 5- and 6-letter past/imperative/infinitive verbs, and the 9 specific Arabic nouns.",
                                    formulasAndDefinitionsAr = listOf(
                                        "اختبار سريع: ضع حرف (و) أو (ف) قبل الكلمة؛ فإن نُطقت الهمزة فهي قطع (وأخذ)، وإن اختفت فهي وصل (واستخرج).",
                                        "الرباعي كله قطع: ماضيه وأمره ومصدره (أَكْرَمَ - أَكْرِمْ - إكرام)."
                                    ),
                                    formulasAndDefinitionsEn = listOf(
                                        "Quick test: Prefix the word with 'wa' (و); if the glottal stop is heard, it is Qat' (وأخذ); if silent, it is Wasl (واستخرج).",
                                        "All 4-letter verb forms (past, imperative, verbal noun) use Qat'."
                                    ),
                                    exercisesAr = listOf(
                                        "بيّن نوع الهمزة مع ذكر السبب: (انتصار - إتقان - استغفر - أحمد).",
                                        "صوّب الخطأ: (إستفاد الطالب من إجتهاده)."
                                    ),
                                    exercisesEn = listOf(
                                        "Classify the initial hamza and state the rule: (انتصار، إتقان، استغفر، أحمد).",
                                        "Correct the spelling error: (إستفاد الطالب من إجتهاده)."
                                    ),
                                    revisionSummaryAr = "الرباعي قطع دائمًا، والخماسي والسداسي وصل دائمًا، والحروف قطع ما عدا (أل).",
                                    revisionSummaryEn = "4-letter verbs = always Qat'; 5- and 6-letter verbs = always Wasl; particles = Qat' except 'Al-'.",
                                    officialSourceUrl = MOE_BOOKS_PORTAL,
                                    publicationDate = "2026-09-15",
                                    englishOrigin = ContentOrigin.AI_TRANSLATED
                                )
                            )
                        )
                    )
                )
            )
        ),
        SubjectSpec(
            id = "prep1_math_ar",
            gradeId = "prep_1",
            trackIds = listOf("general"),
            semester = 1,
            nameAr = "الرياضيات (بالعربية)",
            nameEn = "Mathematics (Arabic Edition)",
            hasOfficialArabic = true,
            hasOfficialEnglish = false,
            officialMoePortalUrl = MOE_BOOKS_PORTAL,
            officialBookPdfUrlAr = MOE_BOOKS_PORTAL,
            officialBookPdfUrlEn = null,
            units = listOf(
                UnitSpec(
                    id = "prep1_math_ar_u1",
                    order = 1,
                    titleAr = "الوحدة الأولى: الأعداد والعمليات عليها (النسبة والتناسب)",
                    titleEn = "Unit 1: Numbers, Ratios & Proportions",
                    englishOrigin = ContentOrigin.OFFICIAL_MOE,
                    chapters = listOf(
                        ChapterSpec(
                            id = "prep1_math_ar_c1",
                            order = 1,
                            titleAr = "التناسب ومقياس الرسم والأعداد الصحيحة",
                            titleEn = "Proportion, Scale Drawing & Integers",
                            lessons = listOf(
                                LessonSpec(
                                    id = "prep1_math_ar_l1",
                                    order = 1,
                                    titleAr = "التناسب وتطبيقاته (مقياس الرسم)",
                                    titleEn = "Proportion & Scale Drawing Applications",
                                    objectivesAr = listOf(
                                        "تحديد ما إذا كانت كميتان متناسبتين أم لا",
                                        "حساب مقياس الرسم والطول الحقيقي والطول في الرسم",
                                        "حل مسائل حياتية على التقسيم التناسبي والنسبة المئوية"
                                    ),
                                    objectivesEn = listOf(
                                        "Determine whether two quantities are proportional",
                                        "Compute drawing scale, actual length, and drawing length",
                                        "Solve real-life proportional division and percentage problems"
                                    ),
                                    contentAr = "التناسب هو تساوي نسبتين أو أكثر: إذا كان أ/ب = ج/د فإن أ×د = ب×ج (حاصل ضرب الطرفين يساوي حاصل ضرب الوسطين).\nمقياس الرسم = الطول في الرسم ÷ الطول الحقيقي (بعد توحيد الوحدات). إذا كان مقياس الرسم < 1 يدل على التصغير (كالخرائط)، وإذا كان > 1 يدل على التكبير (كالحشرات الدقيقة).",
                                    contentEn = "A proportion states that two ratios are equal: a/b = c/d implies a*d = b*c. Scale = Drawing Length / Actual Length (in the same units). Scale < 1 represents reduction (maps), while Scale > 1 represents enlargement.",
                                    formulasAndDefinitionsAr = listOf(
                                        "مقياس الرسم = الطول في الرسم / الطول الحقيقي",
                                        "الطول الحقيقي = الطول في الرسم / مقياس الرسم",
                                        "1 كم = 100,000 سم | 1 م = 100 سم"
                                    ),
                                    formulasAndDefinitionsEn = listOf(
                                        "Scale = Drawing Length / Actual Length",
                                        "Actual Length = Drawing Length / Scale",
                                        "1 km = 100,000 cm | 1 m = 100 cm"
                                    ),
                                    exercisesAr = listOf(
                                        "إذا كانت المسافة بين مدينتين على خريطة 4 سم بمقياس رسم 1 : 500,000، فأوجد المسافة الحقيقية بالكيلومتر.",
                                        "أوجد قيمة س إذا كان: 3 / 5 = س / 20."
                                    ),
                                    exercisesEn = listOf(
                                        "If the map distance between two cities is 4 cm at a scale of 1:500,000, find the actual distance in km.",
                                        "Solve for x: 3 / 5 = x / 20."
                                    ),
                                    revisionSummaryAr = "وحّد الوحدات أولًا قبل حساب مقياس الرسم، وتذكر قاعدة الطرفين والوسطين.",
                                    revisionSummaryEn = "Always unify units first before calculating scale, and apply cross-multiplication.",
                                    officialSourceUrl = MOE_BOOKS_PORTAL,
                                    publicationDate = "2026-09-15",
                                    englishOrigin = ContentOrigin.OFFICIAL_MOE
                                )
                            )
                        )
                    )
                )
            )
        ),
        SubjectSpec(
            id = "prep1_sci_en",
            gradeId = "prep_1",
            trackIds = listOf("languages", "general"),
            semester = 1,
            nameAr = "العلوم (عربي / لغات)",
            nameEn = "Science (Official MOE Arabic & English)",
            hasOfficialArabic = true,
            hasOfficialEnglish = true,
            officialMoePortalUrl = MOE_BOOKS_PORTAL,
            officialBookPdfUrlAr = MOE_BOOKS_PORTAL,
            officialBookPdfUrlEn = MOE_BOOKS_PORTAL,
            units = listOf(
                UnitSpec(
                    id = "prep1_sci_u1",
                    order = 1,
                    titleAr = "الوحدة الأولى: المادة والتركيب الذري",
                    titleEn = "Unit 1: Matter and Atomic Structure",
                    englishOrigin = ContentOrigin.OFFICIAL_MOE,
                    chapters = listOf(
                        ChapterSpec(
                            id = "prep1_sci_c1",
                            order = 1,
                            titleAr = "تركيب الذرة والجدول الدوري",
                            titleEn = "Atomic Structure & The Periodic Table",
                            lessons = listOf(
                                LessonSpec(
                                    id = "prep1_sci_l1",
                                    order = 1,
                                    titleAr = "تركيب الذرة والعدد الذري والكتلي",
                                    titleEn = "Atomic Structure, Atomic Number & Mass Number",
                                    objectivesAr = listOf(
                                        "وصف مكونات الذرة: النواة الموجبة والإلكترونات السالبة",
                                        "حساب عدد البروتونات والنيوترونات من العدد الذري والكتلي",
                                        "كتابة التوزيع الإلكتروني لمستويات الطاقة الرئيسية (K, L, M, N)"
                                    ),
                                    objectivesEn = listOf(
                                        "Describe the atom: positively charged nucleus and orbiting negative electrons",
                                        "Calculate protons and neutrons from Atomic Number (Z) and Mass Number (A)",
                                        "Write electronic configurations for energy levels K, L, M, N"
                                    ),
                                    contentAr = "تتكون الذرة من نواة مركزية تحتوي على بروتونات موجبة الشحنة ونيوترونات متعادلة الشحنة، وتدور حولها إلكترونات سالبة الشحنة في مستويات طاقة محددة. الذرة متعادلة كهربائيًا لأن عدد البروتونات الموجبة يساوي عدد الإلكترونات السالبة.",
                                    contentEn = "An atom consists of a central positively charged nucleus containing protons (+) and neutral neutrons (0), surrounded by negatively charged electrons (-) orbiting in energy levels. An atom is electrically neutral because protons equal electrons.",
                                    formulasAndDefinitionsAr = listOf(
                                        "العدد الذري (Z) = عدد البروتونات = عدد الإلكترونات في الذرة المتعادلة",
                                        "العدد الكتلي (A) = عدد البروتونات + عدد النيوترونات",
                                        "أقصى عدد للإلكترونات في المستوى ن = 2ن² (تُطبَّق حتى المستوى الرابع)"
                                    ),
                                    formulasAndDefinitionsEn = listOf(
                                        "Atomic Number (Z) = Number of protons = Number of electrons (neutral atom)",
                                        "Mass Number (A) = Number of protons + Number of neutrons",
                                        "Max electrons in energy level n = 2n² (applies up to level 4)"
                                    ),
                                    exercisesAr = listOf(
                                        "ذرة الصوديوم ₁₁Na²³: احسب عدد البروتونات والنيوترونات واكتب التوزيع الإلكتروني.",
                                        "علل: النواة موجبة الشحنة بينما الذرة متعادلة كهربائيًا."
                                    ),
                                    exercisesEn = listOf(
                                        "For Sodium ₁₁Na²³: calculate protons, neutrons, and draw its electronic configuration.",
                                        "Give reason: The nucleus is positively charged while the atom is electrically neutral."
                                    ),
                                    revisionSummaryAr = "العدد الذري أسفل يسار الرمز والكتلي أعلى يسار الرمز، والفرق بينهما يعطي عدد النيوترونات.",
                                    revisionSummaryEn = "Atomic number sits bottom-left, mass number top-left; A - Z equals neutrons.",
                                    officialSourceUrl = MOE_BOOKS_PORTAL,
                                    publicationDate = "2026-09-15",
                                    englishOrigin = ContentOrigin.OFFICIAL_MOE
                                )
                            )
                        )
                    )
                )
            )
        ),
        // ==================== SECOND PREPARATORY (الصف الثاني الإعدادي) ====================
        SubjectSpec(
            id = "prep2_sci",
            gradeId = "prep_2",
            trackIds = listOf("general", "languages"),
            semester = 1,
            nameAr = "العلوم — الصف الثاني الإعدادي",
            nameEn = "Science — 2nd Preparatory (AR / EN)",
            hasOfficialArabic = true,
            hasOfficialEnglish = true,
            officialMoePortalUrl = MOE_BOOKS_PORTAL,
            officialBookPdfUrlAr = MOE_BOOKS_PORTAL,
            officialBookPdfUrlEn = MOE_BOOKS_PORTAL,
            units = listOf(
                UnitSpec(
                    id = "prep2_sci_u1",
                    order = 1,
                    titleAr = "الوحدة الأولى: دورية العناصر وخواصها",
                    titleEn = "Unit 1: Periodicity of Elements & Properties",
                    englishOrigin = ContentOrigin.OFFICIAL_MOE,
                    chapters = listOf(
                        ChapterSpec(
                            id = "prep2_sci_c1",
                            order = 1,
                            titleAr = "تدرج خواص العناصر في الجدول الدوري الحديث",
                            titleEn = "Graduation of Properties in the Modern Periodic Table",
                            lessons = listOf(
                                LessonSpec(
                                    id = "prep2_sci_l1",
                                    order = 1,
                                    titleAr = "الحجم الذري والسالبية الكهربية والصفة الفلزية",
                                    titleEn = "Atomic Size, Electronegativity & Metallic Property",
                                    objectivesAr = listOf(
                                        "تفسير تناقص الحجم الذري في الدورة بزيادة العدد الذري",
                                        "التعرف على مفهوم السالبية الكهربية والمركب القطبي كالماء",
                                        "مقارنة الفلزات واللافلزات وأشباه الفلزات"
                                    ),
                                    objectivesEn = listOf(
                                        "Explain why atomic radius decreases across a period as atomic number increases",
                                        "Define electronegativity and polar covalent compounds such as water",
                                        "Compare metals, nonmetals, and metalloids"
                                    ),
                                    contentAr = "في الدورة الواحدة يقل الحجم الذري من اليسار إلى اليمين لزيادة قوة جذب النواة الموجبة لإلكترونات مستوى الطاقة الأخير، بينما في المجموعة يزداد الحجم الذري من أعلى إلى أسفل لزيادة عدد مستويات الطاقة.",
                                    contentEn = "Across a period (left to right), atomic size decreases because nuclear attraction to valence electrons increases. Down a group, atomic size increases due to additional energy shells.",
                                    formulasAndDefinitionsAr = listOf(
                                        "وحدة قياس الحجم الذري: البيكومتر (1 pm = 10⁻¹² m)",
                                        "السالبية الكهربية: مقدرة الذرة في الجزيء التساهمي على جذب إلكترونات الرابطة نحوها"
                                    ),
                                    formulasAndDefinitionsEn = listOf(
                                        "Atomic radius unit: Picometer (1 pm = 10⁻¹² m)",
                                        "Electronegativity: Ability of an atom in a covalent molecule to attract bonding electrons"
                                    ),
                                    exercisesAr = listOf(
                                        "رتّب العناصر الآتية تصاعديًا حسب الحجم الذري: ₄Be, ₆C, ₈O, ₃Li.",
                                        "لماذا يُعد جزيء الماء من المركبات التساهمية القطبية؟"
                                    ),
                                    exercisesEn = listOf(
                                        "Arrange ascendingly by atomic size: ₄Be, ₆C, ₈O, ₃Li.",
                                        "Why is the water molecule classified as a polar covalent compound?"
                                    ),
                                    revisionSummaryAr = "الحجم الذري يقل في الدورة ويزيد في المجموعة، والفلزات تتناسب طرديًا مع الحجم الذري.",
                                    revisionSummaryEn = "Atomic size decreases across periods and increases down groups; metallic character tracks atomic size.",
                                    officialSourceUrl = MOE_BOOKS_PORTAL,
                                    publicationDate = "2026-09-15",
                                    englishOrigin = ContentOrigin.OFFICIAL_MOE
                                )
                            )
                        )
                    )
                )
            )
        ),
        // ==================== THIRD PREPARATORY (الصف الثالث الإعدادي) ====================
        SubjectSpec(
            id = "prep3_math",
            gradeId = "prep_3",
            trackIds = listOf("general", "languages"),
            semester = 1,
            nameAr = "الرياضيات (الجبر وحساب المثلثات والهندسة)",
            nameEn = "Mathematics (Algebra, Trigonometry & Geometry)",
            hasOfficialArabic = true,
            hasOfficialEnglish = true,
            officialMoePortalUrl = MOE_BOOKS_PORTAL,
            officialBookPdfUrlAr = MOE_BOOKS_PORTAL,
            officialBookPdfUrlEn = MOE_BOOKS_PORTAL,
            units = listOf(
                UnitSpec(
                    id = "prep3_math_u1",
                    order = 1,
                    titleAr = "الوحدة الرابعة: حساب المثلثات",
                    titleEn = "Unit 4: Trigonometry",
                    englishOrigin = ContentOrigin.OFFICIAL_MOE,
                    chapters = listOf(
                        ChapterSpec(
                            id = "prep3_math_c1",
                            order = 1,
                            titleAr = "النسب المثلثية الأساسية للزاوية الحادة",
                            titleEn = "Main Trigonometric Ratios of an Acute Angle",
                            lessons = listOf(
                                LessonSpec(
                                    id = "prep3_math_l1",
                                    order = 1,
                                    titleAr = "جا، جتا، ظا للزوايا 30° و 45° و 60°",
                                    titleEn = "sin, cos, tan for Special Angles 30°, 45°, 60°",
                                    objectivesAr = listOf(
                                        "إيجاد الجيب وجيب التمام والظل للزاوية الحادة في المثلث القائم",
                                        "تطبيق النسب المثلثية للزوايا الخاصة دون استخدام الآلة الحاسبة",
                                        "حل معادلات مثلثية بسيطة وإيجاد قياس الزاوية"
                                    ),
                                    objectivesEn = listOf(
                                        "Compute sin, cos, and tan in a right-angled triangle",
                                        "Evaluate trigonometric ratios for 30°, 45°, and 60° without a calculator",
                                        "Solve basic trigonometric equations for angle measures"
                                    ),
                                    contentAr = "في المثلث القائم الزاوية: جا(هـ) = المقابل ÷ الوتر، جتا(هـ) = المجاور ÷ الوتر، ظا(هـ) = المقابل ÷ المجاور = جا(هـ) ÷ جتا(هـ). ولأي زاويتين متتامتين (س + ص = 90°): جا(س) = جتا(ص).",
                                    contentEn = "In a right triangle: sin(θ) = Opposite / Hypotenuse, cos(θ) = Adjacent / Hypotenuse, tan(θ) = Opposite / Adjacent = sin(θ) / cos(θ). For complementary angles (A + B = 90°): sin(A) = cos(B).",
                                    formulasAndDefinitionsAr = listOf(
                                        "جا 30° = 1/2 | جتا 30° = √3/2 | ظا 30° = 1/√3",
                                        "جا 45° = 1/√2 | جتا 45° = 1/√2 | ظا 45° = 1",
                                        "جا 60° = √3/2 | جتا 60° = 1/2 | ظا 60° = √3"
                                    ),
                                    formulasAndDefinitionsEn = listOf(
                                        "sin 30° = 1/2 | cos 30° = √3/2 | tan 30° = 1/√3",
                                        "sin 45° = 1/√2 | cos 45° = 1/√2 | tan 45° = 1",
                                        "sin 60° = √3/2 | cos 60° = 1/2 | tan 60° = √3"
                                    ),
                                    exercisesAr = listOf(
                                        "أثبت أن: جا 60° = 2 جا 30° جتا 30°.",
                                        "أ ب ج مثلث قائم في ب، فيه أ ب = 6 سم، ب ج = 8 سم؛ أوجد جا(ج) + جتا(أ)."
                                    ),
                                    exercisesEn = listOf(
                                        "Prove that: sin 60° = 2 sin 30° cos 30°.",
                                        "In right triangle ABC at B, AB = 6 cm, BC = 8 cm. Find sin(C) + cos(A)."
                                    ),
                                    revisionSummaryAr = "احفظ قيم 30° و45° و60° وتذكر أن جا الزاوية = جتا المتممة لها.",
                                    revisionSummaryEn = "Memorize 30°, 45°, 60° exact ratios and complementary identity sin(A) = cos(90°-A).",
                                    officialSourceUrl = MOE_BOOKS_PORTAL,
                                    publicationDate = "2026-09-15",
                                    englishOrigin = ContentOrigin.OFFICIAL_MOE
                                )
                            )
                        )
                    )
                )
            )
        ),
        // ==================== FIRST SECONDARY (الصف الأول الثانوي) ====================
        SubjectSpec(
            id = "sec1_integrated_sci",
            gradeId = "sec_1",
            trackIds = listOf("general", "languages", "bacc_y1"),
            semester = 1,
            nameAr = "العلوم المتكاملة — الصف الأول الثانوي",
            nameEn = "Integrated Sciences — 1st Secondary",
            hasOfficialArabic = true,
            hasOfficialEnglish = true,
            officialMoePortalUrl = MOE_BOOKS_PORTAL,
            officialBookPdfUrlAr = MOE_BOOKS_PORTAL,
            officialBookPdfUrlEn = MOE_BOOKS_PORTAL,
            units = listOf(
                UnitSpec(
                    id = "sec1_isci_u1",
                    order = 1,
                    titleAr = "الوحدة الأولى: النظام البيئي المائي",
                    titleEn = "Unit 1: The Aquatic Ecosystem",
                    englishOrigin = ContentOrigin.OFFICIAL_MOE,
                    chapters = listOf(
                        ChapterSpec(
                            id = "sec1_isci_c1",
                            order = 1,
                            titleAr = "الخواص الكيميائية والفيزيائية للماء",
                            titleEn = "Chemical & Physical Properties of Water",
                            lessons = listOf(
                                LessonSpec(
                                    id = "sec1_isci_l1",
                                    order = 1,
                                    titleAr = "التفاعلات الكيميائية في الأغلفة المائية والروابط الهيدروجينية",
                                    titleEn = "Water Structure, Hydrogen Bonds & Aquatic Balance",
                                    objectivesAr = listOf(
                                        "تفسير شذوذ كثافة الماء عند 4°م وأهميته للحياة البحرية",
                                        "ربط ارتفاع الحرارة النوعية للماء باستقرار مناخ الأرض",
                                        "تحليل أثر الرقم الهيدروجيني (pH) والملوحة على الكائنات المائية"
                                    ),
                                    objectivesEn = listOf(
                                        "Explain the density anomaly of water at 4°C and its role in aquatic survival",
                                        "Relate water's high specific heat capacity to climate stability",
                                        "Analyze pH and salinity effects on aquatic organisms"
                                    ),
                                    contentAr = "يتكون جزيء الماء من ذرتي هيدروجين وذرة أكسجين بينهما زاوية تقارب 104.5°. تنشأ الروابط الهيدروجينية بين جزيئات الماء فتسبب ارتفاع درجة غليانه (100°م) وارتفاع حرارته النوعية، وتمدد الماء عند التجمد ليطفو الجليد فوق الماء السائل عازلًا الحرارة عن الكائنات بالأسفل.",
                                    contentEn = "A water molecule has two hydrogen atoms and one oxygen atom with a bond angle near 104.5°. Hydrogen bonding raises water's boiling point (100°C) and specific heat capacity, and causes ice to be less dense than liquid water at 4°C so surface ice insulates aquatic life below.",
                                    formulasAndDefinitionsAr = listOf(
                                        "أقصى كثافة للماء النقي = 1 جم/سم³ عند درجة 4° سيلزيوس",
                                        "كمية الحرارة Qth = m × c × Δt"
                                    ),
                                    formulasAndDefinitionsEn = listOf(
                                        "Maximum density of pure water = 1 g/cm³ at 4°C",
                                        "Thermal Energy: Qth = m × c × ΔT"
                                    ),
                                    exercisesAr = listOf(
                                        "فسّر: تستطيع الأسماك العيش في البحيرات المتجمدة شتاءً.",
                                        "احسب كمية الحرارة اللازمة لرفع درجة حرارة 2 كجم من الماء بمقدار 10°م (علماً بأن c = 4180 J/kg.K)."
                                    ),
                                    exercisesEn = listOf(
                                        "Explain why fish survive beneath frozen lake surfaces in winter.",
                                        "Calculate the heat needed to raise 2 kg of water by 10°C (c = 4180 J/kg·K)."
                                    ),
                                    revisionSummaryAr = "الرابطة الهيدروجينية هي مفتاح كل الخواص الفريدة للماء واستدامة النظم المائية.",
                                    revisionSummaryEn = "Hydrogen bonding drives water's unique thermal and density properties sustaining aquatic life.",
                                    officialSourceUrl = MOE_BOOKS_PORTAL,
                                    publicationDate = "2026-09-15",
                                    englishOrigin = ContentOrigin.OFFICIAL_MOE
                                )
                            )
                        )
                    )
                )
            )
        ),
        SubjectSpec(
            id = "sec1_programming_ai",
            gradeId = "sec_1",
            trackIds = listOf("general", "languages", "bacc_y1"),
            semester = 1,
            nameAr = "البرمجة والذكاء الاصطناعي",
            nameEn = "Programming & Artificial Intelligence",
            hasOfficialArabic = true,
            hasOfficialEnglish = true,
            officialMoePortalUrl = MOE_ELEARNING_PORTAL,
            officialBookPdfUrlAr = MOE_BOOKS_PORTAL,
            officialBookPdfUrlEn = MOE_BOOKS_PORTAL,
            isAddedToGpa = false,
            units = listOf(
                UnitSpec(
                    id = "sec1_prog_u1",
                    order = 1,
                    titleAr = "الوحدة الأولى: التفكير الخوارزمي وأساسيات بايثون",
                    titleEn = "Unit 1: Algorithmic Thinking & Python Basics",
                    englishOrigin = ContentOrigin.OFFICIAL_MOE,
                    chapters = listOf(
                        ChapterSpec(
                            id = "sec1_prog_c1",
                            order = 1,
                            titleAr = "المتغيرات والشروط والحلقات التكرارية",
                            titleEn = "Variables, Conditionals & Loops",
                            lessons = listOf(
                                LessonSpec(
                                    id = "sec1_prog_l1",
                                    order = 1,
                                    titleAr = "بناء الخوارزميات وهياكل التحكم المنطقية",
                                    titleEn = "Designing Algorithms & Conditional Logic",
                                    objectivesAr = listOf(
                                        "صياغة خطوات حل المشكلة باستخدام خرائط التدفق",
                                        "كتابة جمل الشرط if / elif / else بلغة بايثون",
                                        "فهم مفهوم البيانات التدريبية في الذكاء الاصطناعي"
                                    ),
                                    objectivesEn = listOf(
                                        "Model problem-solving steps with flowcharts",
                                        "Write if / elif / else conditional branches in Python",
                                        "Understand training data fundamentals in AI systems"
                                    ),
                                    contentAr = "الخوارزمية هي مجموعة خطوات منطقية مرتبة ومحددة لحل مشكلة ما. وفي برمجة الذكاء الاصطناعي، نستخدم التفرع الشرطي والبيانات المهيكلة لتحويل المدخلات إلى قرارات دقيقة.",
                                    contentEn = "An algorithm is a finite, ordered sequence of logical steps to solve a problem. Using conditional branching and structured inputs allows programs to make reliable decisions.",
                                    formulasAndDefinitionsAr = listOf(
                                        "مكونات الخوارزمية: المدخلات -> المعالجة -> المخرجات",
                                        "بناء الشرط: if condition: ... else: ..."
                                    ),
                                    formulasAndDefinitionsEn = listOf(
                                        "Algorithm Flow: Input -> Processing -> Output",
                                        "Conditional syntax: if condition: ... else: ..."
                                    ),
                                    exercisesAr = listOf(
                                        "اكتب خوارزمية لقراءة درجة الطالب وطباعة (ناجح) إذا كانت >= 50.",
                                        "ما الفرق بين المتغير النصي (str) والعدد الصحيح (int)؟"
                                    ),
                                    exercisesEn = listOf(
                                        "Write an algorithm that reads a student's score and prints 'Pass' if >= 50.",
                                        "What is the difference between str and int data types?"
                                    ),
                                    revisionSummaryAr = "تبدأ كل خوارزمية بتحديد المدخلات والمخرجات قبل كتابة الشيفرة.",
                                    revisionSummaryEn = "Every algorithm starts by defining inputs and outputs before writing code.",
                                    officialSourceUrl = MOE_ELEARNING_PORTAL,
                                    publicationDate = "2026-09-20",
                                    englishOrigin = ContentOrigin.OFFICIAL_MOE
                                )
                            )
                        )
                    )
                )
            )
        ),
        // ==================== SECOND SECONDARY (الصف الثاني الثانوي) ====================
        SubjectSpec(
            id = "sec2_physics",
            gradeId = "sec_2",
            trackIds = listOf("sci_general", "bacc_med", "bacc_eng"),
            semester = 1,
            nameAr = "الفيزياء — الصف الثاني الثانوي",
            nameEn = "Physics — 2nd Secondary (AR / EN)",
            hasOfficialArabic = true,
            hasOfficialEnglish = true,
            officialMoePortalUrl = MOE_BOOKS_PORTAL,
            officialBookPdfUrlAr = MOE_BOOKS_PORTAL,
            officialBookPdfUrlEn = MOE_BOOKS_PORTAL,
            units = listOf(
                UnitSpec(
                    id = "sec2_phys_u1",
                    order = 1,
                    titleAr = "الوحدة الأولى: الموجات والضوء",
                    titleEn = "Unit 1: Waves and Light",
                    englishOrigin = ContentOrigin.OFFICIAL_MOE,
                    chapters = listOf(
                        ChapterSpec(
                            id = "sec2_phys_c1",
                            order = 1,
                            titleAr = "الحركة الاهتزازية والموجية",
                            titleEn = "Oscillatory & Wave Motion",
                            lessons = listOf(
                                LessonSpec(
                                    id = "sec2_phys_l1",
                                    order = 1,
                                    titleAr = "العلاقة بين التردد والطول الموجي وسرعة انتشار الموجة",
                                    titleEn = "Wave Speed, Frequency & Wavelength Relation",
                                    objectivesAr = listOf(
                                        "التمييز بين الموجات المستعرضة والطولية",
                                        "استنتاج قانون انتشار الموجات v = λ × ν",
                                        "حساب الزمن الدوري والتردد من الرسم البياني"
                                    ),
                                    objectivesEn = listOf(
                                        "Distinguish transverse waves from longitudinal waves",
                                        "Derive and apply the wave equation v = λ × f",
                                        "Determine period and frequency from displacement graphs"
                                    ),
                                    contentAr = "تنتقل الموجة دون انتقال جزيئات الوسط نفسه، وإنما تنقل الطاقة في اتجاه الانتشار. في الوسط المتجانس تظل سرعة الموجة ثابتة، فيتناسب التردد عكسيًا مع الطول الموجي (v = λν).",
                                    contentEn = "A wave transfers energy without net transport of medium particles. In a uniform medium, wave speed v is constant, so wavelength λ is inversely proportional to frequency f (v = λf).",
                                    formulasAndDefinitionsAr = listOf(
                                        "v = λ × ν (سرعة الموجة = الطول الموجي × التردد)",
                                        "ν = 1 / T = N / t",
                                        "الطول الموجي للمستعرضة = المسافة بين قمتين متتاليتين أو قاعين متتاليين"
                                    ),
                                    formulasAndDefinitionsEn = listOf(
                                        "v = λ × f (Wave Speed = Wavelength × Frequency)",
                                        "f = 1 / T = N / t",
                                        "Transverse wavelength = distance between two successive crests or troughs"
                                    ),
                                    exercisesAr = listOf(
                                        "موجة مستعرضة ترددها 50 Hz وطولها الموجي 0.4 m، احسب سرعة انتشارها.",
                                        "ماذا يحدث للطول الموجي عند انتقال موجة صوتية من الهواء إلى الماء؟"
                                    ),
                                    exercisesEn = listOf(
                                        "A transverse wave has frequency 50 Hz and wavelength 0.4 m. Calculate its speed.",
                                        "What happens to wavelength when a sound wave passes from air into water?"
                                    ),
                                    revisionSummaryAr = "عند ثبوت الوسط تثبت السرعة، وعند انتقال الموجة بين وسطين يثبت التردد.",
                                    revisionSummaryEn = "Same medium -> constant speed; crossing media -> constant frequency.",
                                    officialSourceUrl = MOE_BOOKS_PORTAL,
                                    publicationDate = "2026-09-15",
                                    englishOrigin = ContentOrigin.OFFICIAL_MOE
                                )
                            )
                        )
                    )
                )
            )
        ),
        SubjectSpec(
            id = "sec2_history",
            gradeId = "sec_2",
            trackIds = listOf("lit", "bacc_med", "bacc_eng"),
            semester = 1,
            nameAr = "التاريخ الوطني والحضارة الإسلامية",
            nameEn = "National & Islamic Civilization History",
            hasOfficialArabic = true,
            hasOfficialEnglish = false,
            officialMoePortalUrl = MOE_BACCALAUREATE_PORTAL,
            officialBookPdfUrlAr = MOE_BACCALAUREATE_PORTAL,
            officialBookPdfUrlEn = null,
            units = listOf(
                UnitSpec(
                    id = "sec2_hist_u1",
                    order = 1,
                    titleAr = "الوحدة الأولى: حضارة شبه الجزيرة العربية وظهور الإسلام",
                    titleEn = "Unit 1: Civilization of the Arabian Peninsula & Rise of Islam",
                    englishOrigin = ContentOrigin.AI_TRANSLATED,
                    chapters = listOf(
                        ChapterSpec(
                            id = "sec2_hist_c1",
                            order = 1,
                            titleAr = "ممالك شبه الجزيرة العربية وأحوالها قبل الإسلام",
                            titleEn = "Kingdoms & Conditions Before Islam",
                            lessons = listOf(
                                LessonSpec(
                                    id = "sec2_hist_l1",
                                    order = 1,
                                    titleAr = "المراكز الحضارية في اليمن والحجاز والشام والعراق",
                                    titleEn = "Civilizational Centers in Yemen, Hijaz, Levant & Iraq",
                                    objectivesAr = listOf(
                                        "تحليل العوامل الجغرافية التي أثرت في قيام الممالك العربية القديمة",
                                        "المقارنة بين النظام القبلي والنظام الملكي في شبه الجزيرة"
                                    ),
                                    objectivesEn = listOf(
                                        "Analyze geographic factors shaping ancient Arabian kingdoms",
                                        "Compare tribal and monarchical systems in the peninsula"
                                    ),
                                    contentAr = "شهدت شبه الجزيرة العربية قبل الإسلام قيام ممالك مستقرة مثل معين وسبأ وحمير في الجنوب، والحيرة والغساسنة في الشمال، إضافة إلى المدن التجارية الكبرى في الحجاز (مكة ويثرب والطائف) التي جمعت بين التجارة ومكانتها الدينية.",
                                    contentEn = "Before Islam, stable kingdoms flourished in southern Arabia (Ma'in, Saba, Himyar) and the north (Al-Hirah, Ghassanids), alongside major commercial and religious cities in Hijaz (Mecca, Yathrib, Taif).",
                                    formulasAndDefinitionsAr = listOf(
                                        "حلف الفضول: حلف تعاهدت فيه قبائل مكة على نصرة المظلوم.",
                                        "سد مأرب: أعظم منشآت الري في مملكة سبأ."
                                    ),
                                    formulasAndDefinitionsEn = listOf(
                                        "Hilf al-Fudul: Pact of Meccan tribes to defend the oppressed.",
                                        "Ma'rib Dam: Major irrigation landmark of the Kingdom of Saba."
                                    ),
                                    exercisesAr = listOf(
                                        "استنتج أثر الموقع الجغرافي لمكة على ازدهارها التجاري.",
                                        "قارن بين مملكتي الحيرة والغساسنة من حيث التحالفات السياسية."
                                    ),
                                    exercisesEn = listOf(
                                        "Infer how Mecca's geographic location boosted its trade.",
                                        "Compare Al-Hirah and the Ghassanids regarding political alliances."
                                    ),
                                    revisionSummaryAr = "الجنوب زراعي وتجاري (سبأ وحمير)، والحجاز تجاري ديني، والشمال دول حاجزة.",
                                    revisionSummaryEn = "South: agriculture & maritime trade; Hijaz: caravan trade; North: buffer kingdoms.",
                                    officialSourceUrl = MOE_BACCALAUREATE_PORTAL,
                                    publicationDate = "2026-09-15",
                                    englishOrigin = ContentOrigin.AI_TRANSLATED
                                )
                            )
                        )
                    )
                )
            )
        ),
        // ==================== THIRD SECONDARY (الصف الثالث الثانوي) ====================
        SubjectSpec(
            id = "sec3_biology",
            gradeId = "sec_3",
            trackIds = listOf("sci_bio"),
            semester = 1,
            nameAr = "الأحياء — الصف الثالث الثانوي (علمي علوم)",
            nameEn = "Biology — 3rd Secondary (Scientific Biology)",
            hasOfficialArabic = true,
            hasOfficialEnglish = true,
            officialMoePortalUrl = MOE_BOOKS_PORTAL,
            officialBookPdfUrlAr = MOE_BOOKS_PORTAL,
            officialBookPdfUrlEn = MOE_BOOKS_PORTAL,
            units = listOf(
                UnitSpec(
                    id = "sec3_bio_u1",
                    order = 1,
                    titleAr = "الباب الأول: التركيب والوظيفة في الكائنات الحية",
                    titleEn = "Chapter 1: Structure & Function in Living Organisms",
                    englishOrigin = ContentOrigin.OFFICIAL_MOE,
                    chapters = listOf(
                        ChapterSpec(
                            id = "sec3_bio_c1",
                            order = 1,
                            titleAr = "الدعامة والحركة في الكائنات الحية",
                            titleEn = "Support & Movement in Living Organisms",
                            lessons = listOf(
                                LessonSpec(
                                    id = "sec3_bio_l1",
                                    order = 1,
                                    titleAr = "الدعامة الفسيولوجية والتركيبية في النبات والهيكل العظمي",
                                    titleEn = "Physiological & Structural Support + Human Axial Skeleton",
                                    objectivesAr = listOf(
                                        "التمييز بين الدعامة الفسيولوجية المؤقتة والدعامة التركيبية الدائمة",
                                        "التعرف على تقسيمات العمود الفقري (33 فقرة) وأنواع الفقرات",
                                        "تفسير آلية انقباض العضلة الهيكلية وفق نظرية الخيوط المنزلقة"
                                    ),
                                    objectivesEn = listOf(
                                        "Differentiate temporary physiological support from permanent structural support",
                                        "Identify vertebral column regions (33 vertebrae) and structure",
                                        "Explain skeletal muscle contraction via Sliding Filament Theory"
                                    ),
                                    contentAr = "تعتمد الدعامة الفسيولوجية في النبات على دخول الماء بالخاصية الاسموزية إلى الفجوة العصارية فتنتفخ الخلية وتتوتر جدرانها (دعامة مؤقتة). أما الدعامة التركيبية فتتم بترسيب مواد صلبة كالسليلوز واللجنين والكيوتين والسيوبرين (دعامة دائمة). في الإنسان يبلغ عدد عظام الهيكل 206 عظمات، والعمود الفقري 33 فقرة (24 متمفصلة و9 ملتحمة).",
                                    contentEn = "Physiological support in plants is temporary and relies on osmosis filling the cell vacuole to build turgor pressure. Structural support is permanent via deposition of cellulose, lignin, cutin, or suberin. The adult human skeleton has 206 bones and 33 vertebrae (24 articulated, 9 fused).",
                                    formulasAndDefinitionsAr = listOf(
                                        "العمود الفقري = 7 عنقية + 12 ظهرية + 5 قطنية + 5 عجزية (ملتحمة = 1 عظمة) + 4 عصعصية (ملتحمة = 1 عظمة) = 26 عظمة",
                                        "نظرية هكسلي: تنزلق خيوط الأكتين الرفيعة فوق خيوط الميوسين السميكة بمساعدة أيونات الكالسيوم Ca²⁺ وATP"
                                    ),
                                    formulasAndDefinitionsEn = listOf(
                                        "Vertebral column = 7 cervical + 12 thoracic + 5 lumbar + 5 fused sacral (1 bone) + 4 fused coccygeal (1 bone) = 26 bones",
                                        "Huxley's Theory: Actin filaments slide over myosin via cross-bridges using Ca²⁺ and ATP"
                                    ),
                                    exercisesAr = listOf(
                                        "ما رقم الفقرة التي تتوسط العمود الفقري؟ وما رقم آخر فقرة ظهرية؟",
                                        "فسّر دور أيونات الكالسيوم وإنزيم الكولين إستيريز في الانقباض والانبساط العضلي."
                                    ),
                                    exercisesEn = listOf(
                                        "Which vertebra lies at the exact midpoint of the 33-vertebra column?",
                                        "Explain the roles of Ca²⁺ ions and acetylcholinesterase during contraction and relaxation."
                                    ),
                                    revisionSummaryAr = "عدد فقرات العمود الفقري 33 فقرة، ولكن عدد عظامه 26 عظمة بسبب التحام العجزية والعصعصية.",
                                    revisionSummaryEn = "33 vertebrae form 26 bones due to sacral (5->1) and coccygeal (4->1) fusion.",
                                    officialSourceUrl = MOE_BOOKS_PORTAL,
                                    publicationDate = "2026-09-15",
                                    englishOrigin = ContentOrigin.OFFICIAL_MOE
                                )
                            )
                        )
                    )
                )
            )
        ),
        SubjectSpec(
            id = "sec3_calculus",
            gradeId = "sec_3",
            trackIds = listOf("sci_math"),
            semester = 1,
            nameAr = "الرياضيات البحتة — التفاضل والتكامل (علمي رياضة)",
            nameEn = "Pure Math: Calculus — 3rd Secondary (Scientific Math)",
            hasOfficialArabic = true,
            hasOfficialEnglish = true,
            officialMoePortalUrl = MOE_BOOKS_PORTAL,
            officialBookPdfUrlAr = MOE_BOOKS_PORTAL,
            officialBookPdfUrlEn = MOE_BOOKS_PORTAL,
            units = listOf(
                UnitSpec(
                    id = "sec3_calc_u1",
                    order = 1,
                    titleAr = "الوحدة الأولى: الاشتقاق وتطبيقاته",
                    titleEn = "Unit 1: Differentiation & Applications",
                    englishOrigin = ContentOrigin.OFFICIAL_MOE,
                    chapters = listOf(
                        ChapterSpec(
                            id = "sec3_calc_c1",
                            order = 1,
                            titleAr = "مشتقات الدوال المثلثية والضمنية والمعدلات الزمنية",
                            titleEn = "Derivatives of Trig Functions, Implicit & Related Rates",
                            lessons = listOf(
                                LessonSpec(
                                    id = "sec3_calc_l1",
                                    order = 1,
                                    titleAr = "اشتقاق الدوال المثلثية ومعادلة المماس والعمودي",
                                    titleEn = "Trigonometric Derivatives, Tangent & Normal Equations",
                                    objectivesAr = listOf(
                                        "إيجاد مشتقات قا(س)، قتا(س)، ظا(س)، ظتا(س)",
                                        "تطبيق قاعدة السلسلة والاشتقاق الضمني والبارامتري",
                                        "تكوين معادلة المماس والعمودي للمنحنى عند نقطة"
                                    ),
                                    objectivesEn = listOf(
                                        "Differentiate tan(x), cot(x), sec(x), and csc(x)",
                                        "Apply chain rule, implicit, and parametric differentiation",
                                        "Find tangent and normal line equations at a given point"
                                    ),
                                    contentAr = "ميل المماس للمنحنى ص = د(س) عند النقطة (س₁، ص₁) هو المشتقة الأولى د'(س₁). وميل العمودي = -1 / د'(س₁) بشرط د'(س₁) ≠ 0.",
                                    contentEn = "The slope of the tangent to y = f(x) at (x₁, y₁) is dy/dx = f'(x₁). The slope of the normal is -1 / f'(x₁) when f'(x₁) ≠ 0.",
                                    formulasAndDefinitionsAr = listOf(
                                        "د/دس [ظا س] = قا² س | د/دس [قا س] = قا س ظا س",
                                        "د/دس [ظتا س] = -قتا² س | د/دس [قتا س] = -قتا س ظتا س",
                                        "معادلة المماس: ص - ص₁ = م (س - س₁)"
                                    ),
                                    formulasAndDefinitionsEn = listOf(
                                        "d/dx [tan x] = sec² x | d/dx [sec x] = sec x tan x",
                                        "d/dx [cot x] = -csc² x | d/dx [csc x] = -csc x cot x",
                                        "Tangent equation: y - y₁ = m (x - x₁)"
                                    ),
                                    exercisesAr = listOf(
                                        "أوجد دص/دس إذا كانت ص = قا(3س² + 1).",
                                        "أوجد معادلتي المماس والعمودي للمنحنى س² + ص² = 25 عند النقطة (3، 4)."
                                    ),
                                    exercisesEn = listOf(
                                        "Find dy/dx for y = sec(3x² + 1).",
                                        "Find tangent and normal equations to x² + y² = 25 at (3, 4)."
                                    ),
                                    revisionSummaryAr = "تذكر الإشارة السالبة عند اشتقاق أي دالة مثلثية تبدأ بحرف (ت / co-).",
                                    revisionSummaryEn = "Any co-trig derivative (cos, cot, csc) carries a negative sign.",
                                    officialSourceUrl = MOE_BOOKS_PORTAL,
                                    publicationDate = "2026-09-15",
                                    englishOrigin = ContentOrigin.OFFICIAL_MOE
                                )
                            )
                        )
                    )
                )
            )
        ),
        SubjectSpec(
            id = "sec3_chemistry",
            gradeId = "sec_3",
            trackIds = listOf("sci_bio", "sci_math"),
            semester = 1,
            nameAr = "الكيمياء — العناصر الانتقالية والعضوية",
            nameEn = "Chemistry — Transition Elements & Organic (3rd Sec)",
            hasOfficialArabic = true,
            hasOfficialEnglish = true,
            officialMoePortalUrl = MOE_BOOKS_PORTAL,
            officialBookPdfUrlAr = MOE_BOOKS_PORTAL,
            officialBookPdfUrlEn = MOE_BOOKS_PORTAL,
            units = listOf(
                UnitSpec(
                    id = "sec3_chem_u1",
                    order = 1,
                    titleAr = "الباب الأول: العناصر الانتقالية",
                    titleEn = "Chapter 1: Transition Elements",
                    englishOrigin = ContentOrigin.OFFICIAL_MOE,
                    chapters = listOf(
                        ChapterSpec(
                            id = "sec3_chem_c1",
                            order = 1,
                            titleAr = "السلسلة الانتقالية الأولى وخواصها واستخلاص الحديد",
                            titleEn = "First Transition Series & Iron Extraction",
                            lessons = listOf(
                                LessonSpec(
                                    id = "sec3_chem_l1",
                                    order = 1,
                                    titleAr = "الأهمية الاقتصادية وحالات التأكسد لعناصر السلسلة الأولى",
                                    titleEn = "Economic Importance & Oxidation States of 3d Elements",
                                    objectivesAr = listOf(
                                        "تفسير تعدد حالات التأكسد لعناصر السلسلة الانتقالية الأولى",
                                        "حساب العزم المغناطيسي والتمييز بين البارامغناطيسية والديامغناطيسية",
                                        "تحديد دور العامل الحفاز في خفض طاقة التنشيط"
                                    ),
                                    objectivesEn = listOf(
                                        "Explain variable oxidation states in the 3d transition series",
                                        "Predict paramagnetic vs diamagnetic behavior from unpaired d-electrons",
                                        "Explain how transition catalysts lower activation energy"
                                    ),
                                    contentAr = "تتتابع في السلسلة الانتقالية الأولى امتلاء المستوى الفرعي 3d من السكانديوم (₂₁Sc) إلى الخارصين (₃₀Zn). ويُعزى تعدد حالات التأكسد إلى تقارب طاقتي المستويين الفرعيين 4s و 3d فتخرج الإلكترونات من 4s أولًا ثم 3d بالتتابع.",
                                    contentEn = "In the 3d transition series (₂₁Sc to ₃₀Zn), electrons fill the 3d sublevel. Variable oxidation states occur because 4s and 3d energies are close; electrons leave 4s first before 3d.",
                                    formulasAndDefinitionsAr = listOf(
                                        "العزم المغناطيسي يزداد بزيادة عدد الإلكترونات المفردة في أوربيتالات d",
                                        "شذوذ التوزيع: ₂₄Cr: [Ar] 4s¹ 3d⁵ | ₂₉Cu: [Ar] 4s¹ 3d¹⁰"
                                    ),
                                    formulasAndDefinitionsEn = listOf(
                                        "Magnetic moment increases with unpaired d-electrons",
                                        "Anomalous configurations: ₂₄Cr: [Ar] 4s¹ 3d⁵ | ₂₉Cu: [Ar] 4s¹ 3d¹⁰"
                                    ),
                                    exercisesAr = listOf(
                                        "فسّر: يصعب تأكسد أيون المنجنيز (II) إلى منجنيز (III) بينما يسهل تأكسد الحديد (II) إلى حديد (III).",
                                        "رتّب الأيونات التالية حسب العزم المغناطيسي: Fe²⁺, Cr³⁺, Zn²⁺."
                                    ),
                                    exercisesEn = listOf(
                                        "Explain why Fe²⁺ oxidizes easily to Fe³⁺ while Mn²⁺ resists oxidation to Mn³⁺.",
                                        "Order by magnetic moment: Fe²⁺, Cr³⁺, Zn²⁺."
                                    ),
                                    revisionSummaryAr = "ثبات النصف ممتلئ d⁵ والممتلئ d¹⁰ يفسر شذوذ التوزيع واستقرار الأيونات.",
                                    revisionSummaryEn = "Half-filled d⁵ and completely filled d¹⁰ stability explains anomalous configurations and ion stability.",
                                    officialSourceUrl = MOE_BOOKS_PORTAL,
                                    publicationDate = "2026-09-15",
                                    englishOrigin = ContentOrigin.OFFICIAL_MOE
                                )
                            )
                        )
                    )
                )
            )
        ),
        SubjectSpec(
            id = "sec3_geography",
            gradeId = "sec_3",
            trackIds = listOf("lit"),
            semester = 1,
            nameAr = "الجغرافيا السياسية — الصف الثالث الثانوي (أدبي)",
            nameEn = "Political Geography — 3rd Secondary (Literary)",
            hasOfficialArabic = true,
            hasOfficialEnglish = false,
            officialMoePortalUrl = MOE_BOOKS_PORTAL,
            officialBookPdfUrlAr = MOE_BOOKS_PORTAL,
            officialBookPdfUrlEn = null,
            units = listOf(
                UnitSpec(
                    id = "sec3_geo_u1",
                    order = 1,
                    titleAr = "الوحدة الأولى: الدولة في الجغرافيا السياسية",
                    titleEn = "Unit 1: The State in Political Geography",
                    englishOrigin = ContentOrigin.AI_TRANSLATED,
                    chapters = listOf(
                        ChapterSpec(
                            id = "sec3_geo_c1",
                            order = 1,
                            titleAr = "الدولة: تعريفها وأنواعها وعواصمها",
                            titleEn = "The State: Types, Borders & Capitals",
                            lessons = listOf(
                                LessonSpec(
                                    id = "sec3_geo_l1",
                                    order = 1,
                                    titleAr = "المقومات الطبيعية للدولة (الموقع والمساحة والشكل)",
                                    titleEn = "Natural Pillars of the State: Location, Area & Shape",
                                    objectivesAr = listOf(
                                        "المقارنة بين الدولة الوحدوية المركزية والدولة الفيدرالية",
                                        "تحليل أثر الشكل المندمج والمستطيل والمجزأ على قوة الدولة"
                                    ),
                                    objectivesEn = listOf(
                                        "Compare unitary states with federal states",
                                        "Analyze how compact, elongated, and fragmented shapes affect state strength"
                                    ),
                                    contentAr = "الدولة الوحدوية (مثل مصر وفرنسا واليابان) تضم حكومة مركزية واحدة ومجلسًا نيابيًا واحدًا، بينما الدولة الفيدرالية (مثل الولايات المتحدة والبرازيل والهند) تنقسم فيها السلطات بين حكومة مركزية وحكومات محلية في الشؤون الحياتية.",
                                    contentEn = "A unitary state (Egypt, France, Japan) centralizes authority in one national government, whereas a federal state (USA, Brazil, India) divides sovereignty between central defense/foreign affairs and regional local governments.",
                                    formulasAndDefinitionsAr = listOf(
                                        "أفضل أشكال الدول: الشكل المنتظم المندمج مثل مصر وبلجيكا وفرنسا.",
                                        "قلب الدولة الاقتصادي: إقليم البحيرات العظمى بين أمريكا وكندا."
                                    ),
                                    formulasAndDefinitionsEn = listOf(
                                        "Optimal state shape: Compact/regular (Egypt, France, Belgium).",
                                        "Economic core example: Great Lakes region (USA-Canada)."
                                    ),
                                    exercisesAr = listOf(
                                        "قارن بين اختيار القاهرة والخرطوم وأنقرة كعواصم للدول.",
                                        "بم تفسر: تُعد مصر نموذجًا مثاليًا للدولة الوحدوية؟"
                                    ),
                                    exercisesEn = listOf(
                                        "Compare capital selection criteria for Cairo, Khartoum, and Ankara.",
                                        "Explain why Egypt is an ideal model of a unitary state."
                                    ),
                                    revisionSummaryAr = "التجانس السكاني وقوة الاتصال الداخلي يميزان الدولة الوحدوية ذات الشكل المندمج.",
                                    revisionSummaryEn = "Cultural homogeneity and internal connectivity characterize compact unitary states.",
                                    officialSourceUrl = MOE_BOOKS_PORTAL,
                                    publicationDate = "2026-09-15",
                                    englishOrigin = ContentOrigin.AI_TRANSLATED
                                )
                            )
                        )
                    )
                )
            )
        )
    )

    val officialAssessments: List<OfficialAssessmentItem> = listOf(
        OfficialAssessmentItem(
            id = "moe_prep1_sci_w5",
            titleAr = "التقييم الأسبوعي الخامس — العلوم (الصف الأول الإعدادي)",
            titleEn = "Week 5 Assessment — Science (1st Preparatory)",
            gradeId = "prep_1",
            subjectId = "prep1_sci_en",
            subjectNameAr = "العلوم",
            subjectNameEn = "Science",
            academicYear = "2026-2027",
            semester = 1,
            periodLabelAr = "الأسبوع الخامس — الفصل الأول",
            periodLabelEn = "Week 5 — Semester 1",
            assessmentType = AssessmentType.WEEKLY,
            language = LanguageCode.AR,
            sourceUrl = MOE_ASSESSMENTS_PORTAL,
            pdfDirectUrl = null,
            publishedDateLabel = "2026-10-18",
            isOfficialVerified = true,
            sampleQuestionsAr = listOf(
                "اكتب المصطلح العلمي: عدد البروتونات الموجبة داخل نواة ذرة العنصر.",
                "علل: تتساوى أعداد الإلكترونات مع البروتونات في الذرة المتعادلة."
            ),
            sampleQuestionsEn = listOf(
                "Write the scientific term: Number of positive protons inside an atom's nucleus.",
                "Give reason: Electrons equal protons in a neutral atom."
            )
        ),
        OfficialAssessmentItem(
            id = "moe_prep1_math_oct",
            titleAr = "اختبار شهر أكتوبر الرسمي — الرياضيات (الأول الإعدادي)",
            titleEn = "October Monthly Assessment — Mathematics (1st Prep)",
            gradeId = "prep_1",
            subjectId = "prep1_math_ar",
            subjectNameAr = "الرياضيات",
            subjectNameEn = "Mathematics",
            academicYear = "2026-2027",
            semester = 1,
            periodLabelAr = "شهر أكتوبر 2026",
            periodLabelEn = "October 2026",
            assessmentType = AssessmentType.MONTHLY,
            language = LanguageCode.AR,
            sourceUrl = MOE_ASSESSMENTS_PORTAL,
            pdfDirectUrl = null,
            publishedDateLabel = "2026-10-27",
            isOfficialVerified = true,
            sampleQuestionsAr = listOf(
                "إذا كان مقياس الرسم 1 : 200,000 والمسافة الحقيقية 10 كم، فما الطول على الخريطة؟"
            ),
            sampleQuestionsEn = listOf(
                "At scale 1:200,000 and actual distance 10 km, find the map length in cm."
            )
        ),
        OfficialAssessmentItem(
            id = "moe_prep2_sci_w6",
            titleAr = "الأداء الصفي والمنزلي — العلوم (الصف الثاني الإعدادي)",
            titleEn = "Classroom & Home Assessment — Science (2nd Prep)",
            gradeId = "prep_2",
            subjectId = "prep2_sci",
            subjectNameAr = "العلوم",
            subjectNameEn = "Science",
            academicYear = "2026-2027",
            semester = 1,
            periodLabelAr = "الأسبوع السادس",
            periodLabelEn = "Week 6",
            assessmentType = AssessmentType.GENERAL,
            language = LanguageCode.AR,
            sourceUrl = MOE_ASSESSMENTS_PORTAL,
            pdfDirectUrl = null,
            publishedDateLabel = "2026-10-25",
            isOfficialVerified = true,
            sampleQuestionsAr = listOf(
                "قارن بين الفلزات واللافلزات من حيث سلوكها الكيميائي وتدرج الحجم الذري."
            ),
            sampleQuestionsEn = listOf(
                "Compare metals and nonmetals in terms of chemical behavior and atomic radius trend."
            )
        ),
        OfficialAssessmentItem(
            id = "moe_prep3_math_spec",
            titleAr = "مواصفات الورقة الامتحانية ونموذج استرشادي — الرياضيات (الثالث الإعدادي)",
            titleEn = "Exam Specification & Model Exam — Math (3rd Prep)",
            gradeId = "prep_3",
            subjectId = "prep3_math",
            subjectNameAr = "الرياضيات",
            subjectNameEn = "Mathematics",
            academicYear = "2026-2027",
            semester = 1,
            periodLabelAr = "مواصفات 2026-2027",
            periodLabelEn = "2026-2027 Exam Specs",
            assessmentType = AssessmentType.EXAM_SPECIFICATION,
            language = LanguageCode.AR,
            sourceUrl = MOE_ASSESSMENTS_PORTAL,
            pdfDirectUrl = null,
            publishedDateLabel = "2026-11-02",
            isOfficialVerified = true,
            sampleQuestionsAr = listOf(
                "أوجد قيمة س إذا كانت جا(س) = جتا(2س) حيث س زاوية حادة موجبة."
            ),
            sampleQuestionsEn = listOf(
                "Find acute angle x if sin(x) = cos(2x)."
            )
        ),
        OfficialAssessmentItem(
            id = "moe_sec1_isci_nov",
            titleAr = "التقييم الشهري — العلوم المتكاملة (الصف الأول الثانوي)",
            titleEn = "Monthly Assessment — Integrated Sciences (1st Secondary)",
            gradeId = "sec_1",
            subjectId = "sec1_integrated_sci",
            subjectNameAr = "العلوم المتكاملة",
            subjectNameEn = "Integrated Sciences",
            academicYear = "2026-2027",
            semester = 1,
            periodLabelAr = "شهر نوفمبر 2026",
            periodLabelEn = "November 2026",
            assessmentType = AssessmentType.MONTHLY,
            language = LanguageCode.EN,
            sourceUrl = MOE_ASSESSMENTS_PORTAL,
            pdfDirectUrl = null,
            publishedDateLabel = "2026-11-10",
            isOfficialVerified = true,
            sampleQuestionsAr = listOf(
                "اشرح أثر الروابط الهيدروجينية على استقرار النظم البيئية المائية."
            ),
            sampleQuestionsEn = listOf(
                "Explain how hydrogen bonding stabilizes aquatic ecosystems thermally."
            )
        ),
        OfficialAssessmentItem(
            id = "moe_sec2_phys_w7",
            titleAr = "التقييم الأسبوعي السابع — الفيزياء (الصف الثاني الثانوي)",
            titleEn = "Week 7 Assessment — Physics (2nd Secondary)",
            gradeId = "sec_2",
            subjectId = "sec2_physics",
            subjectNameAr = "الفيزياء",
            subjectNameEn = "Physics",
            academicYear = "2026-2027",
            semester = 1,
            periodLabelAr = "الأسبوع السابع",
            periodLabelEn = "Week 7",
            assessmentType = AssessmentType.WEEKLY,
            language = LanguageCode.AR,
            sourceUrl = MOE_ASSESSMENTS_PORTAL,
            pdfDirectUrl = null,
            publishedDateLabel = "2026-11-08",
            isOfficialVerified = true,
            sampleQuestionsAr = listOf(
                "مصدر مهتز يصنع 600 اهتزازة في الدقيقة؛ احسب الزمن الدوري والتردد."
            ),
            sampleQuestionsEn = listOf(
                "A vibrating source makes 600 oscillations per minute; calculate period and frequency."
            )
        ),
        OfficialAssessmentItem(
            id = "moe_sec3_bio_model",
            titleAr = "النموذج الاسترشادي الأول للثانوية العامة — الأحياء (علمي علوم)",
            titleEn = "Thanaweya Amma Model Exam 1 — Biology (Official MOE)",
            gradeId = "sec_3",
            subjectId = "sec3_biology",
            subjectNameAr = "الأحياء",
            subjectNameEn = "Biology",
            academicYear = "2026-2027",
            semester = 1,
            periodLabelAr = "نماذج الثانوية العامة 2026-2027",
            periodLabelEn = "Thanaweya Amma Models 2026-2027",
            assessmentType = AssessmentType.MODEL_EXAM,
            language = LanguageCode.AR,
            sourceUrl = MOE_ASSESSMENTS_PORTAL,
            pdfDirectUrl = null,
            publishedDateLabel = "2026-11-12",
            isOfficialVerified = true,
            sampleQuestionsAr = listOf(
                "أي الفقرات التالية تمثل آخر فقرة متمفصلة في العمود الفقري للإنسان؟ (أ) 24  (ب) 25  (ج) 29  (د) 33"
            ),
            sampleQuestionsEn = listOf(
                "Which vertebra is the last articulated vertebra in the human vertebral column? (A) 24 (B) 25 (C) 29 (D) 33"
            )
        ),
        OfficialAssessmentItem(
            id = "moe_sec3_chem_bank",
            titleAr = "بنك الأسئلة والمراجعة — الكيمياء (الصف الثالث الثانوي)",
            titleEn = "Official Revision Question Bank — Chemistry (3rd Secondary)",
            gradeId = "sec_3",
            subjectId = "sec3_chemistry",
            subjectNameAr = "الكيمياء",
            subjectNameEn = "Chemistry",
            academicYear = "2026-2027",
            semester = 1,
            periodLabelAr = "مراجعة الباب الأول",
            periodLabelEn = "Chapter 1 Revision",
            assessmentType = AssessmentType.QUESTION_BANK,
            language = LanguageCode.EN,
            sourceUrl = MOE_ASSESSMENTS_PORTAL,
            pdfDirectUrl = null,
            publishedDateLabel = "2026-11-14",
            isOfficialVerified = true,
            sampleQuestionsAr = listOf(
                "احسب عدد الإلكترونات المفردة في أيون الكوبلت (II) ₂₇Co²⁺."
            ),
            sampleQuestionsEn = listOf(
                "Determine the number of unpaired electrons in Cobalt(II) ion ₂₇Co²⁺."
            )
        )
    )

    fun subjectsFor(gradeId: String, trackId: String?, semester: Int? = null): List<SubjectSpec> =
        subjects.filter { s ->
            s.gradeId == gradeId &&
                (trackId.isNullOrBlank() || s.trackIds.contains(trackId)) &&
                (semester == null || s.semester == semester)
        }

    fun allLessons(): List<Triple<SubjectSpec, UnitSpec, LessonSpec>> =
        subjects.flatMap { sub ->
            sub.units.flatMap { unit ->
                unit.chapters.flatMap { chap ->
                    chap.lessons.map { lesson -> Triple(sub, unit, lesson) }
                }
            }
        }

    fun findGrade(gradeId: String): GradeSpec =
        grades.firstOrNull { it.id == gradeId } ?: grades.first()
}
