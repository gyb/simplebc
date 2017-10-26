package blockchain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Block {

	public static final String GENESIS_HASH = "1";
	public static final int GENESIS_PROOF = 100;

	@JsonProperty
	private int id;
	@JsonProperty
	private long timestamp;
	@JsonProperty
	private List<Transaction> transactions;
	@JsonProperty
	private int proof;
	@JsonProperty
	private String previousHash;
	
	public Block() {}

	public Block(int id, Queue<Transaction> transactions, int proof, String previousHash) {
		this.id = id;
		this.timestamp = System.currentTimeMillis();
		this.proof = proof;
		if (transactions != null) {
			this.transactions = new ArrayList<>(transactions);
		} else {
			this.transactions = new ArrayList<>();
		}
		this.previousHash = previousHash;
	}
	
	public int getId() {
		return this.id;
	}

	public int getProof() {
		return proof;
	}

	public String getPreviousHash() {
		return previousHash;
	}
	
	public String hash() {
		return DigestUtils.sha256Hex(id + "|" + timestamp + "|" + transactions.toString()
				+ "|" + proof + "|" + previousHash);
	}
}
