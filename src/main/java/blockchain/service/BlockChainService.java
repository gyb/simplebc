package blockchain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import blockchain.model.Block;
import blockchain.model.Transaction;

@Service
public class BlockChainService {
	private List<Transaction> currentTransactions = new ArrayList<>();
	private final List<Block> chain = new ArrayList<>();
	private final String myNodeId = UUID.randomUUID().toString();
	
	public BlockChainService() {
		createBlock("1", 100);
	}
	
	private Block createBlock(String previousHash, int proof) {
		Block block = new Block(this.chain.size() + 1, currentTransactions, proof, previousHash, this);
		this.chain.add(block);
		this.currentTransactions = new ArrayList<>();
		return block;
	}

	public int createTransaction(Transaction transaction) {
		currentTransactions.add(transaction);
		return lastBlock().getId() + 1;
	}
	
	public Block mine() {
		Block lastBlock = lastBlock();
		int proof = proofOfWork(lastBlock.getProof());
		this.createTransaction(new Transaction("0", myNodeId, 1));
		return createBlock(lastBlock.hash(), proof);
	}
	
	private int proofOfWork(int lastProof) {
		int proof = 0;
		while (!isValid(lastProof, proof)) {
			proof++;
		}
		return proof;
	}
	
	private boolean isValid(int lastProof, int proof) {
		String s = lastProof + "" + proof;
		return DigestUtils.sha256Hex(s).startsWith("00000");
	}
	
	public Block lastBlock() {
		return this.chain.get(chain.size() - 1);
	}
	
	public List<Block> chain() {
		return this.chain;
	}
}
