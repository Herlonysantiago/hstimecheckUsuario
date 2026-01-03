package com.hs.solutions.hstimecheck_2_0.core

object DateFormatter {

    fun isoParaBr(dataIso: String?): String {
        if (dataIso.isNullOrBlank()) return "—"

        return try {
            val partes = dataIso.split("-")
            "%02d/%02d/%04d".format(
                partes[2].toInt(),
                partes[1].toInt(),
                partes[0].toInt()
            )
        } catch (_: Exception) {
            dataIso
        }
    }

    fun brParaIso(dataBr: String?): String? {
        if (dataBr.isNullOrBlank()) return null

        return try {
            val partes = dataBr.split("/")
            "%04d-%02d-%02d".format(
                partes[2].toInt(),
                partes[1].toInt(),
                partes[0].toInt()
            )
        } catch (_: Exception) {
            null
        }
    }
}
