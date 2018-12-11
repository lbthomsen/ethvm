package io.enkrypt.kafka.streams.extensions

import io.enkrypt.avro.capture.TransactionReceiptRecord
import io.enkrypt.avro.processing.FungibleTokenTransferRecord
import io.enkrypt.avro.processing.NonFungibleTokenTransferRecord
import org.ethereum.util.ByteUtil
import java.math.BigInteger
import java.nio.ByteBuffer

fun FungibleTokenTransferRecord.Builder.setAmount(amount: BigInteger): FungibleTokenTransferRecord.Builder =
  this.setAmount(ByteBuffer.wrap(ByteUtil.bigIntegerToBytes(amount)))

fun NonFungibleTokenTransferRecord.Builder.setTokenId(tokenId: BigInteger): NonFungibleTokenTransferRecord.Builder =
  this.setTokenId(ByteBuffer.wrap(ByteUtil.bigIntegerToBytes(tokenId)))

fun TransactionReceiptRecord.isSuccess(): Boolean {
  val status = this.getStatus().bytes()
  return status != null && status.size == 1 && status[0].toInt() == 1
}