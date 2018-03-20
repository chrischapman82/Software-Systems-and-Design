package strategies;

import automail.Building;
import automail.MailItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

/**
 * MyMailPool implements the IMailPool interface. TODO
 * Based on the given sample SimpleMailPool, following the strategy pattern
 * @author Christopher Chapman 760426
 * SWEN30006 {Project A - Mailbot Blues}
 */
public class MyMailPool implements IMailPool, Comparator<MailItem>{
	
	// ArrayLists to store the current pools of mail items, divided into heavy and light
	// priority and non-priority
	private ArrayList<MailItem> nonPriorityPoolHeavy;		
	private ArrayList<MailItem> nonPriorityPoolLight;
	private ArrayList<MailItem> priorityPoolHeavy;
	private ArrayList<MailItem> priorityPoolLight;
	
	// The maximum the weak robot can carry in grams
	private static final int WEAK_ROBOT_MAX_WEIGHT = 2000;	

	public MyMailPool(){
		// Initialises the empty pools of ArrayLists
		nonPriorityPoolHeavy = new ArrayList<>();
		nonPriorityPoolLight = new ArrayList<>();
		priorityPoolHeavy = new ArrayList<>();
		priorityPoolLight = new ArrayList<>();
	}
	
	@Override
	public int compare(MailItem o1, MailItem o2) {
			
			// priority is the most important factor
			if (o1 instanceof PriorityMailItem) {
				
				// If higher priority, then is sorted higher!
				if (o2 instanceof PriorityMailItem) {
					
					// if there's a difference in prio level
					if (((PriorityMailItem) o1).getPriorityLevel() < ((PriorityMailItem) o2).getPriorityLevel()) {
						return -1000;
					}
					// otherewise just compare normally
				}
			}
			
			return o1.getArrivalTime() - o2.getArrivalTime();
	}
	
	// Decides which pool the given mail should be given to
	public void addToPool(MailItem mailItem) {
		// Check whether it has a priority or not
		
		// Classifying the given bots
		boolean is_heavy = mailItem.getWeight() > WEAK_ROBOT_MAX_WEIGHT;
		boolean is_priority = mailItem instanceof PriorityMailItem;
		
		// Passing them off to the other addToPool
		if (is_priority && is_heavy) {
			addToPool(mailItem, priorityPoolHeavy);
		} else if (is_priority && !is_heavy) {
			addToPool(mailItem, priorityPoolLight);
		} else if (!is_priority && is_heavy) {
			addToPool(mailItem, nonPriorityPoolHeavy);
		} else {
			addToPool(mailItem, nonPriorityPoolLight);
		}
	}
	
	/* Adds a given mailItem to the specified pool
	 * 
	 */
	public void addToPool(MailItem mailItem, ArrayList<MailItem> pool) {
		pool.add(mailItem);
		sortPool(pool);
	}
	
	/* Sorts the given pool of mail items
	 * Sorts by:
	 * #1 - Priority. High priority items should always be chosen first
	 * #2 - Delivering closer items first typically decreases the time taken
	 */
	public void sortPool(ArrayList<MailItem> pool) {
		Collections.sort(pool, new Comparator<MailItem>() {
			
			public int compare(MailItem o1, MailItem o2) {
				
				// If the given instances have a priority, compare them
				// A high priority will ALWAYS be a bigger concern than a low prio
				if (o1 instanceof PriorityMailItem) {
					if (o2 instanceof PriorityMailItem) {
						
						if (((PriorityMailItem) o1).getPriorityLevel() < ((PriorityMailItem) o2).getPriorityLevel()) {
							return -1000;
						}
						// otherewise just compare normally
					} else {
						return -10000;
					}
				}
				
				return o1.getDestFloor() - o2.getDestFloor();
				// Older items should go first
				//return o1.getArrivalTime() - o2.getArrivalTime();
			}
		});
	}
	
	/* Chooses the best (from sorting) mail item from the non priority mail pool.
	 * The heavy bot will choose to take from the heavy mail as a prefenece
	 */
	private MailItem getNonPriorityMail(int weightLimit){
		boolean strong_bot = (weightLimit > WEAK_ROBOT_MAX_WEIGHT);	// Checks the type of bot
		
		// If stronger bot, will take from heavy pool
		if (strong_bot) {
			// check the heavy mail first if strong
			if (nonPriorityPoolHeavy.size() > 0) {
				sortPool(nonPriorityPoolHeavy);
				return nonPriorityPoolHeavy.remove(0);
			}
		}
		
		// If heavy is empty, or bot is weak. Take form the Light pool.
		if (nonPriorityPoolLight.size() > 0) {
			sortPool(nonPriorityPoolLight);
			return nonPriorityPoolLight.remove(0);
		} else {
			return null;
		}
	}
	
	
	/* Chooses the best (from sorting) mail item from the priority mail pool.
	 * The heavy bot will choose to take from the heavy mail as a preference
	 */
	private MailItem getHighestPriorityMail(int weightLimit){
		// if strong boy
		boolean strong_bot = (weightLimit > WEAK_ROBOT_MAX_WEIGHT);
		
		if (strong_bot) {
			// check the heavy mail first if strong
			if (priorityPoolHeavy.size() > 0) {
				sortPool(priorityPoolHeavy);
				return priorityPoolHeavy.remove(0);
			}
		}
		
		// both should take priority if they can!
		if (priorityPoolLight.size() > 0) {
			sortPool(priorityPoolLight);
			return priorityPoolLight.remove(0);
		}
		
		else{
			return null;
		}
	}
	
	/* Gets mail that's nearby the mail that has been chosen due to priority.
	 * Checks on either side of the spot where the current mail is
	 */
	private MailItem getNearbyMail(MailItem mail, boolean isHeavy) {
		MailItem ret_mail = null;	// The return value
		if ((isHeavy && (ret_mail= mailRequestedOnTheWayToFloor(mail.getDestFloor(), priorityPoolHeavy)) != null)) {
			return mail;
		}
		if ((ret_mail= mailRequestedOnTheWayToFloor(mail.getDestFloor(), priorityPoolLight)) != null) {
			return mail;
		}
		
		if (isHeavy && (ret_mail= mailRequestedOnTheWayToFloor(mail.getDestFloor(), nonPriorityPoolHeavy)) != null) {
			return mail;
		}
		if ((ret_mail= mailRequestedOnTheWayToFloor(mail.getDestFloor(), nonPriorityPoolLight)) != null) {
			return mail;
		}
		
		return null;
	}
	
	/* Checks whether in the given pool and a floor that the robot's already delivering to,
	 * if there is any mail requested on the way to the given floor.
	 */
	private MailItem mailRequestedOnTheWayToFloor(int floor, ArrayList<MailItem> pool) {
		
		
		if (pool.size() == 0) {
			return null;
		}
		int floor_dist = floor - Building.MAILROOM_LOCATION;
		for (int i=floor; i>Building.MAILROOM_LOCATION; i--) {
			
			for (MailItem mail : pool) {
				if (mail.getDestFloor() == i) {
					return mail;
				}
			}
		}
		return null;
	}
	
	// Always really wanted to be a programmer any way ...
	@Override
	public void fillStorageTube(StorageTube tube, boolean strong) {
		int max = strong ? Integer.MAX_VALUE : 2000; // max weight
		
		// Priority items are important;
		// if there are some, grab one and go, otherwise take as many items as we can and go
		try{
			// Start afresh by emptying undelivered items back in the pool
			while(!tube.isEmpty()) {
				addToPool(tube.pop());
			}
			// Check for a top priority item
			MailItem mail;
			
			// Chooses to always fill up my the whole tube
			
			// Checks for high priority items first!
			while(tube.getSize() < tube.MAXIMUM_CAPACITY && (mail = getHighestPriorityMail(max)) != null) {
				tube.addItem(mail);
			}
			
			// Checks if there are any items on the way
			if (tube.getSize() != 0) {
				while (tube.getSize() < tube.MAXIMUM_CAPACITY && ((mail = getNearbyMail(tube.peek(), (isStrongBot(max)))) != null)) {
					tube.addItem(mail);
				}
			}
			// TODO should get ones that match the floor that I'm already going to!
			// Get as many nonpriority items as available or as fit
			while(tube.getSize() < tube.MAXIMUM_CAPACITY && (mail = getNonPriorityMail(max)) != null) {
				tube.addItem(mail);
			}
		}
		catch(TubeFullException e){
			e.printStackTrace();
		}
	}
	
	private boolean isStrongBot(int weightLimit) {
		return weightLimit > WEAK_ROBOT_MAX_WEIGHT;
	}

	

}
