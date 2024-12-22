package org.firstinspires.ftc.teamcode.Debugging;


import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Hardware.Hardware;
import org.firstinspires.ftc.teamcode.Systems.Drivetrain;


@TeleOp
public class DrivetrainTestOpMode extends OpMode {
    Hardware hardware = new Hardware();
    Drivetrain drivetrain;
    GamepadEx controller;

    @Override
    public void init() {
        hardware.init(hardwareMap);
        controller = new GamepadEx(gamepad1);
        drivetrain = new Drivetrain(hardware, controller);
    }

    @Override
    public void loop() {
        hardware.clearCache();
        drivetrain.update();
        drivetrain.command();

        telemetry.addData("Pos: ", hardware.pinPoint.getPosition());
        telemetry.update();
    }

}
