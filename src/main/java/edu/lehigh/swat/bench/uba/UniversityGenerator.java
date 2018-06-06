package edu.lehigh.swat.bench.uba;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lehigh.swat.bench.uba.model.CourseInfo;
import edu.lehigh.swat.bench.uba.model.GenerationParameters;
import edu.lehigh.swat.bench.uba.model.Ontology;
import edu.lehigh.swat.bench.uba.model.PublicationInfo;
import edu.lehigh.swat.bench.uba.model.RaInfo;
import edu.lehigh.swat.bench.uba.model.TaInfo;
import edu.lehigh.swat.bench.uba.writers.ConsolidationMode;

class UniversityGenerator implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(UniversityGenerator.class);

    private static AtomicBoolean abortMessageShown = new AtomicBoolean(false);

    private final UniversityState univState;

    public UniversityGenerator(UniversityState univState) {
        this.univState = univState;
    }

    public void run() {
        if (!this.univState.getGlobalState().shouldContinue()) {
            // Only show the abort message once
            if (!abortMessageShown.getAndSet(true)) {
                LOGGER.error("Some data generators have failed, skipping further data generation");
            }
            return;
        }

        try {
            this.univState.prepare();
            _generateUniv(this.univState);
            this.univState.setComplete();
        } catch (Throwable e) {
            this.univState.setError(e);
            LOGGER.error("Error in generating University {} - {}", this.univState.getUniversityIndex(), e);
            StringWriter strWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(strWriter);
            e.printStackTrace(writer);
            LOGGER.error(strWriter.toString());
        }
    }

    /**
     * Creates a university.
     * 
     * @param index
     *            Index of the university.
     */
    private void _generateUniv(UniversityState univState) {
        // determine department number
        univState.getInstances()[Ontology.CS_C_DEPT].num = univState.getRandomFromRange(GenerationParameters.DEPT_MIN,
                GenerationParameters.DEPT_MAX);
        univState.getInstances()[Ontology.CS_C_DEPT].count = 0;
        // generate departments
        for (int i = 0; i < univState.getInstances()[Ontology.CS_C_DEPT].num; i++) {
            _generateDept(univState, i);
        }
    }

    /**
     * Creates a department.
     * 
     * @param univIndex
     *            Index of the current university.
     * @param index
     *            Index of the department. NOTE: Use univIndex instead of
     *            instances[CS_C_UNIV].count till generateASection(CS_C_UNIV, )
     *            is invoked.
     */
    private void _generateDept(UniversityState univState, int index) {
        // Start a new file if we're not consolidating or this is the first
        // department for the university
        String filename = univState.getFilename(index);
        if (index == 0 || univState.getGlobalState().consolidationMode() == ConsolidationMode.None) {
            univState.getWriter().startFile(filename, univState.getGlobalState());
        }

        // reset
        univState.reset();

        if (index == 0) {
            _generateASection(univState, Ontology.CS_C_UNIV, univState.getUniversityIndex());
        }
        _generateASection(univState, Ontology.CS_C_DEPT, index);
        for (int i = Ontology.CS_C_DEPT + 1; i < Ontology.CLASS_NUM; i++) {
            univState.getInstances()[i].count = 0;
            for (int j = 0; j < univState.getInstances()[i].num; j++) {
                _generateASection(univState, i, j);
            }
        }

        _generatePublications(univState);
        _generateCourses(univState);
        _generateRaTa(univState);

        if (univState.getGlobalState().consolidationMode() != ConsolidationMode.None) {
            // Consolidating output so file is not yet complete
            if (!univState.getGlobalState().isQuietMode())
                System.out.println(filename + " in progress...");
        }
        String bar = "";
        for (int i = 0; i < filename.length(); i++)
            bar += '-';
        Generator.LOGGER.info(bar);
        Generator.LOGGER.info(filename);
        Generator.LOGGER.info(bar);
        _generateComments(univState);

        // End the file if we aren't consolidating or this is the last file for
        // the university
        if (univState.getGlobalState().consolidationMode() == ConsolidationMode.None
                || index == univState.getInstances()[Ontology.CS_C_DEPT].num - 1) {
            if (univState.getGlobalState().consolidationMode() != ConsolidationMode.Full) {
                // None or Partial consolidation, we are done with this file
                univState.getWriter().endFile(univState.getGlobalState());
                System.out.println(filename + " generated");
            } else {
                // Full Consolidation so output file is not yet complete
                // However we should flush what we have generated to reduce our memory usage
                univState.getWriter().flushFile(univState.getGlobalState());
                System.out.println(filename + " (University " + univState.getUniversityIndex() + ") in progress...");
            }

        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // instance generation

    /**
     * Generates an instance of the specified class
     * 
     * @param classType
     *            Type of the instance.
     * @param index
     *            Index of the instance.
     */
    private void _generateASection(UniversityState state, int classType, int index) {
        _updateCount(state, classType);

        switch (classType) {
        case Ontology.CS_C_UNIV:
            _generateAUniv(state, index);
            break;
        case Ontology.CS_C_DEPT:
            _generateADept(state, index);
            break;
        case Ontology.CS_C_FACULTY:
            _generateAFaculty(state, index);
            break;
        case Ontology.CS_C_PROF:
            _generateAProf(state, index);
            break;
        case Ontology.CS_C_FULLPROF:
            _generateAFullProf(state, index);
            break;
        case Ontology.CS_C_ASSOPROF:
            _generateAnAssociateProfessor(state, index);
            break;
        case Ontology.CS_C_ASSTPROF:
            _generateAnAssistantProfessor(state, index);
            break;
        case Ontology.CS_C_LECTURER:
            _generateALecturer(state, index);
            break;
        case Ontology.CS_C_UNDERSTUD:
            _generateAnUndergraduateStudent(state, index);
            break;
        case Ontology.CS_C_GRADSTUD:
            _generateAGradudateStudent(state, index);
            break;
        case Ontology.CS_C_COURSE:
            _generateACourse(state, index);
            break;
        case Ontology.CS_C_GRADCOURSE:
            _generateAGraduateCourse(state, index);
            break;
        case Ontology.CS_C_RESEARCHGROUP:
            _generateAResearchGroup(state, index);
            break;
        default:
            break;
        }
    }

    /**
     * Generates a university instance.
     * 
     * @param index
     *            Index of the instance.
     */
    private void _generateAUniv(UniversityState univState, int index) {
        univState.getWriter().startSection(Ontology.CS_C_UNIV, univState.getId(Ontology.CS_C_UNIV, index));
        univState.getWriter().addProperty(Ontology.CS_P_NAME, univState.getRelativeName(Ontology.CS_C_UNIV, index),
                false);
        univState.getWriter().endSection(Ontology.CS_C_UNIV);
    }

    /**
     * Generates a department instance.
     * 
     * @param index
     *            Index of the department.
     */
    private void _generateADept(UniversityState univState, int index) {
        univState.getWriter().startSection(Ontology.CS_C_DEPT, univState.getId(Ontology.CS_C_DEPT, index));
        univState.getWriter().addProperty(Ontology.CS_P_NAME, univState.getRelativeName(Ontology.CS_C_DEPT, index),
                false);
        univState.getWriter().addProperty(Ontology.CS_P_SUBORGANIZATIONOF, Ontology.CS_C_UNIV,
                univState.getId(Ontology.CS_C_UNIV, univState.getUniversityIndex()));
        univState.getWriter().endSection(Ontology.CS_C_DEPT);
    }

    /**
     * Generates a faculty instance.
     * 
     * @param index
     *            Index of the faculty.
     */
    private void _generateAFaculty(UniversityState univState, int index) {
        univState.getWriter().startSection(Ontology.CS_C_FACULTY, univState.getId(Ontology.CS_C_FACULTY, index));
        _generateAFaculty_a(univState, Ontology.CS_C_FACULTY, index);
        univState.getWriter().endSection(Ontology.CS_C_FACULTY);
    }

    /**
     * Generates properties for the specified faculty instance.
     * 
     * @param type
     *            Type of the faculty.
     * @param index
     *            Index of the instance within its type.
     */
    private void _generateAFaculty_a(UniversityState univState, int type, int index) {
        int indexInFaculty;
        int courseNum;
        int courseIndex;

        indexInFaculty = univState.getInstances()[Ontology.CS_C_FACULTY].count - 1;

        univState.getWriter().addProperty(Ontology.CS_P_NAME, univState.getRelativeName(type, index), false);

        // undergradutate courses
        courseNum = univState.getRandomFromRange(GenerationParameters.FACULTY_COURSE_MIN,
                GenerationParameters.FACULTY_COURSE_MAX);
        for (int i = 0; i < courseNum; i++) {
            courseIndex = _AssignCourse(univState, indexInFaculty);
            univState.getWriter().addProperty(Ontology.CS_P_TEACHEROF,
                    univState.getId(Ontology.CS_C_COURSE, courseIndex), true);
        }
        // gradutate courses
        courseNum = univState.getRandomFromRange(GenerationParameters.FACULTY_GRADCOURSE_MIN,
                GenerationParameters.FACULTY_GRADCOURSE_MAX);
        for (int i = 0; i < courseNum; i++) {
            courseIndex = _AssignGraduateCourse(univState, indexInFaculty);
            univState.getWriter().addProperty(Ontology.CS_P_TEACHEROF,
                    univState.getId(Ontology.CS_C_GRADCOURSE, courseIndex), true);
        }
        // person properties
        univState.getWriter().addProperty(Ontology.CS_P_UNDERGRADFROM, Ontology.CS_C_UNIV,
                univState.getId(Ontology.CS_C_UNIV, univState.getRandom(GenerationParameters.UNIV_NUM)));
        univState.getWriter().addProperty(Ontology.CS_P_GRADFROM, Ontology.CS_C_UNIV,
                univState.getId(Ontology.CS_C_UNIV, univState.getRandom(GenerationParameters.UNIV_NUM)));
        univState.getWriter().addProperty(Ontology.CS_P_DOCFROM, Ontology.CS_C_UNIV,
                univState.getId(Ontology.CS_C_UNIV, univState.getRandom(GenerationParameters.UNIV_NUM)));
        univState.getWriter().addProperty(Ontology.CS_P_WORKSFOR,
                univState.getId(Ontology.CS_C_DEPT, univState.getInstances()[Ontology.CS_C_DEPT].count - 1), true);
        univState.getWriter().addProperty(Ontology.CS_P_EMAIL, univState.getEmail(type, index), false);
        univState.getWriter().addProperty(Ontology.CS_P_TELEPHONE, "xxx-xxx-xxxx", false);
    }

    /**
     * Assigns an undergraduate course to the specified faculty.
     * 
     * @param indexInFaculty
     *            Index of the faculty.
     * @return Index of the selected course in the pool.
     */
    private int _AssignCourse(UniversityState univState, int indexInFaculty) {
        // NOTE: this line, although overriden by the next one, is
        // deliberately
        // kept
        // to guarantee identical random number generation to the previous
        // version.
        int pos = univState.getRandomFromRange(0, univState.getRemainingUndergradCourses().size() - 1);
        pos = 0; // fetch courses in sequence

        CourseInfo course = new CourseInfo();
        course.indexInFaculty = indexInFaculty;
        course.globalIndex = univState.getRemainingUndergradCourses().get(pos).intValue();
        univState.getUndergradCourses().add(course);

        univState.getRemainingUndergradCourses().remove(pos);

        return course.globalIndex;
    }

    /**
     * Assigns a graduate course to the specified faculty.
     * 
     * @param indexInFaculty
     *            Index of the faculty.
     * @return Index of the selected course in the pool.
     */
    private int _AssignGraduateCourse(UniversityState univState, int indexInFaculty) {
        // NOTE: this line, although overriden by the next one, is
        // deliberately
        // kept
        // to guarantee identical random number generation to the previous
        // version.
        int pos = univState.getRandomFromRange(0, univState.getRemainingGradCourses().size() - 1);
        pos = 0; // fetch courses in sequence

        CourseInfo course = new CourseInfo();
        course.indexInFaculty = indexInFaculty;
        course.globalIndex = ((Integer) univState.getRemainingGradCourses().get(pos)).intValue();
        univState.getGradCourses().add(course);

        univState.getRemainingGradCourses().remove(pos);

        return course.globalIndex;
    }

    /**
     * Generates a professor instance.
     * 
     * @param index
     *            Index of the professor.
     */
    private void _generateAProf(UniversityState univState, int index) {
        univState.getWriter().startSection(Ontology.CS_C_PROF, univState.getId(Ontology.CS_C_PROF, index));
        _generateAProf_a(univState, Ontology.CS_C_PROF, index);
        univState.getWriter().endSection(Ontology.CS_C_PROF);
    }

    /**
     * Generates properties for a professor instance.
     * 
     * @param type
     *            Type of the professor.
     * @param index
     *            Index of the intance within its type.
     */
    private void _generateAProf_a(UniversityState univState, int type, int index) {
        _generateAFaculty_a(univState, type, index);
        univState.getWriter().addProperty(Ontology.CS_P_RESEARCHINTEREST, univState.getRelativeName(
                Ontology.CS_C_RESEARCH, univState.getRandom(GenerationParameters.RESEARCH_NUM)), false);
    }

    /**
     * Generates a full professor instances.
     * 
     * @param index
     *            Index of the full professor.
     */
    private void _generateAFullProf(UniversityState univState, int index) {
        String id;

        id = univState.getId(Ontology.CS_C_FULLPROF, index);
        univState.getWriter().startSection(Ontology.CS_C_FULLPROF, id);
        _generateAProf_a(univState, Ontology.CS_C_FULLPROF, index);
        if (index == univState.getChair()) {
            univState.getWriter().addProperty(Ontology.CS_P_HEADOF,
                    univState.getId(Ontology.CS_C_DEPT, univState.getInstances()[Ontology.CS_C_DEPT].count - 1), true);
        }
        univState.getWriter().endSection(Ontology.CS_C_FULLPROF);
        _assignFacultyPublications(univState, id, GenerationParameters.FULLPROF_PUB_MIN,
                GenerationParameters.FULLPROF_PUB_MAX);
    }

    /**
     * Generates an associate professor instance.
     * 
     * @param index
     *            Index of the associate professor.
     */
    private void _generateAnAssociateProfessor(UniversityState univState, int index) {
        String id = univState.getId(Ontology.CS_C_ASSOPROF, index);
        univState.getWriter().startSection(Ontology.CS_C_ASSOPROF, id);
        _generateAProf_a(univState, Ontology.CS_C_ASSOPROF, index);
        univState.getWriter().endSection(Ontology.CS_C_ASSOPROF);
        _assignFacultyPublications(univState, id, GenerationParameters.ASSOPROF_PUB_MIN,
                GenerationParameters.ASSOPROF_PUB_MAX);
    }

    /**
     * Generates an assistant professor instance.
     * 
     * @param index
     *            Index of the assistant professor.
     */
    private void _generateAnAssistantProfessor(UniversityState univState, int index) {
        String id = univState.getId(Ontology.CS_C_ASSTPROF, index);
        univState.getWriter().startSection(Ontology.CS_C_ASSTPROF, id);
        _generateAProf_a(univState, Ontology.CS_C_ASSTPROF, index);
        univState.getWriter().endSection(Ontology.CS_C_ASSTPROF);
        _assignFacultyPublications(univState, id, GenerationParameters.ASSTPROF_PUB_MIN,
                GenerationParameters.ASSTPROF_PUB_MAX);
    }

    /**
     * Generates a lecturer instance.
     * 
     * @param index
     *            Index of the lecturer.
     */
    private void _generateALecturer(UniversityState univState, int index) {
        String id = univState.getId(Ontology.CS_C_LECTURER, index);
        univState.getWriter().startSection(Ontology.CS_C_LECTURER, id);
        _generateAFaculty_a(univState, Ontology.CS_C_LECTURER, index);
        univState.getWriter().endSection(Ontology.CS_C_LECTURER);
        _assignFacultyPublications(univState, id, GenerationParameters.LEC_PUB_MIN, GenerationParameters.LEC_PUB_MAX);
    }

    /**
     * Assigns publications to the specified faculty.
     * 
     * @param author
     *            Id of the faculty
     * @param min
     *            Minimum number of publications
     * @param max
     *            Maximum number of publications
     */
    private void _assignFacultyPublications(UniversityState univState, String author, int min, int max) {
        int num;
        PublicationInfo publication;

        num = univState.getRandomFromRange(min, max);
        for (int i = 0; i < num; i++) {
            publication = new PublicationInfo();
            publication.id = univState.getId(Ontology.CS_C_PUBLICATION, i, author);
            publication.name = univState.getRelativeName(Ontology.CS_C_PUBLICATION, i);
            publication.authors = new ArrayList<String>();
            publication.authors.add(author);
            univState.getPublications().add(publication);
        }
    }

    /**
     * Assigns publications to the specified graduate student. The publications
     * are chosen from some faculties'.
     * 
     * @param author
     *            Id of the graduate student.
     * @param min
     *            Minimum number of publications.
     * @param max
     *            Maximum number of publications.
     */
    private void _assignGraduateStudentPublications(UniversityState univState, String author, int min, int max) {
        int num;
        PublicationInfo publication;

        num = univState.getRandomFromRange(min, max);
        ArrayList<Integer> list = univState.getRandomList(num, 0, univState.getPublications().size() - 1);
        for (int i = 0; i < list.size(); i++) {
            publication = (PublicationInfo) univState.getPublications().get(list.get(i).intValue());
            publication.authors.add(author);
        }
    }

    /**
     * Generates publication instances. These publications are assigned to some
     * faculties and graduate students before.
     */
    private void _generatePublications(UniversityState univState) {
        for (int i = 0; i < univState.getPublications().size(); i++) {
            _generateAPublication(univState, (PublicationInfo) univState.getPublications().get(i));
        }
    }

    /**
     * Generates a publication instance.
     * 
     * @param publication
     *            Information of the publication.
     */
    private void _generateAPublication(UniversityState univState, PublicationInfo publication) {
        univState.getWriter().startSection(Ontology.CS_C_PUBLICATION, publication.id);
        univState.getWriter().addProperty(Ontology.CS_P_NAME, publication.name, false);
        for (int i = 0; i < publication.authors.size(); i++) {
            univState.getWriter().addProperty(Ontology.CS_P_PUBLICATIONAUTHOR, (String) publication.authors.get(i),
                    true);
        }
        univState.getWriter().endSection(Ontology.CS_C_PUBLICATION);
    }

    /**
     * Generates properties for the specified student instance.
     * 
     * @param type
     *            Type of the student.
     * @param index
     *            Index of the instance within its type.
     */
    private void _generateAStudent_a(UniversityState univState, int type, int index) {
        univState.getWriter().addProperty(Ontology.CS_P_NAME, univState.getRelativeName(type, index), false);
        univState.getWriter().addProperty(Ontology.CS_P_MEMBEROF,
                univState.getId(Ontology.CS_C_DEPT, univState.getInstances()[Ontology.CS_C_DEPT].count - 1), true);
        univState.getWriter().addProperty(Ontology.CS_P_EMAIL, univState.getEmail(type, index), false);
        univState.getWriter().addProperty(Ontology.CS_P_TELEPHONE, "xxx-xxx-xxxx", false);
    }

    /**
     * Generates an undergraduate student instance.
     * 
     * @param index
     *            Index of the undergraduate student.
     */
    private void _generateAnUndergraduateStudent(UniversityState univState, int index) {
        int n;
        ArrayList<Integer> list;

        univState.getWriter().startSection(Ontology.CS_C_UNDERSTUD, univState.getId(Ontology.CS_C_UNDERSTUD, index));
        _generateAStudent_a(univState, Ontology.CS_C_UNDERSTUD, index);
        n = univState.getRandomFromRange(GenerationParameters.UNDERSTUD_COURSE_MIN,
                GenerationParameters.UNDERSTUD_COURSE_MAX);
        list = univState.getRandomList(n, 0, univState.getUndergradCourses().size() - 1);
        for (int i = 0; i < list.size(); i++) {
            CourseInfo info = (CourseInfo) univState.getUndergradCourses().get(list.get(i).intValue());
            univState.getWriter().addProperty(Ontology.CS_P_TAKECOURSE,
                    univState.getId(Ontology.CS_C_COURSE, info.globalIndex), true);
        }
        if (0 == univState.getRandom(GenerationParameters.R_UNDERSTUD_ADVISOR)) {
            univState.getWriter().addProperty(Ontology.CS_P_ADVISOR, _selectAdvisor(univState), true);
        }
        univState.getWriter().endSection(Ontology.CS_C_UNDERSTUD);
    }

    /**
     * Generates a graduate student instance.
     * 
     * @param index
     *            Index of the graduate student.
     */
    private void _generateAGradudateStudent(UniversityState univState, int index) {
        int n;
        ArrayList<Integer> list;
        String id;

        id = univState.getId(Ontology.CS_C_GRADSTUD, index);
        univState.getWriter().startSection(Ontology.CS_C_GRADSTUD, id);
        _generateAStudent_a(univState, Ontology.CS_C_GRADSTUD, index);
        n = univState.getRandomFromRange(GenerationParameters.GRADSTUD_COURSE_MIN,
                GenerationParameters.GRADSTUD_COURSE_MAX);
        list = univState.getRandomList(n, 0, univState.getGradCourses().size() - 1);
        for (int i = 0; i < list.size(); i++) {
            CourseInfo info = (CourseInfo) univState.getGradCourses().get(list.get(i).intValue());
            univState.getWriter().addProperty(Ontology.CS_P_TAKECOURSE,
                    univState.getId(Ontology.CS_C_GRADCOURSE, info.globalIndex), true);
        }
        univState.getWriter().addProperty(Ontology.CS_P_UNDERGRADFROM, Ontology.CS_C_UNIV,
                univState.getId(Ontology.CS_C_UNIV, univState.getRandom(GenerationParameters.UNIV_NUM)));
        if (0 == univState.getRandom(GenerationParameters.R_GRADSTUD_ADVISOR)) {
            univState.getWriter().addProperty(Ontology.CS_P_ADVISOR, _selectAdvisor(univState), true);
        }
        _assignGraduateStudentPublications(univState, id, GenerationParameters.GRADSTUD_PUB_MIN,
                GenerationParameters.GRADSTUD_PUB_MAX);
        univState.getWriter().endSection(Ontology.CS_C_GRADSTUD);
    }

    /**
     * Select an advisor from the professors.
     * 
     * @return Id of the selected professor.
     */
    private String _selectAdvisor(UniversityState univState) {
        int profType;
        int index;

        profType = univState.getRandomFromRange(Ontology.CS_C_FULLPROF, Ontology.CS_C_ASSTPROF);
        index = univState.getRandom(univState.getInstances()[profType].total);
        return univState.getId(profType, index);
    }

    /**
     * Generates a TA instance according to the specified information.
     * 
     * @param ta
     *            Information of the TA.
     */
    private void _generateATa(UniversityState univState, TaInfo ta) {
        univState.getWriter().startAboutSection(Ontology.CS_C_TA,
                univState.getId(Ontology.CS_C_GRADSTUD, ta.indexInGradStud));
        univState.getWriter().addProperty(Ontology.CS_P_TAOF, univState.getId(Ontology.CS_C_COURSE, ta.indexInCourse),
                true);
        univState.getWriter().endSection(Ontology.CS_C_TA);
    }

    /**
     * Generates an RA instance according to the specified information.
     * 
     * @param ra
     *            Information of the RA.
     */
    private void _generateAnRa(UniversityState univState, RaInfo ra) {
        univState.getWriter().startAboutSection(Ontology.CS_C_RA,
                univState.getId(Ontology.CS_C_GRADSTUD, ra.indexInGradStud));
        univState.getWriter().endSection(Ontology.CS_C_RA);
    }

    /**
     * Generates a course instance.
     * 
     * @param index
     *            Index of the course.
     */
    private void _generateACourse(UniversityState univState, int index) {
        univState.getWriter().startSection(Ontology.CS_C_COURSE, univState.getId(Ontology.CS_C_COURSE, index));
        univState.getWriter().addProperty(Ontology.CS_P_NAME, univState.getRelativeName(Ontology.CS_C_COURSE, index),
                false);
        univState.getWriter().endSection(Ontology.CS_C_COURSE);
    }

    /**
     * Generates a graduate course instance.
     * 
     * @param index
     *            Index of the graduate course.
     */
    private void _generateAGraduateCourse(UniversityState univState, int index) {
        univState.getWriter().startSection(Ontology.CS_C_GRADCOURSE, univState.getId(Ontology.CS_C_GRADCOURSE, index));
        univState.getWriter().addProperty(Ontology.CS_P_NAME,
                univState.getRelativeName(Ontology.CS_C_GRADCOURSE, index), false);
        univState.getWriter().endSection(Ontology.CS_C_GRADCOURSE);
    }

    /**
     * Generates course/graduate course instances. These course are assigned to
     * some faculties before.
     */
    private void _generateCourses(UniversityState univState) {
        for (int i = 0; i < univState.getUndergradCourses().size(); i++) {
            _generateACourse(univState, ((CourseInfo) univState.getUndergradCourses().get(i)).globalIndex);
        }
        for (int i = 0; i < univState.getGradCourses().size(); i++) {
            _generateAGraduateCourse(univState, ((CourseInfo) univState.getGradCourses().get(i)).globalIndex);
        }
    }

    /**
     * Chooses RAs and TAs from graduate student and generates their instances
     * accordingly.
     */
    private void _generateRaTa(UniversityState univState) {
        ArrayList<Integer> list, courseList;
        TaInfo ta;
        RaInfo ra;
        int i;

        list = univState.getRandomList(
                univState.getInstances()[Ontology.CS_C_TA].total + univState.getInstances()[Ontology.CS_C_RA].total, 0,
                univState.getInstances()[Ontology.CS_C_GRADSTUD].total - 1);
        courseList = univState.getRandomList(univState.getInstances()[Ontology.CS_C_TA].total, 0,
                univState.getUndergradCourses().size() - 1);

        for (i = 0; i < univState.getInstances()[Ontology.CS_C_TA].total; i++) {
            ta = new TaInfo();
            ta.indexInGradStud = list.get(i).intValue();
            ta.indexInCourse = ((CourseInfo) univState.getUndergradCourses()
                    .get(courseList.get(i).intValue())).globalIndex;
            _generateATa(univState, ta);
        }
        while (i < list.size()) {
            ra = new RaInfo();
            ra.indexInGradStud = list.get(i).intValue();
            _generateAnRa(univState, ra);
            i++;
        }
    }

    /**
     * Generates a research group instance.
     * 
     * @param index
     *            Index of the research group.
     */
    private void _generateAResearchGroup(UniversityState univState, int index) {
        String id;
        id = univState.getId(Ontology.CS_C_RESEARCHGROUP, index);
        univState.getWriter().startSection(Ontology.CS_C_RESEARCHGROUP, id);
        univState.getWriter().addProperty(Ontology.CS_P_SUBORGANIZATIONOF,
                univState.getId(Ontology.CS_C_DEPT, univState.getInstances()[Ontology.CS_C_DEPT].count - 1), true);
        univState.getWriter().endSection(Ontology.CS_C_RESEARCHGROUP);
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Increases by 1 the instance count of the specified class. This also
     * includes the increase of the instance count of all its super class.
     * 
     * @param classType
     *            Type of the instance.
     */
    private void _updateCount(UniversityState univState, int classType) {
        int subClass, superClass;

        univState.getInstances()[classType].count++;
        subClass = classType;
        while ((superClass = Ontology.CLASS_INFO[subClass][Ontology.INDEX_SUPER]) != Ontology.CS_C_NULL) {
            univState.getInstances()[superClass].count++;
            subClass = superClass;
        }
    }

    /**
     * Outputs log information to both the log file and the screen after a
     * department is generated.
     */
    private void _generateComments(UniversityState univState) {
        int classInstNum = 0; // total class instance num in this department
        long totalClassInstNum = 0l; // total class instance num so far
        int propInstNum = 0; // total property instance num in this
                             // department
        long totalPropInstNum = 0l; // total property instance num so far

        Generator.LOGGER.debug("External Seed={} Interal Seed={}", univState.getGlobalState().getBaseSeed(),
                univState.getSeed());

        Generator.LOGGER.info("CLASS INSTANCE# (TOTAL-SO-FAR)");
        Generator.LOGGER.info("----------------------------");
        for (int i = 0; i < Ontology.CLASS_NUM; i++) {
            Generator.LOGGER.debug("{} {} ({})", Ontology.CLASS_TOKEN[i], univState.getInstances()[i].logNum,
                    univState.getGlobalState().getTotalInstances(i));
            classInstNum += univState.getInstances()[i].logNum;
            totalClassInstNum += univState.getGlobalState().getTotalInstances(i);
        }

        Generator.LOGGER.info("TOTAL: {}", classInstNum);
        Generator.LOGGER.info("TOTAL SO FAR: {}", totalClassInstNum);

        Generator.LOGGER.info("PROPERTY INSTANCE# (TOTAL-SO-FAR)");
        Generator.LOGGER.info("-------------------------------");
        for (int i = 0; i < Ontology.PROP_NUM; i++) {
            Generator.LOGGER.debug("{} {} ({})", Ontology.PROP_TOKEN[i], univState.getProperties()[i].logNum,
                    univState.getGlobalState().getTotalProperties(i));
            propInstNum += univState.getProperties()[i].logNum;
            totalPropInstNum += univState.getGlobalState().getTotalProperties(i);
        }
        Generator.LOGGER.info("TOTAL: {}", propInstNum);
        Generator.LOGGER.info("TOTAL SO FAR: {}", totalPropInstNum);

        if (!univState.getGlobalState().isQuietMode()) {
            System.out.println("CLASS INSTANCE #: " + classInstNum + ", TOTAL SO FAR: " + totalClassInstNum);
            System.out.println("PROPERTY INSTANCE #: " + propInstNum + ", TOTAL SO FAR: " + totalPropInstNum);
            System.out.println();
        }

    }

}