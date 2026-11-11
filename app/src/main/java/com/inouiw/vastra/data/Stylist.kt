package com.inouiw.vastra.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.util.Base64
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Talks to the stylist server running on the Mac (vastra-agent). */
object Stylist {
    const val BASE_URL = "http://192.168.0.170:8001"
    private val main = Handler(Looper.getMainLooper())

    /** The shopper's details from the app: size, what they bought before, and what is in the bag. Call on the main thread. */
    fun profileFor(store: AppStore): JSONObject {
        fun dict(p: Product) = JSONObject()
            .put("name", p.name)
            .put("category", p.category)
            .put("colour", p.colour)
            .put("style", p.style)
        val bought = JSONArray()
        store.orders.forEach { o ->
            o.items.forEach { oi -> Catalog.items.firstOrNull { it.name == oi.name }?.let { bought.put(dict(it)) } }
        }
        val bag = JSONArray()
        store.bag.forEach { line -> Catalog.byId(line.id)?.let { bag.put(dict(it)) } }
        return JSONObject()
            .put("size", store.size)
            .put("previouslyBought", bought)
            .put("inBag", bag)
    }

    /** Runs the call in the background; [onResult] gets the outfits on the main thread, or null if it failed. */
    fun suggest(request: String, plan: Plan, items: List<Product>, profile: JSONObject, onResult: (List<Outfit>?) -> Unit) {
        Thread {
            val result = try { call(request, plan, items, profile) } catch (e: Exception) { null }
            main.post { onResult(result) }
        }.start()
    }

    /** Reads the catalog photos of [items] from the app's assets. Items without a photo are skipped. */
    fun garmentBytes(context: Context, items: List<Product>): List<ByteArray> =
        items.mapNotNull { p ->
            if (p.photo.isEmpty()) null
            else try { context.assets.open(p.photo).use { it.readBytes() } } catch (e: Exception) { null }
        }

    /** If a view is blocked, the other turn of the same pose is tried (three-quarter <-> back three-quarter). */
    private val altAngle = mapOf(
        "three-quarter" to "three-quarter-right",
    )

    private fun attempt(photo: ByteArray, garments: List<ByteArray>, outfit: List<String>, angle: String, reference: ByteArray?): Pair<Bitmap, ByteArray>? {
        val first = try { callTryOn(photo, garments, outfit, angle, reference) } catch (e: Exception) { null }
        if (first != null) return first
        return try { callTryOn(photo, garments, outfit, altAngle[angle] ?: angle, reference) } catch (e: Exception) { null }
    }

    /**
     * Dresses the shopper's photo for every angle. The first angle is made from the photo and the garment pictures;
     * the other angles are then made from that first picture too, so the outfit stays the same in every view.
     * A blocked view is retried on the other turn. [onAngle] gets each picture on the main thread, or null.
     */
    fun tryOnSet(photo: ByteArray, garments: List<ByteArray>, outfit: List<String>, angles: List<String>, onAngle: (String, Bitmap?) -> Unit) {
        val first = angles.firstOrNull() ?: return
        Thread {
            val anchor = attempt(photo, garments, outfit, first, null)
            main.post { onAngle(first, anchor?.first) }
            angles.drop(1).forEach { angle ->
                Thread {
                    val picture = attempt(photo, garments, outfit, angle, null)
                    main.post { onAngle(angle, picture?.first) }
                }.start()
            }
        }.start()
    }

    private fun b64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    /** Returns the picture and its raw bytes, or null if the server could not make it. */
    private fun callTryOn(photo: ByteArray, garments: List<ByteArray>, outfit: List<String>, angle: String, reference: ByteArray?): Pair<Bitmap, ByteArray>? {
        val body = JSONObject()
            .put("photo", b64(photo))
            .put("garments", JSONArray().apply { garments.forEach { put(b64(it)) } })
            .put("outfit", JSONArray(outfit))
            .put("angle", angle)
        if (reference != null) body.put("reference", b64(reference))
        val conn = (URL("$BASE_URL/tryon").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 5000
            readTimeout = 240000
            doOutput = true
        }
        conn.outputStream.use { it.write(body.toString().toByteArray()) }
        if (conn.responseCode != 200) return null
        val image = Base64.decode(JSONObject(conn.inputStream.bufferedReader().use { it.readText() }).getString("image"), Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(image, 0, image.size) ?: return null
        return bmp to image
    }

    private fun call(request: String, plan: Plan, items: List<Product>, profile: JSONObject): List<Outfit>? {
        val body = JSONObject()
            .put("request", request)
            .put("profile", profile)
            .put("occasion", plan.occ)
            .put("budget", plan.budget)
            .put("whenLabel", plan.whenLabel)
            .put("items", JSONArray().apply {
                items.forEach { p ->
                    put(JSONObject()
                        .put("id", p.id)
                        .put("name", p.name)
                        .put("category", p.category)
                        .put("style", p.style)
                        .put("colour", p.colour)
                        .put("price", p.price)
                        .put("stock", p.stock))
                }
            })

        val conn = (URL("$BASE_URL/suggest").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 5000
            readTimeout = 60000
            doOutput = true
        }
        conn.outputStream.use { it.write(body.toString().toByteArray()) }
        if (conn.responseCode != 200) return null

        val arr = JSONObject(conn.inputStream.bufferedReader().use { it.readText() }).getJSONArray("outfits")
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.getJSONObject(i)
            val ids = o.getJSONArray("itemIds")
            val pieces = (0 until ids.length()).mapNotNull { j -> items.firstOrNull { it.id == ids.getString(j) } }
            if (pieces.isEmpty()) null else Outfit(o.getString("label"), pieces, o.optString("reason"))
        }
    }
}
