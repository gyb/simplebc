# simple blockchain

A simple blockchain implementation with spring boot。

Ported from https://github.com/dvf/blockchain

## Usage

1. Run a redis service on your machine.

2. Run multiple spring boot services:

$ mvn spring-boot:run -Drun.arguments="server.port=9001" (9002, 9003, etc.)

3. Enjoy it! 

POST http://localhost:port/transactions to add a transaction

POST http://localhost:port/mine to add a block

GET http://localhost:port/chain to view full chain