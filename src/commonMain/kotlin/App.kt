package org.drewcarlson

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import qbittorrent.QBittorrentClient
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


class App : CliktCommand() {
    private val url: String by option()
        .required()
        .help("The full URL of the qBittorrent client to monitor, example: http://localhost:8080")

    private val username: String by option()
        .default("admin")
        .help("The username of the qBittorrent client WebUI.")

    private val password: String by option()
        .default("adminadmin")
        .help("The password of the qBittorrent client WebUI.")

    private val addListFile: Path? by option()
        .convert { Path(it) }
        .help("The file path of the tracker addList.")

    private val banListFile: Path? by option()
        .convert { Path(it) }
        .help("The file path of the tracker banList.")

    private val syncInterval: Duration by option()
        .int()
        .convert { it.seconds }
        .default(10.seconds)
        .help("The interval in seconds to poll qBittorrent for updates.")

    override fun run(): Unit = runBlocking {
        println("Starting qbt-tracker-manager")
        val addList = addListFile.readLines()
        val banList = banListFile.readLines()

        require(addList.isNotEmpty() || banList.isNotEmpty()) {
            "Both the addListFile and banListFile were empty, at least one of them must contain values."
        }

        println("Loaded addList with ${addList.size} items and banList with ${banList.size} items")

        val client = QBittorrentClient(
            baseUrl = url,
            username = username,
            password = password,
            syncInterval = syncInterval,
            dispatcher = Dispatchers.IO,
        )

        try {
            client.login()
            println("Logged into qBittorrent")
        } catch (e: Throwable) {
            println("Failed to login to qBittorrent")
            e.printStackTrace()
            exitProcess(1)
        }

        val processedTorrentHashes = mutableListOf<String>()
        while (true) {
            try {
                println("Setting up torrent observer")
                client.observeMainData()
                    .onStart { println("Torrent observer started") }
                    .onEach { mainData ->
                        mainData.torrents.forEach { (hash, _) ->
                            if (!processedTorrentHashes.contains(hash)) {
                                println("New torrent: $hash")
                                if (banList.isNotEmpty()) {
                                    client.removeTrackers(hash, banList)
                                }
                                if (addList.isNotEmpty()) {
                                    client.addTrackers(hash, addList)
                                }
                                processedTorrentHashes.add(hash)
                                println("Processed torrent: $hash")
                            }
                        }

                        if (mainData.torrentsRemoved.isNotEmpty()) {
                            println("Dropping removed torrents: ${mainData.torrentsRemoved.joinToString()}")
                            processedTorrentHashes.removeAll(mainData.torrentsRemoved)
                        }
                    }
                    .collect()
            } catch (e: Exception) {
                println("Failed to connect to qBittorrent")
                e.printStackTrace()
                delay(syncInterval)
            }
        }
    }

    private fun Path?.readLines(): List<String> {
        if (this == null || !SystemFileSystem.exists(this)) {
            return emptyList()
        }

        val fileContent = SystemFileSystem.source(this).use {
            it.buffered().readString()
        }
        return if (fileContent.isBlank()) {
            emptyList()
        } else {
            fileContent.split('\n')
        }
    }
}
