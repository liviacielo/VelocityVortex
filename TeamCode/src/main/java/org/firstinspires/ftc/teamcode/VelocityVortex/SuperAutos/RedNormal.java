package org.firstinspires.ftc.teamcode.VelocityVortex.SuperAutos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.CameraStuff.HistogramAnalysisThread;
import org.firstinspires.ftc.teamcode.Swerve.Core.FTCSwerve;
import org.firstinspires.ftc.teamcode.VelocityVortex.Robot;

/**
 * Created by Justin on 1/27/2017.
 */
@Autonomous
public class RedNormal extends Robot {
    HistogramAnalysisThread.BeaconResult beaconResult;
    enum RobotState{DriveForward,Shoot,RotateToFirstBeacon,DriveToSecondBeacon,PressFirstBeacon,PressSecondBeacon,
        DriveToDefend,Defend,DriveToCapBall,Stop,AlignToShoot,RotateToCapBall,DriveToCapBall2}
    RobotState state=RobotState.DriveForward;
    boolean waitForServos=true,internalResetPosition=true;
    double extraDistance=0,startGyroHeading,deviationHeading;


    @Override
    public void init() {
        initAutonomous();
    }

    public void init_loop(){
        if(!gyro.isCalibrating()) {
            if(swerveDrive==null){
                swerveDrive=new FTCSwerve(lfa, rfa, lba, rba, lfm, rfm, lbm, rbm, lf, rf, lb, rb, 14, 14);
            }
            swerveDrive.refreshValues();
            swerveDrive.drive(-1, 0, 0, 0);
            swerveDrive.update(true, 15, false);
        }
    }

    @Override
    public void loop() {
        super.loop();
        if(gyro.isCalibrating()){
            return;
        }

        if(swerveDrive==null){
            swerveDrive=new FTCSwerve(lfa, rfa, lba, rba, lfm, rfm, lbm, rbm, lf, rf, lb, rb, 14, 14);
        }
        if(internalResetPosition){
            startGyroHeading=gyro.getHeading();
            internalResetPosition=false;
        }
        beaconResult=thread.getBeaconResult();

        switch(state){
            case DriveForward:
                vuforia.cameraLight(useLight);
                shootRight.setPower(AUTONOMOUS_SHOOTING_POWER);
                shootLeft.setPower(AUTONOMOUS_SHOOTING_POWER);
                if(driveWithEncodersAndGyro(-1, 0, 0, AUTONOMOUS_SHOOT_DRIVE_POWER, 15)){
                    state=RobotState.Shoot;
                    deviationHeading=gyro.getHeading()-startGyroHeading;
                }
                break;
            case Shoot:
                swerveDrive.setPivotPoint(-20,0);
                swerveDrive.drive(0,0,1,0);
                buttonWheel.setPosition(WHEEL_OUT);
                if(shoot(2,AUTONOMOUS_SHOOTING_POWER)){
                    state=RobotState.RotateToFirstBeacon;
                    swerveDrive.setPivotPoint(0,0);
                }
                break;

            case RotateToFirstBeacon:
                if(turnAroundPivotPoint(-16, 0, AUTONOMOUS_NORMAL_DRIVE_POWER,Direction.CLOCKWISE, 90-(int)deviationHeading, 4)){
                    state=RobotState.PressFirstBeacon;
                }
                break;

            case PressFirstBeacon:
                if(beaconResult== HistogramAnalysisThread.BeaconResult.RED_LEFT){
                    extraDistance=5;
                }
                if(alignWithAndPushBeacon("Gears", beaconResult, Side.RED,AUTONOMOUS_PUSHING_POWER,1,false)){
                    state=RobotState.DriveToSecondBeacon;
//                    buttonWheel.setPosition(WHEEL_IN);
                }
                break;

            case DriveToSecondBeacon:
                double drivePower=getRuntime()<10?AUTONOMOUS_SLOW_DRIVE_POWER:AUTONOMOUS_SLOW_DRIVE_POWER;
                if(getRuntime()<10.0&&swerveDrive.getLinearInchesTravelled()>30){
                    drivePower=0;
                }
                if(driveWithHeading(-.7,1,0,drivePower,40+extraDistance,startGyroHeading+90)){
                    state=RobotState.PressSecondBeacon;
                }
                break;

            case PressSecondBeacon:
                buttonWheel.setPosition(WHEEL_OUT);
                if(alignWithAndPushBeacon("Tools", beaconResult, Side.RED,AUTONOMOUS_PUSHING_POWER,1,true)){
                    state=RobotState.RotateToCapBall;
                    buttonWheel.setPosition(WHEEL_IN);
                }
                break;

            case DriveToCapBall:
                if(driveWithEncoders(1,0,0,.2,15)){
                    state=RobotState.DriveToCapBall2;
                }
                break;

            case DriveToCapBall2:
                if(driveWithEncoders(0,-1,0,.3,25)){
                    state=RobotState.Stop;
                }
                break;
            case RotateToCapBall:
                if(turnAroundPivotPoint(-20,0,.4,Direction.COUNTERCLOCKWISE,90,4)){
                    state=RobotState.DriveToCapBall;
                    sweeper.setPower(SWEEPER_OUTAKE);
                }
                break;
            case Stop:
                sweeper.setPower(0);
                swerveDrive.drive(0,0,1,0);
                break;
        }


        telemetry.addData("BeaconResult",beaconResult);
        telemetry.addData("Confidence",thread.getConfidence());
        telemetry.addData("State",state);
        swerveDrive.update(waitForServos,30,true);
    }

    public void stop(){
        super.stop();
        thread.kill();
        try {
            vuforia.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
