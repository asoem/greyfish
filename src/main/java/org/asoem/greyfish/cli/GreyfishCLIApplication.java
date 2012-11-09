package org.asoem.greyfish.cli;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.CloneFactory;
import org.asoem.greyfish.core.agent.FrozenAgent;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.io.H2Logger;
import org.asoem.greyfish.core.io.SimulationLoggers;
import org.asoem.greyfish.core.simulation.Model;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.simulation.ParallelizedSimulationFactory;
import org.asoem.greyfish.core.simulation.Simulations;
import org.asoem.greyfish.core.space.WalledTileSpace;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.Arrays.asList;

/**
 * User: christoph
 * Date: 30.05.12
 * Time: 10:36
 */
public final class GreyfishCLIApplication {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(GreyfishCLIApplication.class);

    private static enum State {
        STARTUP,
        RUNNING,
        SHUTDOWN
    }

    private State state = State.STARTUP;

    @Inject
    private GreyfishCLIApplication(Model<?,?> model,
                                   @Named("steps") final int steps,
                                   @Nullable @Named("verbose") final String verbose,
                                   @Named("parallelizationThreshold") int parallelizationThreshold,
                                   @Named("databasePath") String dbPath) {
        final List<Predicate<ParallelizedSimulation>> predicateList = Lists.newArrayList();

        predicateList.add(new Predicate<ParallelizedSimulation>() {
            @Override
            public boolean apply(ParallelizedSimulation parallelizedSimulation) {
                return parallelizedSimulation.getStep() < steps;
            }
        });

        LOGGER.info("Creating simulation for model {}", model.getClass());
        LOGGER.info("Model parameters after injection: {}", Joiner.on(", ").withKeyValueSeparator("=").join(ModelParameters.asMap(model)));

        final ParallelizedSimulationFactory simulationFactory = new ParallelizedSimulationFactory<Agent, WalledTileSpace<Agent>>(parallelizationThreshold,
                SimulationLoggers.synchronizedLogger(new H2Logger(dbPath.replaceFirst("%\\{uuid\\}", UUID.randomUUID().toString()))), new CloneFactory<Agent>() {
            @Override
            public Agent cloneAgent(Agent prototype) {

                final Agent clone = DeepCloner.clone(prototype, Agent.class);

                return FrozenAgent.builder(prototype.getPopulation())
                        .addActions(clone.getActions())
                        .addProperties(clone.getProperties())
                        .addTraits(clone.getTraits())
                        .build();
            }
        });
        final ParallelizedSimulation<Agent, WalledTileSpace<Agent>> simulation = simulationFactory.createSimulation(model.createSpace(), model.createPrototypes());
        model.initialize(simulation);

        if (verbose != null) {
            startSimulationMonitor(simulation, verbose);
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

    private void startSimulationMonitor(final ParallelizedSimulation simulation, final String verbose) {
        OutputStream outputStream = null;
        try {
            if (verbose.equals("-")) {
                outputStream = System.out;
            }
            else {
                File verboseFile = new File(verbose);
                outputStream = new FileOutputStream(verboseFile);
                LOGGER.info("Writing verbose output to file {}", verboseFile.getAbsolutePath());
            }
        }  catch (IOException e) {
            LOGGER.error("Error while writing", e);
        }

        OutputStreamWriter outputStreamWriter;
        try {
            outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }

        final PrintWriter writer = new PrintWriter(new BufferedWriter(outputStreamWriter), true);

        final Runnable simulationMonitorTask = new Runnable() {
            @Override
            public void run() {
                try {
                    while (state == State.STARTUP)
                        Thread.sleep(10);

                    while (state == State.RUNNING) {
                        writer.println(simulation.getStep() + " - " + simulation.countAgents());
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("Simulation polling thread got interrupted");
                } finally {
                    Closeables.closeQuietly(writer);
                }
            }
        };

        Executors.newSingleThreadExecutor().execute(simulationMonitorTask);
    }

    public static void main(final String[] args) {
        final OptionParser optionParser = createOptionParser();
        final OptionSet optionSet = optionParser.parse(args);

        if (optionSet.has("h") || optionSet.has("?")) {
            printHelp(optionParser);
            System.exit(0);
        }

        final Module commandLineModule = createCommandLineModule(optionSet, new OptionExceptionHandler() {

            @Override
            public void exitWithError(@Nullable String message) {
                if (message != null)
                    System.out.println(message);
                printHelp(optionParser);
                System.exit(1);
            }
        });

        final RandomGenerator randomGenerator =
                optionSet.has("R") ? new Well19937c(0) : new Well19937c();

        Guice.createInjector(
                new CoreModule(randomGenerator),
                commandLineModule
        ).getInstance(GreyfishCLIApplication.class);

        System.exit(0);
    }

    private static void printHelp(OptionParser optionParser) {
        System.out.println("Usage: greyfish [Options] <ModelClass>");
        try {
            optionParser.printHelpOn(System.out);
        } catch (IOException e) {
            LOGGER.error("Error while writing to System.out", e);
        }
    }

    private static Module createCommandLineModule(final OptionSet optionSet, final OptionExceptionHandler optionExceptionHandler) {
        return new AbstractModule() {
            @SuppressWarnings("unchecked")
            @Override
            protected void configure() {

                if (optionSet.nonOptionArguments().size() != 1) {
                    optionExceptionHandler.exitWithError("A single Model CLASS is required");
                }

                final String modelClassName = optionSet.nonOptionArguments().get(0);

                try {
                    final Class<?> modelClass = Class.forName(modelClassName);
                    if (!Model.class.isAssignableFrom(modelClass))
                        optionExceptionHandler.exitWithError("Specified Class does not implement " + Model.class);
                    bind(Model.class).to((Class<Model>) modelClass);
                } catch (ClassNotFoundException e) {
                    optionExceptionHandler.exitWithError("Could not find class " + modelClassName);
                }

                if (optionSet.has("D")) {
                    final Map<String, String> properties = Maps.newHashMap();
                    for (Object s : optionSet.valuesOf("D")) {
                        if (String.class.isInstance(s)) {
                            final String[] split = ((String) s).split("=", 2);
                            if (split.length == 2) {
                                properties.put(split[0], split[1]);
                            }
                            else {
                                optionExceptionHandler.exitWithError("Invalid model property definition (-D): " + s + ". Expected 'key=value'.");
                            }
                        }
                    }

                    bindListener(Matchers.any(), new ModelParameterTypeListener(properties));
                }

                bind(Integer.class).annotatedWith(Names.named("steps"))
                        .toInstance((Integer) optionSet.valueOf("steps"));
                bind(String.class).annotatedWith(Names.named("verbose"))
                        .toProvider(Providers.of(optionSet.has("v") ? (String) optionSet.valueOf("v") : null));
                bind(Integer.class).annotatedWith(Names.named("parallelizationThreshold"))
                        .toInstance((Integer) optionSet.valueOf("pt"));
                bind(String.class).annotatedWith(Names.named("databasePath"))
                        .toInstance((String) optionSet.valueOf("db"));
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static OptionParser createOptionParser() {
        final OptionParser parser = new OptionParser();

        parser.accepts("steps", "stop simulation after MAX steps")
                .withRequiredArg().ofType(Integer.class);
        parser.accepts("v", "Write simulation status report to file or stout if no argument")
                .withRequiredArg().ofType(String.class).describedAs("file");
        parser.accepts("D", "set model parameter for given model class")
                .withRequiredArg().describedAs("key=value");
        parser.accepts("pt", "Set parallelization threshold")
                .withRequiredArg().ofType(Integer.class).defaultsTo(1000);
        parser.accepts("db", "Set database path (%(uuid) will be replaced by simulation uuid)")
                .withRequiredArg().defaultsTo("./%{uuid}").ofType(String.class);
        parser.accepts("R", "Reproducible mode. Sets the seed of the Pseudo Random Generator to 0");
        parser.acceptsAll(asList("h", "?"), "Print this help");

        return parser;
    }

    private static interface OptionExceptionHandler {
        void exitWithError(String message);
    }
}
