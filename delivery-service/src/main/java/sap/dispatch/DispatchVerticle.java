package sap.dispatch;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class DispatchVerticle extends VerticleBase {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DispatchVerticle());
    }

    @Override
    public Future<?> start() {
        Router router = Router.router(vertx);
        router.get("/").handler(ctx -> {
            ctx.response()
                    .putHeader("content-type", "text/plain")
                    .end(getMessage());
        });

        return vertx.createHttpServer()
                .requestHandler(router)
                .listen(Math.max(8081, Integer.getInteger("server.port", 8081)))
                .onSuccess(server -> {
                    System.out.println("Dispatch Service started on port " + server.actualPort());
                });
    }

    @Override
    public String getMessage() {
        return "Hello from Dispatch Service (Vert.x)!";
    }
}
