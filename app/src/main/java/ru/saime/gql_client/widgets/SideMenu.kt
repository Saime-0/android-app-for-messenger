package ru.saime.gql_client.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import ru.saime.gql_client.*
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.cache.Employee
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.screens.TextLargeProfile
import ru.saime.gql_client.utils.getAppVersion

@Composable
fun SideMenu(
	backend: Backend,
	scaffoldState: ScaffoldState,
	scope: CoroutineScope,
	modifier: Modifier = Modifier,
) {
	val navigate = CategoryNavigate(backend, scaffoldState, scope)

//	navigate.DockCategory(Screen.Guide.name, Screen.Guide.routeRef)
//	navigate.DockCategory(Screen.Rooms.name, Screen.Rooms.routeRef)
//	navigate.DockCategory(Screen.Profile().name, Screen.Profile(Cache.Me.ID).routeWithArgs)
//	Column(
//
//	) {
//
//	}
//ShowProfileV2(empID = Cache.Me.ID, backend = backend)
	Column {
		MenuProfile(backend, Modifier.weight(1f))
//		MenuItem(Icons.Outlined.ExitToApp, "Выход", modifier = Modifier.padding(6.dp).align(Alignment.CenterHorizontally))
		TextNightlyBuild(getAppVersion(backend.activity), Modifier.align(Alignment.CenterHorizontally))
	}
}

@Composable
fun TextNightlyBuild(version: String, modifier: Modifier=Modifier) {
	Text(
		"Nightly Build $version",
		modifier = modifier
			.padding(5.dp)
			.graphicsLayer(alpha = 0.99f)
			.drawWithCache {
				val brush = Brush.verticalGradient(
					colors = listOf(
//						Color(0xFFFFFFFF), Color(0xFF4878F1), Color(0xFFE90303)
						Color(0xFF4150D8), Color(0xFF656BF1)
					)
				)
				onDrawWithContent {
					drawContent()
					drawRect(brush, blendMode = BlendMode.SrcAtop)
				}
			}
	)
}

fun Modifier.textBrush(brush: Brush) = this
	.graphicsLayer(alpha = 0.99f)
	.drawWithCache {
		onDrawWithContent {
			drawContent()
			drawRect(brush, blendMode = BlendMode.SrcAtop)
		}
	}
@Composable
fun MenuProfile(backend: Backend, modifier: Modifier=Modifier) {
	val me by remember<Employee> {
		Cache.Data.employees[Cache.Me.ID]!!
	}

	Column(
		modifier = modifier.padding(start = 15.dp),
		verticalArrangement = Arrangement.spacedBy(10.dp)
	) {
		Row(
			modifier = Modifier
				.padding(vertical = 20.dp, horizontal = 20.dp)
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(10.dp),
				modifier = Modifier
					.padding(top = 20.dp)
					.clickable {
						backend.mainNavController.navigate(Screen.Profile(me.empID).routeWithArgs)
					}
			) {
					Photo(
						me.photo.value,
						Modifier
							.size(100.dp)
							.clip(RoundedCornerShape(10.dp))
//					.padding(10.dp)
					)
					TextLargeProfile(text = me.firstName, color = Color.White)
			}
			Spacer(modifier = Modifier.weight(1f))
			IconButton(onClick = {
				Cache.Me.NotificationsEnable = !Cache.Me.NotificationsEnable
				backend.pref.edit(true) {
					putBoolean(PrefNotificationEnable, Cache.Me.NotificationsEnable)
				}
			}) {
				Icon(
					Icons.Default.Notifications,
					null,
					tint = if (Cache.Me.NotificationsEnable) EnableNotifyCC else DisableNotifyCC
				)
			}
		}
		Divider2CC(Modifier.padding(horizontal = 20.dp), MessageMeBackgroundCC)
		// menu items
		Column(
			Modifier
				.padding(23.dp),
			verticalArrangement = Arrangement.spacedBy(23.dp)
		) {
			MenuItem(Icons.Outlined.Settings, "Настройки")
			MenuItem(Icons.Outlined.Search, "Поиск")
			MenuItem(Icons.Outlined.List, "Журнал изменений")
//			DividerV2CC(Modifier.padding(horizontal = 20.dp), MessageMeBackgroundCC)
//			Spacer(modifier = Modifier.height(15.dp))

		}
	}
}

@Composable
fun MenuItem(
	imageVector: ImageVector,
	text: String,
	modifier: Modifier = Modifier,
	color: Color = MarkedMessageBackgroundCC,
) {
	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(23.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Icon(imageVector, null, Modifier.size(28.dp), color)
		TextMenuItem(text, Color.White)
	}

}

@Composable
fun TextMenuItem(
	text: String,
	color: Color = MainTextCC,
	fontSize: TextUnit = 16.sp,
) {
	Text(
		text = text,
		color = color,
		fontSize = fontSize,
		fontWeight = FontWeight.Light,
	)
}

