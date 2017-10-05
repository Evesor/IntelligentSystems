package edu.swin.hets.controller.distributor

import com.xebialabs.overthere.ConnectionOptions
import com.xebialabs.overthere.OperatingSystemFamily
import com.xebialabs.overthere.Overthere
import com.xebialabs.overthere.ssh.SshConnectionBuilder
import com.xebialabs.overthere.ssh.SshConnectionType
import edu.swin.hets.agent.ApplianceAgent
import edu.swin.hets.agent.HomeAgent
import edu.swin.hets.agent.PowerPlantAgent
import edu.swin.hets.agent.ResellerAgent
import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.network.ConnectionDetails
import edu.swin.hets.network.SlaveConnection
import jade.core.Agent
import jade.core.Runtime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Serializable

data class AgentDefinition(val name: String, val className: String, val arguments: String = "") : Serializable
data class ContainerDefinition(val name: String, val agents: List<AgentDefinition>) : Serializable
data class SystemDefinition(val containers: List<ContainerDefinition>) : Serializable

/**
 * Responsible for the distribution of JADE platform containers.
 */
abstract class ContainerDistributor(val runtime: Runtime, val systemDefinition: SystemDefinition) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ContainerDistributor::class.java)
        val DEFAULT_CONTAINER_CONFIGURATION =
                SystemDefinition(listOf(
                        ContainerDefinition("Power Plant Container", listOf(
                                AgentDefinition("PowerPlant1", PowerPlantAgent::class.java.name),
                                AgentDefinition("PowerPlant2", PowerPlantAgent::class.java.name)
                        )),
                        ContainerDefinition("Appliance Container", listOf(
                                AgentDefinition("Appliance1", ApplianceAgent::class.java.name),
                                AgentDefinition("Appliance2", ApplianceAgent::class.java.name)
                        )),
                        ContainerDefinition("Home Container", listOf(
                                AgentDefinition("Home1", HomeAgent::class.java.name),
                                AgentDefinition("Home2", HomeAgent::class.java.name)
                        )),
                        ContainerDefinition("Reseller Container", listOf(
                                AgentDefinition("Reseller1", ResellerAgent::class.java.name),
                                AgentDefinition("Reseller2", ResellerAgent::class.java.name)
                        ))
                ))
    }

    /**
     * Start distributing JADE containers.
     */
    abstract fun start()
}

class ContainerDistributorFactory {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ContainerDistributorFactory::class.java)

        /**
         * Factory method for ContainerDistributor.
         * Will attempt network requests to determine valid connections for Network Containers.
         */
        fun getContainerDistributor(runtime: Runtime, config: SystemConfig): ContainerDistributor {
            with(config.connectionList) {
                val validConnections: MutableList<ConnectionDetails> = mutableListOf()
                forEach {
                    if (validateConnection(it)) validConnections.add(it)
                }

                return when (validConnections.isEmpty()) {
                    true -> {
                        logger.info("No valid connections found, starting local containers")
                        LocalContainerDistributor(runtime, config.containerConfiguration)
                    }
                    false -> {
                        logger.info("${validConnections.size} connections found, starting network containers")
                        NetworkContainerDistributor(runtime, config.containerConfiguration, validConnections)
                    }
                }

            }
        }

        private fun validateConnection(connectionDetails: ConnectionDetails): Boolean {
            val options = ConnectionOptions()
            options.set(SshConnectionBuilder.PRIVATE_KEY_FILE, "${SlaveConnection.KEYSTORE_PATH}/${connectionDetails.privateKey}")
            options.set(ConnectionOptions.CONNECTION_TIMEOUT_MILLIS, SlaveConnection.DEFAULT_CONNECTION_TIMEOUT)
            options.set(ConnectionOptions.USERNAME, SlaveConnection.USERNAME)
            options.set(ConnectionOptions.ADDRESS, connectionDetails.address.hostAddress)
            options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.UNIX)
            options.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.SCP)

            //Actual connection gets tested here
            return try {
                Overthere.getConnection(SshConnectionBuilder.SSH_PROTOCOL, options)
                true
            } catch (e: Exception) {
                with(connectionDetails) {
                    logger.warn("Connection to $name: $address- is not valid!")
                    logger.warn(e.toString())
                }
                false
            }
        }
    }
}

fun validateSystemDefinition(systemDefinition: SystemDefinition): Boolean {
    return systemDefinition.containers.stream().allMatch {
        validateContainerDefinition(it)
    }
}

fun validateContainerDefinition(containerDefinition: ContainerDefinition): Boolean {
    return containerDefinition.agents.stream().allMatch {
        validateAgentDefinition(it)
    }
}

fun validateAgentDefinition(agentDefinition: AgentDefinition): Boolean {
    return Agent::class.java.isAssignableFrom(Class.forName(agentDefinition.className))
}

