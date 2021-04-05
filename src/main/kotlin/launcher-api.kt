package io.github.rednesto.mcassetsbrowser

import kotlinx.browser.window
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.fetch.Response
import kotlin.js.Promise

@Serializable
data class VersionsManifest(val latest: ManifestLatestVersion, val versions: List<ManifestVersion>)

operator fun VersionsManifest.get(version: String): ManifestVersion? =
    this.versions.find { it.id == version }

fun VersionsManifest.getLatest(includeSnapshots: Boolean = false): ManifestVersion? =
    if (includeSnapshots) {
        this[this.latest.snapshot] ?: this[this.latest.release]
    } else {
        this[this.latest.release]
    }

@Serializable
data class ManifestLatestVersion(val release: String, val snapshot: String)

@Serializable
data class ManifestVersion(
    val id: String,
    val type: VersionType,
    val url: String,
    val time: String,
    val releaseTime: String,
    val sha1: String,
    val complianceLevel: Int
)

@Serializable
enum class VersionType {
    @SerialName("release")
    RELEASE,

    @SerialName("snapshot")
    SNAPSHOT,

    @SerialName("old_beta")
    OLD_BETA,

    @SerialName("old_alpha")
    OLD_ALPHA,
}

private val VERSIONS_MANIFEST_JSON = Json {
    ignoreUnknownKeys = true
}

fun fetchVersionsManifest(): Promise<VersionsManifest> =
    window.fetch("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json")
        .then(Response::text)
        .then { VERSIONS_MANIFEST_JSON.decodeFromString(it) }


@Serializable
data class AssetsManifest(val objects: Map<String, AssetData>)

@Serializable
data class AssetData(val hash: String, val size: Int)

private val ASSETS_MANIFEST_JSON = Json {
    ignoreUnknownKeys = true
}

fun fetchAssetsManifest(version: ManifestVersion): Promise<AssetsManifest> =
    window.fetch(version.url)
        .then(Response::json)
        .then { window.fetch(it.asDynamic().assetIndex.url as String) }
        .then(Response::text)
        .then { ASSETS_MANIFEST_JSON.decodeFromString(it) }
