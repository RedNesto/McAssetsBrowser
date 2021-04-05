package io.github.rednesto.mcassetsbrowser

import kotlinx.css.*
import react.RProps
import react.dom.p
import react.functionalComponent
import react.useEffect
import react.useState
import styled.css
import styled.styledDiv

private data class AssetEntry(val path: String, val data: AssetData)

val app = functionalComponent<RProps> {
    val (versionsManifest, setVersionManifest) = useState<VersionsManifest?>(null)
    val (versionsManifestError, setVersionsManifestError) = useState<Throwable?>(null)
    val (selectedVersion, setSelectedVersion) = useState<ManifestVersion?>(null)
    val (assetsManifest, setAssetsManifest) = useState<AssetsManifest?>(null)
    val (assetsManifestError, setAssetsManifestError) = useState<Throwable?>(null)
    val (selectedAsset, setSelectedAsset) = useState<AssetEntry?>(null)
    useEffect(listOf()) {
        fetchVersionsManifest().then(setVersionManifest).catch { throwable ->
            console.error("Error when loading versions manifest: ", throwable.message)
            throwable.printStackTrace()
            setVersionManifest(null)
            setVersionsManifestError(throwable)
        }
    }
    useEffect(listOf(selectedVersion)) {
        if (selectedVersion != null) {
            fetchAssetsManifest(selectedVersion).then(setAssetsManifest).catch { throwable ->
                console.error("Error when loading assets manifest of version ", selectedVersion.id, " from ", selectedVersion.url, ": ", throwable.message)
                throwable.printStackTrace()
                setAssetsManifest(null)
                setAssetsManifestError(throwable)
            }
        }
    }
    styledDiv {
        css {
            display = Display.flex
            flexDirection = FlexDirection.row
            height = 100.vh
            width = 100.vw
        }

        styledDiv {
            css {
                height = LinearDimension.inherit
                width = 10.vw
            }
            val snapshotFilter = ListSelectorFilter<ManifestVersion>("Snapshot") { state, element -> if (!state) element.type != VersionType.SNAPSHOT else true }
            listSelector(
                elements = versionsManifest?.versions.orEmpty().filter { it.type == VersionType.RELEASE || it.type == VersionType.SNAPSHOT },
                elementNameExtractor = { it.id },
                placeholder = versionsManifestError?.let { "Failed to load versions manifest: $it" },
                filters = listOf(snapshotFilter),
                onSelectionChange = setSelectedVersion,
                initialSelection = selectedVersion
            )
        }

        styledDiv {
            css {
                height = LinearDimension.inherit
                minWidth = 30.vw
                maxWidth = 30.vw
            }
            listSelector(
                elements = assetsManifest?.objects.orEmpty().map { (path, data) -> AssetEntry(path, data) },
                elementNameExtractor = { it.path },
                placeholder = assetsManifestError?.let { "Failed to load assets manifest of version ${selectedVersion?.id}: $it" } ?: "Select a version",
                onSelectionChange = setSelectedAsset,
                initialSelection = selectedAsset
            )
        }

        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                height = LinearDimension.inherit
                width = LinearDimension.fillAvailable
                padding(0.px, 8.px)
            }
            if (selectedAsset == null) {
                p { +"Select an asset" }
            } else {
                child(AssetViewer::class) {
                    attrs {
                        path = selectedAsset.path
                        data = selectedAsset.data
                    }
                }
            }
        }
    }
}
