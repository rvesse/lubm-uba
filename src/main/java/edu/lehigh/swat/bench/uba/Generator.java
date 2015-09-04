/**
 * by Yuanbo Guo
 * Semantic Web and Agent Technology Lab, CSE Department, Lehigh University, USA
 * Copyright (C) 2004
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package edu.lehigh.swat.bench.uba;

import java.util.*;

import edu.lehigh.swat.bench.uba.model.CourseInfo;
import edu.lehigh.swat.bench.uba.model.GenerationParameters;
import edu.lehigh.swat.bench.uba.model.InstanceCount;
import edu.lehigh.swat.bench.uba.model.Ontology;
import edu.lehigh.swat.bench.uba.model.PropertyCount;
import edu.lehigh.swat.bench.uba.model.PublicationInfo;
import edu.lehigh.swat.bench.uba.model.RaInfo;
import edu.lehigh.swat.bench.uba.model.TaInfo;
import edu.lehigh.swat.bench.uba.writers.DamlWriter;
import edu.lehigh.swat.bench.uba.writers.OwlWriter;
import edu.lehigh.swat.bench.uba.writers.Writer;

import java.io.*;

public class Generator implements GeneratorCallbackTarget {

  /** delimiter between different parts in an id string*/
  private static final char ID_DELIMITER = '/';
  /** delimiter between name and index in a name string of an instance */
  private static final char INDEX_DELIMITER = '_';
  /** name of the log file */
  private static final String LOG_FILE = "log.txt";

  /** univ-bench ontology url */
  String ontology;
  /** (class) instance information */
  private InstanceCount[] instances_;
  /** property instance information */
  private PropertyCount[] properties_;
  /** data file writer */
  private Writer writer_;
  /** generate DAML+OIL data (instead of OWL) */
  private boolean isDaml_;
  /** random number generator */
  private Random random_;
  /** seed of the random number genertor for the current university */
  private long seed_ = 0l;
  /** user specified seed for the data generation */
  private long baseSeed_ = 0l;
  /** list of undergraduate courses generated so far (in the current department) */
  private ArrayList<CourseInfo> underCourses_;
  /** list of graduate courses generated so far (in the current department) */
  private ArrayList<CourseInfo> gradCourses_;
  /** list of remaining available undergraduate courses (in the current department) */
  private ArrayList<Integer> remainingUnderCourses_;
  /** list of remaining available graduate courses (in the current department) */
  private ArrayList<Integer> remainingGradCourses_;
  /** list of publication instances generated so far (in the current department) */
  private ArrayList<PublicationInfo> publications_;
  /** index of the full professor who has been chosen as the department chair */
  private int chair_;
  /** starting index of the universities */
  private int startIndex_;
  /** log writer */
  private PrintStream log_ = null;
  
  private File outputDir = null;

  /**
   * constructor
   */
  public Generator() {
    instances_ = new InstanceCount[Ontology.CLASS_NUM];
    for (int i = 0; i < Ontology.CLASS_NUM; i++) {
      instances_[i] = new InstanceCount();
    }
    properties_ = new PropertyCount[Ontology.PROP_NUM];
    for (int i = 0; i < Ontology.PROP_NUM; i++) {
      properties_[i] = new PropertyCount();
    }

    random_ = new Random();
    underCourses_ = new ArrayList<CourseInfo>();
    gradCourses_ = new ArrayList<CourseInfo>();
    remainingUnderCourses_ = new ArrayList<Integer>();
    remainingGradCourses_ = new ArrayList<Integer>();
    publications_ = new ArrayList<PublicationInfo>();
  }

  /**
   * Begins the data generation.
   * @param univNum Number of universities to generate.
   * @param startIndex Starting index of the universities.
   * @param seed Seed for data generation.
   * @param daml Generates DAML+OIL data if true, OWL data otherwise.
   * @param ontology Ontology url.
   */
  public void start(int univNum, int startIndex, int seed, boolean daml,
                    String ontology, String workDir) {
    this.ontology = ontology;

    isDaml_ = daml;
    if (daml)
      writer_ = new DamlWriter(this, ontology);
    else
      writer_ = new OwlWriter(this, ontology);

    startIndex_ = startIndex;
    baseSeed_ = seed;
    instances_[Ontology.CS_C_UNIV].num = univNum;
    instances_[Ontology.CS_C_UNIV].count = startIndex;
    outputDir = workDir != null ? new File(workDir) : new File(".");
    outputDir = outputDir.getAbsoluteFile();
    if (!outputDir.exists()) {
        if (!outputDir.mkdirs()) {
            throw new IllegalArgumentException(String.format("Unable to create requested output directory %s", outputDir));
        }
    }
    
    _generate();
    System.out.println("See log.txt for more details.");
  }

  ///////////////////////////////////////////////////////////////////////////
  //writer callbacks

  public void startSectionCB(int classType) {
    instances_[classType].logNum++;
    instances_[classType].logTotal++;
  }

  public void startAboutSectionCB(int classType) {
    startSectionCB(classType);
  }

  public void addPropertyCB(int property) {
    properties_[property].logNum++;
    properties_[property].logTotal++;
  }

  public void addValueClassCB(int classType) {
    instances_[classType].logNum++;
    instances_[classType].logTotal++;
  }

  ///////////////////////////////////////////////////////////////////////////

  /**
   * Sets instance specification.
   */
  private void _setInstanceInfo() {
    int subClass, superClass;

    for (int i = 0; i < Ontology.CLASS_NUM; i++) {
      switch (i) {
        case Ontology.CS_C_UNIV:
          break;
        case Ontology.CS_C_DEPT:
          break;
        case Ontology.CS_C_FULLPROF:
          instances_[i].num = _getRandomFromRange(GenerationParameters.FULLPROF_MIN, GenerationParameters.FULLPROF_MAX);
          break;
        case Ontology.CS_C_ASSOPROF:
          instances_[i].num = _getRandomFromRange(GenerationParameters.ASSOPROF_MIN, GenerationParameters.ASSOPROF_MAX);
          break;
        case Ontology.CS_C_ASSTPROF:
          instances_[i].num = _getRandomFromRange(GenerationParameters.ASSTPROF_MIN, GenerationParameters.ASSTPROF_MAX);
          break;
        case Ontology.CS_C_LECTURER:
          instances_[i].num = _getRandomFromRange(GenerationParameters.LEC_MIN, GenerationParameters.LEC_MAX);
          break;
        case Ontology.CS_C_UNDERSTUD:
          instances_[i].num = _getRandomFromRange(GenerationParameters.R_UNDERSTUD_FACULTY_MIN *
                                         instances_[Ontology.CS_C_FACULTY].total,
                                         GenerationParameters.R_UNDERSTUD_FACULTY_MAX *
                                         instances_[Ontology.CS_C_FACULTY].total);
          break;
        case Ontology.CS_C_GRADSTUD:
          instances_[i].num = _getRandomFromRange(GenerationParameters.R_GRADSTUD_FACULTY_MIN *
                                         instances_[Ontology.CS_C_FACULTY].total,
                                         GenerationParameters.R_GRADSTUD_FACULTY_MAX *
                                         instances_[Ontology.CS_C_FACULTY].total);
          break;
        case Ontology.CS_C_TA:
          instances_[i].num = _getRandomFromRange(instances_[Ontology.CS_C_GRADSTUD].total /
                                         GenerationParameters.R_GRADSTUD_TA_MAX,
                                         instances_[Ontology.CS_C_GRADSTUD].total /
                                         GenerationParameters.R_GRADSTUD_TA_MIN);
          break;
        case Ontology.CS_C_RA:
          instances_[i].num = _getRandomFromRange(instances_[Ontology.CS_C_GRADSTUD].total /
                                         GenerationParameters.R_GRADSTUD_RA_MAX,
                                         instances_[Ontology.CS_C_GRADSTUD].total /
                                         GenerationParameters.R_GRADSTUD_RA_MIN);
          break;
        case Ontology.CS_C_RESEARCHGROUP:
          instances_[i].num = _getRandomFromRange(GenerationParameters.RESEARCHGROUP_MIN, GenerationParameters.RESEARCHGROUP_MAX);
          break;
        default:
          instances_[i].num = Ontology.CLASS_INFO[i][Ontology.INDEX_NUM];
          break;
      }
      instances_[i].total = instances_[i].num;
      subClass = i;
      while ( (superClass = Ontology.CLASS_INFO[subClass][Ontology.INDEX_SUPER]) != Ontology.CS_C_NULL) {
        instances_[superClass].total += instances_[i].num;
        subClass = superClass;
      }
    }
  }

  /** Begins data generation according to the specification */
  private void _generate() {
    System.out.println("Started...");
    try {
      log_ = new PrintStream(new FileOutputStream(System.getProperty("user.dir") +
                                                 System.getProperty("file.separator") + LOG_FILE));
      writer_.start();
      for (int i = 0; i < instances_[Ontology.CS_C_UNIV].num; i++) {
        _generateUniv(i + startIndex_);
      }
      writer_.end();
      log_.close();
    }
    catch (IOException e) {
      System.out.println("Failed to create log file!");
    }
    System.out.println("Completed!");
  }

  /**
   * Creates a university.
   * @param index Index of the university.
   */
  private void _generateUniv(int index) {
    //this transformation guarantees no different pairs of (index, baseSeed) generate the same data
    seed_ = baseSeed_ * (Integer.MAX_VALUE + 1) + index;
    random_.setSeed(seed_);

    //determine department number
    instances_[Ontology.CS_C_DEPT].num = _getRandomFromRange(GenerationParameters.DEPT_MIN, GenerationParameters.DEPT_MAX);
    instances_[Ontology.CS_C_DEPT].count = 0;
    //generate departments
    for (int i = 0; i < instances_[Ontology.CS_C_DEPT].num; i++) {
      _generateDept(index, i);
    }
  }

  /**
   * Creates a department.
   * @param univIndex Index of the current university.
   * @param index Index of the department.
   * NOTE: Use univIndex instead of instances[CS_C_UNIV].count till generateASection(CS_C_UNIV, ) is invoked.
   */
  private void _generateDept(int univIndex, int index) {
      StringBuilder fileName = new StringBuilder();
      fileName.append(outputDir.getAbsolutePath());
      if (fileName.charAt(fileName.length() - 1) != File.separatorChar) 
          fileName.append(File.separatorChar);
      fileName.append(_getName(Ontology.CS_C_UNIV, univIndex));
      fileName.append(INDEX_DELIMITER);
      fileName.append(index);
      fileName.append(_getFileSuffix());
    writer_.startFile(fileName.toString());

    //reset
    _setInstanceInfo();
    underCourses_.clear();
    gradCourses_.clear();
    remainingUnderCourses_.clear();
    remainingGradCourses_.clear();
    for (int i = 0; i < GenerationParameters.UNDER_COURSE_NUM; i++) {
      remainingUnderCourses_.add(new Integer(i));
    }
    for (int i = 0; i < GenerationParameters.GRAD_COURSE_NUM; i++) {
      remainingGradCourses_.add(new Integer(i));
    }
    publications_.clear();
    for (int i = 0; i < Ontology.CLASS_NUM; i++) {
      instances_[i].logNum = 0;
    }
    for (int i = 0; i < Ontology.PROP_NUM; i++) {
      properties_[i].logNum = 0;
    }

    //decide the chair
    chair_ = random_.nextInt(instances_[Ontology.CS_C_FULLPROF].total);

    if (index == 0) {
      _generateASection(Ontology.CS_C_UNIV, univIndex);
    }
    _generateASection(Ontology.CS_C_DEPT, index);
    for (int i = Ontology.CS_C_DEPT + 1; i < Ontology.CLASS_NUM; i++) {
      instances_[i].count = 0;
      for (int j = 0; j < instances_[i].num; j++) {
        _generateASection(i, j);
      }
    }

    _generatePublications();
    _generateCourses();
    _generateRaTa();

    System.out.println(fileName + " generated");
    String bar = "";
    for (int i = 0; i < fileName.length(); i++)
      bar += '-';
    log_.println(bar);
    log_.println(fileName);
    log_.println(bar);
    _generateComments();
    writer_.endFile();
  }

  ///////////////////////////////////////////////////////////////////////////
  //instance generation

  /**
   * Generates an instance of the specified class
   * @param classType Type of the instance.
   * @param index Index of the instance.
   */
  private void _generateASection(int classType, int index) {
    _updateCount(classType);

    switch (classType) {
      case Ontology.CS_C_UNIV:
        _generateAUniv(index);
        break;
      case Ontology.CS_C_DEPT:
        _generateADept(index);
        break;
      case Ontology.CS_C_FACULTY:
        _generateAFaculty(index);
        break;
      case Ontology.CS_C_PROF:
        _generateAProf(index);
        break;
      case Ontology.CS_C_FULLPROF:
        _generateAFullProf(index);
        break;
      case Ontology.CS_C_ASSOPROF:
        _generateAnAssociateProfessor(index);
        break;
      case Ontology.CS_C_ASSTPROF:
        _generateAnAssistantProfessor(index);
        break;
      case Ontology.CS_C_LECTURER:
        _generateALecturer(index);
        break;
      case Ontology.CS_C_UNDERSTUD:
        _generateAnUndergraduateStudent(index);
        break;
      case Ontology.CS_C_GRADSTUD:
        _generateAGradudateStudent(index);
        break;
      case Ontology.CS_C_COURSE:
        _generateACourse(index);
        break;
      case Ontology.CS_C_GRADCOURSE:
        _generateAGraduateCourse(index);
        break;
      case Ontology.CS_C_RESEARCHGROUP:
        _generateAResearchGroup(index);
        break;
      default:
        break;
    }
  }

  /**
   * Generates a university instance.
   * @param index Index of the instance.
   */
  private void _generateAUniv(int index) {
    writer_.startSection(Ontology.CS_C_UNIV, _getId(Ontology.CS_C_UNIV, index));
    writer_.addProperty(Ontology.CS_P_NAME, _getRelativeName(Ontology.CS_C_UNIV, index), false);
    writer_.endSection(Ontology.CS_C_UNIV);
  }

  /**
   * Generates a department instance.
   * @param index Index of the department.
   */
  private void _generateADept(int index) {
    writer_.startSection(Ontology.CS_C_DEPT, _getId(Ontology.CS_C_DEPT, index));
    writer_.addProperty(Ontology.CS_P_NAME, _getRelativeName(Ontology.CS_C_DEPT, index), false);
    writer_.addProperty(Ontology.CS_P_SUBORGANIZATIONOF, Ontology.CS_C_UNIV,
                       _getId(Ontology.CS_C_UNIV, instances_[Ontology.CS_C_UNIV].count - 1));
    writer_.endSection(Ontology.CS_C_DEPT);
  }

  /**
   * Generates a faculty instance.
   * @param index Index of the faculty.
   */
  private void _generateAFaculty(int index) {
    writer_.startSection(Ontology.CS_C_FACULTY, _getId(Ontology.CS_C_FACULTY, index));
    _generateAFaculty_a(Ontology.CS_C_FACULTY, index);
    writer_.endSection(Ontology.CS_C_FACULTY);
  }

  /**
   * Generates properties for the specified faculty instance.
   * @param type Type of the faculty.
   * @param index Index of the instance within its type.
   */
  private void _generateAFaculty_a(int type, int index) {
    int indexInFaculty;
    int courseNum;
    int courseIndex;

    indexInFaculty = instances_[Ontology.CS_C_FACULTY].count - 1;

    writer_.addProperty(Ontology.CS_P_NAME, _getRelativeName(type, index), false);

    //undergradutate courses
    courseNum = _getRandomFromRange(GenerationParameters.FACULTY_COURSE_MIN, GenerationParameters.FACULTY_COURSE_MAX);
    for (int i = 0; i < courseNum; i++) {
      courseIndex = _AssignCourse(indexInFaculty);
      writer_.addProperty(Ontology.CS_P_TEACHEROF, _getId(Ontology.CS_C_COURSE, courseIndex), true);
    }
    //gradutate courses
    courseNum = _getRandomFromRange(GenerationParameters.FACULTY_GRADCOURSE_MIN, GenerationParameters.FACULTY_GRADCOURSE_MAX);
    for (int i = 0; i < courseNum; i++) {
      courseIndex = _AssignGraduateCourse(indexInFaculty);
      writer_.addProperty(Ontology.CS_P_TEACHEROF, _getId(Ontology.CS_C_GRADCOURSE, courseIndex), true);
    }
    //person properties
    writer_.addProperty(Ontology.CS_P_UNDERGRADFROM, Ontology.CS_C_UNIV,
                       _getId(Ontology.CS_C_UNIV, random_.nextInt(GenerationParameters.UNIV_NUM)));
    writer_.addProperty(Ontology.CS_P_GRADFROM, Ontology.CS_C_UNIV,
                       _getId(Ontology.CS_C_UNIV, random_.nextInt(GenerationParameters.UNIV_NUM)));
    writer_.addProperty(Ontology.CS_P_DOCFROM, Ontology.CS_C_UNIV,
                       _getId(Ontology.CS_C_UNIV, random_.nextInt(GenerationParameters.UNIV_NUM)));
    writer_.addProperty(Ontology.CS_P_WORKSFOR,
                       _getId(Ontology.CS_C_DEPT, instances_[Ontology.CS_C_DEPT].count - 1), true);
    writer_.addProperty(Ontology.CS_P_EMAIL, _getEmail(type, index), false);
    writer_.addProperty(Ontology.CS_P_TELEPHONE, "xxx-xxx-xxxx", false);
  }

  /**
   * Assigns an undergraduate course to the specified faculty.
   * @param indexInFaculty Index of the faculty.
   * @return Index of the selected course in the pool.
   */
  private int _AssignCourse(int indexInFaculty) {
    //NOTE: this line, although overriden by the next one, is deliberately kept
    // to guarantee identical random number generation to the previous version.
    int pos = _getRandomFromRange(0, remainingUnderCourses_.size() - 1);
    pos = 0; //fetch courses in sequence

    CourseInfo course = new CourseInfo();
    course.indexInFaculty = indexInFaculty;
    course.globalIndex = remainingUnderCourses_.get(pos).intValue();
    underCourses_.add(course);

    remainingUnderCourses_.remove(pos);

    return course.globalIndex;
  }

  /**
   * Assigns a graduate course to the specified faculty.
   * @param indexInFaculty Index of the faculty.
   * @return Index of the selected course in the pool.
   */
  private int _AssignGraduateCourse(int indexInFaculty) {
    //NOTE: this line, although overriden by the next one, is deliberately kept
    // to guarantee identical random number generation to the previous version.
    int pos = _getRandomFromRange(0, remainingGradCourses_.size() - 1);
    pos = 0; //fetch courses in sequence

    CourseInfo course = new CourseInfo();
    course.indexInFaculty = indexInFaculty;
    course.globalIndex = ( (Integer) remainingGradCourses_.get(pos)).intValue();
    gradCourses_.add(course);

    remainingGradCourses_.remove(pos);

    return course.globalIndex;
  }

  /**
   * Generates a professor instance.
   * @param index Index of the professor.
   */
  private void _generateAProf(int index) {
    writer_.startSection(Ontology.CS_C_PROF, _getId(Ontology.CS_C_PROF, index));
    _generateAProf_a(Ontology.CS_C_PROF, index);
    writer_.endSection(Ontology.CS_C_PROF);
  }

  /**
   * Generates properties for a professor instance.
   * @param type Type of the professor.
   * @param index Index of the intance within its type.
   */
  private void _generateAProf_a(int type, int index) {
    _generateAFaculty_a(type, index);
    writer_.addProperty(Ontology.CS_P_RESEARCHINTEREST,
                       _getRelativeName(Ontology.CS_C_RESEARCH,
                                       random_.nextInt(GenerationParameters.RESEARCH_NUM)), false);
  }

  /**
   * Generates a full professor instances.
   * @param index Index of the full professor.
   */
  private void _generateAFullProf(int index) {
    String id;

    id = _getId(Ontology.CS_C_FULLPROF, index);
    writer_.startSection(Ontology.CS_C_FULLPROF, id);
    _generateAProf_a(Ontology.CS_C_FULLPROF, index);
    if (index == chair_) {
      writer_.addProperty(Ontology.CS_P_HEADOF,
                         _getId(Ontology.CS_C_DEPT, instances_[Ontology.CS_C_DEPT].count - 1), true);
    }
    writer_.endSection(Ontology.CS_C_FULLPROF);
    _assignFacultyPublications(id, GenerationParameters.FULLPROF_PUB_MIN, GenerationParameters.FULLPROF_PUB_MAX);
  }

  /**
   * Generates an associate professor instance.
   * @param index Index of the associate professor.
   */
  private void _generateAnAssociateProfessor(int index) {
    String id = _getId(Ontology.CS_C_ASSOPROF, index);
    writer_.startSection(Ontology.CS_C_ASSOPROF, id);
    _generateAProf_a(Ontology.CS_C_ASSOPROF, index);
    writer_.endSection(Ontology.CS_C_ASSOPROF);
    _assignFacultyPublications(id, GenerationParameters.ASSOPROF_PUB_MIN, GenerationParameters.ASSOPROF_PUB_MAX);
  }

  /**
   * Generates an assistant professor instance.
   * @param index Index of the assistant professor.
   */
  private void _generateAnAssistantProfessor(int index) {
    String id = _getId(Ontology.CS_C_ASSTPROF, index);
    writer_.startSection(Ontology.CS_C_ASSTPROF, id);
    _generateAProf_a(Ontology.CS_C_ASSTPROF, index);
    writer_.endSection(Ontology.CS_C_ASSTPROF);
    _assignFacultyPublications(id, GenerationParameters.ASSTPROF_PUB_MIN, GenerationParameters.ASSTPROF_PUB_MAX);
  }

  /**
   * Generates a lecturer instance.
   * @param index Index of the lecturer.
   */
  private void _generateALecturer(int index) {
    String id = _getId(Ontology.CS_C_LECTURER, index);
    writer_.startSection(Ontology.CS_C_LECTURER, id);
    _generateAFaculty_a(Ontology.CS_C_LECTURER, index);
    writer_.endSection(Ontology.CS_C_LECTURER);
    _assignFacultyPublications(id, GenerationParameters.LEC_PUB_MIN, GenerationParameters.LEC_PUB_MAX);
  }

  /**
   * Assigns publications to the specified faculty.
   * @param author Id of the faculty
   * @param min Minimum number of publications
   * @param max Maximum number of publications
   */
  private void _assignFacultyPublications(String author, int min, int max) {
    int num;
    PublicationInfo publication;

    num = _getRandomFromRange(min, max);
    for (int i = 0; i < num; i++) {
      publication = new PublicationInfo();
      publication.id = _getId(Ontology.CS_C_PUBLICATION, i, author);
      publication.name = _getRelativeName(Ontology.CS_C_PUBLICATION, i);
      publication.authors = new ArrayList<String>();
      publication.authors.add(author);
      publications_.add(publication);
    }
  }

  /**
   * Assigns publications to the specified graduate student. The publications are
   * chosen from some faculties'.
   * @param author Id of the graduate student.
   * @param min Minimum number of publications.
   * @param max Maximum number of publications.
   */
  private void _assignGraduateStudentPublications(String author, int min, int max) {
    int num;
    PublicationInfo publication;

    num = _getRandomFromRange(min, max);
    ArrayList<Integer> list = _getRandomList(num, 0, publications_.size() - 1);
    for (int i = 0; i < list.size(); i++) {
      publication = (PublicationInfo) publications_.get( list.get(i).
                                               intValue());
      publication.authors.add(author);
    }
  }

  /**
   * Generates publication instances. These publications are assigned to some faculties
   * and graduate students before.
   */
  private void _generatePublications() {
    for (int i = 0; i < publications_.size(); i++) {
      _generateAPublication( (PublicationInfo) publications_.get(i));
    }
  }

  /**
   * Generates a publication instance.
   * @param publication Information of the publication.
   */
  private void _generateAPublication(PublicationInfo publication) {
    writer_.startSection(Ontology.CS_C_PUBLICATION, publication.id);
    writer_.addProperty(Ontology.CS_P_NAME, publication.name, false);
    for (int i = 0; i < publication.authors.size(); i++) {
      writer_.addProperty(Ontology.CS_P_PUBLICATIONAUTHOR,
                         (String) publication.authors.get(i), true);
    }
    writer_.endSection(Ontology.CS_C_PUBLICATION);
  }

  /**
   * Generates properties for the specified student instance.
   * @param type Type of the student.
   * @param index Index of the instance within its type.
   */
  private void _generateAStudent_a(int type, int index) {
    writer_.addProperty(Ontology.CS_P_NAME, _getRelativeName(type, index), false);
    writer_.addProperty(Ontology.CS_P_MEMBEROF,
                       _getId(Ontology.CS_C_DEPT, instances_[Ontology.CS_C_DEPT].count - 1), true);
    writer_.addProperty(Ontology.CS_P_EMAIL, _getEmail(type, index), false);
    writer_.addProperty(Ontology.CS_P_TELEPHONE, "xxx-xxx-xxxx", false);
  }

  /**
   * Generates an undergraduate student instance.
   * @param index Index of the undergraduate student.
   */
  private void _generateAnUndergraduateStudent(int index) {
    int n;
    ArrayList<Integer> list;

    writer_.startSection(Ontology.CS_C_UNDERSTUD, _getId(Ontology.CS_C_UNDERSTUD, index));
    _generateAStudent_a(Ontology.CS_C_UNDERSTUD, index);
    n = _getRandomFromRange(GenerationParameters.UNDERSTUD_COURSE_MIN, GenerationParameters.UNDERSTUD_COURSE_MAX);
    list = _getRandomList(n, 0, underCourses_.size() - 1);
    for (int i = 0; i < list.size(); i++) {
      CourseInfo info = (CourseInfo) underCourses_.get( list.get(i).
          intValue());
      writer_.addProperty(Ontology.CS_P_TAKECOURSE, _getId(Ontology.CS_C_COURSE, info.globalIndex), true);
    }
    if (0 == random_.nextInt(GenerationParameters.R_UNDERSTUD_ADVISOR)) {
      writer_.addProperty(Ontology.CS_P_ADVISOR, _selectAdvisor(), true);
    }
    writer_.endSection(Ontology.CS_C_UNDERSTUD);
  }

  /**
   * Generates a graduate student instance.
   * @param index Index of the graduate student.
   */
  private void _generateAGradudateStudent(int index) {
    int n;
    ArrayList<Integer> list;
    String id;

    id = _getId(Ontology.CS_C_GRADSTUD, index);
    writer_.startSection(Ontology.CS_C_GRADSTUD, id);
    _generateAStudent_a(Ontology.CS_C_GRADSTUD, index);
    n = _getRandomFromRange(GenerationParameters.GRADSTUD_COURSE_MIN, GenerationParameters.GRADSTUD_COURSE_MAX);
    list = _getRandomList(n, 0, gradCourses_.size() - 1);
    for (int i = 0; i < list.size(); i++) {
      CourseInfo info = (CourseInfo) gradCourses_.get( list.get(i).
          intValue());
      writer_.addProperty(Ontology.CS_P_TAKECOURSE,
                         _getId(Ontology.CS_C_GRADCOURSE, info.globalIndex), true);
    }
    writer_.addProperty(Ontology.CS_P_UNDERGRADFROM, Ontology.CS_C_UNIV,
                       _getId(Ontology.CS_C_UNIV, random_.nextInt(GenerationParameters.UNIV_NUM)));
    if (0 == random_.nextInt(GenerationParameters.R_GRADSTUD_ADVISOR)) {
      writer_.addProperty(Ontology.CS_P_ADVISOR, _selectAdvisor(), true);
    }
    _assignGraduateStudentPublications(id, GenerationParameters.GRADSTUD_PUB_MIN, GenerationParameters.GRADSTUD_PUB_MAX);
    writer_.endSection(Ontology.CS_C_GRADSTUD);
  }

  /**
   * Select an advisor from the professors.
   * @return Id of the selected professor.
   */
  private String _selectAdvisor() {
    int profType;
    int index;

    profType = _getRandomFromRange(Ontology.CS_C_FULLPROF, Ontology.CS_C_ASSTPROF);
    index = random_.nextInt(instances_[profType].total);
    return _getId(profType, index);
  }

  /**
   * Generates a TA instance according to the specified information.
   * @param ta Information of the TA.
   */
  private void _generateATa(TaInfo ta) {
    writer_.startAboutSection(Ontology.CS_C_TA, _getId(Ontology.CS_C_GRADSTUD, ta.indexInGradStud));
    writer_.addProperty(Ontology.CS_P_TAOF, _getId(Ontology.CS_C_COURSE, ta.indexInCourse), true);
    writer_.endSection(Ontology.CS_C_TA);
  }

  /**
   * Generates an RA instance according to the specified information.
   * @param ra Information of the RA.
   */
  private void _generateAnRa(RaInfo ra) {
    writer_.startAboutSection(Ontology.CS_C_RA, _getId(Ontology.CS_C_GRADSTUD, ra.indexInGradStud));
    writer_.endSection(Ontology.CS_C_RA);
  }

  /**
   * Generates a course instance.
   * @param index Index of the course.
   */
  private void _generateACourse(int index) {
    writer_.startSection(Ontology.CS_C_COURSE, _getId(Ontology.CS_C_COURSE, index));
    writer_.addProperty(Ontology.CS_P_NAME,
                       _getRelativeName(Ontology.CS_C_COURSE, index), false);
    writer_.endSection(Ontology.CS_C_COURSE);
  }

  /**
   * Generates a graduate course instance.
   * @param index Index of the graduate course.
   */
  private void _generateAGraduateCourse(int index) {
    writer_.startSection(Ontology.CS_C_GRADCOURSE, _getId(Ontology.CS_C_GRADCOURSE, index));
    writer_.addProperty(Ontology.CS_P_NAME,
                       _getRelativeName(Ontology.CS_C_GRADCOURSE, index), false);
    writer_.endSection(Ontology.CS_C_GRADCOURSE);
  }

  /**
   * Generates course/graduate course instances. These course are assigned to some
   * faculties before.
   */
  private void _generateCourses() {
    for (int i = 0; i < underCourses_.size(); i++) {
      _generateACourse( ( (CourseInfo) underCourses_.get(i)).globalIndex);
    }
    for (int i = 0; i < gradCourses_.size(); i++) {
      _generateAGraduateCourse( ( (CourseInfo) gradCourses_.get(i)).globalIndex);
    }
  }

  /**
   * Chooses RAs and TAs from graduate student and generates their instances accordingly.
   */
  private void _generateRaTa() {
    ArrayList<Integer> list, courseList;
    TaInfo ta;
    RaInfo ra;
    int i;

    list = _getRandomList(instances_[Ontology.CS_C_TA].total + instances_[Ontology.CS_C_RA].total,
                      0, instances_[Ontology.CS_C_GRADSTUD].total - 1);
    courseList = _getRandomList(instances_[Ontology.CS_C_TA].total, 0,
                            underCourses_.size() - 1);

    for (i = 0; i < instances_[Ontology.CS_C_TA].total; i++) {
      ta = new TaInfo();
      ta.indexInGradStud = list.get(i).intValue();
      ta.indexInCourse = ( (CourseInfo) underCourses_.get( courseList.get(i).intValue())).globalIndex;
      _generateATa(ta);
    }
    while (i < list.size()) {
      ra = new RaInfo();
      ra.indexInGradStud = list.get(i).intValue();
      _generateAnRa(ra);
      i++;
    }
  }

  /**
   * Generates a research group instance.
   * @param index Index of the research group.
   */
  private void _generateAResearchGroup(int index) {
    String id;
    id = _getId(Ontology.CS_C_RESEARCHGROUP, index);
    writer_.startSection(Ontology.CS_C_RESEARCHGROUP, id);
    writer_.addProperty(Ontology.CS_P_SUBORGANIZATIONOF,
                       _getId(Ontology.CS_C_DEPT, instances_[Ontology.CS_C_DEPT].count - 1), true);
    writer_.endSection(Ontology.CS_C_RESEARCHGROUP);
  }

  ///////////////////////////////////////////////////////////////////////////

  /**
   * @return Suffix of the data file.
   */
  private String _getFileSuffix() {
    return isDaml_ ? ".daml" : ".owl";
  }

  /**
   * Gets the id of the specified instance.
   * @param classType Type of the instance.
   * @param index Index of the instance within its type.
   * @return Id of the instance.
   */
  private String _getId(int classType, int index) {
    String id;

    switch (classType) {
      case Ontology.CS_C_UNIV:
        id = "http://www." + _getRelativeName(classType, index) + ".edu";
        break;
      case Ontology.CS_C_DEPT:
        id = "http://www." + _getRelativeName(classType, index) + "." +
            _getRelativeName(Ontology.CS_C_UNIV, instances_[Ontology.CS_C_UNIV].count - 1) +
            ".edu";
        break;
      default:
        id = _getId(Ontology.CS_C_DEPT, instances_[Ontology.CS_C_DEPT].count - 1) + ID_DELIMITER +
            _getRelativeName(classType, index);
        break;
    }

    return id;
  }

  /**
   * Gets the id of the specified instance.
   * @param classType Type of the instance.
   * @param index Index of the instance within its type.
   * @param param Auxiliary parameter.
   * @return Id of the instance.
   */
  private String _getId(int classType, int index, String param) {
    String id;

    switch (classType) {
      case Ontology.CS_C_PUBLICATION:
        //NOTE: param is author id
        id = param + ID_DELIMITER + Ontology.CLASS_TOKEN[classType] + index;
        break;
      default:
        id = _getId(classType, index);
        break;
    }

    return id;
  }

  /**
   * Gets the globally unique name of the specified instance.
   * @param classType Type of the instance.
   * @param index Index of the instance within its type.
   * @return Name of the instance.
   */
  private String _getName(int classType, int index) {
    String name;

    switch (classType) {
      case Ontology.CS_C_UNIV:
        name = _getRelativeName(classType, index);
        break;
      case Ontology.CS_C_DEPT:
        name = _getRelativeName(classType, index) + INDEX_DELIMITER +
            (instances_[Ontology.CS_C_UNIV].count - 1);
        break;
      //NOTE: Assume departments with the same index share the same pool of courses and researches
      case Ontology.CS_C_COURSE:
      case Ontology.CS_C_GRADCOURSE:
      case Ontology.CS_C_RESEARCH:
        name = _getRelativeName(classType, index) + INDEX_DELIMITER +
            (instances_[Ontology.CS_C_DEPT].count - 1);
        break;
      default:
        name = _getRelativeName(classType, index) + INDEX_DELIMITER +
            (instances_[Ontology.CS_C_DEPT].count - 1) + INDEX_DELIMITER +
            (instances_[Ontology.CS_C_UNIV].count - 1);
        break;
    }

    return name;
  }

  /**
   * Gets the name of the specified instance that is unique within a department.
   * @param classType Type of the instance.
   * @param index Index of the instance within its type.
   * @return Name of the instance.
   */
  private String _getRelativeName(int classType, int index) {
    String name;

    switch (classType) {
      case Ontology.CS_C_UNIV:
        //should be unique too!
        name = Ontology.CLASS_TOKEN[classType] + index;
        break;
      case Ontology.CS_C_DEPT:
        name = Ontology.CLASS_TOKEN[classType] + index;
        break;
      default:
        name = Ontology.CLASS_TOKEN[classType] + index;
        break;
    }

    return name;
  }

  /**
   * Gets the email address of the specified instance.
   * @param classType Type of the instance.
   * @param index Index of the instance within its type.
   * @return The email address of the instance.
   */
  private String _getEmail(int classType, int index) {
    String email = "";

    switch (classType) {
      case Ontology.CS_C_UNIV:
        email += _getRelativeName(classType, index) + "@" +
            _getRelativeName(classType, index) + ".edu";
        break;
      case Ontology.CS_C_DEPT:
        email += _getRelativeName(classType, index) + "@" +
            _getRelativeName(classType, index) + "." +
            _getRelativeName(Ontology.CS_C_UNIV, instances_[Ontology.CS_C_UNIV].count - 1) + ".edu";
        break;
      default:
        email += _getRelativeName(classType, index) + "@" +
            _getRelativeName(Ontology.CS_C_DEPT, instances_[Ontology.CS_C_DEPT].count - 1) +
            "." + _getRelativeName(Ontology.CS_C_UNIV, instances_[Ontology.CS_C_UNIV].count - 1) +
            ".edu";
        break;
    }

    return email;
  }

  /**
   * Increases by 1 the instance count of the specified class. This also includes
   * the increase of the instacne count of all its super class.
   * @param classType Type of the instance.
   */
  private void _updateCount(int classType) {
    int subClass, superClass;

    instances_[classType].count++;
    subClass = classType;
    while ( (superClass = Ontology.CLASS_INFO[subClass][Ontology.INDEX_SUPER]) != Ontology.CS_C_NULL) {
      instances_[superClass].count++;
      subClass = superClass;
    }
  }

  /**
   * Creates a list of the specified number of integers without duplication which
   * are randomly selected from the specified range.
   * @param num Number of the integers.
   * @param min Minimum value of selectable integer.
   * @param max Maximum value of selectable integer.
   * @return So generated list of integers.
   */
  private ArrayList<Integer> _getRandomList(int num, int min, int max) {
    ArrayList<Integer> list = new ArrayList<Integer>();
    ArrayList<Integer> tmp = new ArrayList<Integer>();
    for (int i = min; i <= max; i++) {
      tmp.add(new Integer(i));
    }

    for (int i = 0; i < num; i++) {
      int pos = _getRandomFromRange(0, tmp.size() - 1);
      list.add(tmp.get(pos));
      tmp.remove(pos);
    }

    return list;
  }

  /**
   * Randomly selects a integer from the specified range.
   * @param min Minimum value of the selectable integer.
   * @param max Maximum value of the selectable integer.
   * @return The selected integer.
   */
  private int _getRandomFromRange(int min, int max) {
    return min + random_.nextInt(max - min + 1);
  }

  /**
   * Outputs log information to both the log file and the screen after a department
   * is generated.
   */
  private void _generateComments() {
    int classInstNum = 0; //total class instance num in this department
    long totalClassInstNum = 0l; //total class instance num so far
    int propInstNum = 0; //total property instance num in this department
    long totalPropInstNum = 0l; //total property instance num so far
    String comment;

    comment = "External Seed=" + baseSeed_ + " Interal Seed=" + seed_;
    log_.println(comment);
    log_.println();

    comment = "CLASS INSTANCE# TOTAL-SO-FAR";
    log_.println(comment);
    comment = "----------------------------";
    log_.println(comment);
    for (int i = 0; i < Ontology.CLASS_NUM; i++) {
      comment = Ontology.CLASS_TOKEN[i] + " " + instances_[i].logNum + " " +
          instances_[i].logTotal;
      log_.println(comment);
      classInstNum += instances_[i].logNum;
      totalClassInstNum += instances_[i].logTotal;
    }
    log_.println();
    comment = "TOTAL: " + classInstNum;
    log_.println(comment);
    comment = "TOTAL SO FAR: " + totalClassInstNum;
    log_.println(comment);

    comment = "PROPERTY---INSTANCE NUM";
    log_.println();
    comment = "PROPERTY INSTANCE# TOTAL-SO-FAR";
    log_.println(comment);
    comment = "-------------------------------";
    log_.println(comment);
    for (int i = 0; i < Ontology.PROP_NUM; i++) {
      comment = Ontology.PROP_TOKEN[i] + " " + properties_[i].logNum;
      comment = comment + " " + properties_[i].logTotal;
      log_.println(comment);
      propInstNum += properties_[i].logNum;
      totalPropInstNum += properties_[i].logTotal;
    }
    log_.println();
    comment = "TOTAL: " + propInstNum;
    log_.println(comment);
    comment = "TOTAL SO FAR: " + totalPropInstNum;
    log_.println(comment);

    System.out.println("CLASS INSTANCE #: " + classInstNum + ", TOTAL SO FAR: " +
                       totalClassInstNum);
    System.out.println("PROPERTY INSTANCE #: " + propInstNum +
                       ", TOTAL SO FAR: " + totalPropInstNum);
    System.out.println();

    log_.println();
  }

}
