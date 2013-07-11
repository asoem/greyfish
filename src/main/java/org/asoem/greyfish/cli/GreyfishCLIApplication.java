package org.asoem.greyfish.cli;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
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
import org.asoem.greyfish.core.simulation.SimulationModel;
import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

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

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(GreyfishCLIApplication.class);
    private static final OptionParser OPTION_PARSER = new OptionParser();
    private static final OptionSpecBuilder HELP_OPTION_SPEC =
            OPTION_PARSER.acceptsAll(asList("h", "?"), "Print this help");
    private static final ArgumentAcceptingOptionSpec<String> CLASSPATH_OPTION_SPEC =
            OPTION_PARSER.acceptsAll(asList("cp", "classpath"), "add to classpath (where model classes can be found)")
            .withRequiredArg().ofType(String.class);
    private static final OptionSpecBuilder REPRODUCIBLE_MODE_OPTION_SPEC =
            OPTION_PARSER.accepts("R", "Reproducible mode. Sets the seed of the Pseudo Random Generator to 0");
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
                public ModelParameterOptionValue convert(String value) {
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
            }).describedAs("key=value");
    private static final ArgumentAcceptingOptionSpec<String> WORKING_DIRECTORY_OPTION_SPEC =
            OPTION_PARSER.accepts("w", "Set working directory").withOptionalArg().ofType(String.class).defaultsTo("./");
    private static final OptionSpecBuilder QUIET_OPTION_SPEC =
            OPTION_PARSER.accepts("q", "Be quiet. Don't print progress information");
    private static final ArgumentAcceptingOptionSpec<Integer> STEPS_OPTION_SPEC =
            OPTION_PARSER.acceptsAll(asList("s", "steps"), "stop simulation after MAX steps")
            .withRequiredArg().ofType(Integer.class);

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
                        if (!file.canRead())
                            exitWithErrorMessage("Specified classpath is not readable: " + pathName);

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
                    exitWithErrorMessage("Could not find class " + modelClassName);
                }

                if (optionSet.has(MODEL_PARAMETER_OPTION_SPEC)) {
                    final Map<String, String> properties = Maps.newHashMap();
                    for (ModelParameterOptionValue s : optionSet.valuesOf(MODEL_PARAMETER_OPTION_SPEC)) {
                        properties.put(s.key, s.value);
                    }
                    bindListener(Matchers.any(), new ModelParameterTypeListener(properties));
                }

                bind(Integer.class).annotatedWith(Names.named("steps"))
                        .toInstance(optionSet.valueOf(STEPS_OPTION_SPEC));
                bind(Boolean.class).annotatedWith(Names.named("q"))
                        .toProvider(Providers.of(optionSet.has(QUIET_OPTION_SPEC)));
                bind(Integer.class).annotatedWith(Names.named("parallelizationThreshold"))
                        .toInstance(optionSet.valueOf(PARALLELIZATION_THRESHOLD_OPTION_SPEC));
                try {
                    final String path = Files.simplifyPath(optionSet.valueOf(WORKING_DIRECTORY_OPTION_SPEC) + "./data/" + optionSet.valueOf(SIMULATION_NAME_OPTION_SPEC));
                    final H2Logger<DefaultGreyfishAgent> h2Logger = H2Logger.create(path);
                    bind(new TypeLiteral<SimulationLogger<DefaultGreyfishAgent>>(){})
                            .toInstance(SimulationLoggers.synchronizedLogger(
                                    h2Logger));
                } catch (Exception e) {
                    exitWithErrorMessage("Unable to create new database: ", e);
                }
            }
        };
    }

    public static void main(final String[] args) {

        final OptionParser optionParser = OPTION_PARSER;

        try {
            final OptionSet optionSet = optionParser.parse(args);

            if (optionSet.has(HELP_OPTION_SPEC)) {
                printHelp(optionParser);
                System.exit(0);
            }

            final Module commandLineModule = createCommandLineModule(optionSet);

            final RandomGenerator randomGenerator =
                    optionSet.has(REPRODUCIBLE_MODE_OPTION_SPEC) ? new Well19937c(0) : new Well19937c();

            final GreyfishSimulationRunner application = Guice.createInjector(
                    new CoreModule(randomGenerator),
                    commandLineModule
            ).getInstance(GreyfishSimulationRunner.class);

            application.run();
        } catch (OptionException e) {
            exitWithErrorMessage("Failed parsing options: ", e);
        } catch (RuntimeException e) {
            System.out.println("Internal Error: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }

        System.exit(0);
    }

    private static void exitWithErrorMessage(String message) {
        System.out.println("ERROR: " + message);
        printHelp(OPTION_PARSER);
        System.exit(1);
    }

    private static void exitWithErrorMessage(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
        exitWithErrorMessage(message + throwable.getMessage());
    }

    private static void printHelp(OptionParser optionParser) {
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

        private ModelParameterOptionValue(String value, String key) {
            assert key != null;
            assert value != null;

            this.value = value;
            this.key = key;
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
