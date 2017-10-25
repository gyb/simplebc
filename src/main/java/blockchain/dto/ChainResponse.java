package blockchain.dto;

import java.util.List;

import blockchain.model.Block;
import blockchain.model.BlockChain;

public class ChainResponse {

	private int length;
	private List<Block> chain;
	
	public ChainResponse(BlockChain chain) {
		this.chain = chain.getChain();
		this.length = chain.length();
	}
	
	public int getLength() {
		return this.length;
	}
	
	public List<Block> getChain() {
		return this.chain;
	}
}
