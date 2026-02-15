# HW3's description

For this assignment I implemented adding more foods to the app. The view can be opened by pressing the plus button in the main view. The form contains three fields: name, description and image.

Image can be picked using Android's image picker. I chose `ActivityResultContracts.OpenDocument()` for the contract because it allows selecting images from the filesystem (in addition to the gallery?).
Text input is implemented with `TextField()` components combined with state variables (e.g. `val (name, setName) = remember { mutableStateOf("") }`).

Information is persisted using the Room database. The database has a single table that contains foods listed in the app. The table has 4 columns: uid, name, description and imageUrl.

Images can be added from local files. They are then written into app-specific storage and their URI is stored in the food's database entry.

Most difficult part of this assignment was connecting the Room database to the UI as it required writing multiple layers of abstraction. Or at least that was the approach suggested by Android documentation.

It might have been possible to make accessing the database in Composables using Flows from the DAO directly but I didn't look into that as I already had a working implementation.
