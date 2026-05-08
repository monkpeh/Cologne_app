# Cologne Advisor

A web-based fragrance recommendation and collection management app. Users can browse a catalogue of premium colognes, build a personal collection, rate fragrances, get smart recommendations based on the weather and occasion, compare fragrances side by side, and submit new fragrances to the shared catalogue.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Default Login](#default-login)
- [Features](#features)
  - [Registration & Login](#registration--login)
  - [Fragrance Catalogue](#fragrance-catalogue)
  - [My Collection](#my-collection)
  - [Recommendations](#recommendations)
  - [Stats Dashboard](#stats-dashboard)
  - [Side-by-Side Comparison](#side-by-side-comparison)
  - [Submit a Fragrance](#submit-a-fragrance)
  - [Admin Panel](#admin-panel)
- [Project Structure](#project-structure)
- [Data & Persistence](#data--persistence)
- [User Roles](#user-roles)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.3 |
| Security | Spring Security 6 (BCrypt) |
| Database | H2 (file-based, persists across restarts) |
| ORM | Spring Data JPA / Hibernate |
| Templates | Thymeleaf |
| Frontend | Bootstrap 5.3.2 + Bootstrap Icons |
| Build | Maven |

---

## Getting Started

**Prerequisites:** Java 17+, Maven 3.8+

```bash
# Clone the repository
git clone https://github.com/monkpeh/cologne_app.git
cd cologne_app

# Run the application
cd web
mvn spring-boot:run
```

Then open your browser and go to: **http://localhost:8080**

The app creates the database automatically on first startup and seeds it with 30 pre-loaded fragrances from `web/src/main/resources/fragrances.json`.

---

## Default Login

An admin account is created automatically on first startup:

| Field | Value |
|---|---|
| Username | `admin` |
| Password | `admin123` |
This User has all fragrances already in their collection

You can change these in `web/src/main/resources/application.properties`:

```properties
app.admin.username=admin
app.admin.password=admin123
```

---

## Features

### Registration & Login

Any visitor can register a new account at `/register`. Usernames must be unique and passwords must be at least 6 characters. 

Once logged in, users stay authenticated for the duration of their session. Sign out via the button in the top-right of the navbar.

---

### Fragrance Catalogue

Accessible via the **Add** page in the navbar. Shows all 30 pre-loaded fragrances (plus any submitted by users) with:

- Brand, name, scent family, projection and longevity ratings
- Community average star rating
- Short description
- A search bar to filter by brand, name, or scent family

Click **Add to Collection** on any fragrance card to add it to your personal collection. Fragrances already in your collection show a disabled green "In Collection" button.

---

### My Collection

Accessible via the **Collection** page in the navbar. Shows all fragrances you have added, with tools to:

**Sort** your collection by:
- Name (default)
- Brand
- Projection (highest first)
- Longevity (highest first)
- Scent Family

**Filter** your collection by:
- All (default)
- Office Safe only
- Casual / Evening only

**Rate** each fragrance with 1–5 stars. Ratings influence your personal recommendations (highly rated fragrances get a score boost; low-rated ones get a slight penalty). Click the star again or the ✕ to clear a rating.

**Remove** any fragrance from your collection with the Remove button.

**You Might Also Like** — a section at the bottom suggests up to 5 uncollected fragrances from the same scent families as your collection, ranked by how closely their season profile matches your existing fragrances.

---

### Recommendations

Accessible via the **Recommend** page in the navbar. Select your current weather and occasion, then click **Get Recommendation** to receive up to 3 ranked suggestions from your collection.

**Weather options:** Hot, Warm, Mild, Cool, Cold

**Occasion options:** Casual, Office, Date, Social, Formal

Each recommendation shows:
- A match score (0–100%) with a progress bar
- Plain-English reasons explaining why that fragrance was chosen
- A rank badge (gold / silver / bronze)

The scoring algorithm weighs:
- **40%** weather suitability
- **40%** occasion suitability (e.g. office heavily penalises high-projection fragrances; date night rewards them)
- **20%** longevity as a tiebreaker

Your personal star ratings are blended in at 15% weight, so fragrances you love rise slightly and ones you dislike fall.

> You need at least one fragrance in your collection to use this feature.

---

### Stats Dashboard

Accessible via the **Stats** link in the navbar. Displays a personal breakdown of your collection:

| Stat | Description |
|---|---|
| Fragrances Owned | Total count in your collection |
| Rated / Owned | How many you have personally rated vs. total owned |
| Avg Personal Rating | Average of all your star ratings |
| Office / Casual | Count of office-safe vs. casual fragrances |
| Scent Family Breakdown | Bar chart showing how many fragrances you own per scent family |
| Most Projecting | The fragrance in your collection with the highest projection score |
| Longest Lasting | The fragrance in your collection with the highest longevity score |
| Top-Rated | Grid of all fragrances you have rated 4 or 5 stars |

---

### Side-by-Side Comparison

Available from the **Collection** page. Each fragrance card has a checkbox in its top-right corner. Check 2 or 3 fragrances, then click the **Compare Selected** button that appears in the bottom-right corner of the screen.

The comparison page shows a table with one column per fragrance, covering:

- Scent Family
- Projection (stars + progress bar)
- Longevity (stars + progress bar)
- Hot Weather suitability (score out of 10)
- Cold Weather suitability (score out of 10)
- Occasion (Office Safe or Casual)
- Your personal rating (or "Not rated")

Full descriptions for each fragrance are shown below the table.

> Maximum 3 fragrances can be compared at once.

---

### Submit a Fragrance

Accessible via the **Submit a Fragrance** button on the Add page (top-right of the page header). Any logged-in user can add a brand-new fragrance to the shared catalogue — not just admins.

**Fields:**

| Field | Required | Description |
|---|---|---|
| Brand | Yes | Manufacturer or fashion house (e.g. Dior) |
| Name | Yes | Commercial fragrance name (e.g. Sauvage) |
| Scent Family | Yes | Olfactive category (e.g. Fresh / Spicy, Woody / Aromatic) |
| Projection | Yes | 1 (skin-close) to 5 (beast mode) |
| Longevity | Yes | 1 (2–3 hours) to 5 (12+ hours) |
| Hot Weather | Yes | 0 (avoid in summer) to 10 (perfect for summer) |
| Cold Weather | Yes | 0 (avoid in winter) to 10 (perfect for winter) |
| Office Safe | No | Toggle on if appropriate for a professional environment |
| Description | No | Short description of the scent and notable notes |
| Bottle Image URL | No | Direct link to a bottle image (right-click any online image → Copy image address) |

On submission the fragrance is:
1. Saved to the database
2. Added to your personal collection automatically
3. Written to `fragrances.json` so it persists if the database is ever reset

---

### Admin Panel

Accessible via the **Admin** link in the navbar (visible to admin users only).

#### User Management (`/admin`)

| Action | Description |
|---|---|
| View all users | See username, role, and account details for every registered account |
| Reset password | Set a new password for any user (minimum 6 characters) |
| Toggle role | Promote a USER to ADMIN or demote an ADMIN to USER |
| Delete account | Permanently remove a user account |

> Admins cannot delete their own account or change their own role, preventing accidental lockout.

#### Fragrance Management (`/admin/fragrances`)

| Action | Description |
|---|---|
| View catalogue | See all fragrances in a table with thumbnails and quick stats |
| Add fragrance | Add a new fragrance with all fields (same fields as user submission) |
| Edit fragrance | Modify any field on an existing fragrance |
| Delete fragrance | Remove a fragrance from the catalogue — automatically removes it from all users' collections and ratings |

All catalogue changes (add, edit, delete) are automatically written back to `fragrances.json`.

---

## Project Structure

```
cologne_app/
├── data/
│   └── cologne-app.mv.db          # H2 database file (auto-created on first run)
└── web/
    ├── pom.xml
    └── src/main/
        ├── java/com/example/colognerecommendation/
        │   ├── CologneWebApp.java              # Application entry point
        │   ├── config/
        │   │   ├── SecurityConfig.java         # Auth rules, BCrypt, login/logout
        │   │   └── DataInitializer.java        # Seeds default admin account
        │   ├── controller/
        │   │   ├── AuthController.java         # /login, /register
        │   │   ├── CologneController.java      # All user-facing pages
        │   │   └── AdminController.java        # /admin/** pages
        │   ├── engine/
        │   │   ├── RecommendationEngine.java   # Scoring algorithm
        │   │   └── RecommendationResult.java   # Result data class
        │   ├── model/
        │   │   ├── Fragrance.java              # Fragrance entity
        │   │   ├── AppUser.java                # User entity (collection + ratings)
        │   │   ├── UserStats.java              # Stats view model
        │   │   ├── Weather.java                # Enum: HOT/WARM/MILD/COOL/COLD
        │   │   └── Occasion.java              # Enum: CASUAL/OFFICE/DATE/SOCIAL/FORMAL
        │   ├── repository/
        │   │   ├── FragranceRepository.java
        │   │   └── UserRepository.java
        │   └── service/
        │       └── FragranceService.java       # All business logic + JSON sync
        └── resources/
            ├── application.properties
            ├── fragrances.json                 # Seed data (30 fragrances, auto-updated)
            └── templates/
                ├── login.html
                ├── register.html
                ├── collection.html
                ├── add.html
                ├── recommend.html
                ├── stats.html
                ├── compare.html
                ├── fragrance-new.html
                ├── admin.html
                ├── admin-fragrances.html
                └── admin-fragrance-form.html
```

---

## Data & Persistence

**Database:** H2 file-based database stored at `data/cologne-app.mv.db`. Data survives application restarts. The schema is managed automatically by Hibernate (`ddl-auto=update`).

**Seed data:** On first startup (empty database only), the app loads 30 fragrances from `web/src/main/resources/fragrances.json`. This file is also updated automatically whenever any fragrance is added, edited, or deleted — so if the database is ever deleted, restarting the app will restore the full up-to-date catalogue.

**Passwords:** All passwords are hashed with BCrypt before being stored. Plain-text passwords are never saved anywhere.

**To reset the database:** Stop the app, delete `data/cologne-app.mv.db`, and restart. The app will re-seed from `fragrances.json` and recreate the default admin account.

---

## User Roles

| Role | Permissions |
|---|---|
| **USER** | Browse catalogue, manage own collection, rate fragrances, get recommendations, view stats, compare fragrances, submit new fragrances |
| **ADMIN** | Everything a USER can do, plus: manage all user accounts, add/edit/delete any fragrance in the catalogue |
**There's already a Admin user created at runtime,
