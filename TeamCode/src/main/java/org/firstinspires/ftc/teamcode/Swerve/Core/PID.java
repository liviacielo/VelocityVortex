package org.firstinspires.ftc.teamcode.Swerve.Core;

import android.util.Log;

import com.qualcomm.robotcore.hardware.AccelerationSensor;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.VelocityVortex.Robot;

import java.util.ArrayList;

/**
 * Created by hunaid on 9/30/2016.
 */
public class PID {
    double P_Gain=0;
    double I_Gain=0;
    double D_Gain=0;
    double A_Gain=0;
    double DECAY_CONSTANT=2;


    double time;
    double integral=0;

    double lastpow=0;
    double deriv=0;
    double previousError=0;
    double dt;
    int[] positionHistory=new int[3];
    double[] positionTimes=new double[3];
    double acceleration=0;
    ArrayList<Double> accelerations=new ArrayList();
    final int HISTORY_SIZE=3;
    double lastMotorPower=0;


    private DcMotor driveMotor;

    public PID(double P, double I, double D,DcMotor motor,double a){
        positionHistory=new int[]{motor.getCurrentPosition(),motor.getCurrentPosition(),motor.getCurrentPosition()};
        positionTimes=new double[]{System.nanoTime()/1.0E6,System.nanoTime()/1.0E6+1,System.nanoTime()/1.0E6+2};
        driveMotor=motor;
        P_Gain = P;
        I_Gain = I;
        A_Gain=a;
        D_Gain = D;
        time=System.nanoTime()/1E6;
    }
    public double setPIDpower(double error,double motorPower){
//        update history of motor position and time taken
        for(int i=0;i<2;i++){
            positionHistory[i]=positionHistory[i+1];
            positionTimes[i]=positionTimes[i+1];
        }
        positionHistory[2]=driveMotor.getCurrentPosition();
        positionTimes[2]=System.nanoTime()/1E6;

        //calculate dposition/dt
        try {
            double[] v = new double[]{(positionHistory[1] - positionHistory[0]) / (positionTimes[1] - positionTimes[0]),
                    (positionHistory[2] - positionHistory[1]) / (positionTimes[2] - positionTimes[1])};
            //calculate "intermediate" time for upcoming acceleration calculation
            double[] intermediateTimes = new double[]{(positionTimes[0] + positionTimes[1]) / 2, (positionTimes[1] + positionTimes[2]) / 2};
            //calculate dv/dt
            acceleration = (v[1] - v[0]) / (intermediateTimes[1] - intermediateTimes[0]);//acceleration!
            if(accelerations.size()<HISTORY_SIZE){
                accelerations.add(acceleration);
            }else{
                accelerations.remove(0);
                accelerations.add(acceleration);
            }
        }catch(ArithmeticException e){
            e.printStackTrace();
            acceleration=0;
        }
        //calculate average acceleration
        acceleration=0;
        for(double a:accelerations){
            acceleration+=a;
        }
        acceleration/=accelerations.size();
        dt=(System.nanoTime()/1.0E6)-time;//change in time
        time=System.nanoTime()/1.0E6;//reset "last" time

//        acceleration/=(1+(dt/50));//decay more when more time has passed
//        acceleration=acceleration+motorPower-lastMotorPower;
//        lastMotorPower=motorPower;

        if(Math.abs(error)<Math.toRadians(30)){
            integral+=(error*dt/1000.0);
            if(integral>1){
                integral=1;
            }else if(integral<-1){
                integral=-1;
            }
        }else{
            integral=0;
        }

        deriv=((error-previousError)*1000.0)/dt;
        previousError=error;

        lastpow=(error*P_Gain)+(deriv*D_Gain)+(integral*I_Gain)+(acceleration*A_Gain);
        if(Math.abs(error)<Math.toRadians(1)&&acceleration<.1){
            lastpow=0;
        }
        return lastpow;
    }
}