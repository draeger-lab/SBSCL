package fern.network;

public class ConstantAmountManager implements AmountManager {

	private long constantAmount;
	
	public ConstantAmountManager(long constantAmount) {
		this.constantAmount = constantAmount;
	}

	public long getAmount(int species) {
		return constantAmount;
	}

	public void performReaction(int reaction, int times) {
	}

	public void resetAmount() {
	}

	public void rollback() {
	}

	public void save() {
	}

	public void setAmount(int species, long amount) {
	}
	

}
