package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "Direction Tester")
public class DirectionTester extends OpMode {
    private ElapsedTime runtime           = new ElapsedTime();
    private DcMotor leftBackDrive         = null;
    private DcMotor rightBackDrive        = null;
    private DcMotor frontDrive          = null;


    /**
     * User-defined init method
     * <p>
     * This method will be called once, when the INIT button is pressed.
     */

    @Override
    public void init() {
        rightBackDrive  = hardwareMap.get(DcMotor.class, "rightBack"); //Motor 2 = right bottom
        leftBackDrive   = hardwareMap.get(DcMotor.class, "leftBack"); //Motor 3 = left bottom
        frontDrive      = hardwareMap.get(DcMotor.class, "front");
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);
        frontDrive.setDirection(DcMotor.Direction.REVERSE);

        rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        rightBackDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    /**
     * User-defined loop method
     * <p>
     * This method will be called repeatedly during the period between when
     * the play button is pressed and when the OpMode is stopped.
     */
    @Override
    public void loop() {
        leftBackDrive.setPower(gamepad1.square? 1: 0);
        rightBackDrive.setPower(gamepad1.circle? 1: 0);
        frontDrive.setPower(gamepad1.triangle? 1: 0); //should go left
    }
}
