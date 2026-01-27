package com.mobilecomputing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

const val LOREM_IPSUM_1301_CHARS =
    "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna."

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileComputingTheme {
                FoodPage(
                    "A cheesecake",
                    LOREM_IPSUM_1301_CHARS
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
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    if (!expanded) {
        return Box(
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
                    .clickable {
                        setExpanded(true)
                    },
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(Color(0xFFAAAAAA), blendMode = BlendMode.Multiply),
            )
            FoodImage()

        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        FoodImage(modifier = Modifier.clickable {
            setExpanded(false)
        })
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontSize = 8.em,
            modifier = Modifier
                .padding(all = 12.dp)
                .padding(bottom = 0.dp)
        )
        Text(
            text = textBody,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(all = 12.dp)
        )
    }
}
