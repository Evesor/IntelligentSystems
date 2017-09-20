import jade.core.Profile
import jade.core.ProfileImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.Arrays


class ContainerApplication(args: Array<String>) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ContainerApplication::class.java)
        const val PORT: Int = 7777
    }

    private var mainHost: String = "localhost"
    private var containerName: String = UUID.randomUUID().toString()

    init {
        if (validateArguments(args)) {
            containerName = args[0]
            mainHost = args[1]
        }
    }

    fun start() {
        //Get the JADE runtime interface (singleton)
        logger.info("Starting up container")
        val runtime = jade.core.Runtime.instance()
        val profile = ProfileImpl()
        logger.info("Container $containerName target: $mainHost:${PORT}")
        profile.setParameter(Profile.CONTAINER_NAME, containerName)
        profile.setParameter(Profile.MAIN_HOST, mainHost)
        profile.setParameter(Profile.MAIN_PORT, PORT.toString())

        val container = runtime.createAgentContainer(profile)
    }

    private fun validateArguments(args: Array<String>): Boolean {
        return args.size == 2
    }

    private fun handleError(args: Array<String>) {
        logger.error("Error in parsing arguments:")
        Arrays.stream(args).forEach({
            logger.error("\t$it")
        })

        System.exit(1)
    }
}

fun main(args: Array<String>) {
    val app = ContainerApplication(args)
    app.start()
}