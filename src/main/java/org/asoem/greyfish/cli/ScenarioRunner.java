package org.asoem.greyfish.cli;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.commons.cli.*;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.simulation.ParallelizedSimulationFactory;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * User: christoph
 * Date: 30.05.12
 * Time: 10:36
 */
public class ScenarioRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioRunner.class);

    @Inject
    private ScenarioRunner(Scenario scenario, @Nullable @Named("steps") final Integer steps, @Named("verbose") final boolean verbose) {

        final List<Predicate<ParallelizedSimulation>> predicateList = Lists.newArrayList();

        if (steps != null) {
            predicateList.add(new Predicate<ParallelizedSimulation>() {
                @Override
                public boolean apply(ParallelizedSimulation parallelizedSimulation) {
                    return parallelizedSimulation.getStep() < steps;
                }
            });
        }

        final ParallelizedSimulation simulation = scenario.createSimulation(ParallelizedSimulationFactory.INSTANCE);

        if (verbose) {
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            System.out.println(simulation.getStep() + " - " + simulation.countAgents());
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        LOGGER.warn("Simulation polling thread got interrupted");
                    }
                }
            });
        }
        LOGGER.info("Starting {} of scenario {}", simulation, scenario);

        simulation.runWhile(Predicates.and(predicateList));
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Shutting down {}", simulation);
        simulation.shutdown();
    }

    public static void main(final String[] args) {
        try {
            final CommandLineParser parser = new PosixParser();
            final Options options = createOptions();
            final CommandLine line = parser.parse(options, args);
            processCommandLine(line, options);
            System.exit(0);
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
                        final Class<? extends Provider<Scenario>> scenario =
                                (Class<? extends Provider<Scenario>>) Class.forName(line.getOptionValue("scenario"));
                        bind(Scenario.class).toProvider(scenario);
                    } catch (ClassNotFoundException e) {
                        System.err.println("Could not find class " + line.getOptionValue("scenario"));
                        System.exit(1);
                    }
                }
                else {
                    usage("Option --scenario is mandatory", options);
                    System.exit(1);
                }

                ScenarioParameters.bindProperties(binder(), line.getOptionProperties("D"));

                bind(Integer.class).annotatedWith(Names.named("steps")).toInstance(
                        line.hasOption("steps") ? Integer.valueOf(line.getOptionValue("steps")) : null);

                bind(Boolean.class).annotatedWith(Names.named("verbose")).toInstance(line.hasOption("verbose"));
            }
        };

        Guice.createInjector(
                new CoreModule(),
                commandLineModule)
                .getInstance(ScenarioRunner.class);
    }

    private static void usage(String s, Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        System.out.println(s);
        formatter.printHelp( ScenarioRunner.class.getSimpleName(), options );
    }

    @SuppressWarnings("AccessStaticViaInstance")
    private static Options createOptions() {
        final Option scenarioClass = OptionBuilder.withLongOpt("scenario")
                .hasArg()
                .withArgName("CLASS")
                .withDescription("run given scenario CLASS")
                .create("c");

        final Option steps = OptionBuilder.withLongOpt("steps")
                .hasArg()
                .withArgName("MAX")
                .withDescription("stop simulation after MAX steps")
                .create("s");

        final Option verbose = OptionBuilder.withLongOpt("verbose")
                .withDescription("Enable verbose mode")
                .create("v");

        final Option property  = OptionBuilder.withArgName( "property=value" )
                .hasArgs(2)
                .withValueSeparator()
                .withDescription( "use value for given scenario-property" )
                .create( "D" );

        final Option help = new Option("h", "help", false, "Print help");

        final Options options = new Options();
        options.addOption(scenarioClass);
        options.addOption(steps);
        options.addOption(help);
        options.addOption(property);
        options.addOption(verbose);
        return options;
    }
}
