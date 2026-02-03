package com.mobilecomputing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

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

    BackHandler(expanded) {
        setExpanded(false)
    }

    Row(Modifier.zIndex(100.0f)) {
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                println("click!");
            },
            modifier = Modifier.padding(end = 12.dp, top = 42.dp),
            contentPadding = PaddingValues(3.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                fontSize = 8.em,
                text = "\uff0b"
            )
        }
    }
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
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
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
                        )
                        .alpha(if (showBareImage) 1.0f else 0.0f)
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
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() })
                        {
                            setExpanded(!expanded)
                            scope.launch { scrollState.scrollTo(0) }
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
