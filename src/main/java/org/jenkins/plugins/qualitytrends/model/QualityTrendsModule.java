package org.jenkins.plugins.qualitytrends.model;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryProvider;

/**
 * @author Emanuele Zattin
 */
public class QualityTrendsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(StorageManagerFactory.class).toProvider(
                FactoryProvider.newFactory(
                        StorageManagerFactory.class, DbStorageManager.class));
        bind(DbControllerFactory.class).toProvider(
                FactoryProvider.newFactory(
                        DbControllerFactory.class, H2Controller.class));
    }
}
