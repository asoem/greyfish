/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.cli;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.inject.*;
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
import org.asoem.greyfish.core.model.ModelParameters;
import org.asoem.greyfish.impl.io.GreyfishH2ConnectionManager;
import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.io.Resources;
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
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static java.util.Arrays.asList;

public final class GreyfishCLIApplication {

    private static final Logger logger = LoggerFactory.getLogger(GreyfishCLIApplication.class);
    private static final OptionParser optionParser = new OptionParser();
    private static final OptionSpecBuilder helpOptionSpec =
            optionParser.acceptsAll(asList("h", "?"), "Print this help");
    private static final ArgumentAcceptingOptionSpec<String> classpathOptionSpec =
            optionParser.acceptsAll(asList("cp", "classpath"), "add to classpath (where model classes can be found)")
                    .withRequiredArg().ofType(String.class);
    /* private static final OptionSpecBuilder reproducibleModeOptionSpec =
            optionParser.accepts("R", "Reproducible mode. Sets the seed of the Pseudo Random Generator to 0"); */
    private static final ArgumentAcceptingOptionSpec<String> simulationNameOptionSpec =
            optionParser.acceptsAll(asList("n", "name"), "Set simulation name")
                    .withRequiredArg().ofType(String.class);
    private static final ArgumentAcceptingOptionSpec<Integer> parallelizationThresholdOptionSpec =
            optionParser.accepts("pt", "Set parallelization threshold")
                    .withRequiredArg().ofType(Integer.class).defaultsTo(1000);
    private static final ArgumentAcceptingOptionSpec<ModelParameterOptionValue> modelParameterOptionSpec =
            optionParser.accepts("D", "set model parameter for given model class")
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
    private static final ArgumentAcceptingOptionSpec<String> workingDirectoryOptionSpec =
            optionParser.accepts("w", "Set working directory").withOptionalArg().ofType(String.class).defaultsTo("./");
    private static final OptionSpecBuilder quietOptionSpec =
            optionParser.accepts("q", "Be quiet. Don't print progress information");
    private static final ArgumentAcceptingOptionSpec<Integer> commitThresholdSpec =
            optionParser.accepts("ct", "Commit threshold for JDBC batch operations")
                    .withRequiredArg().ofType(int.class).defaultsTo(1000);

    private static final Closer closer = Closer.create();

    private GreyfishCLIApplication() {}

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
                if (optionSet.has(classpathOptionSpec)) {
                    for (String classPath : optionSet.valuesOf(classpathOptionSpec)) {
                        try {
                            final File file = new File(classPath);
                            if (!file.canRead()) {
                                exitWithErrorMessage("Specified classpath is not readable: " + classPath);
                            }

                            classLoader = URLClassLoader.newInstance(
                                    new URL[]{file.toURI().toURL()},
                                    classLoader
                            );
                        } catch (MalformedURLException e) {
                            throw new AssertionError(e);
                        }
                    }
                }

                try {
                    final Class<?> experimentClass = Class.forName(modelClassName, true, classLoader);
                    if (!Experiment.class.isAssignableFrom(experimentClass)) {
                        exitWithErrorMessage("Specified Class does not implement " + Experiment.class);
                    }
                    bind(Experiment.class).to((Class<Experiment>) experimentClass);
                } catch (ClassNotFoundException e) {
                    logger.error("Unable to load class {}", modelClassName, e);
                    exitWithErrorMessage("Could not find class " + modelClassName);
                }

                if (optionSet.has(modelParameterOptionSpec)) {
                    final Map<String, String> properties = Maps.newHashMap();
                    final List<ModelParameterOptionValue> modelParameterOptionValues =
                            optionSet.valuesOf(modelParameterOptionSpec);
                    for (final ModelParameterOptionValue modelParameterOption : modelParameterOptionValues) {
                        if (properties.containsKey(modelParameterOption.key)) {
                            logger.warn("Model parameter {} was defined twice. Overwriting value {} with {}",
                                    modelParameterOption.key, properties.get(modelParameterOption.key),
                                    modelParameterOption.value);
                        }
                        properties.put(modelParameterOption.key, modelParameterOption.value);
                    }
                    bindListener(Matchers.any(), new ModelParameterTypeListener(properties));
                    ModelParameters.bind(binder(), properties);
                }

                bind(Boolean.class).annotatedWith(Names.named("quiet"))
                        .toProvider(Providers.of(optionSet.has(quietOptionSpec)));
                bind(Integer.class).annotatedWith(Names.named("parallelizationThreshold"))
                        .toInstance(optionSet.valueOf(parallelizationThresholdOptionSpec));


                // TODO: Move logger definition to experiment
                final String pathname = optionSet.valueOf(workingDirectoryOptionSpec) + "/"
                        + optionSet.valueOf(simulationNameOptionSpec);
                final String path = Files.simplifyPath(pathname);

                final Provider<SimulationLogger> loggerProvider = new Provider<SimulationLogger>() {
                    @Override
                    public SimulationLogger get() {
                        final GreyfishH2ConnectionManager connectionSupplier =
                                GreyfishH2ConnectionManager.create(path,
                                        GreyfishH2ConnectionManager.defaultInitSql(),
                                        GreyfishH2ConnectionManager.defaultFinalizeSql());
                        final SimulationLogger jdbcLogger = SimulationLoggers.createJDBCLogger(
                                connectionSupplier, optionSet.valueOf(commitThresholdSpec));

                        closer.register(connectionSupplier);
                        // Logger must be closed before the connection (put on stack after the connection)
                        closer.register(jdbcLogger);

                        return jdbcLogger;
                    }
                };

                bind(SimulationLogger.class)
                        .toProvider(loggerProvider)
                        .in(Scopes.SINGLETON);
            }

        };
    }

    public static void main(final String[] args) {

        final Optional<String> commitHash = getCommitHash(GreyfishCLIApplication.class);
        if (commitHash.isPresent()) {
            logger.debug("Git Commit Hash for current Jar: %s", commitHash.get());
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    closer.close();
                } catch (IOException e) {
                    logger.warn("Exception while closing resources", e);
                }
            }
        });

        try {
            final OptionSet optionSet = optionParser.parse(args);

            if (optionSet.has(helpOptionSpec)) {
                printHelp();
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
            final EventBus eventBus = new EventBus(new SubscriberExceptionHandler() {
                @Override
                public void handleException(final Throwable exception, final SubscriberExceptionContext context) {
                    context.getEventBus().post(new AssertionError("The EventBus could not dispatch event: "
                            + context.getSubscriber() + " to " + context.getSubscriberMethod(),
                            exception.getCause()));
                }
            });
            final Module coreModule = new CoreModule(randomGenerator, eventBus);

            final Injector injector = Guice.createInjector(
                    coreModule,
                    commandLineModule
            );

            final ExperimentExecutionService experimentExecutionService =
                    injector.getInstance(ExperimentExecutionService.class);

            if (!optionSet.has(quietOptionSpec)) {
                final ExperimentMonitorService monitorService =
                        new ExperimentMonitorService(
                                System.out, eventBus);

                monitorService.addListener(new Service.Listener() {
                    @Override
                    public void starting() {}

                    @Override
                    public void running() {}

                    @Override
                    public void stopping(final Service.State from) {}

                    @Override
                    public void terminated(final Service.State from) {}

                    @Override
                    public void failed(final Service.State from, final Throwable failure) {
                        logger.error("Monitor service failed", failure);
                    }
                }, MoreExecutors.sameThreadExecutor());

                experimentExecutionService.addListener(
                        new MonitorServiceController(monitorService),
                        MoreExecutors.sameThreadExecutor());
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

    private static void exitWithErrorMessage(final String message, @Nullable final Throwable throwable,
                                             final boolean printHelp) {
        assert message != null;

        logger.error(message, throwable);

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
            printHelp();
        }

        System.exit(1);
    }

    private static void printHelp() {
        System.out.println("Usage: greyfish [Options] <ModelClass>");
        try {
            optionParser.printHelpOn(System.out);
        } catch (IOException e) {
            logger.error("Error while writing to System.out", e);
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

    private static class MonitorServiceController extends Service.Listener {
        private final ExperimentMonitorService monitorService;

        public MonitorServiceController(final ExperimentMonitorService monitorService) {
            this.monitorService = monitorService;
        }

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
    }
}
