package com.example.yungasdistribuidora.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.yungasdistribuidora.YungasApplication
import com.example.yungasdistribuidora.domain.model.UserRole
import com.example.yungasdistribuidora.presentation.administrador.InicioAdministradorScreen
import com.example.yungasdistribuidora.presentation.clients.detail.ClientDetailScreen
import com.example.yungasdistribuidora.presentation.clients.detail.ClientDetailViewModel
import com.example.yungasdistribuidora.presentation.clients.form.ClientFormScreen
import com.example.yungasdistribuidora.presentation.clients.form.ClientFormViewModel
import com.example.yungasdistribuidora.presentation.clients.list.ClientsScreen
import com.example.yungasdistribuidora.presentation.clients.list.ClientsViewModel
import com.example.yungasdistribuidora.presentation.components.ConnectionStatusBanner
import com.example.yungasdistribuidora.presentation.login.LoginScreen
import com.example.yungasdistribuidora.presentation.login.LoginViewModel
import com.example.yungasdistribuidora.presentation.splash.SplashScreen
import com.example.yungasdistribuidora.presentation.splash.SplashViewModel
import com.example.yungasdistribuidora.presentation.twofactor.*
import com.example.yungasdistribuidora.presentation.unauthorized.RolNoDisponibleScreen
import com.example.yungasdistribuidora.presentation.vendedor.InicioVendedorScreen
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val app = YungasApplication.instance
    val authRepo = app.authRepository
    val clientRepo = app.clientRepository
    val offlineSessionManager = app.offlineSessionManager

    var tempRecoveryCodes by remember { mutableStateOf<List<String>>(emptyList()) }
    var tempUserRole by remember { mutableStateOf("") }
    var currentUserEmail by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf("") }
    var currentUserRole by remember { mutableStateOf(UserRole.UNKNOWN) }
    var isOfflineMode by remember { mutableStateOf(false) }
    var lastValidationTimeStr by remember { mutableStateOf<String?>(null) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            val viewModel = remember { SplashViewModel(authRepo, offlineSessionManager) }
            SplashScreen(
                viewModel = viewModel,
                onNavigateOnline = { route ->
                    isOfflineMode = false
                    runBlocking {
                        authRepo.getCurrentUser().onSuccess { user ->
                            currentUserName = user.name
                            currentUserEmail = user.email
                            currentUserRole = user.role
                        }
                    }
                    navController.navigate(route) {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateOffline = { session ->
                    isOfflineMode = true
                    currentUserName = session.name
                    currentUserEmail = session.email
                    currentUserRole = session.role
                    lastValidationTimeStr = dateFormat.format(Date(session.lastOnlineValidationAt))
                    val route = when (session.role) {
                        UserRole.ADMIN -> "admin_home"
                        UserRole.VENDEDOR -> "vendor_home"
                        else -> "role_not_available"
                    }
                    navController.navigate(route) {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToLogin = { message ->
                    authRepo.clearLocalSession()
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            val viewModel = remember { LoginViewModel(authRepo) }
            LoginScreen(
                viewModel = viewModel,
                onNavigateToTwoFactorVerify = { token ->
                    navController.navigate("two_factor_verify/$token")
                },
                onNavigateToTwoFactorSetup = { token ->
                    navController.navigate("two_factor_setup/$token")
                }
            )
        }

        composable(
            route = "two_factor_setup/{token}",
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            val viewModel = remember { TwoFactorSetupViewModel(authRepo, token) }
            TwoFactorSetupScreen(
                viewModel = viewModel,
                onConfirmed = { codes, role ->
                    tempRecoveryCodes = codes
                    tempUserRole = role
                    currentUserRole = UserRole.fromString(role)
                    navController.navigate("recovery_codes") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("recovery_codes") {
            RecoveryCodesScreen(
                recoveryCodes = tempRecoveryCodes,
                userRole = tempUserRole,
                onNavigateToHome = { route ->
                    runBlocking {
                        authRepo.getCurrentUser().onSuccess { user ->
                            currentUserName = user.name
                            currentUserEmail = user.email
                            currentUserRole = user.role
                        }
                    }
                    navController.navigate(route) {
                        popUpTo("recovery_codes") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "two_factor_verify/{token}",
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            val viewModel = remember { TwoFactorVerifyViewModel(authRepo, token) }
            TwoFactorVerifyScreen(
                viewModel = viewModel,
                onVerified = { route ->
                    runBlocking {
                        authRepo.getCurrentUser().onSuccess { user ->
                            currentUserName = user.name
                            currentUserEmail = user.email
                            currentUserRole = user.role
                        }
                    }
                    navController.navigate(route) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRecovery = {
                    navController.navigate("two_factor_recovery/$token")
                },
                onBackToLogin = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "two_factor_recovery/{token}",
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            val viewModel = remember { TwoFactorRecoveryViewModel(authRepo, token) }
            TwoFactorRecoveryScreen(
                viewModel = viewModel,
                onRecovered = { route ->
                    runBlocking {
                        authRepo.getCurrentUser().onSuccess { user ->
                            currentUserName = user.name
                            currentUserEmail = user.email
                            currentUserRole = user.role
                        }
                    }
                    navController.navigate(route) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToVerify = {
                    navController.popBackStack()
                }
            )
        }

        composable("admin_home") {
            Column(modifier = Modifier.fillMaxSize()) {
                ConnectionStatusBanner(isOffline = isOfflineMode, lastValidationTime = lastValidationTimeStr)
                InicioAdministradorScreen(
                    userName = currentUserName.ifBlank { "Administrador" },
                    userEmail = currentUserEmail.ifBlank { "admin@yungasdistribuidora.cc" },
                    onNavigateToClients = { navController.navigate("clients") },
                    onLogout = {
                        runBlocking { authRepo.logout() }
                        isOfflineMode = false
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("vendor_home") {
            Column(modifier = Modifier.fillMaxSize()) {
                ConnectionStatusBanner(isOffline = isOfflineMode, lastValidationTime = lastValidationTimeStr)
                InicioVendedorScreen(
                    userName = currentUserName.ifBlank { "Vendedor" },
                    userEmail = currentUserEmail.ifBlank { "vendedor@yungasdistribuidora.cc" },
                    onNavigateToClients = { navController.navigate("clients") },
                    onLogout = {
                        runBlocking { authRepo.logout() }
                        isOfflineMode = false
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("role_not_available") {
            Column(modifier = Modifier.fillMaxSize()) {
                ConnectionStatusBanner(isOffline = isOfflineMode, lastValidationTime = lastValidationTimeStr)
                RolNoDisponibleScreen(
                    onLogout = {
                        runBlocking { authRepo.logout() }
                        isOfflineMode = false
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Clients Routes
        composable("clients") {
            val isAdmin = currentUserRole == UserRole.ADMIN
            val viewModel = remember { ClientsViewModel(clientRepo, isAdmin) }
            ClientsScreen(
                viewModel = viewModel,
                isAdmin = isAdmin,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { clientId -> navController.navigate("clients/$clientId") },
                onNavigateToCreate = { navController.navigate("clients/new") }
            )
        }

        composable(
            route = "clients/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("id") ?: ""
            val isAdmin = currentUserRole == UserRole.ADMIN
            val viewModel = remember { ClientDetailViewModel(clientRepo, clientId) }
            ClientDetailScreen(
                viewModel = viewModel,
                isAdmin = isAdmin,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    if (isAdmin) {
                        navController.navigate("clients/$id/edit")
                    }
                }
            )
        }

        composable("clients/new") {
            val viewModel = remember { ClientFormViewModel(clientRepo, null) }
            ClientFormScreen(
                viewModel = viewModel,
                isEditing = false,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = "clients/{id}/edit",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("id") ?: ""
            val isAdmin = currentUserRole == UserRole.ADMIN
            if (!isAdmin) {
                // Block vendor from editing
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                val viewModel = remember { ClientFormViewModel(clientRepo, clientId) }
                ClientFormScreen(
                    viewModel = viewModel,
                    isEditing = true,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
        }
    }
}
