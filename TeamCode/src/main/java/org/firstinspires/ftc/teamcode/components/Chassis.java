package org.firstinspires.ftc.teamcode.components;

import com.qualcomm.robotcore.hardware.DcMotor;

public class Chassis {
    public static DcMotor motorFL;
    public static DcMotor motorFR;
    public static DcMotor motorBL;
    public static DcMotor motorBR;

    public static final double speed = 0.6;

    public Chassis(){}

    public static void init(DcMotor mFL, DcMotor mFR, DcMotor mBL, DcMotor mBR) {
        Chassis.motorFL = mFL;
        Chassis.motorFR = mFR;
        Chassis.motorBL = mBL;
        Chassis.motorBR = mBR;

        motorBR.setDirection(DcMotor.Direction.REVERSE);
        motorFR.setDirection(DcMotor.Direction.REVERSE);

        motorFL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorFR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorBL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorBR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        motorFL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    /*
        double left_y = gamepad1.left_stick_y;
        double left_x = gamepad1.left_stick_x;
        double strafe_side = gamepad1.right_stick_x;

        double leftFrontPower;
        double leftBackPower;
        double rightFrontPower;
        double rightBackPower;

        if (Math.abs(left_y) < 0.2) {
            leftFrontPower = -left_x - strafe_side;
            rightFrontPower = left_x + strafe_side;
            leftBackPower = left_x - strafe_side;
            rightBackPower = -left_x + strafe_side;
        } else {
            leftFrontPower = left_y - left_x - strafe_side;
            rightFrontPower = left_y + left_x + strafe_side;
            leftBackPower = left_y + left_x - strafe_side;
            rightBackPower = left_y - left_x + strafe_side;
        }

        max = Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower));
        max = Math.max(max, Math.abs(leftBackPower));
        max = Math.max(max, Math.abs(rightBackPower));

        if (max > 1.0) {
            leftFrontPower /= max;
            rightFrontPower /= max;
            leftBackPower /= max;
            rightBackPower /= max;
        }

        motorFL.setPower(leftFrontPower);
        motorFR.setPower(rightFrontPower);
        motorBL.setPower(leftBackPower);
        motorBR.setPower(rightBackPower);
     */

//    public static void

    public static void forward(double power) {
        motorFL.setPower(power);
        motorFR.setPower(power);
        motorBL.setPower(power);
        motorBR.setPower(power);
    }

    public static void strafe(double power) {
        motorFL.setPower(power);
        motorFR.setPower(-power);
        motorBL.setPower(-power);
        motorBR.setPower(power);
    }

    public static void turn(double power) {
        motorFL.setPower(power);
        motorFR.setPower(-power);
        motorBL.setPower(power);
        motorBR.setPower(-power);
    }

    public static void stop() {
        motorFL.setPower(0);
        motorFR.setPower(0);
        motorBL.setPower(0);
        motorBR.setPower(0);
    }

    public static void runToPosition(int FL, int FR, int BL, int BR) {
        motorFL.setTargetPosition(FL);
        motorFR.setTargetPosition(FR);
        motorBL.setTargetPosition(BL);
        motorBR.setTargetPosition(BR);

        motorFL.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorFR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorBL.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorBR.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while(motorFL.isBusy() || motorFR.isBusy() || motorBL.isBusy() || motorBR.isBusy()){
            if (motorFL.isBusy()) motorFL.setPower(0.5);
            if (motorFR.isBusy()) motorFR.setPower(0.5);
            if (motorBL.isBusy()) motorBL.setPower(0.5);
            if (motorBR.isBusy()) motorBR.setPower(0.5);
        }
    }
}
