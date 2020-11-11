package org.firstinspires.ftc.teamcode.Qualifier_1;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Qualifier_1.Components.Accesories.Intake;
import org.firstinspires.ftc.teamcode.Qualifier_1.Components.Accesories.WobbleGoal;
import org.firstinspires.ftc.teamcode.Qualifier_1.Components.Chassis;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.teamcode.Qualifier_1.Components.Navigations.VuforiaWebcam;
import org.firstinspires.ftc.teamcode.Qualifier_1.Components.ObjectDetection.TensorFlow;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.opMode;

public class Robot {
    private LinearOpMode op = null;
    private HardwareMap hardwareMap = null;
    private ElapsedTime runtime = new ElapsedTime();
    Chassis drivetrain = new Chassis();
    TensorFlow tensorFlow = new TensorFlow(op);
    private Intake intake = null;

    private VuforiaWebcam vuforiaWebcam = null;
    private double vuforiaX = 0;
    private double vuforiaY = 0;
    private double vuforiaAngle = 0;
    private double robotAngle = 0;
    private WobbleGoal wobbleGoal;

    public Robot(LinearOpMode opMode) {
        intake = new Intake(opMode);
    }

    public Robot() {

    }

    public void initChassis(LinearOpMode opMode) {
        op = opMode;
        hardwareMap = op.hardwareMap;

        //Initialize Motors
        DcMotorEx motorLeftFront;
        DcMotorEx motorRightFront;
        DcMotorEx motorLeftBack;
        DcMotorEx motorRightBack;
        DcMotorEx ShooterMotor;
        DcMotorEx wobbleGoalMotor;

        motorLeftFront = (DcMotorEx) hardwareMap.dcMotor.get("motorLeftFront");
        motorRightFront = (DcMotorEx) hardwareMap.dcMotor.get("motorRightFront");
        motorLeftBack = (DcMotorEx) hardwareMap.dcMotor.get("motorLeftBack");
        motorRightBack = (DcMotorEx) hardwareMap.dcMotor.get("motorRightBack");
        ShooterMotor = (DcMotorEx) hardwareMap.dcMotor.get("ShooterMotor");
        wobbleGoalMotor = (DcMotorEx) hardwareMap.dcMotor.get("wobbleGoalMotor");

        //vuforiaWebcam = new VuforiaWebcam(op, VuforiaLocalizer.CameraDirection.BACK);

        drivetrain.init(opMode);
        //vuforiaWebcam.init(opMode);

        this.wobbleGoal = new WobbleGoal(op);
        intake = new Intake(op);

        //vuforiaWebcam.start();
//        getVuforiaPosition();
//        op.telemetry.addData("Position","%.2f %.2f %.2f %.2f", vuforiaX, vuforiaY, vuforiaAngle, robotAngle);
//        op.telemetry.update();
//        op.sleep(1000);

    }

    public void moveVuforiaWebcam(double x, double y, double endAngle) {
        getVuforiaPosition();

        double xdifference = x - vuforiaX;
        double ydifference = y - vuforiaY;

        double turn = endAngle - robotAngle;

        double magnitude = Math.sqrt((xdifference * xdifference) + (ydifference * ydifference));

        double mAngle = robotAngle - Math.toDegrees(Math.acos(ydifference/magnitude)); //move Angle

        op.telemetry.addData("VuforiaX","%.2f %.2f %.2f %.3f %.3f %.3f", vuforiaX, x, xdifference, robotAngle, vuforiaAngle, endAngle );
        op.telemetry.addData("VuforiaY","%.2f %.2f %.2f %.2f %.3f %.3f", vuforiaY, y, ydifference, magnitude, turn, mAngle);
        op.telemetry.update();
        op.sleep(5000);
        drivetrain.moveAngle2(magnitude, mAngle, turn);

        getVuforiaPosition();

        op.telemetry.addData("VuforiaX","%.2f %.2f %.2f %.3f %.3f %.3f", vuforiaX, x, xdifference, robotAngle, vuforiaAngle, endAngle );
        op.telemetry.addData("VuforiaY","%.2f %.2f %.2f %.2f %.3f %.3f", vuforiaY, y, ydifference, magnitude, turn, mAngle);
        op.telemetry.update();
        op.sleep(5000);
    }

    public void stopAllMotors() {
        drivetrain.stopAllMotors();
    }

    public void stopAllMotorsSideways() {
        drivetrain.stopAllMotorsSideways();
    }


    /******** Left Front Motor **********/
    public void moveMotorLeftFront(double distance) {
        drivetrain.moveMotorLeftFront(distance);
    }

    /******** Right Front Motor **********/
    public void moveMotorRightFront(double distance) {
        drivetrain.moveMotorRightFront(distance);
    }

    /******** Left Back Motor **********/
    public void moveMotorLeftBack(double distance) {
        drivetrain.moveMotorLeftBack(distance);
    }

    /******** Right Back Motor **********/
    public void moveMotorRightBack(double distance) {
        drivetrain.moveMotorRightBack(distance);
    }

    /******** shooterMotor **********/
    public void moveShooterMotor(double distance) {
        drivetrain.moveShooterMotor(distance);
    }
    /******** shooterMotor **********/
    public void moveWobbleGoalMotor(double distance) {
        drivetrain.moveWobbleGoalMotor(distance);
    }

    public double getAngle() {
        return drivetrain.getAngle();
    }


    /**
     * Directional Movement
     **/
    public void moveForward(double distance, double power) {
        drivetrain.moveForward(distance, power);
    }

    public void moveForwardIMU(double distance, double power) {
        drivetrain.moveForwardIMU(distance, power);
    }

    public void moveBackward(double distance, double power) {
        drivetrain.moveBackward(distance, power);
    }

    public void moveBackwardIMU(double distance, double power) {
        drivetrain.moveBackwardIMU(distance, power);
    }

    public void moveRight(double distance, double power) {
        drivetrain.moveRight(distance, power);
    }

    public void moveRightIMU(double distance, double power, double startingAngle, double gain, double maxCorrection) {
        drivetrain.moveRightIMU(distance, power, startingAngle, gain, maxCorrection);
    }

    public void moveLeft(double distance, double power) {
        drivetrain.moveLeft(distance, power);
    }

    public void moveLeftIMU(double distance, double power, double startingAngle, double gain, double maxCorrection) {
        drivetrain.moveLeftIMU(distance, power, startingAngle, gain, maxCorrection);
    }

    public void moveAngle2(double distance, double angle, double turn) {
        drivetrain.moveAngle2(distance, angle, turn);
    }


    /**Vuforia**/

    public double getVuforiaAngle() {
        return vuforiaWebcam.getVuforiaAngle();
    }

    public void getVuforiaPosition() {
        vuforiaX = vuforiaWebcam.getVuforiaX();
        vuforiaY = vuforiaWebcam.getVuforiaY();
        vuforiaAngle = vuforiaWebcam.getVuforiaAngle();
        robotAngle = vuforiaAngle + 90;
        robotAngle = (robotAngle>180?robotAngle-360:robotAngle);
    }
    public void stopVuforia() {
        vuforiaWebcam.interrupt();
    }


    /**TensorFlow**/

    public void initTensorFlow() {

    }

    public void runTensorFlow () {

    }

    public void stopTensorFlow () {

    }

    /**Odometry**/

    public void turnOdometry(double target, double power) {
        drivetrain.turnOdometry(target,power);
    }

    public void moveForwardOdometry(double distance, double power) {
        drivetrain.moveForwardOdometry(distance,power);
    }

    public void moveSideOdometry(double distance, double power) {//right is positive use distance to change direction
        drivetrain.moveSideOdometry(distance,power);
    }


    public void xyPath(double x, double y, double power) {
        drivetrain.xyPath(x,y,power);
    }

    public void StraightxyPath(double x, double y, double power) {
        drivetrain.StraightxyPath(x,y,power);
    }

    public void StraightPointPath(double x, double y, double power) {
        drivetrain.StraightPointPath(x,y,power);
    }

    public void DirectPointPath(double x, double y, double power) {
        drivetrain.DirectPointPath(x,y,power);
    }

    public void DirectxyPath(double x, double y, double power) {
        drivetrain.DirectxyPath(x,y,power);
    }
    public void moveAngleOdometry(double angleInRadians,double x, double y, double power){
        drivetrain.moveAngleOdometry(angleInRadians,x,y,power);
    }


    /**
     * wobble goal methods
     */
    public void moveWobbleGoalClockwise(){

        this.wobbleGoal.clockwise();
    }

    public void moveWobbleGoalCounterClockwise(){

        wobbleGoal.counterClockwise();
    }

    public void stopWobbleGoal(){

        wobbleGoal.stop();
    }

    // intake
    public void startIntake(){
        intake.startIntake();
    }

    public void stopIntake(){
        intake.stopIntake();
    }

}