package blockchain.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Block {

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

	public Block(int id, List<Transaction> transactions, int proof, String previousHash) {
		this.id = id;
		this.timestamp = System.currentTimeMillis();
		this.proof = proof;
		this.transactions = new ArrayList<>(transactions);
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
