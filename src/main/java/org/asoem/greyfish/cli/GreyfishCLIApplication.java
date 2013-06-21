package org.asoem.greyfish.cli;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import com.google.inject.*;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.io.H2Logger;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.io.SimulationLoggers;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.simulation.SimulationModel;
import org.asoem.greyfish.core.simulation.Simulations;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
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
    private GreyfishCLIApplication(final SimulationModel<?> model,
                                   @Named("steps") final int steps,
                                   @Nullable @Named("verbose") final String verbose,
                                   @Named("parallelizationThreshold") final int parallelizationThreshold) {
        final List<Predicate<Simulation<?>>> predicateList = Lists.newArrayList();

        predicateList.add(new Predicate<Simulation<?>>() {
            @Override
            public boolean apply(Simulation<?> parallelizedSimulation) {
                return parallelizedSimulation.getStep() < steps;
            }
        });

        LOGGER.info("Creating simulation for model {}", model.getClass());
        LOGGER.info("Model parameters after injection: {}",
                Joiner.on(", ").withKeyValueSeparator("=").useForNull("null").join(ModelParameters.extract(model)));

        final Simulation<?> simulation = model.createSimulation();

        if (verbose != null) {
            startSimulationMonitor(simulation, verbose, steps);
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

    private void startSimulationMonitor(final Simulation<?> simulation, final String verbose, final int steps) {
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

        final Closer closer = Closer.create();
        final PrintWriter writer = closer.register(new PrintWriter(outputStreamWriter, true));

        final Runnable simulationMonitorTask = new Runnable() {
            @Override
            public void run() {

                try {
                    while (state == State.STARTUP)
                        Thread.sleep(10);

                    while (state == State.RUNNING) {
                        //writer.println(simulation.getStep() + " - " + simulation.countAgents());
                        final double progress = (double) simulation.getStep() / steps;
                        final String progressBar = String.format("\r[%s%s] %d%% (%d/%d) (%d active agents)",
                                Strings.repeat("#", (int)(progress * 10)),
                                Strings.repeat(" ", 10 - (int)(progress * 10)),
                                (int)(progress * 100),
                                simulation.getStep(),
                                steps,
                                simulation.countAgents());
                        writer.print(progressBar);
                        writer.flush();
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("Simulation polling thread got interrupted");
                } finally {
                    try {
                        writer.println("\r" + Strings.repeat(" ", 200));
                        closer.close();
                    } catch (IOException e) {
                        LOGGER.warn("Closer.close() had errors", e);
                    }
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
            public void exit() {
                exitWithErrorMessage("");
            }

            @Override
            public void exitWithErrorMessage(String message) {
                System.out.println("ERROR: " + message);
                printHelp(optionParser);
                System.exit(1);
            }

            @Override
            public void exitWithErrorMessage(String message, Throwable throwable) {
                LOGGER.error(message, throwable);
                exitWithErrorMessage(message + throwable.getMessage());
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
                    optionExceptionHandler.exitWithErrorMessage("A single Model CLASS is required");
                }

                final String modelClassName = optionSet.nonOptionArguments().get(0);

                ClassLoader classLoader = GreyfishCLIApplication.class.getClassLoader();
                if (optionSet.has("classpath")) {
                    try {
                        final String pathname = (String) optionSet.valueOf("classpath");
                        final File file = new File(pathname);
                        if (!file.canRead())
                            optionExceptionHandler.exitWithErrorMessage("Specified classpath is not readable: " + pathname);

                        classLoader = URLClassLoader.newInstance(
                                new URL[]{file.toURI().toURL()},
                                classLoader
                        );
                    } catch (MalformedURLException e) {
                        throw new AssertionError(e);
                    }
                }

                try {
                    final Class<?> modelClass = Class.forName(modelClassName, true, classLoader);
                    if (!SimulationModel.class.isAssignableFrom(modelClass))
                        optionExceptionHandler.exitWithErrorMessage("Specified Class does not implement " + SimulationModel.class);
                    bind(new TypeLiteral<SimulationModel<?>>(){}).to((Class<SimulationModel<?>>) modelClass);
                } catch (ClassNotFoundException e) {
                    optionExceptionHandler.exitWithErrorMessage("Could not find class " + modelClassName);
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
                                optionExceptionHandler.exitWithErrorMessage(
                                        "Invalid model property definition (-D): " + s + ". Expected 'key=value'.");
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
                try {
                    final H2Logger<DefaultGreyfishAgent> h2Logger = H2Logger.create(optionSet.valueOf("db").toString());
                    bind(new TypeLiteral<SimulationLogger<DefaultGreyfishAgent>>(){})
                            .toInstance(SimulationLoggers.synchronizedLogger(
                                    h2Logger));
                } catch (Exception e) {
                    optionExceptionHandler.exitWithErrorMessage("Unable to create new database: ", e);
                }
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
        parser.accepts("db", "Set database path")
                .withRequiredArg().ofType(String.class);
        parser.accepts("R", "Reproducible mode. Sets the seed of the Pseudo Random Generator to 0");
        parser.accepts("classpath", "add to classpath (where model classes can be found)")
                .withRequiredArg().ofType(String.class);
        parser.acceptsAll(asList("h", "?"), "Print this help");

        return parser;
    }

    private static interface OptionExceptionHandler {
        void exit();
        void exitWithErrorMessage(String message);
        void exitWithErrorMessage(String message, Throwable throwable);
    }
}
