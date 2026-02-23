package com.mobilecomputing

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.mobilecomputing.db.Food
import com.mobilecomputing.db.FoodComment
import com.mobilecomputing.db.FoodWithComments
import kotlinx.coroutines.launch

@Composable
fun AddFoodButton(onAddFoodClick: () -> Unit) {
    Row(Modifier.zIndex(100.0f)) {
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                onAddFoodClick()
            },
            modifier = Modifier.padding(end = 12.dp, top = 42.dp),
            contentPadding = PaddingValues(3.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge, fontSize = 8.em, text = "\uff0b"
            )
        }
    }
}

@Composable
fun PrevFoodButton(onPrevFoodClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Button(
            onClick = {
                onPrevFoodClick()
            },
            modifier = Modifier
                .padding(top = 24.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(3.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge, fontSize = 16.em, text = "\u2191"
            )
        }
    }
}

@Composable
fun NextFoodButton(onNextFoodClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Button(
            onClick = {
                onNextFoodClick()
            },
            modifier = Modifier
                .padding(bottom = 24.dp)
                .align(Alignment.BottomCenter),
            contentPadding = PaddingValues(3.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge, fontSize = 16.em, text = "\u2193"
            )
        }
    }
}

@Composable
fun FoodImage(
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    food: FoodWithComments
) {
    val uri = food.food.imageUrl
    if (uri == null) {
        Image(
            painter = painterResource(R.drawable.cheesecake),
            contentDescription = food.food.name,
            contentScale = contentScale,
            modifier = modifier,
            colorFilter = colorFilter
        )
    } else {
        AsyncImage(
            model = uri,
            contentDescription = food.food.name,
            contentScale = contentScale,
            modifier = modifier,
            colorFilter = colorFilter
        )
    }
}

@Composable
fun FoodHeroView(food: FoodWithComments, expanded: Boolean, setExpanded: (Boolean) -> Unit) {
    Box(
        Modifier.fillMaxSize(), contentAlignment = Alignment.Center
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
            food = food,
        )
        if (!expanded) {
            Box() {
                FoodImage(
                    food = food, modifier = Modifier
                        .align(
                            // https://stackoverflow.com/questions/68726503/jetpack-compose-how-do-you-position-ui-elements-within-their-parent-with-exact
                            BiasAlignment(0.0f, 0.0f)
                        )
                        .alpha(1.0f)
                )
                Text(
                    text = food.food.name ?: "n/a",
                    modifier = Modifier
                        .align(
                            Alignment.BottomStart
                        )
                        .padding(all = 12.dp)
                        .padding(bottom = 0.dp)
                        .alpha(1.0f),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 8.em,
                )
            }
        }
    }
}

@Composable
fun CommentSection(
    comments: List<FoodComment>, addComment: (String) -> Unit
) {
    val (newComment, setNewComment) = remember { mutableStateOf("") }


    Text(
        text = "Comments",
        style = MaterialTheme.typography.titleMedium,
        color = Color.Black,
        modifier = Modifier.padding(all = 12.dp),
    )

    if (comments.isEmpty()) {
        Text(
            text = "no comments yet",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(all = 12.dp),
        )
    } else {
        for (comment in comments) {
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                modifier = Modifier.padding(all = 12.dp),
            )
            HorizontalDivider()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Send
        ), keyboardActions = KeyboardActions(
            onSend = {
                if (newComment.isEmpty()) return@KeyboardActions;
                addComment(newComment);
                setNewComment("");
            }), value = newComment, onValueChange = { v -> setNewComment(v) }, singleLine = false
        )

        TextButton(
            onClick = {
                if (newComment.isEmpty()) return@TextButton;
                addComment(newComment);
                setNewComment("");
            },
        ) {
            Text(
                style = MaterialTheme.typography.bodyMedium, text = "Comment"
            )
        }

    }
    Spacer(Modifier.height(128.dp))
}


@Composable
fun FoodDetails(
    food: FoodWithComments,
    /** (foodId, newComment) */
    onAddComment: (Int, String) -> Unit, expanded: Boolean, setExpanded: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()


    BackHandler(expanded) {
        setExpanded(false)
        scope.launch { scrollState.scrollTo(0) }
    }
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .background(Color.White)
                .fillMaxHeight(),
        ) {
            Box() {
                FoodImage(
                    food = food, modifier = Modifier
                        .align(
                            // https://stackoverflow.com/questions/68726503/jetpack-compose-how-do-you-position-ui-elements-within-their-parent-with-exact
                            BiasAlignment(0.0f, 0.0f)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            setExpanded(false)
                            scope.launch { scrollState.scrollTo(0) }
                        })
            }
            Text(
                text = food.food.name ?: "n/a",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 8.em,
                color = Color.Black,
                modifier = Modifier
                    .padding(all = 12.dp)
                    .padding(bottom = 0.dp),
            )
            Text(
                text = food.food.description ?: "n/a",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                modifier = Modifier.padding(all = 12.dp),
            )

            CommentSection(comments = food.comments, addComment = { comment ->
                Log.i("COMMENT", "adding comment: \"$comment\"")
                onAddComment(food.food.uid, comment);
            })
        }
    }
}

@Composable
fun FoodPage(
    food: FoodWithComments,
    onAddFoodClick: () -> Unit,
    /** (foodId, newComment) */
    onAddComment: (Int, String) -> Unit,
    onNextFoodClick: (() -> Unit)?,
    onPrevFoodClick: (() -> Unit)?
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    val conf = LocalConfiguration.current;


    if (!expanded) {
        AddFoodButton(onAddFoodClick = onAddFoodClick)
    }
    FoodHeroView(food, expanded, setExpanded)
    AnimatedVisibility(
        expanded,
        enter = slideInVertically(initialOffsetY = { conf.screenHeightDp / 2 }),
        exit = slideOutVertically(targetOffsetY = { conf.screenHeightDp / 2 + 32 }),
        modifier = Modifier,
    ) {
        FoodDetails(food, onAddComment, expanded, setExpanded)
    }
    if (!expanded) {
        if (onPrevFoodClick != null) PrevFoodButton(onPrevFoodClick = onPrevFoodClick)
        if (onNextFoodClick != null) NextFoodButton(onNextFoodClick = onNextFoodClick)
    }
}
