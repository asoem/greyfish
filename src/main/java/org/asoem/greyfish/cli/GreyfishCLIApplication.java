package org.asoem.greyfish.cli;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.commons.cli.*;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.simulation.ParallelizedSimulationFactory;
import org.asoem.greyfish.core.simulation.SimulationTemplate;
import org.asoem.greyfish.core.simulation.Simulations;
import org.asoem.greyfish.models.SimulationTemplateFactory;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.asoem.greyfish.cli.GreyfishCLIApplication.State.*;

/**
 * User: christoph
 * Date: 30.05.12
 * Time: 10:36
 */
public class GreyfishCLIApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreyfishCLIApplication.class);

    private GreyfishCLIApplication.State state = STARTUP;

    @Inject
    private GreyfishCLIApplication(SimulationTemplateFactory simulationTemplateFactory,
                                   @Nullable @Named("steps") final Integer steps,
                                   @Named("verbose") final boolean verbose,
                                   @Named("parallelizationThreshold") int parallelizationThreshold) {
        final List<Predicate<ParallelizedSimulation>> predicateList = Lists.newArrayList();

        if (steps != null) {
            predicateList.add(new Predicate<ParallelizedSimulation>() {
                @Override
                public boolean apply(ParallelizedSimulation parallelizedSimulation) {
                    return parallelizedSimulation.getStep() < steps;
                }
            });
        }

        LOGGER.info("Creating simulation for model {}", simulationTemplateFactory);
        final ParallelizedSimulationFactory simulationFactory = new ParallelizedSimulationFactory(parallelizationThreshold);
        final SimulationTemplate simulationTemplate = simulationTemplateFactory.get();
        assert simulationTemplate != null;
        final ParallelizedSimulation simulation = simulationTemplate.createSimulation(simulationFactory);

        if (verbose) {
            final Runnable simulationMonitorTask = new Runnable() {
                @Override
                public void run() {
                    File verboseFile = null;
                    PrintWriter writer = null;
                    try {
                        verboseFile = File.createTempFile("greyfish_verbose_", ".txt");
                        writer = new PrintWriter(new BufferedWriter(new FileWriter(verboseFile)));
                        LOGGER.info("Writing verbose output to file {}", verboseFile.getAbsolutePath());
                        //writer = new PrintWriter(System.out, true);
                        while (state == STARTUP)
                            Thread.sleep(10);

                        while (state == RUNNING) {
                            writer.println(simulation.getStep() + " - " + simulation.countAgents());
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error("Simulation polling thread got interrupted");
                    } catch (IOException e) {
                        LOGGER.error("Could not write verbose output to file {}", verboseFile, e);
                    } finally {
                        Closeables.closeQuietly(writer);
                    }
                }
            };
            Executors.newSingleThreadExecutor().execute(simulationMonitorTask);
        }

        LOGGER.info("Model properties:\n\t{}", Joiner.on("\n\t").withKeyValueSeparator("=").join(simulationTemplateFactory.getModelProperties()));
        LOGGER.info("Starting {}", simulation);

        final Runnable simulationTask = new Runnable() {
            @Override
            public void run() {
                Simulations.runWhile(simulation, Predicates.and(predicateList));
            }
        };

        final Future<?> future = Executors.newSingleThreadExecutor().submit(simulationTask);

        state = RUNNING;

        try {
            future.get();
        } catch (InterruptedException e) {
            LOGGER.error("Simulation thread got interrupted", e);
        } catch (ExecutionException e) {
            LOGGER.error("Exception occurred while executing simulation", e);
        }
        finally {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("Shutting down simulation {}", simulation);
            simulation.shutdown();
            state = SHUTDOWN;
        }
    }

    public static void main(final String[] args) {
        try {
            final CommandLineParser parser = new PosixParser();
            final Options options = createOptions();
            final CommandLine line = parser.parse(options, args);
            processCommandLine(line, options);
        } catch (ParseException e) {
            System.out.println("Unexpected exception:" + e.getMessage());
            System.exit(1);
        }
    }

    private static void processCommandLine(final CommandLine line, final Options options) {
        if (line.hasOption("help")) {
            usage("", options);
            System.exit(0);
        }

        final AbstractModule commandLineModule = new AbstractModule() {
            @SuppressWarnings("unchecked")
            @Override
            protected void configure() {
                if (line.hasOption("scenario")) {
                    try {
                        final Class<? extends SimulationTemplateFactory> modelFactoryClass =
                                (Class<? extends SimulationTemplateFactory>) Class.forName(line.getOptionValue("scenario"));
                        bind(SimulationTemplateFactory.class).to(modelFactoryClass);
                    } catch (ClassNotFoundException e) {
                        System.err.println("Could not find class " + line.getOptionValue("scenario"));
                        System.exit(1);
                    }
                }
                else {
                    usage("Option --scenario is mandatory", options);
                    System.exit(1);
                }

                ModelParameters.bindProperties(binder(), line.getOptionProperties("D"));

                bind(Integer.class).annotatedWith(Names.named("steps")).toInstance(
                        line.hasOption("steps") ? Integer.valueOf(line.getOptionValue("steps")) : null);

                bind(Boolean.class).annotatedWith(Names.named("verbose")).toInstance(
                        line.hasOption("verbose"));

                bind(Integer.class).annotatedWith(Names.named("parallelizationThreshold")).toInstance(
                        line.hasOption("parallelizationThreshold") ? Integer.valueOf(line.getOptionValue("parallelizationThreshold")) : 1000);
            }
        };

        Guice.createInjector(
                new CoreModule(),
                commandLineModule)
                .getInstance(GreyfishCLIApplication.class);
    }

    private static void usage(String s, Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        System.out.println(s);
        formatter.printHelp( GreyfishCLIApplication.class.getSimpleName(), options );
    }

    @SuppressWarnings("AccessStaticViaInstance")
    private static Options createOptions() {
        final Option scenarioClass = OptionBuilder
                .withLongOpt("scenario")
                .hasArg()
                .withArgName("CLASS")
                .withDescription("run given scenario CLASS")
                .create("c");

        final Option steps = OptionBuilder
                .withLongOpt("steps")
                .hasArg()
                .withArgName("MAX")
                .withDescription("stop simulation after MAX steps")
                .create("s");

        final Option verbose = OptionBuilder
                .withLongOpt("verbose")
                .withDescription("Enable verbose mode")
                .create("v");

        final Option property  = OptionBuilder
                .withArgName("property=value")
                .hasArgs(2)
                .withValueSeparator()
                .withDescription( "use value for given scenario-property" )
                .create( "D" );

        final Option parallelizationThreshold = OptionBuilder
                .withLongOpt("parallelizationThreshold")
                .withArgName( "THRESHOLD" )
                .hasArg()
                .withDescription("")
                .withType(Number.class)
                .create("pt");

        final Option help = new Option("h", "help", false, "Print help");

        final Options options = new Options();
        options.addOption(scenarioClass);
        options.addOption(steps);
        options.addOption(help);
        options.addOption(property);
        options.addOption(verbose);
        options.addOption(parallelizationThreshold);
        return options;
    }

    public enum State {
        STARTUP,
        RUNNING,
        SHUTDOWN
    }
}
