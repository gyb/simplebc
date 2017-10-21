package blockchain.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import blockchain.service.BlockChainService;

public class Block {

	private int id;
	private long timestamp;
	private List<Transaction> transactions;
	private int proof;
	private String previousHash;
	
	public Block(int id, List<Transaction> transactions, int proof, String previousHash, BlockChainService service) {
		this.id = id;
		this.timestamp = System.currentTimeMillis();
		this.proof = proof;
		this.transactions = new ArrayList<>(transactions);
		this.previousHash = previousHash;
		if (this.previousHash == null) {
			this.previousHash = service.lastBlock().hash();
		}
	}
	
	public int getId() {
		return this.id;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public List<Transaction> getTransactions() {
		return transactions;
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
