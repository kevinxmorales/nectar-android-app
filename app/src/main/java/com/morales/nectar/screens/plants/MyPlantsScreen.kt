package com.morales.nectar.screens.plants

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.morales.nectar.DestinationScreen
import com.morales.nectar.composables.CommonImage
import com.morales.nectar.composables.NectarOutlinedButton
import com.morales.nectar.composables.ProgressSpinner
import com.morales.nectar.composables.UserImageCard
import com.morales.nectar.data.models.PlantData
import com.morales.nectar.navigation.BottomNavigationItem
import com.morales.nectar.navigation.BottomNavigationMenu
import com.morales.nectar.navigation.NavParam
import com.morales.nectar.navigation.navigateTo
import com.morales.nectar.screens.NectarViewModel

class PostRow {
    private val maxSize = 3
    val values = mutableListOf<PlantData?>()

    fun size() = values.size
    fun isFull(): Boolean {
        return values.size == maxSize
    }

    fun add(post: PlantData) {
        if (isFull()) return
        values.add(values.size, post)
        return
    }
}

@Composable
fun MyPlantsScreen(
    navController: NavController,
    vm: NectarViewModel,
) {
    val userData = vm.userData.value
    val isLoading = vm.isLoading.value
    val plants = vm.plants.value

    Column {
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Row {
                    ProfileImage(imageUrl = userData?.imageUrl)
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .padding(top = 10.dp)
                    ) {
                        Text(text = userData?.name ?: "", fontWeight = FontWeight.Bold)
                        Text(text = userData?.username ?: "")
                    }
                }
            }
            NectarOutlinedButton(
                buttonText = "Edit Profile",
                buttonColor = Color.LightGray,
                buttonTextColor = Color.Black,
                onClick = { navigateTo(navController, DestinationScreen.Profile) })

            NectarOutlinedButton(
                buttonText = "Add New Plant",
                onClick = { navigateTo(navController, DestinationScreen.CreateNewPlant) })

            PlantsGrid(
                modifier = Modifier
                    .weight(1f)
                    .padding(1.dp)
                    .fillMaxSize(),
                onPostClick = { postData ->
                    navigateTo(
                        navController,
                        DestinationScreen.SinglePlant,
                        NavParam("plant", postData)
                    )
                },
                posts = plants,
                isLoading = isLoading
            )
        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.POSTS,
            navController = navController
        )
    }

    if (isLoading) {
        ProgressSpinner()
    }

}

@Composable
fun ProfileImage(imageUrl: String?) {
    Box(
        modifier = Modifier
            .padding(top = 16.dp)
    ) {
        UserImageCard(
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp),
            userImage = imageUrl
        )
    }
}

@Composable
fun PlantsGrid(
    isLoading: Boolean,
    modifier: Modifier,
    onPostClick: (PlantData) -> Unit,
    posts: List<PlantData>
) {
    if (isLoading) {
        ProgressSpinner()
    } else if (posts.isEmpty()) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "No plants yet")
        }
    } else {
        LazyColumn(modifier = modifier) {
            val rows = arrayListOf<PostRow>()
            var currentRow = PostRow()
            rows.add(currentRow)
            for (post in posts) {
                if (currentRow.isFull()) {
                    currentRow = PostRow()
                    rows.add(currentRow)
                }
                currentRow.add(post)
            }
            items(
                count = rows.size,
                key = { idx -> rows[idx].values.getOrNull(0)?.plantId!! },
                itemContent = { idx ->
                    PostsRow(
                        postRow = rows[idx],
                        onPostClick = onPostClick
                    )
                }
            )
        }
    }
}

@Composable
fun PostsRow(postRow: PostRow, onPostClick: (PlantData) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        val onClick = { idx: Int ->
            val post = postRow.values.getOrNull(idx)
            if (post != null) onPostClick(post)
        }
        for (i in 0 until 3) {
            PostImage(
                imageUrl = postRow.values.getOrNull(i)?.images?.get(0),
                modifier = Modifier
                    .clickable { onClick.invoke(i) }
            )
        }
    }

}

@Composable
fun PostImage(imageUrl: String?, modifier: Modifier) {
    if (imageUrl == null) {
        Box(modifier = Modifier.background(Color.Transparent)) {
            CommonImage(
                uri = null, contentDescription = "placeholder", modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize()
            )
        }
    } else {
        Box(modifier = modifier) {
            CommonImage(
                uri = imageUrl,
                contentDescription = "a plant image",
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize()
                    .background(Color.Transparent)
            )
        }
    }
}

@Composable
fun MyPlantsButtonRow(
    settingsOnClick: () -> Unit,
    newPlantOnClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedButton(
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp
            ),
            modifier = Modifier
                .weight(1f)
                .padding(end = 5.dp),
            onClick = { settingsOnClick.invoke() },
            shape = RoundedCornerShape(10)
        ) {
            Text(text = "Settings", color = Color.Black)
        }

        OutlinedButton(
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp
            ),
            modifier = Modifier
                .weight(1f)
                .padding(start = 5.dp),
            onClick = { newPlantOnClick.invoke() },
            shape = RoundedCornerShape(10)
        ) {
            Text(text = "Add New Plant", color = Color.White)
        }
    }
}


