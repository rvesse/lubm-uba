package edu.lehigh.swat.bench.uba.model;

public class GenerationParameters {

    ///////////////////////////////////////////////////////////////////////////
      //restrictions for data generation
      ///////////////////////////////////////////////////////////////////////////
      /** size of the pool of the undergraduate courses for one department */
      public static final int UNDER_COURSE_NUM = 100; //must >= max faculty # * FACULTY_COURSE_MAX
    /** size of the pool of the graduate courses for one department */
      public static final int GRAD_COURSE_NUM = 100; //must >= max faculty # * FACULTY_GRADCOURSE_MAX
    /** size of the pool of universities */
      public static final int UNIV_NUM = 1000;
    /** size of the pool of reasearch areas */
      public static final int RESEARCH_NUM = 30;
    /** minimum number of departments in a university */
      public static final int DEPT_MIN = 15;
    /** maximum number of departments in a university */
      public static final int DEPT_MAX = 25;
    //must: DEPT_MAX - DEPT_MIN + 1 <> 2 ^ n
      /** minimum number of publications of a full professor */
      public static final int FULLPROF_PUB_MIN = 15;
    /** maximum number of publications of a full professor */
      public static final int FULLPROF_PUB_MAX = 20;
    /** minimum number of publications of an associate professor */
      public static final int ASSOPROF_PUB_MIN = 10;
    /** maximum number of publications of an associate professor */
      public static final int ASSOPROF_PUB_MAX = 18;
    /** minimum number of publications of an assistant professor */
      public static final int ASSTPROF_PUB_MIN = 5;
    /** maximum number of publications of an assistant professor */
      public static final int ASSTPROF_PUB_MAX = 10;
    /** minimum number of publications of a graduate student */
      public static final int GRADSTUD_PUB_MIN = 0;
    /** maximum number of publications of a graduate student */
      public static final int GRADSTUD_PUB_MAX = 5;
    /** minimum number of publications of a lecturer */
      public static final int LEC_PUB_MIN = 0;
    /** maximum number of publications of a lecturer */
      public static final int LEC_PUB_MAX = 5;
    /** minimum number of courses taught by a faculty */
      public static final int FACULTY_COURSE_MIN = 1;
    /** maximum number of courses taught by a faculty */
      public static final int FACULTY_COURSE_MAX = 2;
    /** minimum number of graduate courses taught by a faculty */
      public static final int FACULTY_GRADCOURSE_MIN = 1;
    /** maximum number of graduate courses taught by a faculty */
      public static final int FACULTY_GRADCOURSE_MAX = 2;
    /** minimum number of courses taken by a undergraduate student */
      public static final int UNDERSTUD_COURSE_MIN = 2;
    /** maximum number of courses taken by a undergraduate student */
      public static final int UNDERSTUD_COURSE_MAX = 4;
    /** minimum number of courses taken by a graduate student */
      public static final int GRADSTUD_COURSE_MIN = 1;
    /** maximum number of courses taken by a graduate student */
      public static final int GRADSTUD_COURSE_MAX = 3;
    /** minimum number of research groups in a department */
      public static final int RESEARCHGROUP_MIN = 10;
    /** maximum number of research groups in a department */
      public static final int RESEARCHGROUP_MAX = 20;
    //faculty number: 30-42
      /** minimum number of full professors in a department*/
      public static final int FULLPROF_MIN = 7;
    /** maximum number of full professors in a department*/
      public static final int FULLPROF_MAX = 10;
    /** minimum number of associate professors in a department*/
      public static final int ASSOPROF_MIN = 10;
    /** maximum number of associate professors in a department*/
      public static final int ASSOPROF_MAX = 14;
    /** minimum number of assistant professors in a department*/
      public static final int ASSTPROF_MIN = 8;
    /** maximum number of assistant professors in a department*/
      public static final int ASSTPROF_MAX = 11;
    /** minimum number of lecturers in a department*/
      public static final int LEC_MIN = 5;
    /** maximum number of lecturers in a department*/
      public static final int LEC_MAX = 7;
    /** minimum ratio of undergraduate students to faculties in a department*/
      public static final int R_UNDERSTUD_FACULTY_MIN = 8;
    /** maximum ratio of undergraduate students to faculties in a department*/
      public static final int R_UNDERSTUD_FACULTY_MAX = 14;
    /** minimum ratio of graduate students to faculties in a department*/
      public static final int R_GRADSTUD_FACULTY_MIN = 3;
    /** maximum ratio of graduate students to faculties in a department*/
      public static final int R_GRADSTUD_FACULTY_MAX = 4;
    //MUST: FACULTY_COURSE_MIN >= R_GRADSTUD_FACULTY_MAX / R_GRADSTUD_TA_MIN;
      /** minimum ratio of graduate students to TA in a department */
      public static final int R_GRADSTUD_TA_MIN = 4;
    /** maximum ratio of graduate students to TA in a department */
      public static final int R_GRADSTUD_TA_MAX = 5;
    /** minimum ratio of graduate students to RA in a department */
      public static final int R_GRADSTUD_RA_MIN = 3;
    /** maximum ratio of graduate students to RA in a department */
      public static final int R_GRADSTUD_RA_MAX = 4;
    /** average ratio of undergraduate students to undergraduate student advising professors */
      public static final int R_UNDERSTUD_ADVISOR = 5;
    /** average ratio of graduate students to graduate student advising professors */
      public static final int R_GRADSTUD_ADVISOR = 1;

}
