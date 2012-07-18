package org.asoem.greyfish.scenarios;

import com.google.inject.Provider;
import org.asoem.greyfish.core.scenario.Scenario;

import java.util.Properties;

/**
 * User: christoph
 * Date: 18.07.12
 * Time: 10:26
 */
public interface ModelFactory extends Provider<Scenario> {
    Properties getModelProperties();
}
