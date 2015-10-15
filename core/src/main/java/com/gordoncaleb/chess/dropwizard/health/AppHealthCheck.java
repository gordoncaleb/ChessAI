package com.gordoncaleb.chess.dropwizard.health;

import com.hubspot.dropwizard.guice.InjectableHealthCheck;

public class AppHealthCheck extends InjectableHealthCheck {

    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }

    @Override
    public String getName() {
        return "ChessEngine";
    }
}
