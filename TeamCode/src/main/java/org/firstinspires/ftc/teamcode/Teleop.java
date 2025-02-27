/* Copyright (c) 2021 FIRST. All rights reserved.
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

package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import com.qualcomm.robotcore.hardware.DcMotor;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.util.EnumMap;
import java.util.List;

/*
 * This file contains an example of a Linear "OpMode".
 * An OpMode is a 'program' that runs in either the autonomous or the teleop period of an FTC match.
 * The names of OpModes appear on the menu of the FTC Driver Station.
 * When a selection is made from the menu, the corresponding OpMode is executed.
 *
 * This particular OpMode illustrates driving a 4-motor Omni-Directional (or Holonomic) robot.
 * This code will work with either a Mecanum-Drive or an X-Drive train.
 * Both of these drives are illustrated at https://gm0.org/en/latest/docs/robot-design/drivetrains/holonomic.html
 * Note that a Mecanum drive must display an X roller-pattern when viewed from above.
 *
 * Also note that it is critical to set the correct rotation direction for each motor.  See details below.
 *
 * Holonomic drives provide the ability for the robot to move in three axes (directions) simultaneously.
 * Each motion axis is controlled by one Joystick axis.
 *
 * 1) Axial:    Driving forward and backward               Left-joystick Forward/Backward
 * 2) Lateral:  Strafing right and left                     Left-joystick Right and Left
 * 3) Yaw:      Rotating Clockwise and counter clockwise    Right-joystick Right and Left
 *
 * This code is written assuming that the right-side motors need to be reversed for the robot to drive forward.
 * When you first test your robot, if it moves backward when you push the left stick forward, then you must flip
 * the direction of all 4 motors (see code below).
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

//NOTES:
//git fetch --all
//git reset --hard origin/master
//git pull

// *-----*
// All the code is written by Aajinkya Naik unless stated otherwise. (Also, no AI is used )
// *-----*

//BHI260AP is the IMU

@Config
@TeleOp(name="3 wheelie funsies", group="OpMode")
public class Teleop extends LinearOpMode {
    // Declare OpMode members for each of the 4 motors.
    private ElapsedTime runtime           = new ElapsedTime();
    private DcMotor leftBackDrive         = null;
    private DcMotor rightBackDrive        = null;
    private DcMotor frontDrive            = null;
    private IMU BHI260AP                  = null;
    private final static double LowerBy = 1.5;
    //private static boolean GP2Initialized = false;

    public static double LowerByDef = 1.5;

    static double Kp=.001, Ki=0.001;
    PIController HeadingPID = new PIController();

    @Override
    public void runOpMode() {
        //https://gm0.org/en/latest/docs/software/tutorials/gamepad.html#storing-gamepad-state
        //Gamepad currentGamepad1  = new Gamepad();
        //Gamepad previousGamepad1 = new Gamepad();

        // Initialize the hardware variables. Note that the strings used here must correspond
        // to the names assigned during the robot configuration step on the DS or RC devices.
        rightBackDrive  = hardwareMap.get(DcMotor.class, "rightBack"); //Motor 2 = right bottom
        leftBackDrive   = hardwareMap.get(DcMotor.class, "leftBack"); //Motor 3 = left bottom
        frontDrive      = hardwareMap.get(DcMotor.class, "front");

        BHI260AP = hardwareMap.get(IMU.class, "imu");
        //REV2mDistance = hardwareMap.get(DistanceSensor.class, "sensi");

        IMU.Parameters IMUParams = new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD
                )
        );
        BHI260AP.initialize(IMUParams);
        BHI260AP.resetYaw();

        // ########################################################################################
        // !!!            IMPORTANT Drive Information. Test your motor directions.            !!!!!
        // ########################################################################################
        // Most robots need the motors on one side to be reversed to drive forward.
        // The motor reversals shown here are for a "direct drive" robot (the wheels turn the same direction as the motor shaft)
        // If your robot has additional gear reductions or uses a right-angled drive, it's important to ensure
        // that your motors are turning in the correct direction.  So, start out with the reversals here, BUT
        // when you first test your robot, push the left joystick forward and observe the direction the wheels turn.
        // Reverse the direction (flip FORWARD <-> REVERSE ) of any wheel that runs backward
        // Keep testing until ALL the wheels move the robot forward when you push the left joystick forward.
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);
        frontDrive.setDirection(DcMotor.Direction.REVERSE);

        rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        rightBackDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        rightBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        leftBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        frontDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        //final double GearRatio3 =  2.89;
        final double GearRatio4 = 3.61;
        final double GearRatio5 = 5.23;
        double DriveHDHexMotorCPR = 28 * GearRatio5 * GearRatio4;

        // Effects (just for funsies)
        Effects effects = new Effects();

        // rumble
        Gamepad.RumbleEffect.Builder rumble = effects.RumbleBothMotorsOpp();

        gamepad2.runRumbleEffect(rumble.build());
        gamepad1.runRumbleEffect(rumble.build());

        //LED effects (also for funsies)
        double AddValue = .01;
        int DurationMs = 10;
        Gamepad.LedEffect.Builder Led = effects.RGBGradient(AddValue, DurationMs);
        Led.setRepeating(true);
        gamepad1.runLedEffect(Led.build());

        // Wait for the game to start (driver presses START)

        telemetry.update();
        List<LynxModule> Hubs = hardwareMap.getAll(LynxModule.class); //I took this lynx thingy from gm0's bulk reads page https://gm0.org/en/latest/docs/software/tutorials/bulk-reads.html

        for (LynxModule hub : Hubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }
        /*Thread gamepad2Thread = new Thread( new Gamepad2Thread() );
        gamepad2Thread.setDaemon(false);
        gamepad2Thread.start();*/

        /*while (!GP2Initialized) {
            // this is only here to avoid a race condition with the telemetry.addDatas (in GP2 thread and the telemetry line below).
            // (im not sure if addData is threadsafe)
            sleep(100);
        }*/
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        telemetry.addData("Status", "Initialized");
        runtime.reset();
        waitForStart();

        // run until the end of the match (driver presses STOP)

        double LowerPowerBy = 1;
        boolean LowerModeToggle = false;
        double YawOffsetDEG = 0;
        Buttons ButtonMonitor = new Buttons(false);
        setZPBrake();
        boolean ZPFloatToggle = false;
        double lastHeading = 0;
        BHI260AP.resetYaw();
        while (opModeIsActive()) {
            //previousGamepad1.copy(currentGamepad1); //gamepad from last iteration
            //currentGamepad1.copy(gamepad1);

            for (LynxModule hub : Hubs) {
                hub.clearBulkCache();
            }

            ButtonMonitor.update();
            HeadingPID.setKi(Ki);
            HeadingPID.setKp(Kp);

            int rightBackDriveEncoderPos  = rightBackDrive.getCurrentPosition();
            int leftBackDriveEncoderPos   = leftBackDrive.getCurrentPosition();
            int frontEncoderPos           = frontDrive.getCurrentPosition();

            if (ButtonMonitor.wasPressed(buttonName.options)) {
                ZPFloatToggle = !ZPFloatToggle;
            }
            if (ZPFloatToggle) {
                setZPFloat();
            } else {
                setZPBrake();
            }

            if (ButtonMonitor.wasPressed(buttonName.share)){
                LowerModeToggle = !LowerModeToggle;
            }
            if (LowerModeToggle) {
                LowerPowerBy = LowerBy;
            }
            else {
                LowerPowerBy = LowerByDef;
            }
            if (ButtonMonitor.isPressed(buttonName.share)){
                LowerPowerBy = 2;
            }

            // Rising Edge Detector to dance
            // while ((gamepad1.left_stick_button && gamepad1.right_stick_button) && !(previousGamepad1.left_stick_button && previousGamepad1.right_stick_button)) {
            //     happyDanceRobot();
            // }

            double max;
            // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
            /*
            double axial   = -gamepad1.left_stick_y;  // Note: pushing stick forward gives negative value
            double lateral =  gamepad1.left_stick_x;
            double yaw     =  gamepad1.right_stick_x;
            */
            /*
            1) Axial:    Driving forward and backward               Left-joystick Forward/Backward
            2) Lateral:  Strafing right and left                     Left-joystick Right and Left
            3) Yaw:      Rotating Clockwise and counter clockwise    Right-joystick Right and Left
            */

            // Combine the joystick requests for each axis-motion to determine each wheel's power.
            // Set up a variable for each drive wheel to save the power level for telemetry.
            /*double leftFrontPower  = axial + lateral + yaw;
            double rightFrontPower = axial - lateral - yaw;
            double leftBackPower   = axial - lateral + yaw;
            double rightBackPower  = axial + lateral - yaw;*/

            // hypotenuse = power
            // slope = direction
            double lefty = -gamepad1.left_stick_y;  // Note: pushing stick forward gives negative value
            double leftx = gamepad1.left_stick_x;
            double hypotenuse = Math.sqrt(  Math.pow(leftx, 2)+Math.pow(lefty, 2)  ); //pythagorean theorem

            /*
            slope is basically the direction the robot is gonna go
            hypotenuse is power

            slope * hypotenuse is power for each wheel (hopefully)
            */

            /*double leftBackPower   = hypotenuse;
            double rightBackPower  = hypotenuse;
            double frontPower = hypotenuse;*/

            //https://gm0.org/en/latest/_images/mecanum-drive-directions.png
            //https://cdn11.bigcommerce.com/s-x56mtydx1w/images/stencil/original/products/1445/7196/3213-3606-0002-Product-Insight-3__67245__45972.1701993091.png?c=1

            //https://youtu.be/gnSW2QpkGXQ?si=lnVXFP7B3FyuYVPt - this video is EXTREMELY helpful
            //https://seamonsters-2605.github.io/archive/mecanum/ - this website i found from reddit is helpful too

            Orientation robotOrientation = BHI260AP.getRobotOrientation(
                    AxesReference.INTRINSIC,
                    AxesOrder.XYZ,
                    AngleUnit.DEGREES
            );
            double theta = Math.atan2(lefty, leftx);
            double Roll  = robotOrientation.firstAngle; // X - Roll
            double Pitch = robotOrientation.secondAngle; // Y - Pitch
            double Yaw   = robotOrientation.thirdAngle; // Z - Yaw
            Yaw = Yaw<0? Yaw+360: Yaw;
            if (ButtonMonitor.wasPressed(buttonName.cross))
                YawOffsetDEG = resetYawDEG();
            if (ButtonMonitor.wasPressed(buttonName.square))
                YawOffsetDEG = resetYawDEG();
            if (ButtonMonitor.wasPressed(buttonName.circle))
                YawOffsetDEG = 0;
            if (ButtonMonitor.wasPressed(buttonName.triangle))
                BHI260AP.resetYaw();

            double yawOffsetRAD = Math.toRadians(YawOffsetDEG);
            double Direction1 = Math.sin(theta + Math.PI/4 - yawOffsetRAD); // https://www.desmos.com/calculator/rqqamhfeek
            double Direction2 = Math.sin(theta - Math.PI/4 - yawOffsetRAD); // https://www.desmos.com/calculator/dminewe5vs
            double Direction3 = Math.cos(theta - yawOffsetRAD); //this probably will work or maybe not, idk

            //https://www.desmos.com/calculator/5h9hzufufh

            double rightBackPower  = Direction1 * hypotenuse;
            double leftBackPower   = Direction2 * hypotenuse;
            double frontPower      = Direction3 * hypotenuse;

            // TODO: add dpad stuff again, but with gyroscope stuff
            //if (gamepad1.dpad_up) {
            //    //sin(Math.PI/2) is 1
            //    double RAD = Math.PI/2;
            //    double Dpadirection1 = Math.sin(RAD + Math.PI/4 - YawOfb fset);
            //    double Dpadirection2 = Math.sin(RAD - Math.PI/4 - YawOffset);
            //    // Dpadirection1 is Dpadirection2
            //    leftFrontPower  = Dpadirection1 * hypotenuse; // 1
            //    rightFrontPower = Dpadirection2 * hypotenuse; // 1
            //    leftBackPower   = Dpadirection2 * hypotenuse; // 1
            //    rightBackPower  = Dpadirection1 * hypotenuse; // 1
            //} else if (gamepad1.dpad_down) {
            //    double RAD = Math.PI*3.0/2;
            //    double Dpadirection1 = Math.sin(RAD + Math.PI/4 - YawOffset);
            //    double Dpadirection2 = Math.sin(RAD - Math.PI/4 - YawOffset);
            //    // Dpadirection1 is Dpadirection2
            //    leftFrontPower  = Dpadirection1 * hypotenuse; // -1
            //    rightFrontPower = Dpadirection2 * hypotenuse; // -1
            //    leftBackPower   = Dpadirection2 * hypotenuse; // -1
            //    rightBackPower  = Dpadirection1 * hypotenuse; // -1
            //} else if (gamepad1.dpad_left) {
            //    double RAD = Math.PI*3.0/2;
            //    double RAD2 = Math.PI/2;
            //    double Dpadirection1 = Math.sin(RAD + Math.PI/4 - YawOffset);  // -1
            //    double Dpadirection2 = Math.sin(RAD2 - Math.PI/4 - YawOffset); //  1
            //    leftFrontPower  = Dpadirection1 * hypotenuse; // -1
            //    rightFrontPower = Dpadirection2 * hypotenuse; //  1
            //    leftBackPower   = Dpadirection2 * hypotenuse; //  1
            //    rightBackPower  = Dpadirection1 * hypotenuse; // -1
            //} else if (gamepad1.dpad_right) {
            //    double RAD = Math.PI/2;
            //    double RAD2 = Math.PI*3.0/2;
            //    double Dpadirection1 = Math.sin(RAD + Math.PI/4 - YawOffset);  // -1
            //    double Dpadirection2 = Math.sin(RAD2 - Math.PI/4 - YawOffset); //  1
            //    leftFrontPower  = Dpadirection1 * hypotenuse; //  1
            //    rightFrontPower = Dpadirection2 * hypotenuse; // -1
            //    leftBackPower   = Dpadirection2 * hypotenuse; // -1
            //    rightBackPower  = Dpadirection1 * hypotenuse; //  1
            //}

            leftBackPower -= gamepad1.left_trigger;
            rightBackPower += gamepad1.left_trigger;
            frontPower -= gamepad1.left_trigger/LowerPowerBy;

            leftBackPower += gamepad1.right_trigger;
            rightBackPower -= gamepad1.right_trigger;
            frontPower += gamepad1.right_trigger/LowerPowerBy;

            if (ButtonMonitor.wasPressed(buttonName.right_bumper)) {
                leftBackPower   = 1;
                rightBackPower  = -1;
                frontPower      = -1;
            }
            else if (ButtonMonitor.wasPressed(buttonName.left_bumper)) {
                leftBackPower   = -1;
                rightBackPower  = 1;
                frontPower      = 1;
            }
            YawOffsetDEG = resetYawDEG();

            if (ButtonMonitor.Pressed(buttonName.left_stick_button)) //make the other controller rumble
                gamepad2.rumble(100);
            if (ButtonMonitor.Pressed(buttonName.right_stick_button)) //stop rumbles
                gamepad1.stopRumble();

            /*leftBackPower  *= 2;
            rightBackPower *= 2;
            frontPower     *= 2;*/

            // Normalize the values so no wheel  power exceeds 100%
            // This ensures that the robot maintains the desired motion.
            max = Math.max(Math.abs(frontPower), Math.abs(leftBackPower));
            max = Math.max(max, Math.abs(rightBackPower));

            if (max > 1.0) {
                leftBackPower   /= max;
                rightBackPower  /= max;
                frontPower      /= max;
            }

            leftBackPower  /= LowerPowerBy;
            rightBackPower /= LowerPowerBy;
            frontPower     /= LowerPowerBy;

            // This is test code:
            //
            // Uncomment the following code to test your motor directions.
            // Each button should make the corresponding motor run FORWARD.
            //   1) First get all the motors to take to correct positions on the robot
            //      by adjusting your Robot Configuration if necessary.
            //   2) Then make sure they run in the correct direction by modifying the
            //      the setDirection() calls above.
            // Once the correct motors move in the correct direction re-comment this code.

            /*
            leftFrontPower  = gamepad1.x ? 1.0 : 0.0;  // X gamepad
            leftBackPower   = gamepad1.a ? 1.0 : 0.0;  // A gamepad
            rightFrontPower = gamepad1.y ? 1.0 : 0.0;  // Y gamepad
            rightBackPower  = gamepad1.b ? 1.0 : 0.0;  // B gamepad
            */

            // Send calculated power to wheels


            if (hypotenuse>=.1){
                frontDrive.setPower(frontPower);
                leftBackDrive.setPower(leftBackPower);
                rightBackDrive.setPower(rightBackPower);
                lastHeading = Yaw;
            }
            else {
                double a = PIDHeadingCorrect(lastHeading, false);
                frontDrive.setPower(a);
                leftBackDrive.setPower(leftBackPower+a);
                rightBackDrive.setPower(rightBackPower-a);
            }

            // Show the elapsed game time and wheel power.

            telemetry.addData("Theta value\t", "%4.2f", theta);
            telemetry.addData("Hypotenuse value\t", "%4.2f", hypotenuse);
            telemetry.addData("Theta value (deg)\t", "%4.2f", (theta*(180/Math.PI)+360)%360);
            telemetry.addLine();
            telemetry.addData("X - Roll\t", "%4.2f", Roll);
            telemetry.addData("Y - Pitch\t", "%4.2f", Pitch);
            telemetry.addData("Z - Yaw\t", "%4.2f", Yaw);
            telemetry.addLine();
            telemetry.addData("lastHeading\t", "%4.2f", lastHeading);
            telemetry.addData("Directions", "%4.2f %4.2f %4.2f", Direction1, Direction2,  Direction3);
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Pressed, Low Power Mode?", "%s %s", ButtonMonitor.buttonMap.get(buttonName.share).toString(), LowerModeToggle? "yuh" : "nuh");
            telemetry.addData("Pressed, Float Mode?", "%s %s", ButtonMonitor.buttonMap.get(buttonName.options).toString(), ZPFloatToggle? "Float" : "Brake");
            telemetry.addData("Front ", "%4.2f", frontPower);
            telemetry.addData("Back  left/Right", "%4.2f, %4.2f", leftBackPower, rightBackPower);
            telemetry.addData("Front Encoder", "%d", frontEncoderPos);
            telemetry.addData("Back left/Right Encoders","%d, %d", rightBackDriveEncoderPos, leftBackDriveEncoderPos);
            //telemetry.addData("2m Dis", "Inch: %1.3f, Cm: %1.3f", REV2mDistance.getDistance(DistanceUnit.INCH), REV2mDistance.getDistance(DistanceUnit.CM));
            telemetry.update();
        }
    }

    private void setZPFloat() {
        leftBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        rightBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }
    private void setZPBrake() {
        leftBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public double PIDHeadingCorrect(double tarAngle, boolean isRad){
        if (isRad){
            tarAngle = Math.toDegrees(tarAngle);
        }
        tarAngle += 360;

        double Yaw = BHI260AP.getRobotOrientation(
                AxesReference.INTRINSIC,
                AxesOrder.XYZ,
                AngleUnit.DEGREES
        ).thirdAngle;
        Yaw = Yaw<0? Yaw+360: Yaw;
        Yaw += 360;
        double speed = HeadingPID.update(tarAngle, Yaw);
        return speed > 1 ? 1 : (speed < -1 ? -1 : speed);
    }

    private double resetYawDEG() {
        double Yaw = BHI260AP.getRobotOrientation(
                AxesReference.INTRINSIC,
                AxesOrder.XYZ,
                AngleUnit.DEGREES
        ).thirdAngle;
        Yaw = Yaw<0? Yaw+360: Yaw;
        return Yaw;
    }

    // This button status thing is inspired by u/m0stlyharmless_user and u/fusionforscience on reddit from a post 8y ago :)
    // https://www.reddit.com/r/FTC/comments/5lpaai/comment/dbye175/?utm_source=share&utm_medium=web3x&utm_name=web3xcss&utm_term=1&utm_content=share_button
    // https://www.reddit.com/r/FTC/comments/5lpaai/comment/dcerspj/?utm_source=share&utm_medium=web3x&utm_name=web3xcss&utm_term=1&utm_content=share_button
    enum Status {
        notPressedYet,
        currentlyPressed,
        wasPressed
    }
    enum buttonName {
        options,
        triangle,
        share,
        cross,
        square,
        circle,
        left_stick_button,
        right_stick_button,
        dpad_left,
        dpad_right,
        dpad_up,
        dpad_down,
        right_bumper,
        left_bumper
    }
    enum Gpads { //GP stands for gamepad
        GP1,
        GP2
    }
    private class Buttons {
        public  final EnumMap<buttonName, Status> buttonMap     = new EnumMap<buttonName, Status>(buttonName.class);
        private final EnumMap<buttonName, Button> ButtonStorage = new EnumMap<buttonName, Button>(buttonName.class);
        private Gpads GP;
        private Gamepad gpad = new Gamepad();
        private boolean[] buttonList;

        public Buttons(boolean isGamepad2) {
            GP = Gpads.GP1;
            if (isGamepad2) {
                GP = Gpads.GP2;
            }
            for (buttonName button : buttonName.values()) {
                ButtonStorage.put(button, new Button());
            }
        }

        public boolean Pressed(buttonName button) {
            return buttonMap.get(button) == Status.wasPressed || buttonMap.get(button) == Status.currentlyPressed;
        }

        public boolean wasPressed(buttonName button) {
            return buttonMap.get(button) == Status.wasPressed;
        }

        public boolean isPressed(buttonName button) {
            return buttonMap.get(button) == Status.currentlyPressed;
        }

        public boolean NotPressed(buttonName button){
            return buttonMap.get(button) == Status.notPressedYet;
        }

        public void update(){
            if (GP == Gpads.GP1) {
                gpad.copy(gamepad1);
            }
            else {
                gpad.copy(gamepad2);
            }
            buttonList = new boolean[] {gpad.options, gpad.triangle, gpad.share, gpad.cross, gpad.square, gpad.circle, gpad.left_stick_button, gpad.right_stick_button, gpad.dpad_left, gpad.dpad_right, gpad.dpad_up, gpad.dpad_down, gpad.right_bumper, gpad.left_bumper};
            buttonName[] ButtonArr = buttonName.values();
            for (int i=0; i<buttonList.length; i++) {
                buttonMap.put(ButtonArr[i], ButtonStorage.get(ButtonArr[i]).ButtonStatus(buttonList[i]));
            }
        }

        private class Button { //class in a class in a class for funsies
            private Status status = Status.notPressedYet;
            public Status ButtonStatus(boolean button) {
                if ( (button && status == Status.notPressedYet))
                    status = Status.currentlyPressed;
                else if (!button && status == Status.currentlyPressed)
                    status = Status.wasPressed;
                else if (status == Status.wasPressed)
                    status = Status.notPressedYet;
                return status;
            }
        }
    }
}