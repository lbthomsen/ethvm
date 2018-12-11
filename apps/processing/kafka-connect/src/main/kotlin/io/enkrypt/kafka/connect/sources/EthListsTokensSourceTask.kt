package io.enkrypt.kafka.connect.sources

import com.beust.klaxon.Klaxon
import org.apache.commons.codec.binary.Hex
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.apache.kafka.connect.source.SourceTask
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit

data class ContractLogo(val src: String?, val width: String?, val height: String?) {

  // TODO add ipfs hash

  fun toStruct(): Struct =
      Struct(EthListsTokensSourceTask.logoSchema).apply {
        put("src", src)
        put("width", width)
        put("height", height)
      }

}

data class ContractSupport(val email: String?, val url: String?) {

  fun toStruct(): Struct =
      Struct(EthListsTokensSourceTask.supportSchema).apply {
        put("email", email)
        put("url", url)
      }

}

data class ContractSocial(
  val blog: String?,
  val chat: String?,
  val facebook: String?,
  val forum: String?,
  val github: String?,
  val gitter: String?,
  val instagram: String?,
  val linkedin: String?,
  val reddit: String?,
  val slack: String?,
  val telegram: String?,
  val twitter: String?,
  val youtube: String?
) {

  fun toStruct(): Struct =
      Struct(EthListsTokensSourceTask.socialSchema).apply {
        put("blog", blog)
        put("chat", chat)
        put("facebook", facebook)
        put("forum", forum)
        put("github", github)
        put("gitter", gitter)
        put("instagram", instagram)
        put("linkedin", linkedin)
        put("reddit", reddit)
        put("slack", slack)
        put("telegram", telegram)
        put("twitter", twitter)
        put("youtube", youtube)
      }

}

data class ContractMetadata(
  val name: String,
  val symbol: String,
  val address: String,
  val decimals: Int,
  val ens_address: String?,
  val type: String?,
  val website: String?,
//  val logo: ContractLogo?,
  val support: ContractSupport?,
  val social: ContractSocial?
) {

  fun toStruct(): Struct =
      Struct(EthListsTokensSourceTask.contractMetadataSchema).apply {
        put("name", name)
        put("symbol", symbol)
        put("address", Hex.decodeHex(address.substring(2)))
        put("decimals", decimals)
        put("ens_address", ens_address)
        put("type", type)
//      put("logo", logo?.toStruct())
        put("support", support?.toStruct())
        put("social", social?.toStruct())
        put("website", website)
      }

}

class EthListsTokensSourceTask : SourceTask() {

  val klaxon = Klaxon()

  private lateinit var topic: String
  private lateinit var url: String
  private var syncIntervalSeconds: Int = -1

  private var lastSyncAt: Instant? = null

  override fun version() = "0.0.1"    // TODO load from resources

  override fun start(props: MutableMap<String, String>?) {
    topic = EthListsTokensSourceConnector.Config.topic(props!!)
    url = EthListsTokensSourceConnector.Config.tokensUrl(props)
    syncIntervalSeconds = EthListsTokensSourceConnector.Config.syncInterval(props)
  }

  override fun stop() {
    // do nothing
  }

  override fun poll(): List<SourceRecord> {

    if (!shouldSync()) {
      return emptyList()
    }

    val inputStream = URL(url).openStream()
    val entries = klaxon.parseArray<ContractMetadata>(inputStream)

    val sourcePartition = mapOf("url" to url)

    val result = entries?.map { e ->

      val key = Struct(contractKeySchema).apply {
        put("address", Hex.decodeHex(e.address.substring(2)))
      }

      SourceRecord(sourcePartition, emptyMap<String, Any>(), topic, contractKeySchema, key, contractMetadataSchema, e.toStruct())

    } ?: emptyList()

    lastSyncAt = Instant.now()

    return result
  }

  private fun shouldSync(): Boolean =
      if (lastSyncAt == null) {
        true
      } else {
        ChronoUnit.SECONDS.between(lastSyncAt, Instant.now()) > syncIntervalSeconds
      }

  companion object {

    val contractKeySchema = SchemaBuilder(Schema.Type.STRUCT)
        .name("io.enkrypt.avro.common.ContractKeyRecord")
        .field("address", Schema.BYTES_SCHEMA)
        .build()

    val logoSchema = SchemaBuilder(Schema.Type.STRUCT)
        .name("io.enkrypt.avro.common.ContractLogoRecord")
        .optional()
        .field("src", Schema.OPTIONAL_STRING_SCHEMA)
        .field("width", Schema.OPTIONAL_STRING_SCHEMA)
        .field("height", Schema.OPTIONAL_STRING_SCHEMA)
        .field("ipfs_hash", Schema.OPTIONAL_STRING_SCHEMA)
        .build()

    val supportSchema = SchemaBuilder(Schema.Type.STRUCT)
        .name("io.enkrypt.avro.common.ContractSupportRecord")
        .optional()
        .field("email", Schema.OPTIONAL_STRING_SCHEMA)
        .field("url", Schema.OPTIONAL_STRING_SCHEMA)
        .build()

    val socialSchema = SchemaBuilder(Schema.Type.STRUCT)
        .name("io.enkrypt.avro.common.ContractSocialRecord")
        .optional()
        .field("blog", Schema.OPTIONAL_STRING_SCHEMA)
        .field("chat", Schema.OPTIONAL_STRING_SCHEMA)
        .field("facebook", Schema.OPTIONAL_STRING_SCHEMA)
        .field("forum", Schema.OPTIONAL_STRING_SCHEMA)
        .field("github", Schema.OPTIONAL_STRING_SCHEMA)
        .field("gitter", Schema.OPTIONAL_STRING_SCHEMA)
        .field("instagram", Schema.OPTIONAL_STRING_SCHEMA)
        .field("linkedin", Schema.OPTIONAL_STRING_SCHEMA)
        .field("reddit", Schema.OPTIONAL_STRING_SCHEMA)
        .field("slack", Schema.OPTIONAL_STRING_SCHEMA)
        .field("telegram", Schema.OPTIONAL_STRING_SCHEMA)
        .field("twitter", Schema.OPTIONAL_STRING_SCHEMA)
        .field("youtube", Schema.OPTIONAL_STRING_SCHEMA)
        .build()

    val contractMetadataSchema = SchemaBuilder(Schema.Type.STRUCT)
        .name("io.enkrypt.avro.common.ContractMetadataRecord")
        .field("name", Schema.STRING_SCHEMA)
        .field("symbol", Schema.STRING_SCHEMA)
        .field("address", Schema.BYTES_SCHEMA)
        .field("decimals", Schema.INT32_SCHEMA)
        .field("ens_address", Schema.OPTIONAL_STRING_SCHEMA)
        .field("type", Schema.OPTIONAL_STRING_SCHEMA)
        .field("website", Schema.OPTIONAL_STRING_SCHEMA)
        .field("logo", logoSchema)
        .field("support", supportSchema)
        .field("social", socialSchema)

  }
}