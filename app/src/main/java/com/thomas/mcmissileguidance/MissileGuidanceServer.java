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

import java.util.HashMap;

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

    double[] angleToGravityAngle = {-90.0D,-88.5000190389512D,-87.00015232030685D,-85.50051412994134D,-84.0012188405897D,-82.5023809550801D,-81.00411514932804D,-79.5065363150105D,-78.00975960183784D,-76.51390045933874D,-75.01907467807112D,-73.52539843017D,-72.03298830913975D,-70.54196136879536D,-69.05243516125309D,-67.56452777386815D,-66.07835786501128D,-64.59404469857276D,-63.11170817707703D,-61.63146887328631D,-60.153448060165914D,-58.6777677390789D,-57.204550666071704D,-55.73392037610672D,-54.26600120509189D,-52.800918309551186D,-51.33879768377411D,-49.87976617427613D,-48.42395149139632D,-46.97148221785271D,-45.52248781407007D,-44.07709862009022D,-42.635445853869655D,-41.19766160576547D,-39.763878829006224D,-38.33423132594188D,-36.908853729864504D,-35.487881482189856D,-34.07145080478994D,-32.659698667267065D,-31.252762748962496D,-29.8507813954956D,-28.453893569634932D,-27.062238796309167D,-25.67595710157441D,-24.29518894536457D,-22.920075147864292D,-21.550756809358614D,-20.18737522343023D,-18.830071783395173D,-17.478987881888997D,-16.134264803540702D,-14.796043610698083D,-13.464465022198432D,-12.139669285210516D,-10.821796040208833D,-9.510984179178678D,-8.207371697190258D,-6.911095537522601D,-5.622291430561803D,-4.341093726744718D,-3.067635223866745D,-1.8020469891210962D,-0.5444581762873613D,0.7050041614632699D,1.946215262627691D,3.1790528517276204D,4.40339733634673D,5.619132006183936D,6.826143233414092D,8.024320673604697D,9.213557466398662D,10.393750435138445D,11.564800284576798D,12.726611795794646D,13.879094017427601D,15.022160452290297D,16.15572923848166D,17.27972332405605D,18.394070634354065D,19.49870423110366D,20.59356246242706D,21.678589102921546D,22.75373348302293D,23.818950606908555D,24.874201258252757D,25.919452093209923D,26.954675720069787D,27.979850765104327D,28.99496192420583D,29.999999999999996D,30.99496192420584D,31.97985076510433D,32.95467572006979D,33.91945209320992D,34.874201258252754D,35.81895060690856D,36.753733483022934D,37.67858910292155D,38.59356246242706D,39.49870423110366D,40.39407063435406D,41.27972332405604D,42.15572923848166D,43.022160452290294D,43.8790940174276D,44.726611795794646D,45.564800284576805D,46.393750435138436D,47.21355746639866D,48.0243206736047D,48.82614323341409D,49.61913200618393D,50.40339733634673D,51.17905285172763D,51.94621526262769D,52.70500416146327D,53.455541823712636D,54.197953010878905D,54.93236477613327D,55.65890627325528D,56.3777085694382D,57.088904462477394D,57.79262830280974D,58.48901582082133D,59.17820395979117D,59.860330714789484D,60.53553497780156D,61.20395638930192D,61.8657351964593D,62.521012118111D,63.16992821660483D,63.81262477656977D,64.44924319064138D,65.0799248521357D,65.70481105463543D,66.32404289842559D,66.93776120369084D,67.54610643036509D,68.1492186045044D,68.7472372510375D,69.34030133273295D,69.92854919521007D,70.51211851781014D,71.0911462701355D,71.66576867405813D,72.23612117099378D,72.80233839423454D,73.36455414613036D,73.9229013799098D,74.47751218592992D,75.02851778214728D,75.57604850860368D,76.12023382572387D,76.66120231622588D,77.1990816904488D,77.73399879490813D,78.26607962389329D,78.7954493339283D,79.32223226092108D,79.84655193983409D,80.36853112671369D,80.88829182292297D,81.40595530142724D,81.92164213498872D,82.43547222613186D,82.94756483874693D,83.45803863120466D,83.96701169086023D,84.47460156983D,84.98092532192888D,85.48609954066126D,85.99024039816216D,86.4934636849895D,86.99588485067196D,87.4976190449199D,87.99878115941031D,88.49948587005865D,88.99984767969313D,89.5D,90.0D};
    
    @Override
    public StreamObserver<MissileState> getGuidance(final StreamObserver<ControlInput> controlInputObserver) {
      return new StreamObserver<MissileState>() {
        private int id = 0;

        double init_pitch = 0;
        double init_yaw = 0;

        int targetId = 2;
        double[][] target = new double[3][3];
        boolean boom = false;

        public double length(double x, double y, double z) {
          return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(y, 2));
        }

        @Override
        public void onNext(MissileState missileState) {
          target[0][0] = 0;
          target[0][1] = 100;
          target[0][2] = 0;

          target[1][0] = 0;
          target[1][1] = 400;
          target[1][2] = 0;

          target[2][0] = 50;
          target[2][1] = 100;
          target[2][2] = 50;

          double dif_x = target[targetId][0] - missileState.getPosX();
          double dif_y = target[targetId][1] - missileState.getPosY();
          double dif_z = target[targetId][2] - missileState.getPosZ();
          
          init_yaw = -(90 + Math.toDegrees(Math.atan(dif_z/dif_x)));
          if (dif_x > 0) {
            init_yaw +=180;
          }
          double hyp_xz = Math.sqrt(Math.pow(dif_x, 2) + Math.pow(dif_z, 2));
          init_pitch = 90 - Math.toDegrees(Math.atan(hyp_xz/dif_y));
          if (init_pitch > 90) {
            init_pitch -= 180;
          }
          init_pitch = angleToGravityAngle[(int)Math.round(init_pitch) + 90];
          logger.log(Level.INFO, "################################## yaw " + init_yaw);
          logger.log(Level.INFO, "################################## pitch " + init_pitch);

          if (length(dif_x, dif_y, dif_z) <= 20 && targetId > 0) {
            targetId--;
          } else if (length(dif_x, dif_y, dif_z) <= 10 && targetId == 0){
            boom = true;
          }
          //logger.log(Level.INFO, "received missileState, pos " + missileState.getPosX() + " " + missileState.getPosY() + " " + missileState.getPosZ());
          // send control to missile
          var controlInput = ControlInput.newBuilder().setId(id++).setPitchTurn(init_pitch - missileState.getPitch()).setYawTurn(init_yaw - missileState.getYaw()).setExplode(boom).setDisarm(false).setHardwareConfig(
                        MissileHardwareConfig.newBuilder()
                          .setWarhead(MissileHardwareConfig.Warhead.TNT_M)
                          .setAirframe(MissileHardwareConfig.Airframe.DEFAULT_AIRFRAME)
                          .setMotor(MissileHardwareConfig.Motor.SINGLE_STAGE_M)
                          .setBattery(MissileHardwareConfig.Battery.LI_ION_M)
                          .setSeeker(MissileHardwareConfig.Seeker.NO_SEEKER)
                          .setInertialSystem(MissileHardwareConfig.InertialSystem.DEFAULT_IMU)
                          .build()).build();
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
