// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.drive;

import java.util.HashMap;

import javax.lang.model.element.Element;
import javax.swing.text.AbstractDocument.ElementEdit;

import com.pathplanner.lib.PathConstraints;
import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.commands.FollowPathWithEvents;
import com.pathplanner.lib.commands.PPRamseteCommand;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.Constants.AutoConstants;
import frc.robot.Constants.DriveConstants;
import frc.robot.Constants.IntakeConstants;
import frc.robot.commands.manipulator.SetElevatorPositionCommand;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.ElementTransitSubsystem;

public class PathPlannerCommand extends FollowPathWithEvents {
  private DriveSubsystem driveSubsystem;
  private ElementTransitSubsystem elementTransit;

  private static HashMap<String, Command> eventMap = new HashMap<String, Command>();

  public static void initializeCommands(DriveSubsystem driveSubsystem, ElementTransitSubsystem elementTransit) {
    eventMap.put("intakeCone", Commands.run(elementTransit::intakeCone, elementTransit));
    eventMap.put("outtakeCone", Commands.run(elementTransit::outTakeCone, elementTransit));
    eventMap.put("intakeCube", Commands.run(elementTransit::intakeCube, elementTransit));
    eventMap.put("outtakeCube", Commands.run(elementTransit::outTakeCube, elementTransit));

    eventMap.put("elevatorLow", new SetElevatorPositionCommand(IntakeConstants.elevatorLowSetPoint, elementTransit));
    eventMap.put("elevatorMid", new SetElevatorPositionCommand(IntakeConstants.elevatorMidSetPoint, elementTransit));
    eventMap.put("elevatorLow", new SetElevatorPositionCommand(IntakeConstants.elevatorLowSetPoint, elementTransit));
  }

  public static PathPlannerTrajectory loadPathPlannerTrajectory(String path) {
    PathPlannerTrajectory trajectory = PathPlanner.loadPath(path, new PathConstraints(
        AutoConstants.kMaxSpeedMetersPerSecond, AutoConstants.kMaxAccelerationMetersPerSecondSquared));

    return trajectory;
  }

  private static PPRamseteCommand loadPathFollowCommand(String pathName, ElementTransitSubsystem elementTransit,
      DriveSubsystem driveSubsystem) {
    return new PPRamseteCommand(loadPathPlannerTrajectory(pathName),
        driveSubsystem::getPose,
        new RamseteController(AutoConstants.kRamseteB, AutoConstants.kRamseteZeta),
        new SimpleMotorFeedforward(AutoConstants.ksVolts, AutoConstants.kvVoltSecondsPerMeter,
            AutoConstants.kaVoltSecondsSquaredPerMeter),
        DriveConstants.driveKinematics,
        driveSubsystem::getWheelSpeeds,
        new PIDController(AutoConstants.kPDriveVel, 0, 0),
        new PIDController(AutoConstants.kPDriveVel, 0, 0),
        driveSubsystem::tankDriveVolts,
        driveSubsystem, elementTransit);
  }

  private static PPRamseteCommand createPathFollowCommand(PathPlannerTrajectory trajectory,
      ElementTransitSubsystem elementTransit, DriveSubsystem driveSubsystem) {
    return new PPRamseteCommand(trajectory,
        driveSubsystem::getPose,
        new RamseteController(AutoConstants.kRamseteB, AutoConstants.kRamseteZeta),
        new SimpleMotorFeedforward(AutoConstants.ksVolts, AutoConstants.kvVoltSecondsPerMeter,
            AutoConstants.kaVoltSecondsSquaredPerMeter),
        DriveConstants.driveKinematics,
        driveSubsystem::getWheelSpeeds,
        new PIDController(AutoConstants.kPDriveVel, 0, 0),
        new PIDController(AutoConstants.kPDriveVel, 0, 0),
        driveSubsystem::tankDriveVolts,
        driveSubsystem, elementTransit);
  }

  /** Creates a new PathPlannerCommand. */
  public PathPlannerCommand(PathPlannerTrajectory trajectory, ElementTransitSubsystem elementTransit,
      DriveSubsystem driveSubsystem) {
    super(createPathFollowCommand(trajectory, elementTransit, driveSubsystem),
        trajectory.getMarkers(), eventMap);

    this.elementTransit = elementTransit;
    this.driveSubsystem = driveSubsystem;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(elementTransit, driveSubsystem);
  }

  public PathPlannerCommand(String pathName, ElementTransitSubsystem elementTransit, DriveSubsystem driveSubsystem) {
    this(loadPathPlannerTrajectory(pathName), elementTransit, driveSubsystem);
  }
}
