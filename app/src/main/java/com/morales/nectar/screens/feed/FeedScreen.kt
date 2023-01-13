package com.morales.nectar.screens.feed

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.morales.nectar.DestinationScreen
import com.morales.nectar.composables.CommonImage
import com.morales.nectar.composables.LikeAnimation
import com.morales.nectar.composables.ProgressSpinner
import com.morales.nectar.data.models.PlantData
import com.morales.nectar.navigation.NavParam
import com.morales.nectar.navigation.navigateTo
import com.morales.nectar.screens.NectarViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    navController: NavController,
    vm: NectarViewModel,
) {
    val userData = vm.userData.value
    val userDataLoading = vm.isLoading.value

    val personalizedFeed = vm.plantsFeed.value
    val personalizedFeedLoading = vm.plantsFeedProgress.value
    /*

    LaunchedEffect(key1 = Unit) {
        vm.getPersonalizedFeed()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
        ) {
        }
        val currentUserId: String = userData?.id!!
        FeedPostList(
            currentUserId = currentUserId,
            loading = personalizedFeedLoading or userDataLoading,
            modifier = Modifier.weight(1f),
            navController = navController,
            posts = personalizedFeed,
            vm = vm
        )
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.FEED,
            navController = navController
        )
    }


     */
}

@Composable
fun FeedPostList(
    currentUserId: String,
    modifier: Modifier,
    loading: Boolean,
    navController: NavController,
    posts: List<PlantData>,
    vm: NectarViewModel
) {
    Box(modifier = modifier) {
        LazyColumn {
            items(items = posts) { post ->
                Post(post = post, currentUserId = currentUserId, vm = vm, onPostClick = {
                    navigateTo(
                        navController,
                        DestinationScreen.SinglePlant,
                        NavParam("plant", post)
                    )
                })
            }
        }
        if (loading) {
            ProgressSpinner()
        }
    }
}

@Composable
fun Post(post: PlantData, currentUserId: String, vm: NectarViewModel, onPostClick: () -> Unit) {

    val likeAnimation = remember { mutableStateOf(false) }
    val dislikeAnimation = remember { mutableStateOf(false) }


    Card(
        shape = RoundedCornerShape(corner = CornerSize(4.dp)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 4.dp, bottom = 4.dp)
            ) {
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp),
                    shape = CircleShape
                ) {
                    CommonImage(
                        data = post.userImage,
                        contentDescription = "a picture of the post's owner",
                        contentScale = ContentScale.Crop
                    )
                }
                Text(text = post.username ?: "", Modifier.padding(4.dp))
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                CommonImage(
                    data = post.images?.get(0),
                    contentDescription = "the image of the plant",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 150.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (post.likes?.contains(currentUserId) == true) {
                                        dislikeAnimation.value = true
                                    } else {
                                        likeAnimation.value = true
                                    }
                                    //vm.onLikePost(post)
                                },
                                onTap = {
                                    onPostClick.invoke()
                                }
                            )
                        }
                )

                if (likeAnimation.value) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000L)
                        likeAnimation.value = false
                    }
                    LikeAnimation()
                }
                if (dislikeAnimation.value) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000L)
                        dislikeAnimation.value = false
                    }
                    LikeAnimation(false)
                }
            }
        }
    }
}