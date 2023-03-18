package com.morales.nectar.android.composables

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.morales.nectar.R
import com.morales.nectar.screens.NectarViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProgressSpinner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable(enabled = false) {}
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun CommonImage(
    uri: String?,
    modifier: Modifier = Modifier.wrapContentSize(),
    contentDescription: String,
    size: Int = 120
) {
    if (uri == null) {
        Card(
            modifier = Modifier
                .size(size.dp, size.dp)
                .background(Color.Transparent)
                .alpha(0f)
        ) {}
    } else {
        Card(
            modifier = Modifier
                .size(size.dp, size.dp)
                .background(Color.Transparent),
            shape = RoundedCornerShape(10.dp)
        ) {
            AsyncImage(
                modifier = modifier,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .build(),
                contentDescription = contentDescription,
                placeholder = painterResource(R.drawable.placeholder),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun CommonImageFull(
    uri: String?,
    modifier: Modifier = Modifier.wrapContentSize(),
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .build(),
        modifier = modifier,
        contentDescription = contentDescription,
        contentScale = contentScale,
        placeholder = painterResource(R.drawable.placeholder),
    )
}

@Composable
fun UserImageCard(
    userImage: String?,
    modifier: Modifier
) {
    Card(modifier = modifier, shape = CircleShape) {
        if (userImage.isNullOrEmpty()) {
            Image(
                colorFilter = ColorFilter.tint(Color.Gray),
                contentDescription = "default user profile image",
                painter = painterResource(id = R.drawable.ic_user)
            )
        } else {
            CommonImage(uri = userImage, contentDescription = "the users profile image")
        }
    }
}

@Composable
fun CommonDivider() {
    Divider(
        color = Color.LightGray,
        modifier = Modifier
            .alpha(0.3f)
            .padding(top = 8.dp, bottom = 8.dp),
        thickness = 1.dp
    )
}

private enum class LikeIconSize {
    SMALL,
    LARGE
}

@Composable
fun LikeAnimation(like: Boolean = true) {
    var sizeState by remember { mutableStateOf(LikeIconSize.SMALL) }
    val transition = updateTransition(targetState = sizeState, label = "")
    val size by transition.animateDp(
        label = "",
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        }
    ) { state ->
        when (state) {
            LikeIconSize.SMALL -> 0.dp
            LikeIconSize.LARGE -> 150.dp
        }
    }
    Image(
        painter = painterResource(id = if (like) R.drawable.ic_like else R.drawable.ic_unlike),
        contentDescription = "the like icon",
        modifier = Modifier.size(size),
        colorFilter = ColorFilter.tint(if (like) Color.Red else Color.Gray)
    )
    sizeState = LikeIconSize.LARGE
}

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    val transition = rememberInfiniteTransition()
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        )
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFB8B5B5),
                Color(0xFF8F8B8B),
                Color(0xFFB8B5B5),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    )
        .onGloballyPositioned {
            size = it.size
        }
}


@Composable
fun PlantImage(imageUri: String?) {
    val modifier = Modifier
        .height(150.dp)
        .width(150.dp)
        .padding(8.dp)
    if (!imageUri.isNullOrEmpty()) {
        CommonImage(
            uri = imageUri,
            contentDescription = "An image you selected for your plant",
            modifier = modifier,
            size = 150
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.placeholder),
            contentDescription = "Placeholder image",
            modifier = modifier
        )
    }
}

@Composable
fun NotificationMessage(vm: NectarViewModel) {
    val notificationState = vm.popupNotification.value
    val notificationMessage = notificationState?.getContentOrNull()
    if (notificationMessage != null) {
        Toast.makeText(LocalContext.current, notificationMessage, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun NectarSubmitButton(buttonText: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(8.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
    ) {
        Text(text = buttonText, color = Color.White)
    }
}

@Composable
fun NectarTextField(
    label: String,
    onChange: (String) -> Unit,
    value: String,
    modifier: Modifier = Modifier
        .padding(8.dp)
        .size(250.dp)
        .border(1.dp, Color.LightGray)
) {
    TextField(
        value = value,
        onValueChange = { onChange.invoke(it) },
        modifier = modifier,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.White,
            errorCursorColor = Color.Black,
            leadingIconColor = Color.Black,
            cursorColor = Color.Black,
            textColor = Color.Black,
            focusedIndicatorColor = Color.Black,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        label = {
            Text(text = label, color = Color.LightGray)
        }
    )
}

@Composable
fun NectarOutlinedButton(
    buttonText: String,
    buttonTextColor: Color = Color.White,
    buttonColor: Color = Color.Blue,
    onClick: () -> Unit
) {
    OutlinedButton(
        colors = ButtonDefaults.buttonColors(backgroundColor = buttonColor),
        elevation = ButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth(),
        onClick = { onClick.invoke() },
        shape = RoundedCornerShape(10)
    ) {
        Text(text = buttonText, color = buttonTextColor)
    }
}

@Composable
fun NectarConfirmDialog(onCancel: () -> Unit, onSubmit: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onCancel.invoke() },
        title = { Text(text = "Are you sure you want to delete?") },
        buttons = {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NectarSubmitButton(buttonText = "Confirm", onClick = { onSubmit.invoke() })
                NectarSubmitButton(buttonText = "Cancel", onClick = { onCancel.invoke() })

            }
        }
    )
}

@Composable
fun NectarDatePicker(pickedDate: LocalDate, onClick: (LocalDate) -> Unit) {
    val ctx = LocalContext.current

    val formattedDate by remember {
        derivedStateOf {
            DateTimeFormatter
                .ofPattern("MMM dd yyyy")
                .format(pickedDate)
        }
    }
    val dateDialogState = rememberMaterialDialogState()
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            dateDialogState.show()
        }) {
            Text(text = "Pick date")
        }
        Text(text = formattedDate)
        Spacer(modifier = Modifier.height(16.dp))
    }
    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton(text = "Ok") {

            }
            negativeButton(text = "Cancel")
        }
    ) {
        datepicker(
            initialDate = LocalDate.now(),
            title = "Pick a date"
        ) {
            onClick.invoke(it)
        }
    }
}
