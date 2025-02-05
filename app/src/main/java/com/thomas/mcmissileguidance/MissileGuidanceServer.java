package com.thomas.mcmissileguidance;

import com.chrisbesch.mcmissile.guidance.GuidanceGrpc.GuidanceImplBase;
import com.chrisbesch.mcmissile.guidance.ControlInput;
import com.chrisbesch.mcmissile.guidance.Missile;
import com.chrisbesch.mcmissile.guidance.MissileState;
import com.chrisbesch.mcmissile.guidance.MissileHardwareConfig;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MissileGuidanceServer {
  private static final Logger logger = Logger.getLogger(MissileGuidanceServer.class.getName());

  private final int port;
  private final Server server;

  public MissileGuidanceServer(int port) {
    this.port = port;
    server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create()).addService(new GuidanceService()).build();
  }

  /** Start serving requests. */
  public void start() throws IOException {
    server.start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        try {
          MissileGuidanceServer.this.stop();
        } catch (InterruptedException e) {
          e.printStackTrace(System.err);
        }
        System.err.println("*** server shut down");
      }
    });
  }

  /** Stop serving requests and shutdown resources. */
  public void stop() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  public static void main(String[] args) throws Exception {
    MissileGuidanceServer server = new MissileGuidanceServer(42069);
    server.start();
    server.blockUntilShutdown();
  }

  private static class GuidanceService extends GuidanceImplBase {
    @Override
    public StreamObserver<MissileState> getGuidance(final StreamObserver<ControlInput> controlInputObserver) {
      return new StreamObserver<MissileState>() {
        private int id = 0;
        @Override
        public void onNext(MissileState missileState) {
          // TODO: do something with the new missile state

          logger.log(Level.INFO, "received missileState, pos " + missileState.getPosX() + " " + missileState.getPosY() + " " + missileState.getPosZ());
          // send control to missile
          var controlInput = ControlInput.newBuilder().setId(id++).setPitchTurn(0).setYawTurn(0).setExplode(false).setDisarm(false).build();
          controlInputObserver.onNext(controlInput);
        }

        @Override
        public void onError(Throwable t) {
          logger.log(Level.WARNING, "getGuidance error");
        }

        @Override
        public void onCompleted() {
          controlInputObserver.onCompleted();
        }
      };
    }
  }
}
