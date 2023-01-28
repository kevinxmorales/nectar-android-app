package com.morales.nectar.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.morales.nectar.DestinationScreen
import com.morales.nectar.R

enum class BottomNavigationItem(val icon: Int, val navDestination: DestinationScreen) {
    POSTS(R.drawable.ic_home, DestinationScreen.MyPosts),
    CARE(R.drawable.ic_care, DestinationScreen.CareLogEntries),
    SEARCH(R.drawable.ic_search, DestinationScreen.Search),
}

@Composable
fun BottomNavigationMenu(selectedItem: BottomNavigationItem, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 4.dp)
            .background(Color.White)
    ) {
        for (item in BottomNavigationItem.values()) {
            Image(
                painter = painterResource(id = item.icon),
                contentDescription = "Navigation Button for ${item.name}",
                modifier = Modifier
                    .size(40.dp)
                    .padding(5.dp)
                    .weight(1f)
                    .clickable { navigateTo(navController, item.navDestination) },
                colorFilter = ColorFilter.tint(
                    if (item == selectedItem) Color.Black
                    else Color.Gray
                )
            )
        }
    }
}