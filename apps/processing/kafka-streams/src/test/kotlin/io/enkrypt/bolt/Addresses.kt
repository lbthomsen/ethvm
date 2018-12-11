package io.enkrypt.bolt

import io.enkrypt.bolt.extensions.hex
import org.ethereum.crypto.ECKey

object Addresses {

  const val ETHER_CONTRACT = "0000000000000000000000000000000000000000"

  fun createAddress() = ECKey().address.hex()!!

}