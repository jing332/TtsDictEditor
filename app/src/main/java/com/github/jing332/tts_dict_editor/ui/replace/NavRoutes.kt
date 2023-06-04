package com.github.jing332.tts_dict_editor.ui.replace

sealed class NavRoutes(val route: String) {
    object Manager : NavRoutes("manager")
    object Editor : NavRoutes("edit") {
        const val KEY_RULE = "rule"
        const val KEY_GROUPS = "groups"
    }
}