// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import frc.robot.Constants;
import frc.robot.Constants.ModuleConstants;

public class SwerveModule {
  private final Spark m_driveMotor;
  private final Spark m_turningMotor;

  private final Encoder m_driveEncoder;
  private final Encoder m_turningEncoder;

  private final PIDController m_drivePIDController = new PIDController(
      1.0,
      0.0,
      0.0
  );

  /**
   * Constructs a SwerveModule.
   *
   * @param driveMotorChannel   ID for the drive motor.
   * @param turningMotorChannel ID for the turning motor.
   */
  public SwerveModule(
      int driveMotorChannel,
      int turningMotorChannel,
      int[] driveEncoderPorts,
      int[] turningEncoderPorts,
      boolean driveMotorReversed,
      boolean turningMotorReversed,
      boolean driveEncoderReversed,
      boolean turningEncoderReversed) {
    m_driveMotor = new Spark(driveMotorChannel);
    m_turningMotor = new Spark(turningMotorChannel);

    m_driveMotor.setInverted(driveMotorReversed);
    m_turningMotor.setInverted(turningMotorReversed);

    this.m_turningEncoder = new Encoder(turningEncoderPorts[0], turningEncoderPorts[1]);
    this.m_driveEncoder = new Encoder(driveEncoderPorts[0], driveEncoderPorts[1]);

    // Set the distance per pulse for the drive encoder. We can simply use the
    // distance traveled for one rotation of the wheel divided by the encoder
    // resolution.
    m_driveEncoder.setDistancePerPulse(ModuleConstants.kDriveEncoderDistancePerPulse);

    // Set whether drive encoder should be reversed or not
    m_driveEncoder.setReverseDirection(driveEncoderReversed);

    // Set the distance (in this case, angle) per pulse for the turning encoder.
    // This is the the angle through an entire rotation (2 * pi) divided by the
    // encoder resolution.
    m_turningEncoder.setDistancePerPulse(ModuleConstants.kTurningEncoderDistancePerPulse);

    // Set whether turning encoder should be reversed or not
    m_turningEncoder.setReverseDirection(turningEncoderReversed);

    // Limit the PID Controller's input range between -pi and pi and set the input
    // to be continuous.

    // m_turningPIDController.setTolerance(0.025);
  }

  /**
   * Returns the current state of the module.
   *
   * @return The current state of the module.
   */
  public SwerveModuleState getState() {
    return new SwerveModuleState(m_driveEncoder.getRate(), new Rotation2d(m_turningEncoder.get()));
  }

  /**
   * Sets the desired state for the module.
   *
   * @param desiredState Desired state with speed and angle.
   */
  public void setDesiredState(SwerveModuleState desiredState) {
    // Optimize the reference state to avoid spinning further than 90 degrees

    SwerveModuleState state = desiredState;
    state = SwerveModuleState.optimize(desiredState, new Rotation2d(-m_turningEncoder.getDistance()));

    // Calculate the drive output from the drive PID controller.
    double driveOutput = m_drivePIDController.calculate(m_driveEncoder.getRate(), state.speedMetersPerSecond);

    // System.out.println("\n" + m_turningEncoder.getFPGAIndex()/2 + "\n\t" + m_driveEncoder.getRate() + "\n\t" + m_drivePIDController.atSetpoint() + "\n\t" + driveOutput + "\n\t" + state.speedMetersPerSecond);

    System.out.println(m_turningEncoder.getFPGAIndex()/2 + "\n\t" + m_turningEncoder.getDistance() + "\n\t" + state.angle.getRadians());
    
    if (m_drivePIDController.atSetpoint()) m_driveMotor.set(0.0);
    else m_driveMotor.set(driveOutput);

    // System.out.println(state.angle.getRadians() / ModuleConstants.kTurningEncoderDistancePerPulse);

    m_turningMotor.set(state.angle.getRadians() / 2 / Math.PI);

    // if (m_turningPIDController.atSetpoint()) {
    //   m_turningPIDController.reset();
    //   m_turningMotor.set(0.0);
    // } else {
    //   m_turningMotor.set(turnOutput);
    // }
  }

  /** Zeros all the SwerveModule encoders. */
  public void resetEncoders() {
    m_driveEncoder.reset();
    m_turningEncoder.reset();
  }
}
