package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

public class Pivot {
    private DcMotor leftMotor;
    private DcMotor rightMotor;
    private static final double DEFAULT_POWER = 0.8;
    private static final int POSITION_TOLERANCE = 10;

    // Preset positions - adjust these for your robot
    public static final int POSITION_DOWN = 0;
    public static final int POSITION_PICKUP = 200;
    public static final int POSITION_HALF = 500;
    public static final int POSITION_UP = 900;

    private int targetPosition = 0;
    private PivotState currentState = PivotState.STOPPED;

    private enum PivotState {
        MOVING,
        HOLDING,
        STOPPED
    }

    public Pivot(DcMotor leftMotor, DcMotor rightMotor) {
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        leftMotor.setDirection(DcMotor.Direction.FORWARD);
        rightMotor.setDirection(DcMotor.Direction.REVERSE);
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        resetEncoders();
    }

    private void setState(PivotState newState, double power) {
        // Only change motors if state is different
        if (newState != currentState) {
            switch (newState) {
                case MOVING:
                    if (leftMotor.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
                        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    }
                    leftMotor.setPower(power);
                    rightMotor.setPower(power);
                    break;

                case HOLDING:
                    int currentPos = getCurrentPosition();
                    leftMotor.setTargetPosition(currentPos);
                    rightMotor.setTargetPosition(currentPos);
                    leftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    rightMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    leftMotor.setPower(DEFAULT_POWER);
                    rightMotor.setPower(DEFAULT_POWER);
                    break;

                case STOPPED:
                    leftMotor.setPower(0);
                    rightMotor.setPower(0);
                    break;
            }
            currentState = newState;
        }

        // If moving up or down, update power even if already in that state
        else if ((newState == PivotState.MOVING) && power != Math.abs(leftMotor.getPower())) {
            leftMotor.setPower(power);
            rightMotor.setPower(power);
        }
    }

    public void resetEncoders() {
        leftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void move(double power) {
        setState(PivotState.MOVING, power);
    }

    public void stop() {
        setState(PivotState.STOPPED, 0);
    }

    public void hold() {
        setState(PivotState.HOLDING, DEFAULT_POWER);
    }

    public void setTargetPosition(int position) {
        targetPosition = position;
        leftMotor.setTargetPosition(position);
        rightMotor.setTargetPosition(position);

        // Only change mode and power if not already in RUN_TO_POSITION
        if (leftMotor.getMode() != DcMotor.RunMode.RUN_TO_POSITION) {
            leftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            leftMotor.setPower(DEFAULT_POWER);
            rightMotor.setPower(DEFAULT_POWER);
        }
        currentState = PivotState.HOLDING;
    }

    public boolean isAtTargetPosition() {
        int leftError = Math.abs(leftMotor.getCurrentPosition() - targetPosition);
        int rightError = Math.abs(rightMotor.getCurrentPosition() - targetPosition);
        return leftError < POSITION_TOLERANCE && rightError < POSITION_TOLERANCE;
    }

    public boolean isDrifting() {
        int positionDifference = Math.abs(leftMotor.getCurrentPosition() - rightMotor.getCurrentPosition());
        return positionDifference > POSITION_TOLERANCE;
    }

    public int getLeftPosition() {
        return leftMotor.getCurrentPosition();
    }

    public int getRightPosition() {
        return rightMotor.getCurrentPosition();
    }

    public int getCurrentPosition() {
        return (leftMotor.getCurrentPosition() + rightMotor.getCurrentPosition()) / 2;
    }

    public PivotState getCurrentState() {
        return currentState;
    }
}
