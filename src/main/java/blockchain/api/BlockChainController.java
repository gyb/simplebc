package blockchain.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import blockchain.dto.ChainResponse;
import blockchain.model.Block;
import blockchain.model.Transaction;
import blockchain.service.BlockChainService;

@RestController
public class BlockChainController {
	@Autowired
	private BlockChainService service;

	@RequestMapping(value="/transactions", method=RequestMethod.POST)
	public String createTransaction(@RequestBody Transaction transaction) {
		int id = service.createTransaction(transaction);
		return "Transaction will be added to Block {" + id + "}";
	}
	
	@RequestMapping(value="/mine", method=RequestMethod.POST)
	public Block createBlock() {
		return service.mine();
	}
	
	@RequestMapping("/chain")
	public ChainResponse getChain() {
		return new ChainResponse(service.chain());
	}
}
