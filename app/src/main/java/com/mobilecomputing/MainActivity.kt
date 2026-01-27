package com.mobilecomputing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mobilecomputing.ui.theme.MobileComputingTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val LOREM_IPSUM_1310_CHARS =
    "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Puuhattu nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna."

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileComputingTheme {
                FoodPage(
                    "A cheesecake",
                    LOREM_IPSUM_1310_CHARS
                )
            }
        }
    }
}

@Composable
fun FoodImage(
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null
) {
    Image(
        painter = painterResource(R.drawable.cheesecake),
        contentDescription = "A cheesecake",
        contentScale = contentScale,
        modifier = modifier,
        colorFilter = colorFilter
    )
}

@Composable
fun FoodPage(
    title: String,
    textBody: String,
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val (showBareImage, setShowBareImage) = remember { mutableStateOf(true) }


    // this snippet by AI
    val backgroundColor by animateColorAsState(
        targetValue = if (expanded) Color.White else Color.Transparent,
        finishedListener = { color ->
            if (color == Color.White) {
                setShowBareImage(false)
            } else {
                setShowBareImage(true)
            }
        },
        animationSpec = tween(350)
    )
    val textColor by animateColorAsState(
        targetValue = if (expanded) Color.Black else Color.Transparent
    )
    val bareTextColor by animateColorAsState(
        targetValue = if (!expanded) Color.White else Color.Transparent
    )

    val conf = LocalConfiguration.current;

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        FoodImage(
            modifier = Modifier
                .align(
                    // https://stackoverflow.com/questions/68726503/jetpack-compose-how-do-you-position-ui-elements-within-their-parent-with-exact
                    BiasAlignment(0.0f, 0.0f)
                )
                .blur(32.dp)
                .clickable(indication = null,interactionSource = remember { MutableInteractionSource() }) {
                    setExpanded(true)
                },
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(Color(0xFFAAAAAA), blendMode = BlendMode.Multiply),
        )
        if (!expanded) {
            Box() {
                FoodImage(
                    modifier = Modifier
                        .align(
                            // https://stackoverflow.com/questions/68726503/jetpack-compose-how-do-you-position-ui-elements-within-their-parent-with-exact
                            BiasAlignment(0.0f, 0.0f)
                        ).alpha(if (showBareImage) 1.0f else 0.0f)
                )
                Text(
                    text = title,
                    modifier = Modifier
                        .align(
                            Alignment.BottomStart
                        )
                        .padding(all = 12.dp)
                        .padding(bottom = 0.dp)
                        .alpha(if (showBareImage) 1.0f else 0.0f),
                    color = bareTextColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 8.em,
                )
            }
        }
    }
    AnimatedVisibility(
        expanded,
        enter = slideInVertically(initialOffsetY = { conf.screenHeightDp / 2 }),
        exit = slideOutVertically(targetOffsetY = { conf.screenHeightDp / 2 + 32 }),
        modifier = Modifier,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .background(backgroundColor),
        ) {
            Box() {
                FoodImage(
                    modifier = Modifier
                        .align(
                            // https://stackoverflow.com/questions/68726503/jetpack-compose-how-do-you-position-ui-elements-within-their-parent-with-exact
                            BiasAlignment(0.0f, 0.0f)
                        )
                        .clickable(indication = null,interactionSource = remember { MutableInteractionSource() })
                        {
                            setExpanded(!expanded)
                            scope.launch {scrollState.scrollTo(0) }
                        }
                )
                Text(
                    text = title,
                    modifier = Modifier
                        .align(
                            Alignment.BottomStart
                        )
                        .padding(all = 12.dp)
                        .padding(bottom = 0.dp),
                    color = bareTextColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 8.em,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 8.em,
                color = textColor,
                modifier = Modifier
                    .padding(all = 12.dp)
                    .padding(bottom = 0.dp),
            )
            Text(
                text = textBody,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier
                    .padding(all = 12.dp),
            )
        }
    }

}
