/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.Reno;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * This is NOT an opmode.
 *
 * This class can be used to define all the specific hardware for a single robot.
 * In this case that robot is a Pushbot.
 * See PushbotTeleopTank_Iterative and others classes starting with "Pushbot" for usage examples.
 *
 * This hardware class assumes the following device names have been configured on the robot:
 * Note:  All names are lower case and some have single spaces between words.
 *
 * Motor channel:  Left  drive motor:        "left_drive"
 * Motor channel:  Right drive motor:        "right_drive"
 * Motor channel:  Manipulator drive motor:  "left_arm"
 * Servo channel:  Servo to open left claw:  "left_hand"
 * Servo channel:  Servo to open right claw: "right_hand"
 */
public class HardwareRobot
{
    /* Public OpMode members. */
    public DcMotor  leftDriveFront   = null;
    public DcMotor  rightDriveFront  = null;
    public DcMotor  leftDriveBack = null;
    public DcMotor rightDriveBack = null;

    // definitions for slider motor, servo motor, and claw motors
    public DcMotor sliderMotor     = null;

    public Servo  gripperServo    = null;

    public DcMotor  leftArm     = null;
    //public Servo    leftClaw    = null;
    //public Servo    rightClaw   = null;

    //public static final double MID_SERVO       =  0.5 ;
    //public static final double ARM_UP_POWER    =  0.45 ;
    //public static final double ARM_DOWN_POWER  = -0.45 ;

    /* local OpMode members. */
    HardwareMap hwMap           =  null;
    private ElapsedTime period  = new ElapsedTime();
    private String motorStatus;

    /* Constructor */
    public HardwareRobot(){

    }

    /* Initialize standard Hardware interfaces */
    public void init(HardwareMap ahwMap) {
        // Save reference to Hardware map
        hwMap = ahwMap;

        // Define and Initialize Motors
        leftDriveFront  = hwMap.get(DcMotor.class, "FrontLeft");
        rightDriveFront = hwMap.get(DcMotor.class, "FrontRight");
        leftDriveBack  = hwMap.get(DcMotor.class, "BackLeft");
        rightDriveBack = hwMap.get(DcMotor.class, "BackRight");
        sliderMotor = hwMap.get(DcMotor.class, "SliderMotor");  // for slider dc motor

        gripperServo = hwMap.get(Servo.class, "TestServo");  // for gripper servo motor

        leftDriveFront.setDirection(DcMotor.Direction.FORWARD); // Set to REVERSE if using AndyMark motors// Drive.setDirection(DcMotor.Direction.REVERSE);// Set to FORWARD if using AndyMark motors
        leftDriveBack.setDirection(DcMotor.Direction.FORWARD);
        // Set all motors to zero power
        leftDriveFront.setPower(0);
        rightDriveFront.setPower(0);
        leftDriveBack.setPower(0);
        rightDriveBack.setPower(0);
        sliderMotor.setPower(0);

        gripperServo.setPosition(0.0);

        // Set all motors to run without encoders.
        // May want to use RUN_USING_ENCODERS if encoders are installed.
       // leftDriveFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        //rightDriveFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        //leftDriveBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        //rightDriveBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        //leftArm.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // Define and initialize ALL installed servos.
       // leftClaw  = hwMap.get(Servo.class, "left_hand");
       // rightClaw = hwMap.get(Servo.class, "right_hand");
        //leftClaw.setPosition(MID_SERVO);
       // rightClaw.setPosition(MID_SERVO);
    }

    public void turn(double power)
    {
        //telemetry.addData("Status", "ready to move backward for 2 seconds");
        //Moving backward
        //power = -0.2;

        this.leftDriveFront.setPower(power);
        this.leftDriveBack.setPower(power);
        this.rightDriveBack.setPower(power);
        this.rightDriveFront.setPower(power);
    }
    public void drive(double power)
    {
        // Moving forward
        //telemetry.addData("Status", "ready to move forward for 4 seconds");
        //power = 0.2;
        this.leftDriveFront.setPower(power);
        this.leftDriveBack.setPower(power);
        this.rightDriveBack.setPower(power);
        this.rightDriveFront.setPower(power);


    }

    public void stop()
    {
        // Moving forward
        //telemetry.addData("Status", "ready to move forward for 4 seconds");
        //power = 0.2;
        this.leftDriveFront.setPower(0);
        this.leftDriveBack.setPower(0);
        this.rightDriveBack.setPower(0);
        this.rightDriveFront.setPower(0);


    }

    public void tankDrive(double leftPower, double rightPower) {

        this.leftDriveFront.setPower(leftPower);
        this.leftDriveBack.setPower(leftPower);
        this.rightDriveBack.setPower(rightPower);
        this.rightDriveFront.setPower(rightPower);
        this.setMotorStatus(leftPower, leftPower, rightPower, rightPower);
    }

    public void arcadeDrive(double drive, double rotate) {

        double maximum = Math.max(Math.abs(drive), Math.abs(rotate));
        double total = drive + rotate;
        double difference = drive - rotate;


        if(drive >= 0)
        {
            if (rotate >= 0)
            {
                this.tankDrive(maximum, difference);
            }
            else
            {
                this.tankDrive(total, maximum);
            }

        }
        else
        {
            if (rotate >= 0)
            {
                this.tankDrive(total, -1 * maximum);
            }
            else
            {
                this.tankDrive(-1 * maximum, difference);
            }
        }

    }

    public void setDriveForward()
    {
        this.leftDriveFront.setDirection(DcMotor.Direction.REVERSE);
        rightDriveFront.setDirection(DcMotor.Direction.FORWARD);
        leftDriveBack.setDirection(DcMotor.Direction.REVERSE);
        rightDriveBack.setDirection(DcMotor.Direction.FORWARD);
    }

    public void setDriveBackward()
    {
        leftDriveFront.setDirection(DcMotor.Direction.FORWARD);
        rightDriveFront.setDirection(DcMotor.Direction.REVERSE);
        leftDriveBack.setDirection(DcMotor.Direction.FORWARD);
        rightDriveBack.setDirection(DcMotor.Direction.REVERSE);
    }

    public void resetEncoder()
    {
        leftDriveFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightDriveFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftDriveBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightDriveBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftDriveFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightDriveFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftDriveBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightDriveBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void enableENcoder()
    {
        leftDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftDriveBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightDriveBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void setMotorStatus(double leftFrontSpeed, double leftBackSpeed, double rightFrontSpeed, double rightBackSpeed)
    {
        motorStatus =  String.format("Speed - LF %5.2f:LB %5.2f:RF %5.2f:RB %5.2f", leftFrontSpeed, leftBackSpeed, rightFrontSpeed, rightBackSpeed);
    }

    public String getMotorStatus()
    {
        return this.motorStatus;
    }

 }

