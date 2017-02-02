package org.firstinspires.ftc.teamcode.VelocityVortex;

import android.util.Log;
import android.view.GestureDetector;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity;
import org.firstinspires.ftc.teamcode.CameraStuff.EyeOfSauron;
import org.firstinspires.ftc.teamcode.CameraStuff.FTCTarget;
import org.firstinspires.ftc.teamcode.CameraStuff.FTCVuforia;
import org.firstinspires.ftc.teamcode.CameraStuff.HistogramAnalysisThread;
import org.firstinspires.ftc.teamcode.Logging.DataLogger;
import org.firstinspires.ftc.teamcode.Swerve.Core.AbsoluteEncoder;
import org.firstinspires.ftc.teamcode.Swerve.Core.Constants;
import org.firstinspires.ftc.teamcode.Swerve.Core.FTCSwerve;
import org.firstinspires.ftc.teamcode.Swerve.Core.Vector;

import java.util.HashMap;

/**
 * Created by Justin on 10/15/2016.
 */
public class Robot extends OpMode {
    public static final double WHEEL_IN=.95;
    public static final double WHEEL_OUT=.3;
    public static final double NECK_FLAT=.45;
    public static final double CAP_RIGHT_IN=.7;
    public static final double CAP_LEFT_IN=.7;
    public static final double CAP_RIGHT_OUT=.4;
    public static final double CAP_LEFT_OUT=1;
    public static final double CAP_RIGHT_HOLD=.6;
    public static final double CAP_LEFT_HOLD=.9;
    public static final int SLIDE_DOWN=0;
    public static final int SLIDE_UP=20200;
    public static final double SHOOTER_DOWN=.6;
    public static final double SHOOTER_UP=0;
    public static final double SWEEPER_INTAKE=1;
    public static final double SWEEPER_OUTAKE=-1;
    public static final double SWEEPER_STOP=0;

    public final double CAMERA_OFFSET_FROM_PLOW=42;
    public final double SPONGE_OFFSET_FROM_CAMERA=70;
    public final double BUTTON_DISTANCE_FROM_WALL=55;
    public final double BUTTON_OFFSET_FROM_CENTER=65;


    public DcMotor lfm,lbm,rfm,rbm,slideMotor,shootLeft,shootRight,sweeper;
    public Servo lf,lb,rf,rb;
    public Servo buttonWheel, capLeft, capRight,shootServo,neck;
    public AnalogInput lfa,lba,rfa,rba;
    public FTCSwerve swerveDrive;
    public AbsoluteEncoder lfe,rfe,rbe,lbe;
//    public DataLogger dataLogger;
//    public ModernRoboticsI2cRangeSensor leftRangeMeter,rightRangeMeter;

    public int slideStartPosition;//if start position is not 0

    public double lastLoop;
    public GyroSensor gyro;
    public boolean resetPosition=true;


    public HistogramAnalysisThread thread;
    public FTCVuforia vuforia;


    @Override
    public void init() {
//        dataLogger=DataLogger.create(50);
        lfm=hardwareMap.dcMotor.get("lfm");
        rfm=hardwareMap.dcMotor.get("rfm");
        lbm=hardwareMap.dcMotor.get("lbm");
        rbm=hardwareMap.dcMotor.get("rbm");
        gyro=hardwareMap.gyroSensor.get("gyro");
        gyro.calibrate();
        sweeper=hardwareMap.dcMotor.get("sweeper");
        shootLeft=hardwareMap.dcMotor.get("shootLeft");
        shootRight=hardwareMap.dcMotor.get("shootRight");
        shootRight.setDirection(DcMotorSimple.Direction.REVERSE);
        shootLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shootRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lf=hardwareMap.servo.get("lf");
        lb=hardwareMap.servo.get("lb");
        rf=hardwareMap.servo.get("rf");
        rb=hardwareMap.servo.get("rb");
        shootServo=hardwareMap.servo.get("shootServo");
        shootServo.setPosition(SHOOTER_DOWN);
        slideMotor=hardwareMap.dcMotor.get("slideMotor");
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        slideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        slideStartPosition=slideMotor.getCurrentPosition();
        lf.setPosition(0.5);
        lb.setPosition(0.5);
        rf.setPosition(0.5);
        rb.setPosition(0.5);
        neck=hardwareMap.servo.get("neck");
        buttonWheel=hardwareMap.servo.get("buttonWheel");
        capLeft=hardwareMap.servo.get("capLeft");
        capRight=hardwareMap.servo.get("capRight");
        buttonWheel.setPosition(WHEEL_IN);
        neck.setPosition(NECK_FLAT);
        capRight.setPosition(CAP_RIGHT_IN);
        capLeft.setPosition(CAP_LEFT_IN);
        lfa=hardwareMap.analogInput.get("lfa");
        lba=hardwareMap.analogInput.get("lba");
        rfa=hardwareMap.analogInput.get("rfa");
        rba=hardwareMap.analogInput.get("rba");
        lfe=new AbsoluteEncoder(Constants.FL_OFFSET, lfa);
        rfe=new AbsoluteEncoder(Constants.FR_OFFSET, rfa);
        rbe=new AbsoluteEncoder(Constants.BR_OFFSET, rba);
        lbe=new AbsoluteEncoder(Constants.BL_OFFSET, lba);
        swerveDrive=new FTCSwerve(lfa,rfa,lba,rba,lfm,rfm,lbm,rbm,lf,rf,lb,rb,14,14);
        lastLoop=System.nanoTime();
    }

    @Override
    public void loop(){
        swerveDrive.refreshValues();
        telemetry.addData("LoopTime",System.nanoTime()/1E6-lastLoop);
        lastLoop=System.nanoTime()/1E6;

    }
    @Override
    public  void stop(){
        lf.setPosition(0.5);
        lb.setPosition(0.5);
        rf.setPosition(0.5);
        rb.setPosition(0.5);
        capRight.setPosition(CAP_RIGHT_HOLD);
        capLeft.setPosition(CAP_LEFT_HOLD);
    }






    //===================================
    //methods for autonomous opmodes
    //===================================

    public void initAutonomous(){
        vuforia=new FTCVuforia(FtcRobotControllerActivity.getActivity());
        vuforia.addTrackables("FTC_2016-17.xml");
        vuforia.initVuforia();
        thread=new HistogramAnalysisThread(vuforia);
        thread.stopAnalyzing();
        thread.start();
    }

    private boolean resetDrivePosition=true;
    //return true if driving is finished
    public boolean driveWithEncoders(double translationX,double translationY, double rotation, double power, double inches){
        if(resetDrivePosition){
            resetDrivePosition=false;
            startHeading=gyro.getHeading();
            swerveDrive.resetPosition();
        }
        int currentHeading=gyro.getHeading();
        Vector targetVector = new Vector(Math.cos(Math.toRadians(startHeading)), Math.sin(Math.toRadians(startHeading)));
        Vector currentVector = new Vector(Math.cos(Math.toRadians(currentHeading)), Math.sin(Math.toRadians(currentHeading)));
        //angleBetween is the angle from currentPosition to target position in radians
        //it has a range of -pi to pi, with negative values being clockwise and positive counterclockwise of the current angle
        double angleBetween = Math.atan2(currentVector.x * targetVector.y - currentVector.y * targetVector.x, currentVector.x * targetVector.x + currentVector.y * targetVector.y);
        if(swerveDrive.getLinearInchesTravelled()<inches){
            if(rotation!=0) {
                swerveDrive.drive(translationX, translationY, rotation, power);
            }else{
                swerveDrive.drive(translationX,translationY,angleBetween/2,power);
            }
            return false;
        }else{
            resetDrivePosition=true;
            swerveDrive.drive(translationX,translationY,rotation,0);
            return true;
        }
    }

    private enum PressingState{AlignWithBeacon,PressButton}
    private PressingState state=PressingState.AlignWithBeacon;
    public enum Side{BLUE,RED}
    private Vector buttonVector=new Vector(1,0);
    private boolean targetFound=false;

    public boolean alignWithAndPushBeacon(String targetName, HistogramAnalysisThread.BeaconResult beaconResult, Side side,double power){
        if(resetPosition){
            resetPosition=false;
            state=PressingState.AlignWithBeacon;
            thread.startAnalyzing();
            targetFound=false;
            swerveDrive.resetPosition();
        }
        HashMap<String, double[]> data = vuforia.getVuforiaData();
        FTCTarget currentBeacon=new FTCTarget();
        try{
            if(data.containsKey(targetName)){
                currentBeacon=new FTCTarget(data,targetName);
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        double buttonOffsetFromCenter;
        if(side==Side.BLUE){
            buttonOffsetFromCenter=BUTTON_OFFSET_FROM_CENTER;
        }else{
            buttonOffsetFromCenter=-BUTTON_OFFSET_FROM_CENTER;
        }
        switch(state){
            case AlignWithBeacon:
                buttonWheel.setPosition(WHEEL_OUT);
                if(currentBeacon.isFound()){
                    Vector direction;
                    if(beaconResult== HistogramAnalysisThread.BeaconResult.INCONCLUSIVE){
                        direction = new Vector(currentBeacon.getDistance() -BUTTON_DISTANCE_FROM_WALL-SPONGE_OFFSET_FROM_CAMERA,
                                               currentBeacon.getHorizontalDistance());
                    }else if(beaconResult== HistogramAnalysisThread.BeaconResult.RED_LEFT){
                        direction = new Vector(currentBeacon.getDistance()-BUTTON_DISTANCE_FROM_WALL-SPONGE_OFFSET_FROM_CAMERA,
                                               currentBeacon.getHorizontalDistance()+buttonOffsetFromCenter+CAMERA_OFFSET_FROM_PLOW);
                    }else{
                        direction = new Vector(currentBeacon.getDistance()-BUTTON_DISTANCE_FROM_WALL-SPONGE_OFFSET_FROM_CAMERA,
                                               currentBeacon.getHorizontalDistance()-buttonOffsetFromCenter+CAMERA_OFFSET_FROM_PLOW);
                    }
                    direction=rotateVector(direction,currentBeacon.getYRotation());
                    buttonVector=direction;
                    swerveDrive.drive(direction.x, direction.y, currentBeacon.getYRotation(),power);

                    if(direction.getMagnitude()<400){
                        targetFound=true;
                    }
                    if(direction.getMagnitude()<100){
                        state=PressingState.PressButton;
                        resetDrivePosition=true;
                    }
                    return false;
                }else if(targetFound){
                    state=PressingState.PressButton;
                    thread.stopAnalyzing();
                    thread.resetResult();
                    resetDrivePosition=true;
                    return false;
                } else{
                    swerveDrive.drive(buttonVector.x,buttonVector.y,0,0);
                    return false;
                }


            case PressButton:
                if(driveWithEncoders(buttonVector.x,buttonVector.y,0,power,mmToInch(buttonVector.getMagnitude())+1)) {
                    resetPosition=true;
                    return true;
                }else{
                    return false;
                }
        }
        return false;
    }

    public boolean defendBeacon(String targetName){
        if(resetPosition){
            resetPosition=false;
            buttonWheel.setPosition(WHEEL_IN);
        }
        HashMap<String, double[]> data = vuforia.getVuforiaData();
        FTCTarget currentBeacon=new FTCTarget();
        try{
            if(data.containsKey(targetName)){
                currentBeacon=new FTCTarget(data,targetName);
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        if(currentBeacon.isFound()) {
            Vector direction = new Vector(currentBeacon.getDistance() - 250, currentBeacon.getHorizontalDistance());
            if (direction.getMagnitude() > 15||Math.abs(currentBeacon.getYRotation())>Math.toRadians(3)) {
                swerveDrive.drive(direction.x, direction.y, currentBeacon.getYRotation()*2, Range.scale(direction.getMagnitude(),0,250,.05,.15));
                return false;
            }else{
                resetPosition=true;
                return true;
            }
        }else{
            swerveDrive.drive(0,0,1,0);
            return false;
        }
    }

    public enum Direction{CLOCKWISE,COUNTERCLOCKWISE}
    private int startHeading;
    private int targetHeading;

    public boolean turnAroundPivotPoint(double x,double y,double power,int degrees,Direction direction,int threshold){
        if(resetPosition){
            resetPosition=false;
            swerveDrive.setPivotPoint(x,y);
            startHeading=gyro.getHeading();
            if(direction==Direction.COUNTERCLOCKWISE) {
                targetHeading = wrapGyroHeading(startHeading -degrees);
            }else{
                targetHeading=wrapGyroHeading(startHeading+degrees);
            }
        }
        int currentHeading=gyro.getHeading();
        Vector targetVector = new Vector(Math.cos(Math.toRadians(targetHeading)), Math.sin(Math.toRadians(targetHeading)));
        Vector currentVector = new Vector(Math.cos(Math.toRadians(currentHeading)), Math.sin(Math.toRadians(currentHeading)));
        //angleBetween is the angle from currentPosition to target position in radians
        //it has a range of -pi to pi, with negative values being clockwise and positive counterclockwise of the current angle
        double angleBetween = Math.atan2(currentVector.x * targetVector.y - currentVector.y * targetVector.x, currentVector.x * targetVector.x + currentVector.y * targetVector.y);
        if(angleBetween>0){
            power=Math.abs(power);
        }else if(angleBetween<0){
            power=-Math.abs(power);
        }
        if(Math.abs(angleBetween)>Math.toRadians(threshold)){
            swerveDrive.drive(0,0,1,power);
            return false;
        }else{
            swerveDrive.drive(0,0,1,0);
            swerveDrive.setPivotPoint(0,0);
            resetPosition=true;
            return true;
        }
    }

    private long servoTravelStart,startTime,lastShot;
    private boolean resetServoTime=true;
    private enum ShootServoState{MovingUp,MovingDown}
    private ShootServoState servoState;
    private int shots;
    public boolean shoot(int targetShots,double power){
        if(resetPosition){
            startTime=System.currentTimeMillis();
            resetPosition=false;
            shots=0;
        }
        shootLeft.setPower(power);
        shootRight.setPower(power);
        if(System.currentTimeMillis()-startTime>200){
            if(resetServoTime){
                servoTravelStart=System.currentTimeMillis();
                resetServoTime=false;
            }
            if(servoState==ShootServoState.MovingUp){
                if(System.currentTimeMillis()-servoTravelStart>300){
                    shots++;
                    lastShot=System.currentTimeMillis();
                    if(shots<targetShots) {
                        servoState = ShootServoState.MovingDown;
                        return false;
                    }else{
                        shootLeft.setPower(0);
                        shootRight.setPower(0);
                        resetPosition=true;
                        return true;
                    }
                }else{
                    shootServo.setPosition(SHOOTER_UP);
                    return false;
                }
            }else{
                shootServo.setPosition(SHOOTER_DOWN);
                if(System.currentTimeMillis()-lastShot>600){
                    servoState=ShootServoState.MovingUp;
                    servoTravelStart=System.currentTimeMillis();
                }
                return false;
            }
        }else{
            return false;
        }
    }

    public double mmToInch(double mm){
        return mm*.0393701;
    }

    public int wrapGyroHeading(int heading){
        if(heading>359){
            heading-=360;
            return wrapGyroHeading(heading);
        }else if(heading<0){
            heading+=360;
            return wrapGyroHeading(heading);
        }else{
            return heading;
        }
    }


//    rotation matrix is:
//    cost   -sint
//    sint    cost
    public Vector rotateVector(Vector input,double radians){
        double x=input.x*Math.cos(radians)-input.y*Math.sin(radians);
        double y=input.x*Math.sin(radians)+input.y*Math.cos(radians);
        return new Vector(x,y);
    }
}
