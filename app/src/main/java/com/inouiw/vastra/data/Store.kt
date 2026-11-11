package com.inouiw.vastra.data

import android.content.ContentResolver
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class BagLine(val docId: String, val id: String, val size: String, val qty: Int)
data class Plan(val occ: String = "dinner", val whenLabel: String = "Tonight", val budget: Int = 100)
data class OrderItem(val name: String, val size: String, val qty: Int, val price: Int)
data class Order(
    val code: String,
    val pickup: String,
    val date: String,
    val total: Int,
    val items: List<OrderItem>,
    val createdAt: Long = 0L,
)
data class Profile(val name: String = "", val phone: String = "", val city: String = "", val address: String = "")
data class IntroSlide(val title: String, val copy: String)
data class PromoSlide(val title: String, val copy: String, val button: String, val art: String)

/** Every screen in the app. Mirrors the prototype's HTML pages. */
sealed interface Route {
    data object Splash : Route
    data object Intro : Route
    data object Login : Route
    data object ProfileSetup : Route
    data object Home : Route
    data object Planner : Route
    data object Suggest : Route
    data class TryOn(val itemId: String? = null) : Route
    data object Looks : Route
    data object Bag : Route
    data object Checkout : Route
    data class Placed(val code: String, val pickup: String, val total: Int) : Route
    data object Store : Route
    data object Profile : Route
    data object Orders : Route
}

/**
 * Everything the app shows comes from Firestore:
 *  - products/{productId}                 the dress, top, bottom and footwear catalog
 *  - users/{userId}                       name, phone, city, size, plan, profile photo
 *  - users/{userId}/bag/{productId_size}  bag lines
 *  - users/{userId}/orders/{orderCode}    orders with their items
 * The only thing kept on the device is the signed-in user id and the welcome-seen flag.
 */
class AppStore(private val prefs: SharedPreferences, private val filesDir: File) {
    private val db = FirebaseFirestore.getInstance()
    private var userRef: DocumentReference? = null
    private val listeners = mutableListOf<ListenerRegistration>()

    val bag = mutableStateListOf<BagLine>()
    val orders = mutableStateListOf<Order>()
    var plan by mutableStateOf(Plan())
    var profile by mutableStateOf(Profile())
    var photoPath by mutableStateOf<String?>(null)
    private var sizeState by mutableStateOf("M")
    var introSlides by mutableStateOf<List<IntroSlide>>(emptyList())
        private set
    var introPrompt by mutableStateOf("")
        private set
    var introLooks by mutableStateOf<List<String>>(emptyList())
        private set
    var promos by mutableStateOf<List<PromoSlide>>(emptyList())
        private set
    var ready by mutableStateOf(false)
        private set
    /** Set when Firestore refuses or fails a read, so the splash can say why it is waiting. */
    var loadError by mutableStateOf<String?>(null)
        private set
    private var productsLoaded = false
    private var contentLoaded = false
    val stack = mutableStateListOf<Route>(Route.Splash)

    /** Outfits from the stylist server; null means use the rule-based picks. */
    var stylistOutfits by mutableStateOf<List<Outfit>?>(null)
    /** The outfit the shopper chose for the Looks screen. */
    var lookOutfit by mutableStateOf<Outfit?>(null)

    /** Size picked for the user; saved to their profile in Firestore. */
    var size: String
        get() = sizeState
        set(value) {
            sizeState = value
            userRef?.set(mapOf("size" to value), SetOptions.merge())
        }

    init {
        photoPath = File(filesDir, PHOTO_FILE).takeIf { it.exists() }?.absolutePath
        watchProducts()
        watchContent()
        prefs.getString("userId", null)?.let { attachUser(it) }
    }

    /** Welcome shows once per device. */
    val introSeen: Boolean get() = prefs.getBoolean("introSeen", false)

    val loggedIn: Boolean get() = userRef != null

    fun markIntroSeen() { prefs.edit().putBoolean("introSeen", true).apply() }

    /**
     * Finds the user with this phone in Firestore, or creates one with a unique id.
     * [onResult] runs with true once the user is attached.
     */
    fun login(phone: String, onResult: (Boolean) -> Unit) {
        db.collection("users").whereEqualTo("phone", phone).limit(1).get()
            .addOnSuccessListener { query ->
                val existing = query.documents.firstOrNull()
                val ref = existing?.reference ?: db.collection("users").document()
                val fields = mutableMapOf<String, Any>("phone" to phone)
                if (existing == null) {
                    fields["name"] = "New member"
                    fields["city"] = "Mumbai"
                    fields["size"] = "M"
                    fields["plan"] = planMap(Plan())
                    fields["createdAt"] = System.currentTimeMillis()
                }
                ref.set(fields, SetOptions.merge())
                    .addOnSuccessListener {
                        prefs.edit().putString("userId", ref.id).apply()
                        attachUser(ref.id)
                        onResult(true)
                    }
                    .addOnFailureListener { onResult(false) }
            }
            .addOnFailureListener { onResult(false) }
    }

    val current: Route get() = stack.last()

    // ---- navigation -------------------------------------------------------

    fun push(r: Route) { stack.add(r) }

    fun replace(r: Route) { stack[stack.lastIndex] = r }

    fun reset(r: Route) { stack.clear(); stack.add(r) }

    /** Tab switches start the stack at Home, so Back walks back to Home. */
    fun tab(r: Route) {
        stack.clear()
        if (r != Route.Home) stack.add(Route.Home)
        stack.add(r)
    }

    fun back(): Boolean {
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    // ---- bag ---------------------------------------------------------------

    fun inBag(id: String) = bag.any { it.id == id }

    fun bagCount() = bag.sumOf { it.qty }

    fun bagTotal(): Int = bag.sumOf { line -> (Catalog.byId(line.id)?.price ?: 0) * line.qty }

    fun addToBag(id: String, size: String = this.size) {
        val docId = "${id}_$size"
        val qty = minOf(5, (bag.firstOrNull { it.docId == docId }?.qty ?: 0) + 1)
        bagRef()?.document(docId)?.set(
            mapOf("productId" to id, "size" to size, "qty" to qty, "addedAt" to System.currentTimeMillis()),
            SetOptions.merge(),
        )
    }

    fun removeFromBag(id: String) {
        bag.filter { it.id == id }.forEach { bagRef()?.document(it.docId)?.delete() }
    }

    fun setQty(index: Int, qty: Int) {
        val line = bag.getOrNull(index) ?: return
        bagRef()?.document(line.docId)?.set(mapOf("qty" to qty.coerceIn(1, 5)), SetOptions.merge())
    }

    fun removeAt(index: Int) {
        bag.getOrNull(index)?.let { bagRef()?.document(it.docId)?.delete() }
    }

    // ---- orders ------------------------------------------------------------

    /** Same numbering as the prototype: VST-48213, VST-48214, ... Writes the order and empties the bag. */
    fun placeOrder(): Order {
        val lines = bag.toList()
        val items = lines.mapNotNull { line ->
            Catalog.byId(line.id)?.let { OrderItem(it.name, line.size, line.qty, it.price) }
        }
        val now = System.currentTimeMillis()
        val order = Order(
            code = "VST-${48212 + orders.size + 1}",
            pickup = (1000 + Random.nextInt(9000)).toString(),
            date = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(Date(now)),
            total = bagTotal(),
            items = items,
            createdAt = now,
        )
        userRef?.let { ref ->
            val batch = db.batch()
            batch.set(ref.collection("orders").document(order.code), mapOf(
                "code" to order.code,
                "pickup" to order.pickup,
                "date" to order.date,
                "total" to order.total,
                "createdAt" to now,
                "items" to items.map { mapOf("name" to it.name, "size" to it.size, "qty" to it.qty, "price" to it.price) },
            ))
            // Take the bought pieces out of stock, so suggestions and the catalog stay correct.
            lines.forEach { line ->
                if (Catalog.byId(line.id) != null) {
                    batch.update(db.collection("products").document(line.id), "stock", FieldValue.increment(-line.qty.toLong()))
                }
            }
            lines.forEach { batch.delete(ref.collection("bag").document(it.docId)) }
            batch.commit()
        }
        return order
    }

    // ---- plan & photo ------------------------------------------------------

    fun updatePlan(p: Plan) {
        plan = p
        userRef?.set(mapOf("plan" to planMap(p)), SetOptions.merge())
    }

    /** Scales the picked image to 640px max, uploads it to the user's profile and keeps a local copy of it. */
    fun importPhoto(resolver: ContentResolver, uri: Uri): Boolean {
        val input = resolver.openInputStream(uri) ?: return false
        val raw = input.use { BitmapFactory.decodeStream(it) } ?: return false
        val scale = minOf(1f, 640f / maxOf(raw.width, raw.height))
        val bmp = if (scale < 1f) Bitmap.createScaledBitmap(raw, (raw.width * scale).toInt(), (raw.height * scale).toInt(), true) else raw
        val bytes = ByteArrayOutputStream().use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.toByteArray()
        }
        val file = File(filesDir, PHOTO_FILE)
        FileOutputStream(file).use { it.write(bytes) }
        photoPath = file.absolutePath
        userRef?.set(mapOf("photo" to Base64.encodeToString(bytes, Base64.NO_WRAP)), SetOptions.merge())
        return true
    }

    // ---- Firestore listeners -----------------------------------------------

    private fun watchProducts() {
        db.collection("products").addSnapshotListener { snap, err ->
            if (err != null) { loadError = "Products: ${err.message}"; return@addSnapshotListener }
            if (snap == null) return@addSnapshotListener
            if (snap.isEmpty) {
                // First run: upload the starting catalog once. The listener fires again with the data.
                val batch = db.batch()
                Catalog.seed.forEach { p ->
                    batch.set(db.collection("products").document(p.id), mapOf(
                        "name" to p.name,
                        "category" to p.category,
                        "style" to p.style,
                        "price" to p.price,
                        "colour" to p.colour,
                        "shape" to p.shape,
                        "fill" to p.fill,
                        "stock" to p.stock.toLong(),
                        "photo" to p.photo,
                    ))
                }
                batch.commit()
                return@addSnapshotListener
            }
            // Catalog version 2 (pounds and the photo items): upload the seed once per install.

            if (prefs.getInt("catalogVersion", 0) < 2) {

                val b = db.batch()

                Catalog.seed.forEach { p ->

                    b.set(db.collection("products").document(p.id), mapOf(

                        "name" to p.name, "category" to p.category, "style" to p.style, "price" to p.price,

                        "colour" to p.colour, "shape" to p.shape, "fill" to p.fill, "photo" to p.photo,

                    ), SetOptions.merge())

                }

                b.commit()

                prefs.edit().putInt("catalogVersion", 2).apply()

            }

            Catalog.setAll(snap.documents.mapNotNull { it.toProduct() })
            // Products saved before stock existed get a starting stock of 5.
            val unstocked = snap.documents.filter { it.get("stock") == null }
            if (unstocked.isNotEmpty()) {
                val b = db.batch()
                unstocked.forEach { b.set(it.reference, mapOf("stock" to 5L), SetOptions.merge()) }
                b.commit()
            }
            productsLoaded = true
            updateReady()
        }
    }

    /** Onboarding text, the Lisbon prompt, its look labels and the home promo text, all from the content collection. */
    private fun watchContent() {
        db.collection("content").addSnapshotListener { snap, err ->
            if (err != null) { loadError = "Content: ${err.message}"; return@addSnapshotListener }
            if (snap == null) return@addSnapshotListener
            val docs = snap.documents.associateBy { it.id }
            val complete = docs.containsKey("intro") && docs.containsKey("intro_demo") && docs.containsKey("home")
            if (!complete) {
                if (!snap.metadata.isFromCache) seedContent(docs.keys)
                return@addSnapshotListener
            }
            introSlides = docs.getValue("intro").asList("slides").mapNotNull { raw ->
                (raw as? Map<*, *>)?.let { IntroSlide(it.str("title"), it.str("copy")) }
            }
            val demo = docs.getValue("intro_demo")
            introPrompt = demo.getString("prompt") ?: ""
            introLooks = demo.asList("looks").mapNotNull { it as? String }
            promos = docs.getValue("home").asList("promos").mapNotNull { raw ->
                (raw as? Map<*, *>)?.let { PromoSlide(it.str("title"), it.str("copy"), it.str("button"), it.str("art", "dress1")) }
            }
            contentLoaded = true
            updateReady()
        }
    }

    /** First run: write the starting text so the content collection exists. */
    private fun seedContent(existing: Set<String>) {
        val batch = db.batch()
        if ("intro" !in existing) batch.set(db.collection("content").document("intro"), mapOf("slides" to listOf(
            mapOf("title" to "Tell us the occasion", "copy" to "Describe the event and your budget. Vastra builds complete looks from what's in stock."),
            mapOf("title" to "See it on you first", "copy" to "Try any look on your own avatar before it goes into your bag."),
            mapOf("title" to "Your size, already known", "copy" to "Fit suggestions learn from what has fitted you before. You confirm every bag."),
        )))
        if ("intro_demo" !in existing) batch.set(db.collection("content").document("intro_demo"), mapOf(
            "prompt" to "Wedding in Lisbon, £250, relaxed",
            "looks" to listOf("Look 1 · £182", "Look 2 · £204", "Look 3 · £196"),
        ))
        if ("home" !in existing) batch.set(db.collection("content").document("home"), mapOf("promos" to listOf(
            mapOf("title" to "New collection\nfor every occasion", "copy" to "Fresh pieces for dinners, offices and weekends.", "button" to "Shop dresses", "art" to "dress1"),
            mapOf("title" to "Tops you will\nwear all week", "copy" to "Shirts, tees and blouses in everyday colours.", "button" to "Shop tops", "art" to "top2"),
            mapOf("title" to "Plan your\nnext occasion", "copy" to "Pick an occasion and a budget. We build the looks.", "button" to "Plan now", "art" to "shoe3"),
        )))
        batch.commit()
    }

    private fun updateReady() { ready = productsLoaded && contentLoaded }

    private fun attachUser(uid: String) {
        listeners.forEach { it.remove() }
        listeners.clear()
        val ref = db.collection("users").document(uid)
        userRef = ref
        listeners += ref.addSnapshotListener { snap, _ ->
            if (snap == null || !snap.exists()) return@addSnapshotListener
            val d = snap.data.orEmpty()
            profile = Profile(
                name = d["name"] as? String ?: "",
                phone = d["phone"] as? String ?: "",
                city = d["city"] as? String ?: "",
                address = d["address"] as? String ?: "",
            )
            (d["plan"] as? Map<*, *>)?.let { m ->
                plan = Plan(
                    occ = m["occ"] as? String ?: "dinner",
                    whenLabel = m["when"] as? String ?: "Tonight",
                    budget = (m["budget"] as? Number)?.toInt() ?: 100,
                )
            }
            sizeState = d["size"] as? String ?: sizeState
            (d["photo"] as? String)?.let { writePhoto(it) }
        }
        listeners += ref.collection("bag").orderBy("addedAt").addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            bag.clear()
            snap.documents.forEach { d ->
                bag.add(BagLine(
                    docId = d.id,
                    id = d.getString("productId") ?: return@forEach,
                    size = d.getString("size") ?: "M",
                    qty = (d.getLong("qty") ?: 1L).toInt(),
                ))
            }
        }
        listeners += ref.collection("orders").addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            orders.clear()
            orders.addAll(snap.documents.map { it.toOrder() }.sortedByDescending { it.createdAt })
        }
    }

    private fun bagRef() = userRef?.collection("bag")

    private fun planMap(p: Plan) = mapOf("occ" to p.occ, "when" to p.whenLabel, "budget" to p.budget)

    /** Rewrites the local photo copy only when the Firestore photo changes. */
    private fun writePhoto(b64: String) {
        val sig = b64.hashCode().toString()
        val file = File(filesDir, PHOTO_FILE)
        if (prefs.getString("photoSig", null) == sig && file.exists()) return
        try {
            file.writeBytes(Base64.decode(b64, Base64.DEFAULT))
            prefs.edit().putString("photoSig", sig).apply()
            photoPath = file.absolutePath
        } catch (e: IllegalArgumentException) {
            // Bad base64 in the document: keep the photo we already have.
        }
    }

    private companion object {
        const val PHOTO_FILE = "profile.jpg"
    }
}

private fun DocumentSnapshot.asList(field: String): List<*> = get(field) as? List<*> ?: emptyList<Any>()

private fun Map<*, *>.str(key: String, default: String = ""): String = this[key] as? String ?: default

private fun DocumentSnapshot.toProduct(): Product? {
    val name = getString("name") ?: return null
    return Product(
        id = id,
        category = getString("category") ?: "",
        name = name,
        style = getString("style") ?: "",
        price = (getLong("price") ?: 0L).toInt(),
        colour = getString("colour") ?: "",
        shape = (getLong("shape") ?: 0L).toInt(),
        fill = getLong("fill") ?: 0L,
        stock = (getLong("stock") ?: 0L).toInt(),
        photo = getString("photo") ?: "",
    )
}

private fun DocumentSnapshot.toOrder(): Order {
    val rawItems = get("items") as? List<*> ?: emptyList<Any>()
    return Order(
        code = getString("code") ?: id,
        pickup = getString("pickup") ?: "",
        date = getString("date") ?: "",
        total = (getLong("total") ?: 0L).toInt(),
        items = rawItems.mapNotNull { raw ->
            val m = raw as? Map<*, *> ?: return@mapNotNull null
            OrderItem(
                name = m["name"] as? String ?: "",
                size = m["size"] as? String ?: "",
                qty = (m["qty"] as? Number)?.toInt() ?: 1,
                price = (m["price"] as? Number)?.toInt() ?: 0,
            )
        },
        createdAt = getLong("createdAt") ?: 0L,
    )
}
