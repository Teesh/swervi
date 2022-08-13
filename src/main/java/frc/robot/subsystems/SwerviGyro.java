package frc.robot.subsystems;

import edu.wpi.first.hal.SimDevice;
import edu.wpi.first.hal.SimDouble;
import edu.wpi.first.hal.SimDevice.Direction;
import edu.wpi.first.wpilibj.interfaces.Gyro;

public class SwerviGyro implements Gyro {
  @Override
  public void close() throws Exception {}

  @Override
  public void calibrate() {}

  @Override
  public void reset() {
      m_offsetX = getAngleX();
      m_offsetY = getAngleY();
      m_offsetZ = getAngleZ();
  }

  @Override
  public double getAngle() {
      return getAngleZ();
  }

  @Override
  public double getRate() {
      return getRateZ();
  }

  private final SimDouble m_angleX;
  private final SimDouble m_angleY;
  private final SimDouble m_angleZ;
  private final SimDouble m_rateX;
  private final SimDouble m_rateY;
  private final SimDouble m_rateZ;

  private double m_offsetX = 0;
  private double m_offsetY = 0;
  private double m_offsetZ = 0;


  public SwerviGyro() {
    SimDevice m_gyro = SimDevice.create("Gyro:SwerviGyro");
    m_angleX = m_gyro.createDouble("angle_x", Direction.kInput, 0.0);
    m_angleY = m_gyro.createDouble("angle_y", Direction.kInput, 0.0);
    m_angleZ = m_gyro.createDouble("angle_z", Direction.kInput, 0.0);
    m_rateX = m_gyro.createDouble("rate_x", Direction.kInput, 0.0);
    m_rateY = m_gyro.createDouble("rate_y", Direction.kInput, 0.0);
    m_rateZ = m_gyro.createDouble("rate_z", Direction.kInput, 0.0);
  }

  public double getAngleX() {
    return m_angleX.get() - m_offsetX;
  }

  public double getAngleY() {
    return m_angleY.get() - m_offsetY;
  }

  public double getAngleZ() {
    return m_angleZ.get() - m_offsetZ;
  }

  public double getRateX() {
    return m_rateX.get();
  }

  public double getRateY() {
    return m_rateY.get();
  }

  public double getRateZ() {
    return m_rateZ.get();
  } 
}