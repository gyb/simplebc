package blockchain.dto;

import java.util.List;

import blockchain.model.Block;

public class ChainResponse {

	private int length;
	private List<Block> chain;
	
	public ChainResponse(List<Block> chain) {
		this.chain = chain;
		this.length = chain.size();
	}
	
	public int getLength() {
		return this.length;
	}
	
	public List<Block> getChain() {
		return this.chain;
	}
}
