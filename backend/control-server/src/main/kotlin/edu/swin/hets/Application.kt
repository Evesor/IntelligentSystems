package edu.swin.hets

import edu.swin.hets.agent.PowerPlantAgent
import edu.swin.hets.configuration.SystemConfig
import edu.swin.hets.controller.JadeController
import edu.swin.hets.controller.container.LocalContainerDistributor
import edu.swin.hets.network.ConnectionDetails
import edu.swin.hets.network.SlaveConnection
import jade.core.ProfileImpl
import jade.core.Runtime
import org.apache.commons.configuration2.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class Application(args: Array<String>) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Application::class.java.name)
        const val DEV_MODE_ARG: String = "--dev"
    }

    private val devMode: Boolean = args.contains(DEV_MODE_ARG)
    private val configuration: Configuration = SystemConfig().loadConfig()

    fun start() {
        logger.info("Loading connectionDetails")
        logger.info("Starting JADE deployment server...")
        val jadeController = JadeController(Runtime.instance())
        jadeController.start()
        val collection: Collection<ConnectionDetails>? = configuration.getCollection(ConnectionDetails::class.java, SystemConfig.CONNECTION_LIST, null)

        (jadeController.containerDistributor as LocalContainerDistributor).startContainer(
                "HOLY HECK DOOD XD",
                Runtime.instance(),
                ProfileImpl().apply {
                })

        jadeController.containerDistributor.containers["HOLY HECK DOOD XD"]?.createNewAgent("asdf",PowerPlantAgent::class.java.name, arrayOf())

        jadeController.getContainers().forEach{
            println(it.name)
        }

        jadeController.getAllAgents().forEach {
            println(it.name)
        }

        jadeController.stop()

//        if (!devMode && collection != null)
//            startUpRemotes(collection)
//        else
//            jadeController.getAgents()
        //jadeController.configureAgents()
    }

    private fun startUpRemotes(serverList: Collection<ConnectionDetails>) {
        serverList.stream().forEach({
            SlaveConnection(it, configuration).start()
        })
    }
}


fun main(args: Array<String>) {
    val app = Application(args)
    app.start()
}