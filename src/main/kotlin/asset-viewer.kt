package io.github.rednesto.mcassetsbrowser

import kotlinx.css.*
import react.*
import react.dom.a
import react.dom.audio
import react.dom.h2
import react.dom.p
import styled.css
import styled.styledDiv

external interface AssetViewerProps : RProps {
    var path: String
    var data: AssetData
}

@JsExport
class AssetViewer(props: AssetViewerProps) : RComponent<AssetViewerProps, RState>(props) {

    override fun RBuilder.render() {
        h2 { +props.path }
        a {
            +"Download"
            attrs {
                href = getAssetDownloadUrl(props.data.hash)
                this["download"] = props.path
            }
        }
        p { +"Hash: ${props.data.hash}" }
        p { +"Size: ${props.data.size}" }
        val renderer = assetRenderers.find { it.supports(props.path) }
        if (renderer != null) {
            with(renderer) {
                render(props.data)
            }
        }
    }
}

private fun getAssetDownloadUrl(hash: String) = "https://resources.download.minecraft.net/" + hash.substring(0, 2) + '/' + hash

private val assetRenderers: List<AssetRenderer> = listOf(
    ImageRenderer,
    AudioRenderer,
    TextRenderer,
    FallbackRenderer
)

private interface AssetRenderer {

    fun supports(filename: String): Boolean

    fun RBuilder.render(data: AssetData)
}

private object ImageRenderer : AssetRenderer {

    override fun supports(filename: String): Boolean = filename.endsWith(".png")

    override fun RBuilder.render(data: AssetData) {
        styledDiv {
            css {
                flex(1.0, 1.0, 0.px)
                backgroundImage = Image("url('" + getAssetDownloadUrl(data.hash) + "')")
                backgroundSize = "contain"
                backgroundRepeat = BackgroundRepeat.noRepeat
            }
        }
    }
}

private object AudioRenderer : AssetRenderer {

    override fun supports(filename: String): Boolean = filename.endsWith(".ogg")

    override fun RBuilder.render(data: AssetData) {
        audio {
            attrs {
                controls = true
                src = getAssetDownloadUrl(data.hash)
            }
        }
    }
}

external interface TextAssetAreaProps : RProps {
    var assetUrl: String
}

private val textAssetArea = functionalComponent<TextAssetAreaProps> { props ->
    p {
        +"Text files can't be previewed due to technical restrictions. You can still "
        a(props.assetUrl) { +"download the file" }
        +"."
    }

    // We can't download files programmatically because resources.download.minecraft.net uses CORS headers...
    //val (text, setText) = useState("")
    //useEffect(listOf(props.assetUrl)) {
    //    window.fetch(props.assetUrl)
    //        .then(Response::text)
    //        .then(setText)
    //}
    //styledTextArea {
    //    css {
    //        flexGrow = 1.0
    //        resize = Resize.none
    //    }
    //    attrs {
    //        value = text
    //        readonly = true
    //    }
    //}
}

private object TextRenderer : AssetRenderer {

    override fun supports(filename: String): Boolean = filename.endsWith(".json") || filename.endsWith(".lang")

    override fun RBuilder.render(data: AssetData) {
        child(textAssetArea) {
            attrs.assetUrl = getAssetDownloadUrl(data.hash)
        }
    }
}

private object FallbackRenderer : AssetRenderer {

    override fun supports(filename: String): Boolean = true

    override fun RBuilder.render(data: AssetData) {
        p { +"This file format is not supported" }
    }
}
