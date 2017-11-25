package models.coinmarketcap

case class Ticker(id: String, name: String, symbol: String, rank: Int, priceUsd: Double, priceBtc: Double,
                  vol24hUsd: Int, marketCapUsd: Long, availableSupply: Long, totalSupply: Long, maxSupply: Long,
                  pctChange1h: Double, pctChange24h: Double, pctChange7d: Double, lastUpdated: Long) {}