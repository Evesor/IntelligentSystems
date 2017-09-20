package edu.swin.hets.network

import com.xebialabs.overthere.*
import com.xebialabs.overthere.local.LocalConnection
import com.xebialabs.overthere.local.LocalFile
import com.xebialabs.overthere.ssh.SshConnectionBuilder
import com.xebialabs.overthere.ssh.SshConnectionType
import com.xebialabs.overthere.util.OverthereFileCopier
import edu.swin.hets.configuration.SystemConfig
import org.apache.commons.configuration2.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


class SlaveConnection(val connectionDetails: ConnectionDetails, val configuration: Configuration) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(SlaveConnection::class.java)
        const val DEFAULT_CONNECTION_TIMEOUT: Int = 30000
        const val USERNAME: String = "ubuntu"
        const val KEYSTORE_PATH: String = "keystore"

        const val HOST_MACHINE_EXECUTABLE_PATH: String = "remote-executable"
        const val SLAVE_MACHINE_EXECUTABLE_PATH: String = "/home/ubuntu"
        const val DEPLOYED_EXECUTABLE_FILENAME: String = "hets.jar"
    }

    private var connection: OverthereConnection? = null

    init {
        prepareConnection()
    }

    fun start() {
        try {
            val file = File("${HOST_MACHINE_EXECUTABLE_PATH}/${DEPLOYED_EXECUTABLE_FILENAME}")
            val srcFile: OverthereFile = LocalFile(LocalConnection("local", ConnectionOptions()), file)

            val destFile: OverthereFile? = connection?.getFile("${SLAVE_MACHINE_EXECUTABLE_PATH}/${DEPLOYED_EXECUTABLE_FILENAME}")
            OverthereFileCopier.copy(srcFile, destFile)

            val process: OverthereProcess? = connection?.startProcess(
                    CmdLine.build(
                            "java",
                            "-jar",
                            "${SLAVE_MACHINE_EXECUTABLE_PATH}/${DEPLOYED_EXECUTABLE_FILENAME}",
                            connectionDetails.name,
                            configuration.getString(SystemConfig.HOST_MACHINE_ADDRESS)
                    ))

            val stdout = BufferedReader(InputStreamReader(process?.getStdout()))
            val exitCode = process?.waitFor()
            System.err.println("Exit code: " + exitCode)
        } finally {
            stop()
        }
    }

    private fun prepareConnection() {
        val options = ConnectionOptions()
        options.set(SshConnectionBuilder.PRIVATE_KEY_FILE, "${KEYSTORE_PATH}/${connectionDetails.privateKey}")
        options.set(ConnectionOptions.CONNECTION_TIMEOUT_MILLIS, DEFAULT_CONNECTION_TIMEOUT)
        options.set(ConnectionOptions.USERNAME, USERNAME)
        options.set(ConnectionOptions.ADDRESS, connectionDetails.address.hostAddress)
        options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.UNIX)
        options.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.SCP)

        connection = Overthere.getConnection(SshConnectionBuilder.SSH_PROTOCOL, options)
    }

    fun stop() {
        connection?.apply {
            close()
            connection = null
        }
    }

    fun shutdownJava() {
        try {
            logger.warn("Shutting down java on ${connectionDetails.name}")
            connection?.execute(CmdLine.build("killall", "java"))
        } finally {
            stop()
        }
    }

    fun provisionJRE() {
        try {
            logger.info("updating apt-get on ${connectionDetails.name}")
            connection?.execute(CmdLine.build("sudo", "apt-get", "update"))
            logger.info("installing JRE on ${connectionDetails.name}")
            connection?.execute(CmdLine.build("sudo", "apt-get", "install", "default-jre"))
        } finally {
            stop()
        }
    }
}