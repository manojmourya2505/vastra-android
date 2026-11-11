package com.inouiw.vastra

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.theme.Paper
import com.inouiw.vastra.ui.screens.BagScreen
import com.inouiw.vastra.ui.screens.CheckoutScreen
import com.inouiw.vastra.ui.screens.HomeScreen
import com.inouiw.vastra.ui.screens.IntroScreen
import com.inouiw.vastra.ui.screens.LoginScreen
import com.inouiw.vastra.ui.screens.OrdersScreen
import com.inouiw.vastra.ui.screens.PlacedScreen
import com.inouiw.vastra.ui.screens.PlannerScreen
import com.inouiw.vastra.ui.screens.ProfileScreen
import com.inouiw.vastra.ui.screens.ProfileSetupScreen
import com.inouiw.vastra.ui.screens.SplashScreen
import com.inouiw.vastra.ui.screens.StoreScreen
import com.inouiw.vastra.ui.screens.SuggestScreen
import com.inouiw.vastra.ui.screens.TryOnScreen
import com.inouiw.vastra.ui.screens.LooksScreen
import com.inouiw.vastra.ui.theme.VastraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Our screens are white, so the status and navigation bar icons must always be dark.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        setContent {
            VastraTheme {
                VastraApp()
            }
        }
    }
}

@Composable
fun VastraApp() {
    val context = LocalContext.current
    val store = remember {
        AppStore(context.getSharedPreferences("vastra", Context.MODE_PRIVATE), context.filesDir)
    }

    // Back walks the stack; with nothing left to pop, the system closes the app.
    BackHandler(enabled = store.stack.size > 1) { store.back() }

    Box(Modifier.fillMaxSize().background(Paper)) {
        AnimatedContent(
            targetState = store.current,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
            label = "screen",
        ) { route ->
            // Keep content below the status bar on every screen except the full-bleed splash.
            Box(
                Modifier
                    .fillMaxSize()
                    .then(if (route == Route.Splash) Modifier else Modifier.statusBarsPadding()),
            ) {
                RouteScreen(route, store)
            }
        }
    }
}

@Composable
private fun RouteScreen(route: Route, store: AppStore) {
    when (route) {
        Route.Splash -> SplashScreen(store)
        Route.Intro -> IntroScreen(store)
        Route.Login -> LoginScreen(store)
        Route.ProfileSetup -> ProfileSetupScreen(store)
        Route.Home -> HomeScreen(store)
        Route.Planner -> PlannerScreen(store)
        Route.Suggest -> SuggestScreen(store)
        is Route.TryOn -> TryOnScreen(store, route.itemId)
        Route.Looks -> LooksScreen(store)
        Route.Bag -> BagScreen(store)
        Route.Checkout -> CheckoutScreen(store)
        is Route.Placed -> PlacedScreen(store, route.code, route.pickup, route.total)
        Route.Store -> StoreScreen(store)
        Route.Profile -> ProfileScreen(store)
        Route.Orders -> OrdersScreen(store)
    }
}
