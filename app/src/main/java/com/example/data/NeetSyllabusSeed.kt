package com.example.data

object NeetSyllabusSeed {
    fun initialChapters(): List<SyllabusChapterEntity> = listOf(
        // PHYSICS (Class 11 & 12 High-Yield & Core NCERT)
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 11, chapterName = "Units, Dimensions & Vectors", weightagePct = 4.5f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 11, chapterName = "Kinematics (1D & 2D Motion)", weightagePct = 5.0f, isHighYield = false),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 11, chapterName = "Laws of Motion & Friction", weightagePct = 4.8f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 11, chapterName = "Work, Energy & Power", weightagePct = 4.5f, isHighYield = false),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 11, chapterName = "Rotational Motion & COM", weightagePct = 6.2f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 11, chapterName = "Gravitation & Fluids", weightagePct = 4.2f, isHighYield = false),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 11, chapterName = "Thermodynamics & KTG", weightagePct = 6.8f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 11, chapterName = "Oscillations & Waves", weightagePct = 4.6f, isHighYield = false),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 12, chapterName = "Electrostatics & Capacitance", weightagePct = 7.5f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 12, chapterName = "Current Electricity", weightagePct = 7.8f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 12, chapterName = "Moving Charges & Magnetism", weightagePct = 6.0f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 12, chapterName = "EMI & Alternating Current", weightagePct = 5.5f, isHighYield = false),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 12, chapterName = "Ray & Wave Optics", weightagePct = 8.4f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 12, chapterName = "Modern Physics & Nuclei", weightagePct = 9.0f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.PHYSICS.name, classLevel = 12, chapterName = "Semiconductor Electronics", weightagePct = 6.2f, isHighYield = true),

        // CHEMISTRY (Physical, Inorganic, Organic)
        SyllabusChapterEntity(subjectName = NeetSubject.CHEMISTRY.name, classLevel = 11, chapterName = "Mole Concept & Stoichiometry", weightagePct = 4.5f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.CHEMISTRY.name, classLevel = 11, chapterName = "Atomic Structure & Periodicity", weightagePct = 5.0f, isHighYield = false),
        SyllabusChapterEntity(subjectName = NeetSubject.CHEMISTRY.name, classLevel = 11, chapterName = "Chemical Bonding & MOT", weightagePct = 7.5f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.CHEMISTRY.name, classLevel = 11, chapterName = "Chemical Thermodynamics", weightagePct = 5.8f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.CHEMISTRY.name, classLevel = 11, chapterName = "Ionic & Chemical Equilibrium", weightagePct = 6.4f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.CHEMISTRY.name, classLevel = 11, chapterName = "General Organic Chemistry (GOC)", weightagePct = 8.2f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.CHEMISTRY.name, classLevel = 11, chapterName = "Hydrocarbons", weightagePct = 5.5f, isHighYield = false),
        SyllabusChapterEntity(subjectName = NeetSubject.CHEMISTRY.name, classLevel = 12, chapterName = "Solutions & Electrochemistry", weightagePct = 7.2f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.CHEMISTRY.name, classLevel = 12, chapterName = "Chemical Kinetics", weightagePct = 5.2f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.CHEMISTRY.name, classLevel = 12, chapterName = "d & f Block & Coordination Comp.", weightagePct = 8.8f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.CHEMISTRY.name, classLevel = 12, chapterName = "Aldehydes, Ketones & Acids", weightagePct = 7.6f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.CHEMISTRY.name, classLevel = 12, chapterName = "Alcohols, Phenols & Amines", weightagePct = 6.5f, isHighYield = false),
        SyllabusChapterEntity(subjectName = NeetSubject.CHEMISTRY.name, classLevel = 12, chapterName = "Biomolecules & Haloalkanes", weightagePct = 5.4f, isHighYield = false),

        // BOTANY
        SyllabusChapterEntity(subjectName = NeetSubject.BOTANY.name, classLevel = 11, chapterName = "Biological Classification", weightagePct = 5.6f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.BOTANY.name, classLevel = 11, chapterName = "Plant Kingdom", weightagePct = 5.4f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.BOTANY.name, classLevel = 11, chapterName = "Morphology & Anatomy of Plants", weightagePct = 7.0f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.BOTANY.name, classLevel = 11, chapterName = "Cell: Unit of Life & Cell Cycle", weightagePct = 8.5f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.BOTANY.name, classLevel = 11, chapterName = "Photosynthesis & Respiration", weightagePct = 7.2f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.BOTANY.name, classLevel = 11, chapterName = "Plant Growth & Regulators", weightagePct = 4.6f, isHighYield = false),
        SyllabusChapterEntity(subjectName = NeetSubject.BOTANY.name, classLevel = 12, chapterName = "Sexual Reproduction in Flowering Plants", weightagePct = 6.8f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.BOTANY.name, classLevel = 12, chapterName = "Principles of Inheritance & Variation", weightagePct = 8.6f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.BOTANY.name, classLevel = 12, chapterName = "Molecular Basis of Inheritance", weightagePct = 9.4f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.BOTANY.name, classLevel = 12, chapterName = "Microbes & Ecosystem Ecology", weightagePct = 7.8f, isHighYield = true),

        // ZOOLOGY
        SyllabusChapterEntity(subjectName = NeetSubject.ZOOLOGY.name, classLevel = 11, chapterName = "Animal Kingdom", weightagePct = 6.5f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.ZOOLOGY.name, classLevel = 11, chapterName = "Structural Organisation in Animals", weightagePct = 5.0f, isHighYield = false),
        SyllabusChapterEntity(subjectName = NeetSubject.ZOOLOGY.name, classLevel = 11, chapterName = "Biomolecules (Zoology NCERT)", weightagePct = 6.2f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.ZOOLOGY.name, classLevel = 11, chapterName = "Breathing, Body Fluids & Circulation", weightagePct = 7.4f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.ZOOLOGY.name, classLevel = 11, chapterName = "Excretory, Locomotion & Neural Control", weightagePct = 8.0f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.ZOOLOGY.name, classLevel = 11, chapterName = "Chemical Coordination & Integration", weightagePct = 5.6f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.ZOOLOGY.name, classLevel = 12, chapterName = "Human Reproduction & Health", weightagePct = 8.4f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.ZOOLOGY.name, classLevel = 12, chapterName = "Evolution", weightagePct = 5.2f, isHighYield = false),
        SyllabusChapterEntity(subjectName = NeetSubject.ZOOLOGY.name, classLevel = 12, chapterName = "Human Health & Disease", weightagePct = 7.8f, isHighYield = true),
        SyllabusChapterEntity(subjectName = NeetSubject.ZOOLOGY.name, classLevel = 12, chapterName = "Biotechnology: Principles & Apps", weightagePct = 9.6f, isHighYield = true)
    )
}
