package io.enkrypt.bolt.extensions

import org.bson.Document
import org.ethereum.core.*
import org.ethereum.vm.LogInfo
import org.ethereum.vm.program.InternalTransaction

fun Block?.toDocument(summary: BlockSummary) = Document(
  mapOf(
    "hash" to this?.hash?.toHex(),
    "number" to this?.number,
    "header" to this?.header.toDocument(summary),
    "transactions" to this?.transactionsList?.mapIndexed { pos, tx -> tx.toDocument(pos, summary) },
    "uncles" to this?.uncleList?.map { it.toDocument(summary) },
    "stats" to summary.statistics.toDocument()
  )
)

fun BlockStatistics?.toDocument(): Document? {
  return if (this == null) {
    null
  } else {
    Document(
      mapOf(
        "successfulTxs" to numSuccessfulTxs,
        "failedTxs" to numFailedTxs,
        "pendingTxs" to numPendingTxs,
        "processingTimeMs" to processingTimeMs,
        "txs" to totalTxs,
        "internalTxs" to totalInternalTxs,
        "totalGasPrice" to totalGasPrice,
        "avgGasPrice" to avgGasPrice,
        "totalTxsFees" to totalTxsFees,
        "avgTxsFees" to avgTxsFees
      )
    )
  }
}

fun BlockHeader?.toDocument(summary: BlockSummary) = Document(
  mapOf(
    "parentHash" to this?.parentHash?.toHex(),
    "sha3Uncles" to this?.unclesHash?.toHex(),
    "hash" to this?.hash?.toHex(),
    "number" to this?.number,
    "timestamp" to this?.timestamp,
    "nonce" to this?.nonce.toHex(),
    "miner" to this?.coinbase?.toHex(),
    "rewards" to summary.rewards?.entries?.associate { it.key.toHex() to it.value },
    "difficulty" to this?.difficulty.toBigInteger(),
    "totalDifficulty" to summary.totalDifficulty,
    "stateRoot" to this?.stateRoot,
    "transactionsRoot" to this?.txTrieRoot,
    "receiptsRoot" to this?.receiptsRoot,
    "logsBloom" to this?.logsBloom,
    "gasLimit" to this?.gasLimit.toBigInteger(),
    "gasUsed" to this?.gasUsed?.toBigInteger(),
    "mixHash" to this?.mixHash,
    "extraData" to this?.extraData
  )
)

fun Transaction?.toDocument(pos: Int, blockSummary: BlockSummary): Document {
  val txHash = this?.hash.toHex()
  val receipt = blockSummary.receipts.find { txHash == it.transaction.hash.toHex() }
  val executionSummary = blockSummary.summaries.find { txHash == it.transaction.hash.toHex() }

  return this.toDocument(pos, receipt, executionSummary)
}

fun Transaction?.toDocument(pos: Int? = null, receipt: TransactionReceipt?, executionSummary: TransactionExecutionSummary?): Document {
  val internalTxs = executionSummary?.internalTransactions ?: emptyList()

  return Document(
    mapOf(
      "hash" to this?.hash.toHex(),
      "nonce" to this?.nonce.toHex(),
      "from" to this?.sender.toHex(),
      "to" to this?.receiveAddress?.toHex(),
      "contractAddress" to this?.contractAddress?.toHex(),
      "status" to receipt?.isTxStatusOK,
      "fee" to executionSummary?.fee,
      "value" to this?.value,
      "data" to this?.data,
      "logs" to executionSummary?.logs?.map { it.toDocument() },
      "gasPrice" to this?.gasPrice.toBigInteger(),
      "gasLimit" to this?.gasLimit.toBigInteger(),
      "gasUsed" to executionSummary?.gasUsed,
      "gasRefund" to executionSummary?.gasRefund,
      "gasLeftover" to executionSummary?.gasLeftover,
      "traces" to internalTxs.map { it.toDocument() },
      "v" to this?.signature?.v,
      "r" to this?.signature?.r,
      "s" to this?.signature?.s
    )
  )
}

fun LogInfo?.toDocument(): Document = Document(
  mapOf(
    "address" to this?.address.toHex(),
    "topics" to this?.topics?.map { it.data },
    "data" to this?.data
  )
)

fun InternalTransaction?.toDocument(): Document = Document(
  mapOf(
    "parentHash" to this?.parentHash.toHex(),
    "hash" to this?.hash.toHex(),
    "opcode" to this?.note,
    "deep" to this?.deep,
    "index" to this?.index,
    "rejected" to this?.isRejected,
    "from" to this?.sender.toHex(),
    "to" to this?.receiveAddress.toHex(),
    "value" to this?.value,
    "data" to this?.data,
    "gas" to this?.gasLimit?.toBigInteger(),
    "gasPrice" to this?.gasLimit?.toBigInteger(),
    "nonce" to this?.nonce.toHex()
  )
)

fun AccountState?.toDocument(address: String): Document = Document(
  mapOf(
    "address" to address,
    "nonce" to this?.nonce,
    "balance" to this?.balance
  )
)
