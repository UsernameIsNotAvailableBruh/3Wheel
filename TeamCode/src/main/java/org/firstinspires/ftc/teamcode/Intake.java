package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Intake {
    private final Servo leftServo;
    private final Servo rightServo;
    private boolean isOpen;

    public static final double MOVING_DISTANCE = 0.058;
    public static final double LEFT_CLOSED_POSITION = 0.03488;
    public static final double RIGHT_CLOSED_POSITION = 0.7914;

    public Intake(HardwareMap hardwareMap) {
        leftServo = hardwareMap.get(Servo.class, "CLAWLEFT");
        rightServo = hardwareMap.get(Servo.class, "CLAWRIGHT");

         leftServo.setDirection(Servo.Direction.FORWARD);
         rightServo.setDirection(Servo.Direction.REVERSE);

//        leftServo.scaleRange(LEFT_CLOSED_POSITION, LEFT_CLOSED_POSITION + MOVING_DISTANCE);
//        rightServo.scaleRange(RIGHT_CLOSED_POSITION, RIGHT_CLOSED_POSITION + MOVING_DISTANCE);
    }

    public void open() {
        leftServo.setPosition(LEFT_CLOSED_POSITION + MOVING_DISTANCE);
        rightServo.setPosition(RIGHT_CLOSED_POSITION + MOVING_DISTANCE);
        isOpen = true;
    }

    public void close() {
        leftServo.setPosition(LEFT_CLOSED_POSITION);
        rightServo.setPosition(RIGHT_CLOSED_POSITION);
        isOpen = false;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean isClosed() {
        return !isOpen;
    }

    public double getLeftPosition() {
        return leftServo.getPosition();
    }

    public double getRightPosition() {
        return rightServo.getPosition();
    }

    public void setLeftPosition(double position) {
        leftServo.setPosition(position);
    }

    public void setRightPosition(double position) {
        rightServo.setPosition(position);
    }
}
