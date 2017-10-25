package blockchain.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

public class BlockChain {
	public final static String POW_PREFIX = "00000";

	private int length;
	private List<Block> chain;
	
	public BlockChain() {
		this.chain = new ArrayList<>();
	}
	
	public BlockChain(List<Block> chain) {
		this.chain = new ArrayList<>(chain);
	}
	
	public void add(Block block) {
		this.chain.add(block);
	}

	public List<Block> getChain() {
		return new ArrayList<>(chain);
	}

	public int length() {
		return length;
	}

	public Block lastBlock() {
		return chain.get(chain.size() - 1);
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
		if (chain.isEmpty()) return false;
		for (int i=1; i<chain.size(); i++) {
			Block last = chain.get(i-1);
			Block curr = chain.get(i);
			if (! last.hash().equals(curr.getPreviousHash())) {
				return false;
			}
			if (! isValid(last.getProof(), curr.getProof())) {
				return false;
			}
		}
		return true;
	}
}
