# HW1's description

I added image using the builtin Image composable. Idea for the application came up from a discussion with friend where people can share what foods they have made and you can find inspiration for what to eat. I wanted to create a TikTok-like interface where an image of a food is displayed on the entire screen. For this assignment I implemented the fullscreen display where the food is displayed at the top of the screen and at the bottom there's its title and a text body with details.

The title and the text body are displayed with different fonts by using different font sizes (e.g. `Text(fontSize=8.em)`) and font styles (e.g. `Text(style=MaterialTheme.typography.titleLarge)`).

I made the content scrollable by adding `Modifier.verticalScroll` to a parent Composable which contained the pages components.

Registering user's clicks had a similiar approach where adding a `Modifier.clickable` made it possible to modify a state variable (`remember {mutableStateOf()}`). Using the state variable makes it possible to rerender the page.
