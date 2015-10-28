package com.gordoncaleb.chess.ui.dropwizard;

import com.gordoncaleb.chess.ui.dropwizard.guice.AppGuiceModule;
import com.hubspot.dropwizard.guice.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class ChessApplication extends Application<Config> {

    private GuiceBundle<Config> guiceBundle;

    public static void main(String[] args) throws Exception {
        new ChessApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        guiceBundle = GuiceBundle.<Config>newBuilder()
                .addModule(new AppGuiceModule())
                .enableAutoConfig(getClass().getPackage().getName())
                .setConfigClass(Config.class)
                .build();

        bootstrap.addBundle(guiceBundle);
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {

    }
}
