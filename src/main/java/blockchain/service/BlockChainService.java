package blockchain.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import blockchain.model.Block;
import blockchain.model.Transaction;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BlockChainService {
	public final static String BLOCKCHAIN_CHANNEL = "blockchain";
	public final static String POW_PREFIX = "00000";
	private final static Logger logger = LoggerFactory.getLogger(BlockChainService.class);

	private StringRedisTemplate redisTemplate;
	private ObjectMapper mapper = new ObjectMapper();

	private volatile List<Transaction> currentTransactions;
	private volatile List<Block> chain = new ArrayList<>();
	private final String myNodeId = UUID.randomUUID().toString();
	
	@Autowired
	public BlockChainService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.currentTransactions = new CopyOnWriteArrayList<>();
		this.chain = new CopyOnWriteArrayList<>();
		//create the genesis block
		createBlock("1", 100);
	}
	
	private Block createBlock(String previousHash, int proof) {
		Block block = new Block(this.chain.size() + 1, currentTransactions, proof, previousHash);
		this.chain.add(block);
		this.currentTransactions = new CopyOnWriteArrayList<>();
		return block;
	}

	public int createTransaction(Transaction transaction) {
		currentTransactions.add(transaction);
		return lastBlock().getId() + 1;
	}
	
	public Block mine() {
		int proof = proofOfWork(lastBlock().getProof());
		this.createTransaction(new Transaction("0", myNodeId, 1));
		Block newBlock = createBlock(lastBlock().hash(), proof);
		logger.info("A new block was created!");
		try {
			this.redisTemplate.convertAndSend(BLOCKCHAIN_CHANNEL, mapper.writeValueAsString(chain));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return newBlock;
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
	
	public Block lastBlock() {
		return this.chain.get(chain.size() - 1);
	}
	
	public List<Block> chain() {
		return this.chain;
	}
	
	private boolean isValid(List<Block> chain) {
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
	
	public void resolveChain(String message) throws JsonParseException, JsonMappingException, IOException {
		List<Block> otherChain = mapper.readValue(message, new TypeReference<List<Block>>() {});
		if (otherChain.size() > this.chain.size() && isValid(otherChain)) {
			this.chain = otherChain;
			logger.info("Our chain was replaced by others");
		}
	}
}
