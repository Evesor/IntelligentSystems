package edu.swin.hets.configuration

import edu.swin.hets.network.ConnectionDetails
import org.apache.commons.configuration2.Configuration
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths


class SystemConfig {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(SystemConfig::class.java.name)

        const val PROPERTIES_PATH: String = "system.properties"
        const val SERVER_LIST_PATH: String = "servers.properties"
        const val HOST_MACHINE_ADDRESS: String = "host.machine.address"
        const val WEB_ENDPOINT_ADDRESS: String = "web.endpoint.address"
        const val CONNECTION_LIST: String = "connection.list"
    }

    fun loadConfig(): Configuration {
        logger.info("Loading config file")
        val fileExists = Files.exists(Paths.get(PROPERTIES_PATH))
        return when (fileExists) {
            true -> {
                logger.info("Configuration file ${PROPERTIES_PATH} found.")
                val config = Configurations().properties(Paths.get(PROPERTIES_PATH).toFile())
                config.setProperty(CONNECTION_LIST, readServers())
                config
            }
            false -> {
                logger.warn("Configuration file ${PROPERTIES_PATH} not found.")
                logger.warn("Falling back to default empty configuration.")
                createDefaultConfiguration()
            }
        }
    }

    private fun createDefaultConfiguration(): Configuration {
        val configuration = PropertiesConfiguration()
        configuration.setProperty(HOST_MACHINE_ADDRESS, getExternalIpAddress())
        configuration.setProperty(WEB_ENDPOINT_ADDRESS, "")
        configuration.setProperty(CONNECTION_LIST, readServers())
        return configuration
    }

    private fun readServers(): MutableCollection<ConnectionDetails> {
        val file = Paths.get(SERVER_LIST_PATH).toFile()
        if (!file.exists()) {
            logger.warn("Server list not found!")
            return mutableListOf()
        }

        val lineList: List<String> = file.inputStream().bufferedReader().readLines()
        val connectionList: MutableCollection<ConnectionDetails> = mutableListOf()
        lineList.forEach({
            val str = it.split(",")
            connectionList.add(ConnectionDetails(str[0], str[1], str[2]))
        })

        return connectionList
    }

    private fun getExternalIpAddress(): String {
        var ip: String = ""
        try {
            ip = BufferedReader(
                    InputStreamReader(
                            URL("http://checkip.amazonaws.com")
                                    .openStream())).readLine()
        } catch (exception: IOException) {
            logger.error(exception.toString())
        }

        return ip
    }
}