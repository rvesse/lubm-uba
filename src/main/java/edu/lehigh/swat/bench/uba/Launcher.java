package edu.lehigh.swat.bench.uba;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormat;
import org.slf4j.LoggerFactory;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.HelpOption;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.help.ExitCodes;
import com.github.rvesse.airline.annotations.restrictions.AllowedRawValues;
import com.github.rvesse.airline.SingleCommand;
import com.github.rvesse.airline.parser.errors.ParseException;

import edu.lehigh.swat.bench.uba.writers.WriterType;

@Command(name = "generate.sh", description = "Artificial Data Generator for the Lehigh University Benchmark (LUBM) for SPARQL query engines")
@ExitCodes(codes = { 0, 1, 2, 3 }, descriptions = { "Data was generated successfully", "Help was displayed",
        "Invalid arguments", "Error during data generation" })
public class Launcher {

    /** name of the log file */
    private static final String DEFAULT_LOG_FILE = "log.txt";

    private static final String DEFAULT_ONTOLOGY_URL = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl";

    @Option(name = { "-u",
            "--univ" }, title = "NumberOfUniversities", arity = 1, description = "Sets the number of universities to generate data for (default 1)")
    private int univNum = 1;

    @Option(name = { "-i",
            "--index" }, title = "StartingIndex", arity = 1, description = "Starting index of the universities (default 0)")
    private int startIndex = 0;

    @Option(name = { "-s",
            "--seed" }, title = "Seed", arity = 1, description = "Seed used for random data generation (default 0)")
    private int seed = 0;

    @Option(name = {
            "--format" }, title = "OutputFormat", arity = 1, description = "Sets the desired output format (default OWL)")
    @AllowedRawValues(allowedValues = { "OWL", "DAML", "NTRIPLES", "TURTLE" })
    private WriterType format = WriterType.OWL;

    @Option(name = { "--onto",
            "--ontology" }, title = "OntologyUrl", description = "URL for the benchmark ontology used as the base URL in the generated data (default "
                    + DEFAULT_ONTOLOGY_URL + ")")
    private String ontology = "http://swat.cse.lehigh.edu/onto/univ-bench.owl";

    @Option(name = { "-o",
            "--output" }, title = "OutputDirectory", description = "Sets the output directory to which generated files are written (defaults to working directory)")
    private String workDir = null;

    @Option(name = { "-l", "--log" }, title = "LogFile", description = "Sets the log file (default " + DEFAULT_LOG_FILE
            + ")")
    private String logFile;

    @Option(name = {
            "--log-pattern" }, title = "LogPattern", description = "Provides a log4j log pattern used for the log file (default "
                    + PatternLayout.TTCC_CONVERSION_PATTERN + ")")
    private String logPattern = null;

    @Option(name = { "-t",
            "--threads" }, title = "NumThreads", arity = 1, description = "Sets the number of threads to use for data generation (default 1) which can speed up generating data for larger numbers of universities")
    private int threads = 1;

    @Option(name = { "--compress" }, description = "When set output files are automatically compressed with GZip")
    private boolean compress = false;

    @Option(name = {
            "--consolidate" }, description = "When set each university generates a single output file rather than an output file per university department")
    private boolean consolidate = false;

    @Option(name = {
            "--timing" }, description = "When set outputs the elapsed time at the end of the generation process")
    private boolean timing = false;

    @Inject
    private HelpOption<Launcher> help;

    public static void main(String[] args) {
        SingleCommand<Launcher> parser = SingleCommand.singleCommand(Launcher.class);
        try {
            Launcher launcher = parser.parse(args);

            // Show help if requested
            if (launcher.help.showHelpIfRequested()) {
                System.exit(1);
            }

            // Configure logging
            BasicConfigurator.configure();
            Logger.getRootLogger().removeAllAppenders();
            if (launcher.logFile == null) {
                launcher.logFile = DEFAULT_LOG_FILE;
            }
            // Ensure a log pattern and ensure it includes the message
            String pattern = StringUtils.isBlank(launcher.logPattern) ? PatternLayout.TTCC_CONVERSION_PATTERN
                    : launcher.logPattern;
            if (!pattern.contains("%m"))
                pattern += " %m%n";
            System.out.println(String.format("Logging to %s with pattern %s", launcher.logFile, pattern));
            Logger.getRootLogger().addAppender(new FileAppender(new PatternLayout(pattern), launcher.logFile));

            // Run the generator
            Generator generator = new Generator();
            long start = System.currentTimeMillis();
            generator.start(launcher.univNum, launcher.startIndex, launcher.seed, launcher.format, launcher.ontology,
                    launcher.workDir, launcher.consolidate, launcher.compress, launcher.threads);
            long elapsed = System.currentTimeMillis() - start;

            if (launcher.timing) {
                Duration duration = Duration.millis(elapsed);
                System.out.print("Took ");
                System.out.print(PeriodFormat.getDefault().print(duration.toPeriod()));
                System.out.println(" to generate data");
                org.slf4j.Logger log = LoggerFactory.getLogger(Launcher.class);
                log.info("Took {} to generate data", PeriodFormat.getDefault().print(duration.toPeriod()));
            }

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            System.exit(3);
        }

        // If we got here everything worked OK
        System.exit(0);
    }
}
