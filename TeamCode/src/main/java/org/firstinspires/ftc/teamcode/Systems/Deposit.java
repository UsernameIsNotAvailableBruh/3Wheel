package org.firstinspires.ftc.teamcode.Systems;

import org.firstinspires.ftc.teamcode.Hardware.Constants.DepositConstants;
import org.firstinspires.ftc.teamcode.Hardware.Hardware;
import org.firstinspires.ftc.teamcode.Systems.Mechaisms.Arm;
import org.firstinspires.ftc.teamcode.Systems.Mechaisms.Claw;
import org.firstinspires.ftc.teamcode.Systems.Mechaisms.DepositSlides;

public class Deposit {
    private Claw claw;
    private Arm arm;
    private DepositSlides slides;

    public enum TargetState {
        transfer,
        specIntake,
        specDepositReady,
        specDepositClipped,
        sampleDeposit;
    }

    public enum ClawState {
        released,
        gripped;
    }

    private TargetState targetState;
    private ClawState clawState;

    public Deposit(Hardware hardware){
        claw = new Claw(hardware);
        arm = new Arm(hardware);
        slides = new DepositSlides(hardware);

    }

    public void goToTransfer() {
        targetState = TargetState.transfer;

        arm.setPosition(DepositConstants.armRightTransferPos);
        slides.setTargetCM(DepositConstants.slideTransferPos);
    }

    public void goToSpecIntake() {
        targetState = TargetState.specIntake;

        arm.setPosition(DepositConstants.armRightSpecIntakePos);
        slides.setTargetCM(DepositConstants.slideSpecIntakePos);
    }

    public void goToSampleDeposit() {
        targetState = TargetState.sampleDeposit;

        arm.setPosition(DepositConstants.armRightSampleDepositPos);
        slides.setTargetCM(DepositConstants.slideSampleDepositPos);
    }

    public void goToSpecDepositReady() {
        targetState = TargetState.specDepositReady;

        arm.setPosition(DepositConstants.armRightSpecDepositPos);
        slides.setTargetCM(DepositConstants.slideSpecDepositReadyPos);
    }

    public void goToSpecClipped() {
        targetState = TargetState.specDepositClipped;

        arm.setPosition(DepositConstants.armRightSpecDepositPos);
        slides.setTargetCM(DepositConstants.slideSpecClippedPos);
    }

    public void release() {
        clawState = ClawState.released;

        claw.setPosition(DepositConstants.clawOpenPos);
    }

    public void grip() {
        clawState = ClawState.gripped;

        claw.setPosition(DepositConstants.clawClosedPos);
    }

    public void update() {
        slides.update();
    }

    public void command() {
        slides.command();
    }

    public TargetState getTargetState() {
        return targetState;
    }

    public ClawState getClawState() {
        return clawState;
    }

    public double getSlidePos() {
        return slides.getPosition();
    }
}
