package com.gordoncaleb.chess.ui.server;

import com.gordoncaleb.chess.ui.server.Dtos.MoveRequest;
import com.gordoncaleb.chess.ui.server.Dtos.NewGameRequest;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

/**
 * Javalin HTTP server exposing the chess engine to the TypeScript web UI.
 *
 * REST API (all JSON):
 *   POST /api/games                  -> create a game, returns state
 *   GET  /api/games/{id}             -> current state
 *   POST /api/games/{id}/moves       -> apply a human move, returns state
 *   POST /api/games/{id}/engine-move -> let the engine reply, returns state
 *
 * The built React app is served from the classpath at /public so the whole
 * thing can run from a single fat jar in production. During development the
 * Vite dev server proxies /api to this process instead.
 */
public class ChessServer {

    private static final int DEFAULT_PORT = 7070;

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        create(new GameService()).start(port);
        System.out.println("Chess server listening on http://localhost:" + port);
    }

    static Javalin create(GameService service) {
        // Only wire up static serving when a built frontend is actually on the
        // classpath. This lets the API run standalone (e.g. against the Vite dev
        // server) without a production bundle present.
        boolean hasFrontend = ChessServer.class.getResource("/public/index.html") != null;

        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            if (hasFrontend) {
                config.staticFiles.add(staticFiles -> {
                    staticFiles.hostedPath = "/";
                    staticFiles.directory = "/public";
                    staticFiles.location = Location.CLASSPATH;
                });
                // Serve index.html for client-side routes / a bare "/".
                config.spaRoot.addFile("/", "/public/index.html", Location.CLASSPATH);
            }
        });

        if (!hasFrontend) {
            System.out.println("No built frontend on classpath; serving API only. "
                    + "Run `npm run build` in web/ (or use the Vite dev server).");
        }

        app.post("/api/games", ctx -> {
            NewGameRequest req = ctx.bodyAsClass(NewGameRequest.class);
            ctx.json(service.newGame(req.humanSide(), req.thinkTimeMs()));
        });

        app.get("/api/games/{id}", ctx ->
                service.get(ctx.pathParam("id"))
                        .ifPresentOrElse(
                                session -> ctx.json(service.state(session)),
                                () -> ctx.status(404).result("No such game")));

        app.post("/api/games/{id}/moves", ctx -> {
            var session = service.get(ctx.pathParam("id"));
            if (session.isEmpty()) {
                ctx.status(404).result("No such game");
                return;
            }
            MoveRequest req = ctx.bodyAsClass(MoveRequest.class);
            service.applyHumanMove(session.get(), req)
                    .ifPresentOrElse(ctx::json,
                            () -> ctx.status(422).result("Illegal move"));
        });

        app.post("/api/games/{id}/engine-move", ctx -> {
            var session = service.get(ctx.pathParam("id"));
            if (session.isEmpty()) {
                ctx.status(404).result("No such game");
                return;
            }
            service.applyEngineMove(session.get())
                    .ifPresentOrElse(ctx::json,
                            () -> ctx.status(409).result("Not the engine's turn"));
        });

        return app;
    }
}
