package edu.lehigh.swat.bench.uba.model;

public class Ontology {

    ///////////////////////////////////////////////////////////////////////////
    // ontology class information
    // NOTE: prefix "CS" was used because the predecessor of univ-bench ontology
    // is called cs ontolgy.
    ///////////////////////////////////////////////////////////////////////////
    /** n/a */
    public static final int CS_C_NULL = -1;
    /** University */
    public static final int CS_C_UNIV = 0;
    /** Department */
    public static final int CS_C_DEPT = CS_C_UNIV + 1;
    /** Faculty */
    public static final int CS_C_FACULTY = CS_C_DEPT + 1;
    /** Professor */
    public static final int CS_C_PROF = CS_C_FACULTY + 1;
    /** FullProfessor */
    public static final int CS_C_FULLPROF = CS_C_PROF + 1;
    /** AssociateProfessor */
    public static final int CS_C_ASSOPROF = CS_C_FULLPROF + 1;
    /** AssistantProfessor */
    public static final int CS_C_ASSTPROF = CS_C_ASSOPROF + 1;
    /** Lecturer */
    public static final int CS_C_LECTURER = CS_C_ASSTPROF + 1;
    /** Student */
    public static final int CS_C_STUDENT = CS_C_LECTURER + 1;
    /** UndergraduateStudent */
    public static final int CS_C_UNDERSTUD = CS_C_STUDENT + 1;
    /** GraduateStudent */
    public static final int CS_C_GRADSTUD = CS_C_UNDERSTUD + 1;
    /** TeachingAssistant */
    public static final int CS_C_TA = CS_C_GRADSTUD + 1;
    /** ResearchAssistant */
    public static final int CS_C_RA = CS_C_TA + 1;
    /** Course */
    public static final int CS_C_COURSE = CS_C_RA + 1;
    /** GraduateCourse */
    public static final int CS_C_GRADCOURSE = CS_C_COURSE + 1;
    /** Publication */
    public static final int CS_C_PUBLICATION = CS_C_GRADCOURSE + 1;
    /** Chair */
    public static final int CS_C_CHAIR = CS_C_PUBLICATION + 1;
    /** Research */
    public static final int CS_C_RESEARCH = CS_C_CHAIR + 1;
    /** ResearchGroup */
    public static final int CS_C_RESEARCHGROUP = CS_C_RESEARCH + 1;
    /** class information */
    public static final int[][] CLASS_INFO = {
            /* {instance number if not specified, direct super class} */
            // NOTE: the super classes specifed here do not necessarily reflect
            // the entailment of the ontology
            { 2, CS_C_NULL }, // CS_C_UNIV
            { 1, CS_C_NULL }, // CS_C_DEPT
            { 0, CS_C_NULL }, // CS_C_FACULTY
            { 0, CS_C_FACULTY }, // CS_C_PROF
            { 0, CS_C_PROF }, // CS_C_FULLPROF
            { 0, CS_C_PROF }, // CS_C_ASSOPROF
            { 0, CS_C_PROF }, // CS_C_ASSTPROF
            { 0, CS_C_FACULTY }, // CS_C_LECTURER
            { 0, CS_C_NULL }, // CS_C_STUDENT
            { 0, CS_C_STUDENT }, // CS_C_UNDERSTUD
            { 0, CS_C_STUDENT }, // CS_C_GRADSTUD
            { 0, CS_C_NULL }, // CS_C_TA
            { 0, CS_C_NULL }, // CS_C_RA
            { 0, CS_C_NULL }, // CS_C_COURSE, treated as undergrad course here
            { 0, CS_C_NULL }, // CS_C_GRADCOURSE
            { 0, CS_C_NULL }, // CS_C_PUBLICATION
            { 0, CS_C_NULL }, // CS_C_CHAIR
            { 0, CS_C_NULL }, // CS_C_RESEARCH
            { 0, CS_C_NULL } // CS_C_RESEARCHGROUP
    };
    /** class name strings */
    public static final String[] CLASS_TOKEN = { "University", // CS_C_UNIV
            "Department", // CS_C_DEPT
            "Faculty", // CS_C_FACULTY
            "Professor", // CS_C_PROF
            "FullProfessor", // CS_C_FULLPROF
            "AssociateProfessor", // CS_C_ASSOPROF
            "AssistantProfessor", // CS_C_ASSTPROF
            "Lecturer", // CS_C_LECTURER
            "Student", // CS_C_STUDENT
            "UndergraduateStudent", // CS_C_UNDERSTUD
            "GraduateStudent", // CS_C_GRADSTUD
            "TeachingAssistant", // CS_C_TA
            "ResearchAssistant", // CS_C_RA
            "Course", // CS_C_COURSE
            "GraduateCourse", // CS_C_GRADCOURSE
            "Publication", // CS_C_PUBLICATION
            "Chair", // CS_C_CHAIR
            "Research", // CS_C_RESEARCH
            "ResearchGroup" // CS_C_RESEARCHGROUP
    };
    /** number of classes */
    public static final int CLASS_NUM = CLASS_INFO.length;
    /** index of instance-number in the elements of array CLASS_INFO */
    public static final int INDEX_NUM = 0;
    /** index of super-class in the elements of array CLASS_INFO */
    public static final int INDEX_SUPER = 1;
    ///////////////////////////////////////////////////////////////////////////
    // ontology property information
    ///////////////////////////////////////////////////////////////////////////
    /** name */
    public static final int CS_P_NAME = 0;
    /** takesCourse */
    public static final int CS_P_TAKECOURSE = CS_P_NAME + 1;
    /** teacherOf */
    public static final int CS_P_TEACHEROF = CS_P_TAKECOURSE + 1;
    /** undergraduateDegreeFrom */
    public static final int CS_P_UNDERGRADFROM = CS_P_TEACHEROF + 1;
    /** mastersDegreeFrom */
    public static final int CS_P_GRADFROM = CS_P_UNDERGRADFROM + 1;
    /** doctoralDegreeFrom */
    public static final int CS_P_DOCFROM = CS_P_GRADFROM + 1;
    /** advisor */
    public static final int CS_P_ADVISOR = CS_P_DOCFROM + 1;
    /** memberOf */
    public static final int CS_P_MEMBEROF = CS_P_ADVISOR + 1;
    /** publicationAuthor */
    public static final int CS_P_PUBLICATIONAUTHOR = CS_P_MEMBEROF + 1;
    /** headOf */
    public static final int CS_P_HEADOF = CS_P_PUBLICATIONAUTHOR + 1;
    /** teachingAssistantOf */
    public static final int CS_P_TAOF = CS_P_HEADOF + 1;
    /** reseachAssistantOf */
    public static final int CS_P_RESEARCHINTEREST = CS_P_TAOF + 1;
    /** emailAddress */
    public static final int CS_P_EMAIL = CS_P_RESEARCHINTEREST + 1;
    /** telephone */
    public static final int CS_P_TELEPHONE = CS_P_EMAIL + 1;
    /** subOrganizationOf */
    public static final int CS_P_SUBORGANIZATIONOF = CS_P_TELEPHONE + 1;
    /** worksFor */
    public static final int CS_P_WORKSFOR = CS_P_SUBORGANIZATIONOF + 1;
    /** property name strings */
    public static final String[] PROP_TOKEN = { "name", "takesCourse", "teacherOf", "undergraduateDegreeFrom",
            "mastersDegreeFrom", "doctoralDegreeFrom", "advisor", "memberOf", "publicationAuthor", "headOf",
            "teachingAssistantOf", "researchInterest", "emailAddress", "telephone", "subOrganizationOf", "worksFor" };
    /** number of properties */
    public static final int PROP_NUM = PROP_TOKEN.length;

}
