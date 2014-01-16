package org.asoem.greyfish.cli;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import joptsimple.*;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.io.SimulationLoggers;
import org.asoem.greyfish.core.model.Experiment;
import org.asoem.greyfish.core.model.ModelParameterTypeListener;
import org.asoem.greyfish.utils.Resources;
import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static java.util.Arrays.asList;

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
    private static final ArgumentAcceptingOptionSpec<Integer> COMMIT_THRESHOLD_SPEC =
            OPTION_PARSER.accepts("ct", "Commit threshold for JDBC batch operations")
                    .withRequiredArg().ofType(int.class).defaultsTo(1000);

    private static final Closer CLOSER = Closer.create();

    private GreyfishCLIApplication() {
    }

    private static Module createCommandLineModule(final OptionSet optionSet) {

        return new AbstractModule() {
            @SuppressWarnings("unchecked")
            @Override
            protected void configure() {

                if (optionSet.nonOptionArguments().size() != 1) {
                    exitWithErrorMessage("Missing CLASS argument");
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
                    if (!Experiment.class.isAssignableFrom(modelClass)) {
                        exitWithErrorMessage("Specified Class does not implement " + Experiment.class);
                    }
                    bind(Experiment.class).to((Class<Experiment>) modelClass);
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

                // TODO: One should be able to define the database url independent of the working directory
                final String pathname = optionSet.valueOf(WORKING_DIRECTORY_OPTION_SPEC) + "/"
                        + optionSet.valueOf(SIMULATION_NAME_OPTION_SPEC);
                final String path = Files.simplifyPath(pathname);

                try {
                    final GreyfishH2ConnectionManager connectionSupplier =
                            GreyfishH2ConnectionManager.create(path);
                    SimulationLogger jdbcLogger = SimulationLoggers.createJDBCLogger(connectionSupplier, optionSet.valueOf(COMMIT_THRESHOLD_SPEC));

                    CLOSER.register(connectionSupplier);
                    CLOSER.register(jdbcLogger); // Must be closed before the connection (put on stack after the connection)

                    bind(SimulationLogger.class).toInstance(jdbcLogger);
                } catch (Exception e) {
                    exitWithErrorMessage("Unable to create new database: ", e);
                }
            }

        };
    }

    public static void main(final String[] args) {

        final Optional<String> commitHash = getCommitHash(GreyfishCLIApplication.class);
        if (commitHash.isPresent()) {
            LOGGER.debug("Git Commit Hash for current Jar: %s", commitHash.get());
        }

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
            final EventBus eventBus = new EventBus();
            final Module coreModule = new CoreModule(randomGenerator, eventBus);

            final Injector injector = Guice.createInjector(
                    coreModule,
                    commandLineModule
            );

            final ExperimentExecutionService experimentExecutionService =
                    injector.getInstance(ExperimentExecutionService.class);

            if (!optionSet.has(QUIET_OPTION_SPEC)) {
                final ExperimentMonitorService monitorService =
                        new ExperimentMonitorService(experimentExecutionService.getExperiment(), System.out, optionSet.valueOf(STEPS_OPTION_SPEC), eventBus);

                monitorService.addListener(new Service.Listener() {
                    @Override
                    public void starting() {
                    }

                    @Override
                    public void running() {
                    }

                    @Override
                    public void stopping(final Service.State from) {
                    }

                    @Override
                    public void terminated(final Service.State from) {
                    }

                    @Override
                    public void failed(final Service.State from, final Throwable failure) {
                        LOGGER.error("Monitor service failed", failure);
                    }
                }, MoreExecutors.sameThreadExecutor());

                experimentExecutionService.addListener(new Service.Listener() {
                    @Override
                    public void starting() {
                        monitorService.startAsync().awaitRunning();
                    }

                    @Override
                    public void running() {
                    }

                    @Override
                    public void stopping(final Service.State from) {
                    }

                    @Override
                    public void terminated(final Service.State from) {
                        monitorService.stopAsync();
                    }

                    @Override
                    public void failed(final Service.State from, final Throwable failure) {
                        monitorService.stopAsync();
                    }
                }, MoreExecutors.sameThreadExecutor());
            }

            // start getSimulation
            experimentExecutionService.startAsync();

            // stop getSimulation on shutdown request (^C)
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (experimentExecutionService.isRunning()) {
                        experimentExecutionService.stopAsync().awaitTerminated();
                    }
                }
            });

            try {
                experimentExecutionService.awaitTerminated();
            } catch (IllegalStateException e) {
                exitWithErrorMessage("Simulation execution failed", e);
            }
        } catch (OptionException e) {
            exitWithErrorMessage("Failed parsing options: ", e, true);
        } catch (Throwable e) {
            exitWithErrorMessage(String.format("Exception during simulation execution: %s\n"
                    + "Check log file for a stack trace.",
                    e.getCause() != null ? e.getCause().getMessage() : e.getMessage()), e);
        }

        System.exit(0);
    }

    private static void exitWithErrorMessage(final String message) {
        exitWithErrorMessage(message, false);
    }

    private static void exitWithErrorMessage(final String message, final boolean printHelp) {
        exitWithErrorMessage(message, null, printHelp);
    }

    private static void exitWithErrorMessage(final String message, final Throwable throwable) {
        exitWithErrorMessage(message, throwable, false);
    }

    private static void exitWithErrorMessage(final String message, @Nullable final Throwable throwable, final boolean printHelp) {
        assert message != null;

        LOGGER.error(message, throwable);

        final StringBuilder messageBuilder = new StringBuilder();
        messageBuilder
                .append("ERROR: ")
                .append(message);
        if (throwable != null) {
            messageBuilder
                    .append(" Exception: ")
                    .append(throwable.getMessage());
        }

        System.out.println(messageBuilder.toString());

        if (printHelp) {
            printHelp(OPTION_PARSER);
        }

        System.exit(1);
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
        public String first() {
            return key;
        }

        @Override
        public String second() {
            return value;
        }
    }

    private static Optional<String> getCommitHash(final Class<?> clazz) {
        try {
            final JarFile jarFile = Resources.getJarFile(clazz);
            final Manifest manifest = jarFile.getManifest();
            final Attributes attr = manifest.getMainAttributes();
            return Optional.of(attr.getValue("Git-Commit-Hash"));
        } catch (IOException e) {
            throw new IOError(e);
        } catch (UnsupportedOperationException e) {
            return Optional.absent();
        }
    }
}
