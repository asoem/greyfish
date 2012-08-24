package org.asoem.greyfish.cli;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.simulation.ParallelizedSimulationFactory;
import org.asoem.greyfish.core.simulation.SimulationTemplate;
import org.asoem.greyfish.core.simulation.Simulations;
import org.asoem.greyfish.models.Model;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.Arrays.asList;

/**
 * User: christoph
 * Date: 30.05.12
 * Time: 10:36
 */
public class GreyfishCLIApplication {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(GreyfishCLIApplication.class);

    private static enum State {
        STARTUP,
        RUNNING,
        SHUTDOWN
    }

    private State state = State.STARTUP;

    @Inject
    private GreyfishCLIApplication(Model model,
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

        LOGGER.info("Creating simulation for model {}", model);
        final ParallelizedSimulationFactory simulationFactory = new ParallelizedSimulationFactory(parallelizationThreshold);
        final SimulationTemplate simulationTemplate = model.createTemplate();
        assert simulationTemplate != null;
        final ParallelizedSimulation simulation = simulationTemplate.createSimulation(simulationFactory);

        if (verbose) {
            startSimulationMonitor(simulation);
        }

        LOGGER.info("Starting {}", simulation);
        final Runnable simulationTask = new Runnable() {
            @Override
            public void run() {
                state = State.RUNNING;
                Simulations.runWhile(simulation, Predicates.and(predicateList));
            }
        };
        final Future<?> future = Executors.newSingleThreadExecutor().submit(simulationTask);
        try {
            future.get();
        } catch (InterruptedException e) {
            LOGGER.error("Simulation thread got interrupted", e);
        } catch (ExecutionException e) {
            LOGGER.error("Exception occurred while executing simulation", e);
        }
        finally {
            LOGGER.info("Shutting down simulation {}", simulation);
            simulation.shutdown();
            state = State.SHUTDOWN;
        }
    }

    private void startSimulationMonitor(final ParallelizedSimulation simulation) {
        final Runnable simulationMonitorTask = new Runnable() {
            @Override
            public void run() {
                File verboseFile = null;
                PrintWriter writer = null;
                try {
                    verboseFile = File.createTempFile("greyfish_verbose_", ".txt");
                    writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(verboseFile), "UTF-8")), true);
                    LOGGER.info("Writing verbose output to file {}", verboseFile.getAbsolutePath());
                    while (state == State.STARTUP)
                        Thread.sleep(10);

                    while (state == State.RUNNING) {
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

    public static void main(final String[] args) {
        // --steps 0 -pt 500 -v -DcompetitionKernelSigma=0.5 org.asoem.greyfish.models.EvolutionOfAssortativeMating

        final OptionParser optionParser = createOptionParser();
        final OptionSet optionSet = optionParser.parse(args);

        if (optionSet.has("h") || optionSet.has("?")) {
            printHelp(optionParser);
            System.exit(0);
        }

        final Module commandLineModule = createCommandLineModule(optionSet, new OptionExceptionHandler() {

            @Override
            public void handle(@Nullable String message) {
                if (message != null)
                    System.out.println(message);
                printHelp(optionParser);
                System.exit(1);
            }
        });

        Guice.createInjector(
                new CoreModule(),
                commandLineModule)
                .getInstance(GreyfishCLIApplication.class);

        System.exit(0);
    }

    private static void printHelp(OptionParser optionParser) {
        System.out.println("Usage: greyfish [Options] <ModelClass>");
        try {
            optionParser.printHelpOn(System.out);
        } catch (IOException e) {
            // NOP
        }
    }

    private static Module createCommandLineModule(final OptionSet optionSet, final OptionExceptionHandler optionExceptionHandler) {
        return new AbstractModule() {
            @SuppressWarnings("unchecked")
            @Override
            protected void configure() {

                if (optionSet.nonOptionArguments().size() != 1) {
                    optionExceptionHandler.handle("A single Model CLASS is required");
                }

                final String modelClassName = optionSet.nonOptionArguments().get(0);

                try {
                    final Class<? extends Model> modelFactoryClass =
                            (Class<? extends Model>) Class.forName(modelClassName);
                    bind(Model.class).to(modelFactoryClass);
                } catch (ClassNotFoundException e) {
                    optionExceptionHandler.handle("Could not find class " + modelClassName);
                }

                if (optionSet.has("D")) {
                    final Properties properties = new Properties();
                    try {
                        properties.load(new StringReader(Joiner.on("\n").join(optionSet.valuesOf("D"))));
                    } catch (IOException e) {
                        throw new IOError(e);
                    }
                    ModelParameters.bindProperties(binder(), properties);
                }

                bind(Integer.class).annotatedWith(Names.named("steps"))
                        .toInstance((Integer) optionSet.valueOf("steps"));
                bind(Boolean.class).annotatedWith(Names.named("verbose"))
                        .toInstance(optionSet.has("verbose"));
                bind(Integer.class).annotatedWith(Names.named("parallelizationThreshold"))
                        .toInstance((Integer) optionSet.valueOf("pt"));
                bind(File.class).annotatedWith(Names.named("database_dir"))
                        .toInstance((File) optionSet.valueOf("db"));
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static OptionParser createOptionParser() {
        final OptionParser parser = new OptionParser();

        parser.accepts("steps", "stop simulation after MAX steps").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
        parser.accepts("verbose", "Enable verbose mode").withOptionalArg().ofType(File.class);
        parser.accepts("D", "set model parameter for given model class").withRequiredArg().describedAs("key=value");
        parser.accepts("pt", "Set parallelization threshold").withRequiredArg().ofType(Integer.class).defaultsTo(1000);
        parser.accepts("db", "Set directory where database is written to").withRequiredArg().defaultsTo("./").ofType(File.class);
        parser.acceptsAll(asList("h", "?"), "Print this help");

        return parser;
    }

    private static interface OptionExceptionHandler {
        void handle(String message);
    }
}
