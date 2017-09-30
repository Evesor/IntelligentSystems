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


class SystemConfig(args: Array<String>) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(SystemConfig::class.java.name)

        const val PROPERTIES_PATH: String = "config/system.properties"
        const val SERVER_LIST_PATH: String = "config/servers.properties"
        const val JADE_SYSTEM_DEFINITION_PATH: String = "config/system-definition.json"

        const val HOST_MACHINE_ADDRESS: String = "host.machine.address"
        const val WEB_ENDPOINT_ADDRESS: String = "web.endpoint.address"

        const val DEV_MODE_ARG: String = "--dev"
    }

    var hostMachineAddress: String = getExternalIpAddress()
    var endpointAddress: String = ""
    var connectionList = readServers()
    var devMode = args.contains(DEV_MODE_ARG) //TODO: implement a check on intialization of this class, for now assume that all connections are valid

    init {
        loadConfig()?.let {
            (it.getProperty(HOST_MACHINE_ADDRESS) as? String)?.apply { hostMachineAddress = this }
            (it.getProperty(WEB_ENDPOINT_ADDRESS) as? String)?.apply { endpointAddress = this }
        }

        if (connectionList.isEmpty()) {
            logger.info("No connections found, fallback to dev mode")
            devMode = true
        }
    }

    fun loadConfig(): Configuration? {
        logger.info("Loading config file")
        val fileExists = Files.exists(Paths.get(PROPERTIES_PATH))
        return when (fileExists) {
            true -> {
                logger.info("Configuration file $PROPERTIES_PATH found. Loading...")
                val config = Configurations().properties(Paths.get(PROPERTIES_PATH).toFile())
                config
            }
            false -> {
                logger.warn("Configuration file $PROPERTIES_PATH not found.")
                return null
            }
        }
    }

    private fun readServers(): Collection<ConnectionDetails> {
        val file = Paths.get(SERVER_LIST_PATH).toFile()
        if (!file.exists()) {
            logger.warn("Server connection file not found")
            return listOf()
        }

        val lineList: List<String> = file.inputStream().bufferedReader().readLines()
        val connectionList: MutableCollection<ConnectionDetails> = mutableListOf()
        lineList.forEach({
            val str = it.split(",")
            connectionList.add(ConnectionDetails(str[0], str[1], str[2]))
        })

        return connectionList.toList()
    }

    private fun getExternalIpAddress(): String {
        var ip = ""
        try {
            ip = BufferedReader(InputStreamReader(
                            URL("http://checkip.amazonaws.com").openStream())).readLine()
        } catch (exception: IOException) {
            logger.error(exception.toString())
        }

        return ip
    }
}