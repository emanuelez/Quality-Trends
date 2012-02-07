package org.jenkins.plugins.qualitytrends.model;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryProvider;

/**
 * @author Emanuele Zattin
 */
public class QualityTrendsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ParserResultHandlerFactory.class).toProvider(
                FactoryProvider.newFactory(
                        ParserResultHandlerFactory.class, H2Handler.class));
        bind(DbControllerFactory.class).toProvider(
                FactoryProvider.newFactory(
                        DbControllerFactory.class, H2Controller.class));
    }
}
