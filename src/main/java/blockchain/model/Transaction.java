package blockchain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {

	@JsonProperty
	private String sender;
	@JsonProperty
	private String recipient;
	@JsonProperty
	private int amount;
	
	public Transaction() {}
	
	public Transaction(String sender, String recipient, int amount) {
		this.sender = sender;
		this.recipient = recipient;
		this.amount = amount;
	}
	
	public String toString() {
		return "sender:" + sender + ", recipient:" + recipient + ", amount:" + amount;
	}
}
