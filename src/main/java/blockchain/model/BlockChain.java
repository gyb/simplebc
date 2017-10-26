package blockchain.model;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.codec.digest.DigestUtils;

public class BlockChain {
	public final static String POW_PREFIX = "00000";

	private final String id = UUID.randomUUID().toString();
	private final Deque<Block> chain;
	
	public BlockChain() {
		this.chain = new ConcurrentLinkedDeque<>();
	}

	public BlockChain(List<Block> chain) {
		this.chain = new ConcurrentLinkedDeque<>(chain);
	}
	
	public boolean add(Block block, String id) {
		if (! this.id.equals(id)) {
			return false;
		}
		this.chain.add(block);
		return true;
	}

	public List<Block> getChain() {
		return new ArrayList<>(chain);
	}

	public int length() {
		return this.chain.size();
	}
	
	public String id() {
		return this.id;
	}

	public Block lastBlock() {
		return chain.getLast();
	}
	
	public int proofOfWork() {
		return proofOfWork(lastBlock().getProof());
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
		return DigestUtils.sha256Hex(s).startsWith(POW_PREFIX);
	}

	public boolean isValid() {
		if (chain.isEmpty()) {
			return false;
		}
		Iterator<Block> iter = chain.iterator();
		Block last = iter.next();
		while (iter.hasNext()) {
			Block curr = iter.next();
			if (! last.hash().equals(curr.getPreviousHash())) {
				return false;
			}
			if (! isValid(last.getProof(), curr.getProof())) {
				return false;
			}
			last = curr;
		}
		return true;
	}
	
	public boolean isLongerThan(BlockChain another) {
		return this.length() > another.length();
	}
}
