package blockchain.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.dto.ChainResponse;
import blockchain.model.Block;
import blockchain.model.BlockChain;
import blockchain.model.Transaction;

@Service
public class BlockChainService {
	public final static String BLOCKCHAIN_CHANNEL = "blockchain";
	public final static String MINE_REWARD_ADDRESS = "0";
	public final static int MINE_REWARD_AMOUNT = 1;

	private final static Logger logger = LoggerFactory.getLogger(BlockChainService.class);

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper mapper = new ObjectMapper();

	private volatile Queue<Transaction> currentTransactions;
	private volatile BlockChain chain;
	private final String myNodeId = UUID.randomUUID().toString();
	
	@Autowired
	public BlockChainService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.chain = new BlockChain();
		//create the genesis block
		createBlock(Block.GENESIS_HASH, Block.GENESIS_PROOF);
	}
	
	private Block createBlock(String previousHash, int proof) {
		String id = this.chain.id();
		Block block = new Block(this.chain.length() + 1, currentTransactions, proof, previousHash);
		boolean ok = this.chain.add(block, id);
		if (! ok) {
			return null;
		}
		this.currentTransactions = new ConcurrentLinkedQueue<>();
		return block;
	}

	public int createTransaction(Transaction transaction) {
		currentTransactions.add(transaction);
		return chain.lastBlock().getId() + 1;
	}
	
	public Block mine() {
		int proof = chain.proofOfWork();
		this.createTransaction(new Transaction(MINE_REWARD_ADDRESS, myNodeId, MINE_REWARD_AMOUNT));
		Block newBlock = createBlock(chain.lastBlock().hash(), proof);
		if (newBlock == null) {
			return null;
		}

		logger.info("A new block was created!");
		try {
			this.redisTemplate.convertAndSend(BLOCKCHAIN_CHANNEL, mapper.writeValueAsString(chain.getChain()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return newBlock;
	}
	
	public ChainResponse chain() {
		return new ChainResponse(this.chain);
	}
	
	public void resolveChain(String message) throws JsonParseException, JsonMappingException, IOException {
		BlockChain otherChain = new BlockChain(mapper.readValue(message, new TypeReference<List<Block>>() {}));
		
		if (otherChain.isLongerThan(this.chain) && otherChain.isValid()) {
			this.chain = otherChain;
			logger.info("Our chain was replaced by others");
		}
	}
}
