package strategies;
import java.util.ArrayList;

import automail.Clock;
import automail.PriorityMailItem;
import automail.Robot;
import automail.StorageTube;
import exceptions.TubeFullException;

public class MyRobotBehaviour implements IRobotBehaviour {
	
	private boolean newPriority; // Used if we are notified that a priority item has arrived. 
	private boolean isStrong;
	
	static private int HIGH_PRIORITY = 100;
	static private int LOW_PRIORITY = 10;
	
	
	public MyRobotBehaviour(boolean strong) {
		
		newPriority = false; 
		this.isStrong = strong;
	}
	
	public void startDelivery() {
		newPriority = false;
	}
	
	@Override
    public void priorityArrival(int priority, int weight) {
    	// Oh! A new priority item has arrived.
		// (Why's it telling me the weight?)
		
		int WEAK_ROBOT_MAX_WEIGHT = 2000; 	// the max weight that a weak robot can carry
		boolean isHeavy = weight < WEAK_ROBOT_MAX_WEIGHT;
		
		// Only care about the high priority ones atm
		if (priority == HIGH_PRIORITY) {
			
			if (this.isStrong && isHeavy) {
				// TODO who has to deal with this!
				newPriority = true;
			}
		}
		// else do nothing!
    }
 
	
	/*
	 * (non-Javadoc)
	 * @see strategies.IRobotBehaviour#returnToMailRoom(automail.StorageTube)
	 */
	@Override
	public boolean returnToMailRoom(StorageTube tube) {
		if (tube.isEmpty()) {
			return false; // Empty tube means we are returning anyway
		} else {
			// Assumes that the priority item will always be at the top!
			// Return if we don't have a priority item and a new one came in
			//Boolean priority = hasPriorityItem(tube); 
			Boolean priority = (tube.peek() instanceof PriorityMailItem);
			
			return !priority && newPriority;
		}
	}
	
	
	private boolean hasPriorityItem(StorageTube tube) {
		Boolean priority;
		StorageTube backup = new StorageTube(); 
		
		try {
			while (!tube.isEmpty()) {
				if (tube.peek() instanceof PriorityMailItem) {
					
					while (!backup.isEmpty() && !tube.isFull()) {
							tube.addItem(backup.pop());
					}
					return true;
				}
	
				if (!backup.isFull()) {
					
						backup.addItem(tube.pop());
					}
				}
			} catch (TubeFullException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return false;
	}
}
