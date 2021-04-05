package io.github.rednesto.mcassetsbrowser

import kotlinx.css.*
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.input
import react.dom.label
import react.dom.p
import styled.*

class ListSelectorFilter<E>(val name: String, val predicate: (state: Boolean, element: E) -> Boolean)

external interface ListSelectorProps<E> : RProps {
    var elements: List<E>
    var elementNameExtractor: (element: E) -> String
    var placeholder: String?
    var searchPredicate: ((searchTerms: String, element: E) -> Boolean)?
    var filters: List<ListSelectorFilter<E>>?
    var onSelectionChange: ((element: E) -> Unit)?
    var initialSelection: E?
}

data class ListSelectorState<E>(
    var selectedElement: E? = null,
    var searchTerms: String? = null,
    var filtersState: List<FilterState<E>>
) : RState

class FilterState<E>(val filter: ListSelectorFilter<E>, var state: Boolean)

@JsExport
class ListSelector<E>(props: ListSelectorProps<E>) : RComponent<ListSelectorProps<E>, ListSelectorState<E>>(props) {

    init {
        state = ListSelectorState(
            selectedElement = props.initialSelection,
            filtersState = props.filters.orEmpty().map { FilterState(it, false) }
        )
    }

    private val defaultSearchPredicate: (searchTerm: String, element: E) -> Boolean =
        { searchTerms, element -> props.elementNameExtractor(element).contains(searchTerms, ignoreCase = true) }

    override fun RBuilder.render() {
        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                height = LinearDimension.inherit
                width = LinearDimension.fillAvailable
            }
            styledForm {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    width = LinearDimension.fillAvailable
                    margin(10.px, 4.px, 4.px)
                }
                for (filterState in state.filtersState) {
                    label {
                        input {
                            attrs {
                                type = InputType.checkBox
                                onChangeFunction = changeFunction@{ event ->
                                    val targetInput = event.target as? HTMLInputElement ?: return@changeFunction
                                    setState {
                                        filterState.state = targetInput.checked
                                    }
                                }
                            }
                        }
                        +filterState.filter.name
                    }
                }
                styledInput {
                    css {
                        width = LinearDimension.fillAvailable
                    }
                    attrs {
                        type = InputType.text
                        placeholder = "Search..."
                        onChangeFunction = changeFunction@{ event ->
                            val targetInput = event.target as? HTMLInputElement ?: return@changeFunction
                            setState {
                                searchTerms = targetInput.value
                            }
                        }
                    }
                }
            }
            if (props.elements.isEmpty()) {
                renderPlaceholder()
            } else {
                renderList()
            }
        }
    }

    private fun RBuilder.renderPlaceholder() {
        styledP {
            css {
                alignSelf = Align.center
            }
            +(props.placeholder ?: "No elements") }
    }

    private fun RBuilder.renderList() {
        styledUl {
            css {
                overflowY = Overflow.auto
                margin(0.px)
                padding(0.px)
            }
            val searchTerms = state.searchTerms
            val searchPredicate = props.searchPredicate ?: defaultSearchPredicate
            for (element in props.elements) {
                if (searchTerms != null && searchTerms.isNotBlank() && !searchPredicate(searchTerms, element)) {
                    continue
                }

                if (state.filtersState.all { it.filter.predicate(it.state, element) }) {
                    renderItem(element)
                }
            }
        }
    }

    private fun RBuilder.renderItem(element: E) {
        val isSelectedElement = element == state.selectedElement
        styledLi {
            css {
                padding(8.px, 16.px)
                cursor = Cursor.pointer
                listStyleType = ListStyleType.none
                hover {
                    backgroundColor = Color.lightGray
                }
                if (isSelectedElement) {
                    backgroundColor = Color.lightGray
                }
            }
            attrs {
                id = element.hashCode().toString()
                onClickFunction = { _ ->
                    setState {
                        selectedElement = element
                        props.onSelectionChange?.invoke(element)
                    }
                }
            }
            styledP {
                css {
                    if (isSelectedElement) {
                        fontWeight = FontWeight.bold
                    }
                    margin(0.px)
                }
                +props.elementNameExtractor(element)
            }
        }
    }
}

fun <E> RBuilder.listSelector(
    elements: List<E>,
    elementNameExtractor: (element: E) -> String,
    placeholder: String? = null,
    searchPredicate: ((searchTerms: String, element: E) -> Boolean)? = null,
    filters: List<ListSelectorFilter<E>>? = null,
    onSelectionChange: ((element: E) -> Unit)? = null,
    initialSelection: E? = null,
    handler: RHandler<ListSelectorProps<E>> = {}
) {
    child<ListSelectorProps<E>, ListSelector<E>> {
        attrs {
            this.elements = elements
            this.elementNameExtractor = elementNameExtractor
            this.placeholder = placeholder
            this.searchPredicate = searchPredicate
            this.filters = filters
            this.onSelectionChange = onSelectionChange
            this.initialSelection = initialSelection
        }
        handler()
    }
}
