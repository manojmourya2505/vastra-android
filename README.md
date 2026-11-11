# Vastra: Android app

Vastra is a shopping app that lets you see clothes on your own photo before you buy. An AI stylist builds outfits for your occasion and budget, and try-on shows the chosen outfit on you from several angles.

## Screens

1. **Intro.** Three short slides: tell us the occasion, your size, and your bag. A Vastra logo animation plays on launch.
2. **Sign in.** Enter your phone number and a one-time code. The test account uses code 123456.
3. **Your photo.** Pick one full-length photo of yourself. It's stored on the phone and in your profile.
4. **Home.** A promo banner, categories (All, Dresses, Tops, Bottoms, Footwear), and the product grid. Retailer picks appear at the top.
5. **Product detail.** Photo, name, colour, price in pounds, size picker, and buttons to add to bag or try it on.
6. **Planner.** Choose the occasion (Dinner, Office, Weekend, Nightwear), the time (Tonight, Tomorrow, This week) and a budget slider. Tap "Build looks".
7. **Building your looks.** A short loader while the AI stylist works. Three garment cards fan out, and a checklist ticks off each step.
8. **Looks.** Outfit suggestions under your budget, each with a label, its pieces, the total in pounds, and the reason it was picked.
9. **Try on.** Your photo dressed in the chosen piece, with thumbnails of the pieces you're wearing. You can pick tops and bottoms together or a single dress. Views are made from your photo.
10. **Your looks.** A swipeable card stack showing the outfit from three angles: three-quarter, side and back. Drag a card sideways to see the next angle. A bottom button adds the whole outfit to your bag once all views are ready.
11. **Bag.** Items with size, quantity controls, remove, and the total. "Hold to checkout" moves on.
12. **Checkout.** Pick up in store, with the store address and pickup time. "Place order" creates the order.
13. **Order placed.** Confirmation with a 4-digit pickup code and the store.
14. **Orders and profile.** Past orders, your name, phone, photo, size, city and saved addresses.

## How it works

- **Login:** the phone number is stored in Firebase Firestore. Sign-in uses a one-time code.
- **Data:** products, the user profile (name, phone, city, size, photo), the bag and orders are stored in Firestore. The photo is kept on the phone as a copy.
- **AI stylist:** the app sends your budget, occasion, and catalogue items to a small Python server on your computer. The server asks OpenAI for outfit suggestions, which the app shows as looks.
- **Try-on:** the app sends your photo and the chosen pieces to the server, which asks OpenAI's image model to dress you. Each view (three-quarter, side, back) is generated from your photo and the catalogue photos, so the garment stays the same.
- **Budget and currency:** every price is shown in pounds (£).

## Tech stack

- Kotlin, Jetpack Compose and Material 3
- Firebase Firestore (firebase-bom 33.7.0)
- Custom navigation stack, Animatable card stack, and a build-loader overlay
- HTTP calls to the stylist server with `HttpURLConnection`

## Setup

1. Open the project in Android Studio.
2. Add your own `google-services.json` (from your Firebase project) to `app/`. It isn't included in this repo.
3. In `app/src/main/java/com/inouiw/vastra/data/Stylist.kt`, set `BASE_URL` to your computer's local IP, for example `http://192.168.x.x:8001`.
4. Run the stylist server from the companion repo `vastra-stylist-agent`.
5. Build and run on a phone. Allow cleartext HTTP to your computer's address for local testing.

## Notes

- **Retailer photos are not included.** The product photos in `app/src/main/assets/products/` were licensed for education testing only, so they are excluded from this public repo. Products without a photo show a simple garment drawing.
- **Try-on needs the stylist server running.** Each try-on makes several AI image requests, which cost money.
- **Test data.** The test account and OTP 123456 are for development only.
