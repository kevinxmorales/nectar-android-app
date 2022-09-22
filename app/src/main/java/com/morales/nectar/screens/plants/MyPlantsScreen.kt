package com.morales.nectar.screens.plants

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.morales.nectar.DestinationScreen
import com.morales.nectar.composables.CommonImage
import com.morales.nectar.composables.ProgressSpinner
import com.morales.nectar.composables.UserImageCard
import com.morales.nectar.data.remote.requests.PlantData
import com.morales.nectar.navigation.BottomNavigationItem
import com.morales.nectar.navigation.BottomNavigationMenu
import com.morales.nectar.navigation.NavParam
import com.morales.nectar.navigation.navigateTo
import com.morales.nectar.screens.NectarViewModel

data class PostRow(
    var post1: PlantData? = null,
    var post2: PlantData? = null,
    var post3: PlantData? = null,
) {
    fun isFull() = post1 != null && post2 != null && post3 != null

    fun add(post: PlantData) {
        if (post1 == null) {
            post1 = post
        } else if (post2 == null) {
            post2 = post
        } else if (post3 == null) {
            post3 = post
        }
    }
}

@Composable
fun MyPlantsScreen(
    navController: NavController,
    vm: NectarViewModel,
) {
    val userData = vm.userData.value
    val isLoading = vm.isLoading.value
    val postsLoading = vm.refreshPostsProgress.value
    val posts = vm.posts.value
    val numFollowers = vm.numFollowers.value
    val newPostImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val encoded = Uri.encode(uri.toString())
            val route = DestinationScreen.NewPost.createRoute(encoded)
            navController.navigate(route)
        }
    }

    Column {
        Column(modifier = Modifier.weight(1f)) {
            Row {
                ProfileImage(imageUrl = userData?.imageUrl)
                Text(
                    text = "${posts.size}\n plants",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$numFollowers\n following",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${if (userData?.following == null) "0" else userData.following.size}\n followers",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                val usernameDisplay =
                    if (userData?.username == null) "" else "@${userData.username}"
                Text(text = userData?.name ?: "", fontWeight = FontWeight.Bold)
                Text(text = usernameDisplay)
            }
            OutlinedButton(
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent
                ),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                ),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                onClick = { navigateTo(navController, DestinationScreen.Profile) },
                shape = RoundedCornerShape(10)
            ) {
                Text(text = "Edit Profile", color = Color.Black)
            }
            OutlinedButton(
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Blue
                ),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                ),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                onClick = { navigateTo(navController, DestinationScreen.CreateNewPlantScreen) },
                shape = RoundedCornerShape(10)
            ) {
                Text(text = "Add New Plant", color = Color.White)
            }
            PostList(
                isContextLoading = isLoading,
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
                posts = posts,
                postsLoading = postsLoading
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
                .size(80.dp),
            userImage = imageUrl
        )
    }
}

@Composable
fun PostList(
    isContextLoading: Boolean,
    modifier: Modifier,
    onPostClick: (PlantData) -> Unit,
    posts: List<PlantData>,
    postsLoading: Boolean
) {
    if (postsLoading) {
        ProgressSpinner()
    } else if (posts.isEmpty()) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isContextLoading) {
                Text(text = "No plants available")
            }
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
                key = { idx -> rows[idx].post1?.plantId!! },
                itemContent = { idx -> PostsRow(item = rows[idx], onPostClick = onPostClick) }
            )
        }
    }
}

@Composable
fun PostsRow(item: PostRow, onPostClick: (PlantData) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        PostImage(
            imageUrl = item.post1?.images?.get(0),
            modifier = Modifier
                .weight(1f)
                .clickable { item.post1?.let { post -> onPostClick(post) } }
        )
        PostImage(
            imageUrl = item.post2?.images?.get(0),
            modifier = Modifier
                .weight(1f)
                .clickable { item.post2?.let { post -> onPostClick(post) } }
        )
        PostImage(
            imageUrl = item.post3?.images?.get(0),
            modifier = Modifier
                .weight(1f)
                .clickable { item.post3?.let { post -> onPostClick(post) } }
        )
    }
}

@Composable
fun PostImage(imageUrl: String?, modifier: Modifier) {
    Box(modifier = modifier) {
        val imageModifier = if (imageUrl == null) {
            modifier.clickable(enabled = false) {}
        } else {
            Modifier
                .padding(1.dp)
                .fillMaxSize()
        }
        CommonImage(
            data = imageUrl,
            contentDescription = "a plant image",
            modifier = imageModifier
        )
    }
}


