package com.gordoncaleb.chess.ui.dropwizard;

import com.gordoncaleb.chess.ui.dropwizard.guice.AppGuiceModule;
import com.hubspot.dropwizard.guice.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class ChessApplication extends Application<Configuration> {

    private GuiceBundle<Configuration> guiceBundle;

    public static void main(String[] args) throws Exception {
        new ChessApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        guiceBundle = GuiceBundle.newBuilder()
                .addModule(new AppGuiceModule())
                .enableAutoConfig(getClass().getPackage().getName())
                .setConfigClass(Configuration.class)
                .build();

        bootstrap.addBundle(guiceBundle);
    }

    @Override
    public void run(Configuration config, Environment environment) throws Exception {

    }
}
