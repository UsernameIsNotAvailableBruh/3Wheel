package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.ElapsedTime;

public class PIController {
    private double Kp = .01;
    private double Ki = .042;
    public double integralSumLimit = 3;
    private double lastTarget = 0;
    private double integralSum = 0;
    private ElapsedTime PIDtime = new ElapsedTime();

    public PIController(double kp, double ki){
        Kp = kp;
        Ki = ki;
    }

    public PIController(){
        Kp = 0;
        Ki = 0;
    }

    public void setKi(double ki) {
        Ki = ki;
    }
    public void setKp(double kp) {
        Kp = kp;
    }

    public double update(double target, double current) {
        double error = target-current;
        double seconds = PIDtime.seconds();
        double p = Kp * error;
        integralSum += error*seconds;
        double i = Ki * integralSum;

        // ----------------optimizations----------------
        //integralSum optimizations:
        if (lastTarget != target) { //target pos changes, reset isum fr
            integralSum = 0;
        }
        //set limits for isum
        if (integralSum > integralSumLimit) {
            integralSum = integralSumLimit;
        }
        if (integralSum < -integralSumLimit) {
            integralSum = -integralSumLimit;
        }
        //derivative low pass filter:
        //filter out high frequency noise to increase derivative performance
        /*double currentFilterEstimate = (Kda * lastFilterEstimate) + (1-Kda) * (error - lastError);
        lastFilterEstimate = currentFilterEstimate;*/
        // ----------------optimizations----------------

        //double derivative =  currentFilterEstimate / seconds;
        //double d = Kd * derivative;
        //In my testing, the D seems to have no positive effect. So no D.
        //double NoFilterD = (error - lastError) / seconds;
        //double f = Kf * Math.cos(current*Math.PI*2/PPR5203_117RPM); //cos(Ticks*(2pi/PPR)) //cosine to make it nonlinear, hopefully helps with nonlinear gravity stuff
        //idk if the f line ^ works or not and PID(no F) seems to work fine.

        PIDtime.reset();
        //lastError = error;
        lastTarget = target;

        double power = p+i;
        if ((power < .01 && power > 0) || (power > -.01 && power < 0)) { //basically telling the PID to put the fries in the bag
            //really small values are basically zero.. so its just better to just zero em
            power = 0;
        }
        int n = 3;
        power = Math.round(power * Math.pow(10, n))/Math.pow(10, n); //round to n decimal places
        power = power > 1 ? 1 : (power < -1 ? -1 : power);
        return power;
    }
}