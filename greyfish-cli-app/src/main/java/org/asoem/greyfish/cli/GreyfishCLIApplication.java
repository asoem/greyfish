package org.asoem.greyfish.cli;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import com.google.common.util.concurrent.Monitor;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.inject.*;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import joptsimple.*;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.io.H2Logger;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.io.SimulationLoggers;
import org.asoem.greyfish.core.model.ModelParameterTypeListener;
import org.asoem.greyfish.core.model.SimulationModel;
import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * User: christoph
 * Date: 30.05.12
 * Time: 10:36
 */
public final class GreyfishCLIApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreyfishCLIApplication.class);
    private static final OptionParser OPTION_PARSER = new OptionParser();
    private static final OptionSpecBuilder HELP_OPTION_SPEC =
            OPTION_PARSER.acceptsAll(asList("h", "?"), "Print this help");
    private static final ArgumentAcceptingOptionSpec<String> CLASSPATH_OPTION_SPEC =
            OPTION_PARSER.acceptsAll(asList("cp", "classpath"), "add to classpath (where model classes can be found)")
                    .withRequiredArg().ofType(String.class);
    /* private static final OptionSpecBuilder REPRODUCIBLE_MODE_OPTION_SPEC =
            OPTION_PARSER.accepts("R", "Reproducible mode. Sets the seed of the Pseudo Random Generator to 0"); */
    private static final ArgumentAcceptingOptionSpec<String> SIMULATION_NAME_OPTION_SPEC =
            OPTION_PARSER.acceptsAll(asList("n", "name"), "Set simulation name")
                    .withRequiredArg().ofType(String.class);
    private static final ArgumentAcceptingOptionSpec<Integer> PARALLELIZATION_THRESHOLD_OPTION_SPEC =
            OPTION_PARSER.accepts("pt", "Set parallelization threshold")
                    .withRequiredArg().ofType(Integer.class).defaultsTo(1000);
    private static final ArgumentAcceptingOptionSpec<ModelParameterOptionValue> MODEL_PARAMETER_OPTION_SPEC =
            OPTION_PARSER.accepts("D", "set model parameter for given model class")
                    .withRequiredArg().withValuesConvertedBy(new ValueConverter<ModelParameterOptionValue>() {
                @Override
                public ModelParameterOptionValue convert(final String value) {
                    final String[] split = value.split("=", 2);
                    return new ModelParameterOptionValue(split[0], split[1]);
                }

                @Override
                public Class<ModelParameterOptionValue> valueType() {
                    return ModelParameterOptionValue.class;
                }

                @Override
                public String valuePattern() {
                    return "key=value";
                }
            });
    private static final ArgumentAcceptingOptionSpec<String> WORKING_DIRECTORY_OPTION_SPEC =
            OPTION_PARSER.accepts("w", "Set working directory").withOptionalArg().ofType(String.class).defaultsTo("./");
    private static final OptionSpecBuilder QUIET_OPTION_SPEC =
            OPTION_PARSER.accepts("q", "Be quiet. Don't print progress information");
    private static final ArgumentAcceptingOptionSpec<Integer> STEPS_OPTION_SPEC =
            OPTION_PARSER.acceptsAll(asList("s", "steps"), "stop simulation after MAX steps")
                    .withRequiredArg().ofType(Integer.class).required();

    private static final Closer CLOSER = Closer.create();

    private GreyfishCLIApplication() {}

    private static Module createCommandLineModule(final OptionSet optionSet) {

        return new AbstractModule() {
            @SuppressWarnings("unchecked")
            @Override
            protected void configure() {

                if (optionSet.nonOptionArguments().size() != 1) {
                    exitWithErrorMessage("A single Model CLASS is required");
                }

                final String modelClassName = optionSet.nonOptionArguments().get(0);

                ClassLoader classLoader = GreyfishCLIApplication.class.getClassLoader();
                if (optionSet.has(CLASSPATH_OPTION_SPEC)) {
                    try {
                        final String pathName = optionSet.valueOf(CLASSPATH_OPTION_SPEC);
                        final File file = new File(pathName);
                        if (!file.canRead()) {
                            exitWithErrorMessage("Specified classpath is not readable: " + pathName);
                        }

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
                        exitWithErrorMessage("Specified Class does not implement " + SimulationModel.class);
                    bind(new TypeLiteral<SimulationModel<?>>(){}).to((Class<SimulationModel<?>>) modelClass);
                } catch (ClassNotFoundException e) {
                    LOGGER.error("Unable to load class {}", modelClassName, e);
                    exitWithErrorMessage("Could not find class " + modelClassName);
                }

                if (optionSet.has(MODEL_PARAMETER_OPTION_SPEC)) {
                    final Map<String, String> properties = Maps.newHashMap();
                    for (final ModelParameterOptionValue modelParameterOption : optionSet.valuesOf(MODEL_PARAMETER_OPTION_SPEC)) {
                        if (properties.containsKey(modelParameterOption.key)) {
                            LOGGER.warn("Model parameter {} was defined twice. Overwriting value {} with {}",
                                    modelParameterOption.key, properties.get(modelParameterOption.key), modelParameterOption.value);
                        }
                        properties.put(modelParameterOption.key, modelParameterOption.value);
                    }
                    bindListener(Matchers.any(), new ModelParameterTypeListener(properties));
                }

                bind(Integer.class).annotatedWith(Names.named("steps"))
                        .toInstance(optionSet.valueOf(STEPS_OPTION_SPEC));
                bind(Boolean.class).annotatedWith(Names.named("quiet"))
                        .toProvider(Providers.of(optionSet.has(QUIET_OPTION_SPEC)));
                bind(Integer.class).annotatedWith(Names.named("parallelizationThreshold"))
                        .toInstance(optionSet.valueOf(PARALLELIZATION_THRESHOLD_OPTION_SPEC));

                final String pathname = optionSet.valueOf(WORKING_DIRECTORY_OPTION_SPEC) + "/"
                        + optionSet.valueOf(SIMULATION_NAME_OPTION_SPEC);

                final String path = Files.simplifyPath(pathname);
                try {
                    final SimulationLogger<DefaultGreyfishAgent> simulationLogger =
                            SimulationLoggers.synchronizedLogger(H2Logger.<DefaultGreyfishAgent>create(path));
                    CLOSER.register(simulationLogger);
                    bind(new TypeLiteral<SimulationLogger<DefaultGreyfishAgent>>(){})
                            .toInstance(simulationLogger);
                } catch (Exception e) {
                    exitWithErrorMessage("Unable to create new database: ", e);
                }
            }
        };
    }

    public static void main(final String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    CLOSER.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception while closing resources", e);
                }
            }
        });

        final OptionParser optionParser = OPTION_PARSER;

        try {
            final OptionSet optionSet = optionParser.parse(args);

            if (optionSet.has(HELP_OPTION_SPEC)) {
                printHelp(optionParser);
                System.exit(0);
            }

            final Module commandLineModule = createCommandLineModule(optionSet);
            final RandomGenerator randomGenerator = RandomGenerators.threadLocalGenerator(
                    new Supplier<RandomGenerator>() {
                        @Override
                        public RandomGenerator get() {
                            return new Well19937c();
                        }
                    });
            final Module coreModule = new CoreModule(randomGenerator);

            final Injector injector = Guice.createInjector(
                    coreModule,
                    commandLineModule
            );

            /* TODO: this monitor will be deprecated as soon as guava 15.0 is released.
                Services will have their own monitors */
            final Monitor monitor = new Monitor();

            final SimulationExecutionService simulationExecutionService =
                    injector.getInstance(SimulationExecutionService.class);
            simulationExecutionService.addListener(new Service.Listener() {
                @Override public void starting() {
                    monitor.enter();
                    monitor.leave();
                }
                @Override public void running() {}
                @Override public void stopping(final Service.State from) {}
                @Override public void terminated(final Service.State from) {
                    monitor.enter();
                    monitor.leave();
                }
                @Override public void failed(final Service.State from, final Throwable failure) {
                    monitor.enter();
                    monitor.leave();
                }
            }, MoreExecutors.sameThreadExecutor());

            if (!optionSet.has(QUIET_OPTION_SPEC)) {
                final SimulationMonitorService monitorService =
                        new SimulationMonitorService(simulationExecutionService.getSimulation(), System.out, optionSet.valueOf(STEPS_OPTION_SPEC));
                monitorService.addListener(new Service.Listener() {
                    @Override public void starting() {}
                    @Override public void running() {}
                    @Override public void stopping(final Service.State from) {}
                    @Override public void terminated(final Service.State from) {}
                    @Override public void failed(final Service.State from, final Throwable failure) {
                        LOGGER.error("Monitor service failed", failure);
                    }
                }, MoreExecutors.sameThreadExecutor());
                simulationExecutionService.addListener(new Service.Listener() {
                    @Override public void starting() {}
                    @Override public void running() {
                        monitorService.startAndWait();
                    }
                    @Override public void stopping(final Service.State from) {}
                    @Override public void terminated(final Service.State from) {
                        monitorService.stopAndWait();
                    }
                    @Override public void failed(final Service.State from, final Throwable failure) {
                        monitorService.stopAndWait();
                    }
                }, MoreExecutors.sameThreadExecutor());
            }


            // start simulation
            simulationExecutionService.startAndWait();

            // stop simulation on shutdown request (^C)
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (simulationExecutionService.isRunning()) {
                        simulationExecutionService.stopAndWait();
                    }
                }
            });

            // await termination
            monitor.enterWhenUninterruptibly(new Monitor.Guard(monitor) {
                @Override
                public boolean isSatisfied() {
                    return simulationExecutionService.state() == Service.State.TERMINATED
                            || simulationExecutionService.state() == Service.State.FAILED;
                }
            });
            monitor.leave();
        } catch (OptionException e) {
            LOGGER.error("Failed parsing options: {}", e);
            exitWithErrorMessage("Failed parsing options: ", e, true);
        } catch (Throwable e) {
            LOGGER.error("Exception during simulation execution", e);
            exitWithErrorMessage(String.format("Exception during simulation execution: %s.\n"
                    + "Check log file for detailed information",
                    e.getCause().getMessage()));
        }

        System.exit(0);
    }

    private static void exitWithErrorMessage(final String message) {
        exitWithErrorMessage(message, false);
    }

    private static void exitWithErrorMessage(final String message, final boolean printHelp) {
        System.out.println("ERROR: " + message);
        if (printHelp) {
            printHelp(OPTION_PARSER);
        }
        System.exit(1);
    }

    private static void exitWithErrorMessage(final String message, final Throwable throwable) {
        exitWithErrorMessage(message, throwable, false);
    }

    private static void exitWithErrorMessage(final String message, final Throwable throwable, final boolean printHelp) {
        LOGGER.error(message, throwable);
        exitWithErrorMessage(message + throwable.getMessage(), printHelp);
    }

    private static void printHelp(final OptionParser optionParser) {
        System.out.println("Usage: greyfish [Options] <ModelClass>");
        try {
            optionParser.printHelpOn(System.out);
        } catch (IOException e) {
            LOGGER.error("Error while writing to System.out", e);
        }
    }

    private static class ModelParameterOptionValue implements Product2<String, String> {

        private final String value;
        private final String key;

        private ModelParameterOptionValue(final String key, final String value) {
            assert key != null;
            assert value != null;

            this.key = key;
            this.value = value;
        }

        @Override
        public String _1() {
            return key;
        }

        @Override
        public String _2() {
            return value;
        }
    }
}
