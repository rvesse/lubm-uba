package edu.lehigh.swat.bench.uba;

import javax.inject.Inject;

import com.github.rvesse.airline.Command;
import com.github.rvesse.airline.HelpOption;
import com.github.rvesse.airline.Option;
import com.github.rvesse.airline.SingleCommand;
import com.github.rvesse.airline.parser.ParseException;

@Command(name = "generate.sh", description = "Artificial Data Generator for the Lehigh University Benchmark (LUBM) for SPARQL query engines", exitCodes = {
        0, 1, 2, 3 }, exitDescriptions = { "Data was generated successfully", "Help was displayed", "Invalid arguments",
                "Error during data generation" })
public class Launcher {

    @Option(name = { "-u",
            "--univ" }, title = "NumberOfUniversities", arity = 1, description = "Sets the number of universities to generate data for (default 1)")
    private int univNum = 1;

    @Option(name = { "-i",
            "--index" }, title = "StartingIndex", arity = 1, description = "Starting index of the universities (default 0)")
    private int startIndex = 0;

    @Option(name = { "-s",
            "--seed" }, title = "Seed", arity = 1, description = "Seed used for random data generation (default 0)")
    private int seed = 0;

    @Option(name = { "--daml" }, description = "Generate DAML+OIL data instead of OWL data")
    private boolean daml = false;

    @Option(name = { "--onto",
            "--ontology" }, title = "OntologyUrl", description = "URL for the benchmark ontology used as the base URL in the generated data (default http://swat.cse.lehigh.edu/onto/univ-bench.owl)")
    private String ontology = "http://swat.cse.lehigh.edu/onto/univ-bench.owl";

    @Option(name = { "-o",
            "--output" }, title = "OutputDirectory", description = "Sets the output directory to which generated files are written")
    private String workDir = null;
    
    @Inject
    private HelpOption help;

    public static void main(String[] args) {
        SingleCommand<Launcher> parser = SingleCommand.singleCommand(Launcher.class);
        try {
            Launcher launcher = parser.parse(args);
            
            // Show help if requested
            if (launcher.help.showHelpIfRequested()) {
                System.exit(1);
            }
            
            // Run the generator
            Generator generator = new Generator();
            generator.start(launcher.univNum, launcher.startIndex, launcher.seed, launcher.daml, launcher.ontology, launcher.workDir);
            
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
