package edu.swin.hets.web

import spark.Filter
import spark.Spark
import jdk.nashorn.internal.objects.NativeArray.forEach
import spark.Request
import spark.Response


class SparkCorsFilter {
    private val corsHeaders = HashMap<String, String>()

    init {
        corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS")
        corsHeaders.put("Access-Control-Allow-Origin", "*")
        corsHeaders.put("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,")
        corsHeaders.put("Access-Control-Allow-Credentials", "true")
    }

    fun apply() {
        val filter = object : Filter {
            @Throws(Exception::class)
            override fun handle(request: Request, response: Response) {
                corsHeaders.forEach { key, value -> response.header(key, value) }
            }
        }
        Spark.after(filter)
    }
}