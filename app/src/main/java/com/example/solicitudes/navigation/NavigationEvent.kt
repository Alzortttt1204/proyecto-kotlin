package com.example.solicitudes.navigation

// Representa los distintos tipos de eventos de navegación (guía 10)
sealed class NavigationEvent {

    /**
     * Evento para navegar a un destino específico.
     * @param route ruta destino ya construida (por ejemplo "detalle_solicitud/7").
     * @param popUpToRoute ruta hasta la cual se hace "pop" en la pila; null = no se hace pop.
     * @param inclusive si es true, popUpToRoute también se elimina de la pila.
     * @param singleTop evita copias del mismo destino en la parte superior de la pila.
     * @param limpiarPila si es true se borra toda la pila (al iniciar o cerrar sesión),
     * para que el botón "atrás" no vuelva a pantallas de otro usuario.
     */
    data class NavigateTo(
        val route: String,
        val popUpToRoute: String? = null,
        val inclusive: Boolean = false,
        val singleTop: Boolean = false,
        val limpiarPila: Boolean = false
    ) : NavigationEvent()

    // Volver a la pantalla anterior en la pila de navegación
    data object PopBackStack : NavigationEvent()

    // Navegar "hacia arriba" en la jerarquía de la aplicación
    data object NavigateUp : NavigationEvent()
}
