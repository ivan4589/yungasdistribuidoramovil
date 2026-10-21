package com.example.yungasdistribuidora.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun YungasBottomBar(
    currentRoute: String,
    isAdmin: Boolean,
    onNavigate: (String) -> Unit
) {
    val inicioRoute = if (isAdmin) "admin_home" else "vendor_home"
    val items = listOf(
        Triple("Inicio", inicioRoute, Icons.Default.Home),
        Triple("Clientes", "clients", Icons.Default.Person),
        Triple("Productos", "products", Icons.Default.List),
        Triple("Ventas", "my_presales", Icons.Default.ShoppingCart),
        Triple("Perfil", "profile", Icons.Default.AccountCircle)
    )

    NavigationBar(
        containerColor = Color(0xFF103B56),
        contentColor = Color.White
    ) {
        items.forEach { (title, route, icon) ->
            val selected = currentRoute == route || (route == "admin_home" && (currentRoute == "admin_home" || currentRoute == "vendor_home"))
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                selected = selected,
                onClick = {
                    val targetRoute = if (route == "admin_home") inicioRoute else route
                    if (currentRoute != targetRoute) {
                        onNavigate(targetRoute)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF35D5B1),
                    selectedTextColor = Color(0xFF35D5B1),
                    unselectedIconColor = Color(0xFFA8B8C5),
                    unselectedTextColor = Color(0xFFA8B8C5),
                    indicatorColor = Color(0xFF071E30)
                )
            )
        }
    }
}
