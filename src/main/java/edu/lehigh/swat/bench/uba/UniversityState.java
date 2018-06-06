package edu.lehigh.swat.bench.uba;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import edu.lehigh.swat.bench.uba.model.CourseInfo;
import edu.lehigh.swat.bench.uba.model.GenerationParameters;
import edu.lehigh.swat.bench.uba.model.InstanceCount;
import edu.lehigh.swat.bench.uba.model.Ontology;
import edu.lehigh.swat.bench.uba.model.PropertyCount;
import edu.lehigh.swat.bench.uba.model.PublicationInfo;
import edu.lehigh.swat.bench.uba.writers.ConsolidationMode;
import edu.lehigh.swat.bench.uba.writers.Writer;

public class UniversityState implements GeneratorCallbackTarget {

    private final GlobalState state;

    /** random number generator */
    private final Random random;

    /** seed of the random number generator for the current university */
    private final long seed;

    /** index of this university */
    private final int index;

    /** (class) instance information */
    private InstanceCount[] instances;

    /** property instance information */
    private PropertyCount[] properties;

    /**
     * list of undergraduate courses generated so far (in the current
     * department)
     */
    private ArrayList<CourseInfo> underCourses;
    /** list of graduate courses generated so far (in the current department) */
    private ArrayList<CourseInfo> gradCourses;
    /**
     * list of remaining available undergraduate courses (in the current
     * department)
     */
    private ArrayList<Integer> remainingUnderCourses;
    /**
     * list of remaining available graduate courses (in the current department)
     */
    private ArrayList<Integer> remainingGradCourses;
    /**
     * list of publication instances generated so far (in the current
     * department)
     */
    private ArrayList<PublicationInfo> publications;
    /**
     * index of the full professor who has been chosen as the department chair
     */
    private int chair;

    private Writer writer;
    private boolean completed = false;
    private Throwable error = null;

    public UniversityState(GlobalState state, int index) {
        this.state = state;
        this.index = index;
        this.seed = state.getBaseSeed() * (Integer.MAX_VALUE + 1) + index;
        this.random = new Random(seed);
    }

    /**
     * Should be called before the first time this state is used, initializes
     * the data structures needed for actual data generation to succeed
     */
    public void prepare() {
        this.instances = new InstanceCount[Ontology.CLASS_NUM];
        for (int i = 0; i < Ontology.CLASS_NUM; i++) {
            this.instances[i] = new InstanceCount();
        }
        this.properties = new PropertyCount[Ontology.PROP_NUM];
        for (int i = 0; i < Ontology.PROP_NUM; i++) {
            this.properties[i] = new PropertyCount();
        }

        underCourses = new ArrayList<CourseInfo>();
        gradCourses = new ArrayList<CourseInfo>();
        remainingUnderCourses = new ArrayList<Integer>();
        remainingGradCourses = new ArrayList<Integer>();
        publications = new ArrayList<PublicationInfo>();

        writer = state.createWriter(this);
    }

    public boolean hasCompleted() {
        return this.completed;
    }

    public void setComplete() {
        this.completed = true;

        cleanup();
    }

    private void cleanup() {
        // Throw away our reference to writer because it could be holding
        // various buffers which if we no longer need and as such should be GC'd
        this.writer = null;
        
        // Throw away all our temporary state
        for (int i = 0; i < this.instances.length; i++) {
            this.instances[i] = null;
        }
        this.instances = null;
        for (int i = 0; i < this.properties.length; i++) {
            this.properties[i] = null;
        }
        this.properties = null;
        
        underCourses.clear();
        gradCourses.clear();
        remainingUnderCourses.clear();
        remainingGradCourses.clear();
        publications.clear();
        
        underCourses = null;
        gradCourses = null;
        remainingUnderCourses = null;
        remainingGradCourses = null;
        publications = null;
    }

    public void setError(Throwable e) {
        this.error = e;
        this.state.incrementErrorCount();

        cleanup();
    }

    public boolean hasError() {
        return this.error != null;
    }

    public Throwable getError() {
        return this.error;
    }

    public GlobalState getGlobalState() {
        return state;
    }

    public long getSeed() {
        return this.seed;
    }

    public int getUniversityIndex() {
        return this.index;
    }

    public int getChair() {
        return this.chair;
    }

    public void setChair(int chair) {
        this.chair = chair;
    }

    public Writer getWriter() {
        return writer;
    }

    public String getFilename(int deptIndex) {
        StringBuilder fileName = new StringBuilder();

        // Base in output directory
        fileName.append(this.state.getOutputDirectory().getAbsolutePath());
        if (fileName.charAt(fileName.length() - 1) != File.separatorChar)
            fileName.append(File.separatorChar);

        if (this.state.consolidationMode() != ConsolidationMode.Full) {
            // University
            fileName.append(getName(Ontology.CS_C_UNIV, this.index));

            if (this.state.consolidationMode() == ConsolidationMode.None) {
                // Department Index
                fileName.append(Generator.INDEX_DELIMITER);
                fileName.append(deptIndex);
            }
        } else {
            fileName.append("Universities-");
            fileName.append(Integer.toString(this.state.getWriterPool().getWriterId()));
        }

        // Extension
        fileName.append(this.state.getFileExtension());

        // Compression?
        if (this.state.compressFiles()) {
            fileName.append(".gz");
        }

        return fileName.toString();
    }

    /**
     * Gets the globally unique name of the specified instance.
     * 
     * @param classType
     *            Type of the instance.
     * @param index
     *            Index of the instance within its type.
     * @return Name of the instance.
     */
    public String getName(int classType, int index) {
        String name;

        switch (classType) {
        case Ontology.CS_C_UNIV:
            name = getRelativeName(classType, index);
            break;
        case Ontology.CS_C_DEPT:
            name = getRelativeName(classType, index) + Generator.INDEX_DELIMITER + (this.getUniversityIndex());
            break;
        // NOTE: Assume departments with the same index share the same pool of
        // courses and researches
        case Ontology.CS_C_COURSE:
        case Ontology.CS_C_GRADCOURSE:
        case Ontology.CS_C_RESEARCH:
            name = getRelativeName(classType, index) + Generator.INDEX_DELIMITER
                    + (this.instances[Ontology.CS_C_DEPT].count - 1);
            break;
        default:
            name = getRelativeName(classType, index) + Generator.INDEX_DELIMITER
                    + (this.instances[Ontology.CS_C_DEPT].count - 1) + Generator.INDEX_DELIMITER
                    + (this.getUniversityIndex());
            break;
        }

        return name;
    }

    /**
     * Gets the name of the specified instance that is unique within a
     * department.
     * 
     * @param classType
     *            Type of the instance.
     * @param index
     *            Index of the instance within its type.
     * @return Name of the instance.
     */
    public String getRelativeName(int classType, int index) {
        String name;

        switch (classType) {
        case Ontology.CS_C_UNIV:
            // should be unique too!
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
     * 
     * @param classType
     *            Type of the instance.
     * @param index
     *            Index of the instance within its type.
     * @return The email address of the instance.
     */
    public String getEmail(int classType, int index) {
        String email = "";

        switch (classType) {
        case Ontology.CS_C_UNIV:
            email += getRelativeName(classType, index) + "@" + getRelativeName(classType, index) + ".edu";
            break;
        case Ontology.CS_C_DEPT:
            email += getRelativeName(classType, index) + "@" + getRelativeName(classType, index) + "."
                    + getRelativeName(Ontology.CS_C_UNIV, this.getUniversityIndex()) + ".edu";
            break;
        default:
            email += getRelativeName(classType, index) + "@"
                    + getRelativeName(Ontology.CS_C_DEPT, this.instances[Ontology.CS_C_DEPT].count - 1) + "."
                    + getRelativeName(Ontology.CS_C_UNIV, this.getUniversityIndex()) + ".edu";
            break;
        }

        return email;
    }

    /**
     * Gets the id of the specified instance.
     * 
     * @param classType
     *            Type of the instance.
     * @param index
     *            Index of the instance within its type.
     * @return Id of the instance.
     */
    public String getId(int classType, int index) {
        String id;

        switch (classType) {
        case Ontology.CS_C_UNIV:
            id = "http://www." + getRelativeName(classType, index) + ".edu";
            break;
        case Ontology.CS_C_DEPT:
            id = "http://www." + getRelativeName(classType, index) + "."
                    + getRelativeName(Ontology.CS_C_UNIV, this.getUniversityIndex()) + ".edu";
            break;
        default:
            id = getId(Ontology.CS_C_DEPT, this.instances[Ontology.CS_C_DEPT].count - 1) + Generator.ID_DELIMITER
                    + getRelativeName(classType, index);
            break;
        }

        return id;
    }

    /**
     * Gets the id of the specified instance.
     * 
     * @param classType
     *            Type of the instance.
     * @param index
     *            Index of the instance within its type.
     * @param param
     *            Auxiliary parameter.
     * @return Id of the instance.
     */
    public String getId(int classType, int index, String param) {
        String id;

        switch (classType) {
        case Ontology.CS_C_PUBLICATION:
            // NOTE: param is author id
            id = param + Generator.ID_DELIMITER + Ontology.CLASS_TOKEN[classType] + index;
            break;
        default:
            id = getId(classType, index);
            break;
        }

        return id;
    }

    @Override
    public void startSectionCB(int classType) {
        this.instances[classType].logNum++;
        this.state.incrementTotalInstances(classType);
    }

    @Override
    public void startAboutSectionCB(int classType) {
        startSectionCB(classType);
    }

    @Override
    public void addPropertyCB(int property) {
        this.properties[property].logNum++;
        this.state.incrementTotalProperties(property);
    }

    @Override
    public void addValueClassCB(int classType) {
        this.instances[classType].logNum++;
        this.state.incrementTotalInstances(classType);
    }

    /**
     * Creates a list of the specified number of integers without duplication
     * which are randomly selected from the specified range.
     * 
     * @param num
     *            Number of the integers.
     * @param min
     *            Minimum value of selectable integer.
     * @param max
     *            Maximum value of selectable integer.
     * @return So generated list of integers.
     */
    public ArrayList<Integer> getRandomList(int num, int min, int max) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        ArrayList<Integer> tmp = new ArrayList<Integer>();
        for (int i = min; i <= max; i++) {
            tmp.add(new Integer(i));
        }

        for (int i = 0; i < num; i++) {
            int pos = getRandomFromRange(0, tmp.size() - 1);
            list.add(tmp.get(pos));
            tmp.remove(pos);
        }

        return list;
    }

    /**
     * Randomly selects a integer from the specified range.
     * 
     * @param min
     *            Minimum value of the selectable integer.
     * @param max
     *            Maximum value of the selectable integer.
     * @return The selected integer.
     */
    public int getRandomFromRange(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    public int getRandom(int max) {
        return random.nextInt(max);
    }

    public void reset() {
        this.resetInstanceInfo();
        underCourses.clear();
        gradCourses.clear();
        remainingUnderCourses.clear();
        remainingGradCourses.clear();
        for (int i = 0; i < GenerationParameters.UNDER_COURSE_NUM; i++) {
            remainingUnderCourses.add(new Integer(i));
        }
        for (int i = 0; i < GenerationParameters.GRAD_COURSE_NUM; i++) {
            remainingGradCourses.add(new Integer(i));
        }
        publications.clear();
        for (int i = 0; i < Ontology.CLASS_NUM; i++) {
            this.instances[i].logNum = 0;
        }
        for (int i = 0; i < Ontology.PROP_NUM; i++) {
            this.properties[i].logNum = 0;
        }

        // decide the chair
        chair = this.random.nextInt(this.instances[Ontology.CS_C_FULLPROF].total);
    }

    /**
     * Sets instance specification.
     */
    public void resetInstanceInfo() {
        int subClass, superClass;

        for (int i = 0; i < Ontology.CLASS_NUM; i++) {
            switch (i) {
            case Ontology.CS_C_UNIV:
                break;
            case Ontology.CS_C_DEPT:
                break;
            case Ontology.CS_C_FULLPROF:
                this.instances[i].num = getRandomFromRange(GenerationParameters.FULLPROF_MIN,
                        GenerationParameters.FULLPROF_MAX);
                break;
            case Ontology.CS_C_ASSOPROF:
                this.instances[i].num = getRandomFromRange(GenerationParameters.ASSOPROF_MIN,
                        GenerationParameters.ASSOPROF_MAX);
                break;
            case Ontology.CS_C_ASSTPROF:
                this.instances[i].num = getRandomFromRange(GenerationParameters.ASSTPROF_MIN,
                        GenerationParameters.ASSTPROF_MAX);
                break;
            case Ontology.CS_C_LECTURER:
                this.instances[i].num = getRandomFromRange(GenerationParameters.LEC_MIN, GenerationParameters.LEC_MAX);
                break;
            case Ontology.CS_C_UNDERSTUD:
                this.instances[i].num = getRandomFromRange(
                        GenerationParameters.R_UNDERSTUD_FACULTY_MIN * this.instances[Ontology.CS_C_FACULTY].total,
                        GenerationParameters.R_UNDERSTUD_FACULTY_MAX * this.instances[Ontology.CS_C_FACULTY].total);
                break;
            case Ontology.CS_C_GRADSTUD:
                this.instances[i].num = getRandomFromRange(
                        GenerationParameters.R_GRADSTUD_FACULTY_MIN * this.instances[Ontology.CS_C_FACULTY].total,
                        GenerationParameters.R_GRADSTUD_FACULTY_MAX * this.instances[Ontology.CS_C_FACULTY].total);
                break;
            case Ontology.CS_C_TA:
                this.instances[i].num = getRandomFromRange(
                        this.instances[Ontology.CS_C_GRADSTUD].total / GenerationParameters.R_GRADSTUD_TA_MAX,
                        this.instances[Ontology.CS_C_GRADSTUD].total / GenerationParameters.R_GRADSTUD_TA_MIN);
                break;
            case Ontology.CS_C_RA:
                this.instances[i].num = getRandomFromRange(
                        this.instances[Ontology.CS_C_GRADSTUD].total / GenerationParameters.R_GRADSTUD_RA_MAX,
                        this.instances[Ontology.CS_C_GRADSTUD].total / GenerationParameters.R_GRADSTUD_RA_MIN);
                break;
            case Ontology.CS_C_RESEARCHGROUP:
                this.instances[i].num = getRandomFromRange(GenerationParameters.RESEARCHGROUP_MIN,
                        GenerationParameters.RESEARCHGROUP_MAX);
                break;
            default:
                this.instances[i].num = Ontology.CLASS_INFO[i][Ontology.INDEX_NUM];
                break;
            }
            this.instances[i].total = this.instances[i].num;
            subClass = i;
            while ((superClass = Ontology.CLASS_INFO[subClass][Ontology.INDEX_SUPER]) != Ontology.CS_C_NULL) {
                this.instances[superClass].total += this.instances[i].num;
                subClass = superClass;
            }
        }
    }

    public InstanceCount[] getInstances() {
        return this.instances;
    }

    public PropertyCount[] getProperties() {
        return this.properties;
    }

    public ArrayList<PublicationInfo> getPublications() {
        return this.publications;
    }

    public ArrayList<CourseInfo> getUndergradCourses() {
        return this.underCourses;
    }

    public ArrayList<Integer> getRemainingUndergradCourses() {
        return this.remainingUnderCourses;
    }

    public ArrayList<CourseInfo> getGradCourses() {
        return this.gradCourses;
    }

    public ArrayList<Integer> getRemainingGradCourses() {
        return this.remainingGradCourses;
    }
}
