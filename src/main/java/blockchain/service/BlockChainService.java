package blockchain.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import blockchain.model.Block;
import blockchain.model.BlockChain;
import blockchain.model.Transaction;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BlockChainService {
	public final static String BLOCKCHAIN_CHANNEL = "blockchain";
	public final static String MINE_REWARD_ADDRESS = "0";
	public final static int MINE_REWARD_AMOUNT = 1;

	private final static Logger logger = LoggerFactory.getLogger(BlockChainService.class);

	private StringRedisTemplate redisTemplate;
	private ObjectMapper mapper = new ObjectMapper();

	private volatile List<Transaction> currentTransactions;
	private BlockChain chain;
	private final String myNodeId = UUID.randomUUID().toString();
	
	@Autowired
	public BlockChainService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.currentTransactions = new CopyOnWriteArrayList<>();
		this.chain = new BlockChain();
		//create the genesis block
		createBlock(Block.GENESIS_HASH, Block.GENESIS_PROOF);
	}
	
	private Block createBlock(String previousHash, int proof) {
		Block block = new Block(this.chain.length() + 1, currentTransactions, proof, previousHash);
		this.chain.add(block);
		this.currentTransactions = new CopyOnWriteArrayList<>();
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
		logger.info("A new block was created!");
		try {
			this.redisTemplate.convertAndSend(BLOCKCHAIN_CHANNEL, mapper.writeValueAsString(chain));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return newBlock;
	}
	
	public BlockChain chain() {
		return this.chain;
	}
	
	public void resolveChain(String message) throws JsonParseException, JsonMappingException, IOException {
		BlockChain otherChain = new BlockChain(
				mapper.readValue(message, new TypeReference<List<Block>>() {}));
		
		if (otherChain.length() > this.chain.length() && otherChain.isValid()) {
			this.chain = otherChain;
			logger.info("Our chain was replaced by others");
		}
	}
}
