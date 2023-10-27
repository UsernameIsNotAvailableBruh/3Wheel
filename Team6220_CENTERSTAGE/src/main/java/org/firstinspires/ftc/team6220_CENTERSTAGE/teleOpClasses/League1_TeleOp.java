package org.firstinspires.ftc.team6220_CENTERSTAGE.teleOpClasses;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.team6220_CENTERSTAGE.MecanumDrive;

import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name="League1_TeleOp", group ="amogus")
public class League1_TeleOp extends LinearOpMode {

    // define states for turning, using slides, outtaking

    TurnStates curTurningState = TurnStates.TURNING_MANUAL;
    enum TurnStates {
        TURNING_MANUAL,
        TURNING_90,
        TURNING_FIELD_CENTRIC
    }

    SlideStates curSlideState = SlideStates.SLIDES_MANUAL;
    enum SlideStates {
        SLIDES_MANUAL,
        SLIDES_FULL_EXTEND,
        SLIDES_FULL_RETRACT
    }

    OuttakeStates curOuttakeState = OuttakeStates.OUTTAKE_REFILL;
    enum OuttakeStates {
        OUTTAKE_REFILL,
        OUTTAKE_CLOSED,
        OUTTAKE_DROP_1,
        OUTTAKE_DROP_2
    }

    // toggle for tilting outtake forward and back
    boolean outtakeTiltedForward = false;

    // drive powers, read from input and then manipulated every loop
    double drivePowerX = 0.0;
    double drivePowerY = 0.0;
    double turnPower = 0.0;

    // holds heading from imu read which is done in roadrunner's mecanum drive class for us
    double currentHeading = 0.0;
    double targetHeading = 0.0;

    // constants
    final double TURN_STICK_DEADZONE = 0.01;
    final double TURN_POWER_MULTIPLIER = 1.0;
    final double DRIVE_POWER_X_MULTIPLIER = 1.0;
    final double DRIVE_POWER_Y_MULTIPLIER = 0.7;
    final double MIN_HEADING_ACCURACY = 5.0; // degrees off from target
    final double SLOWMODE_MULTIPLIER = 0.3;

    // useful groups of keycodes
    final GamepadKeys.Button[] BUMPER_KEYCODES = {
            GamepadKeys.Button.LEFT_BUMPER,
            GamepadKeys.Button.RIGHT_BUMPER
    };
    final GamepadKeys.Button[] DPAD_KEYCODES = {
            GamepadKeys.Button.DPAD_UP,
            GamepadKeys.Button.DPAD_LEFT,
            GamepadKeys.Button.DPAD_DOWN,
            GamepadKeys.Button.DPAD_RIGHT
    };

    @Override
    public void runOpMode() throws InterruptedException {

        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));

        GamepadEx gp1 = new GamepadEx(gamepad1);
        GamepadEx gp2 = new GamepadEx(gamepad2);

        waitForStart();

        while (opModeIsActive()) {

            // udpate gamepads and read inputs
            gp1.readButtons();
            gp2.readButtons();

            drivePowerX = gp1.getLeftX() * DRIVE_POWER_X_MULTIPLIER;
            drivePowerY = gp1.getLeftY() * DRIVE_POWER_Y_MULTIPLIER;
            turnPower = gp1.getRightX();

            // get heading from imu in degrees
            currentHeading = drive.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);


            // update turn state
            if (Math.abs(turnPower) > TURN_STICK_DEADZONE) {
                targetHeading = 0.0;
                curTurningState = TurnStates.TURNING_MANUAL;

            } else if (justPressedAny(gp1, BUMPER_KEYCODES) > -1) {
                // sets target to 90 added to current heading
                // if already turning 90, add to target instead of current
                // also limits target angle between -180 and 180
                targetHeading = limitAngle(
                    (curTurningState == TurnStates.TURNING_90 ? targetHeading : currentHeading) +
                    (gp1.wasJustPressed(GamepadKeys.Button.LEFT_BUMPER) ? 90 : -90)
                );
                curTurningState = TurnStates.TURNING_90;

            } else if (justPressedAny(gp1, DPAD_KEYCODES) > -1) {
                // find which dpad button was pressed, use its index * 90 degrees as target
                // up: 0*90=0, left: 1*90=90, down: 2*90=180, right: 3*90=270 (-> -90)
                targetHeading = limitAngle(justPressedAny(gp1, DPAD_KEYCODES) * 90.0);
                curTurningState = TurnStates.TURNING_FIELD_CENTRIC;

            } else if (Math.abs(currentHeading - targetHeading) < MIN_HEADING_ACCURACY) {
                curTurningState = TurnStates.TURNING_MANUAL;
            }

            // use turn state
            switch (curTurningState) {
                case TURNING_MANUAL:
                    turnPower *= TURN_POWER_MULTIPLIER;
                    break;
                case TURNING_90:
                    // falls through to field centric
                case TURNING_FIELD_CENTRIC:
                    // https://www.desmos.com/calculator/zdsmmtbnwf (updated)
                    // flips difference so that it mimics turn stick directions
                    turnPower = clamp(-shortestDifference(currentHeading, targetHeading) / 90.0);
            }

            // check for slowmode
            if (gp1.getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER) > 0.0 ||
                    gp1.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER) > 0.0) {

                drivePowerX *= SLOWMODE_MULTIPLIER;
                drivePowerY *= SLOWMODE_MULTIPLIER;
                turnPower *= SLOWMODE_MULTIPLIER;
            }
            // clamp powers between -1.0 and 1.0
            drivePowerX = clamp(drivePowerX);
            drivePowerY = clamp(drivePowerY);
            turnPower = clamp(turnPower);

            drive.setDrivePowers(new PoseVelocity2d(
                    new Vector2d(
                            -drivePowerY,
                            -drivePowerX
                    ),
                    -turnPower
            ));

            // NOTE /!\ this doesn't work yet as we haven't set it up
            drive.updatePoseEstimate();

            // telemetry

            telemetry.addData("current turning state", curTurningState);
            telemetry.addData("imu reading", currentHeading);
            telemetry.addData("target heading", targetHeading);
            telemetry.addData("turn power", turnPower);
            telemetry.addData("drive power X", drivePowerX);
            telemetry.addData("drive power Y", drivePowerY);

            //telemetry.addData("x", drive.pose.position.x);
            //telemetry.addData("y", drive.pose.position.y);
            //telemetry.addData("heading", drive.pose.heading);

            telemetry.update();
        }
    }



    /**
     * checks all the buttons from an array to find an index of one that's pressed
     * @param gamepad gamepad object to read from
     * @param keycodes array of button enums
     * @return index of first pressed button, none found returns -1
     */
    private static int justPressedAny(GamepadEx gamepad, GamepadKeys.Button[] keycodes) {
        for (int i = 0; i < keycodes.length; i++) {
            if (gamepad.wasJustPressed(keycodes[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * keeps angles between [-180,180] so that it does not exceed possible imu readings
     * tested in https://www.desmos.com/calculator/igccxromri
     * @param angle angle to limit (degrees)
     * @return equivalent angle between -180 and 180
     */
    private static double limitAngle(double angle) {
        return ((angle + 180.0) % 360.0) - 180.0;
    }

    /**
     * finds the shortest distance between two angles in the imu range (-180 to 180)
     * tested in https://www.desmos.com/calculator/bsw432fulz
     * @param current current heading
     * @param target destination heading
     * @return shortest difference current -> target
     */
    private static double shortestDifference(double current, double target) {
        return limitAngle(target - current);
    }

    /**
     * clamp value between a minimum and maximum value
     * @param val value to clamp
     * @param min minimum value allowed
     * @param max maximum value allowed
     * @return clamped value
     */
    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
    /**
     * shortcut for clamping between -1.0 and 1.0
     * @param val value to clamp
     * @return clamped value
     */
    private static double clamp(double val) {
        return clamp(val, -1.0, 1.0);
    }
}
