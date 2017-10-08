package edu.swin.hets.network

import com.xebialabs.overthere.*
import com.xebialabs.overthere.local.LocalConnection
import com.xebialabs.overthere.local.LocalFile
import com.xebialabs.overthere.ssh.SshConnectionBuilder
import com.xebialabs.overthere.ssh.SshConnectionType
import com.xebialabs.overthere.util.OverthereFileCopier
import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.controller.distributor.ContainerDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


class SlaveConnection(private val connectionDetails: ConnectionDetails) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(SlaveConnection::class.java)
        const val DEFAULT_CONNECTION_TIMEOUT: Int = 30000
        const val USERNAME: String = "ubuntu"
        const val KEYSTORE_PATH: String = "keystore"

        const val HOST_MACHINE_EXECUTABLE_PATH: String = "remote-executable"
        const val SLAVE_MACHINE_EXECUTABLE_PATH: String = "/home/ubuntu"
        const val DEPLOYED_EXECUTABLE_FILENAME: String = "hets.jar"

        const val HOST_PATHNAME = "$HOST_MACHINE_EXECUTABLE_PATH/$DEPLOYED_EXECUTABLE_FILENAME"
        const val REMOTE_PATHNAME = "$SLAVE_MACHINE_EXECUTABLE_PATH/$DEPLOYED_EXECUTABLE_FILENAME"
    }

    private var connection: OverthereConnection? = null

    /**
     * Determines if the executable container exists on the host by comparing an md5 hash to the local file
     *
     * @return Whether the file exists on the remote host
     */
    fun remoteFileExists(): Boolean {
        try {
            startConnection()
            logger.info("File does not exist.")
            return false
        } finally {
            stopConnection()
        }
    }

    /**
     * Uploads the executable container to the remote host
     */
    fun uploadExecutable() {
        try {
            startConnection()

            val file = File(HOST_PATHNAME)
            val srcFile: OverthereFile = LocalFile(LocalConnection("local", ConnectionOptions()), file)
            val destFile: OverthereFile? = connection?.getFile(REMOTE_PATHNAME)

            logger.info("Beginning upload of file $HOST_PATHNAME to ${connectionDetails.address.hostAddress}")
            OverthereFileCopier.copy(srcFile, destFile)
            logger.info("Completed upload of file $HOST_PATHNAME to ${connectionDetails.address.hostAddress}")
        } finally {
            stopConnection()
        }
    }

    /**
     * Starts a container that will connect to the main host
     *
     * @param systemConfig the control host's system configuration
     * @param containerDefinition the structure of the container to spin up
     * @return the return code for the command executed
     */
    fun startContainer(systemConfig: SystemConfig, containerDefinition: ContainerDefinition): Int {
        try {
            startConnection()
            val process: OverthereProcess? = connection?.startProcess(
                    CmdLine.build(
                            "java",
                            "-jar",
                            "$SLAVE_MACHINE_EXECUTABLE_PATH/$DEPLOYED_EXECUTABLE_FILENAME",
                            containerDefinition.name,
                            systemConfig.hostMachineAddress
                    ))

            //val stdout = BufferedReader(InputStreamReader(process?.stdout))
            val exitCode = process?.waitFor()
            System.err.println("Exit code: " + exitCode)
            return exitCode ?: 1 //deal with this better
        } finally {
            stopConnection()
        }
    }

    private fun startConnection() {
        val options = ConnectionOptions()
        options.set(SshConnectionBuilder.PRIVATE_KEY_FILE, "$KEYSTORE_PATH/${connectionDetails.privateKey}")
        options.set(ConnectionOptions.CONNECTION_TIMEOUT_MILLIS, DEFAULT_CONNECTION_TIMEOUT)
        options.set(ConnectionOptions.USERNAME, USERNAME)
        options.set(ConnectionOptions.ADDRESS, connectionDetails.address.hostAddress)
        options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.UNIX)
        options.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.SCP)

        try {
            connection = Overthere.getConnection(SshConnectionBuilder.SSH_PROTOCOL, options)
        } catch (rie: RuntimeIOException) {
            println(rie.toString())
        }
    }

    private fun stopConnection() {
        connection?.apply {
            close()
            connection = null
        }
    }

    fun shutdownJava() {
        try {
            startConnection()
            logger.warn("Shutting down java on ${connectionDetails.name}")
            connection?.execute(CmdLine.build("killall", "java"))
        } finally {
            stopConnection()
        }
    }

    fun provisionJRE() {
        try {
            startConnection()
            logger.info("updating apt-get on ${connectionDetails.name}")
            connection?.execute(CmdLine.build("sudo", "apt-get", "update"))
            logger.info("installing JRE on ${connectionDetails.name}")
            connection?.execute(CmdLine.build("sudo", "apt-get", "install", "default-jre"))
        } finally {
            stopConnection()
        }
    }
}