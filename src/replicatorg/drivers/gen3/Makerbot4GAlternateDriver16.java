package replicatorg.drivers.gen3;

import java.util.EnumMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import javax.vecmath.Point3d;

import org.w3c.dom.Element;

import replicatorg.app.Base;
import replicatorg.drivers.RetryException;
import replicatorg.machine.model.AxisId;
import replicatorg.machine.model.MachineModel;
import replicatorg.machine.model.ToolModel;
import replicatorg.util.Point5d;

public class Makerbot4GAlternateDriver16 extends Makerbot4GAlternateDriver {
	
	public String getDriverName() {
		return "Makerbot4GAlternate16";
	}

	protected void queueAbsolutePoint(Point5d steps, long micros) throws RetryException {
		// Turn on fan if necessary
		for (AxisId axis : getHijackedAxes()) {
			if (steps.axis(axis) != 0) {
				enableStepperExtruderFan(true);
			}
		}

		PacketBuilder pb = new PacketBuilder(MotherboardCommandCode.QUEUE_POINT_ABS_16.getCode());

		if (Base.logger.isLoggable(Level.FINE)) {
			Base.logger.log(Level.FINE,"Queued absolute point " + steps + " at "
					+ Long.toString(micros) + " usec.");
		}

		// just add them in now.
		pb.add16((short) steps.x());
		pb.add16((short) steps.y());
		pb.add16((short) steps.z());
		pb.add16((short) steps.a());
		pb.add16((short) steps.b());
		pb.add32((int) micros);

		runCommand(pb.getPacket());
	}

	protected void queueNewPoint(Point5d steps, long us, int relative) throws RetryException {

		// Turn on fan if necessary
		for (AxisId axis : getHijackedAxes()) {
			if (steps.axis(axis) != 0) {
				enableStepperExtruderFan(true);
			}
		}
		PacketBuilder pb = new PacketBuilder(MotherboardCommandCode.QUEUE_POINT_NEW_16.getCode());

		if (Base.logger.isLoggable(Level.FINE)) {
			Base.logger.log(Level.FINE,"Queued new-style point " + steps + " over "
					+ Long.toString(us) + " usec., relative " + Integer.toString(relative));
		}

		// just add them in now.
		pb.add16((short) steps.x());
		pb.add16((short) steps.y());
		pb.add16((short) steps.z());
		pb.add16((short) steps.a());
		pb.add16((short) steps.b());
		pb.add32((int) us);
		pb.add8((int) relative);

		runCommand(pb.getPacket());
	}


	public void setCurrentPosition(Point5d p) throws RetryException {
		PacketBuilder pb = new PacketBuilder(MotherboardCommandCode.SET_POSITION_16.getCode());

		Point5d steps = machine.mmToSteps(p);
		pb.add16((short) steps.x());
		pb.add16((short) steps.y());
		pb.add16((short) steps.z());
		pb.add16((short) steps.a());
		pb.add16((short) steps.b());

		Base.logger.log(Level.FINE,"Set current position to " + p + " (" + steps
					+ ")");

		runCommand(pb.getPacket());
	
		// Set the current position explicitly instead of calling the super, to avoid sending the current position command twice.
		currentPosition.set(p);
//		super.setCurrentPosition(p);
	}

	protected Point5d reconcilePosition() {
		// If we're writing to a file, we can't actually know what the current position is.
		if (fileCaptureOstream != null) {
			return null;
		}
		PacketBuilder pb = new PacketBuilder(MotherboardCommandCode.GET_POSITION_16.getCode());
		PacketResponse pr = runQuery(pb.getPacket());
		Point5d steps = new Point5d(pr.get16(), pr.get16(), pr.get16(), pr.get16(), pr.get16());
//		Base.logger.fine("Reconciling : "+machine.stepsToMM(steps).toString());
		return machine.stepsToMM(steps);
	}
	


	
}
