# Cologne App — Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           COLOGNE RECOMMENDATION APP                        │
│                         Spring Boot 3.2.3 • Java • H2                       │
└─────────────────────────────────────────────────────────────────────────────┘

  Browser
    │  HTTP Requests
    ▼
┌───────────────────────────────────────────────────────────┐
│                    Spring Security Filter                  │
│   • Login/Logout   • Role-based access (USER / ADMIN)     │
│   • Route guards   • Password encoding (BCrypt)           │
└─────────────────────────┬─────────────────────────────────┘
                          │
          ┌───────────────┼────────────────┐
          │               │                │
          ▼               ▼                ▼
  ┌──────────────┐ ┌─────────────┐ ┌──────────────────┐
  │ AuthController│ │CologneCtrl  │ │  AdminController  │
  │              │ │             │ │  (/admin/**)      │
  │ GET  /login  │ │ GET  /      │ │                   │
  │ POST /login  │ │ GET  /add   │ │ User management   │
  │ GET  /register│ │ POST /add  │ │ Fragrance CRUD    │
  │ POST /register│ │ GET /remove│ │ Role toggling     │
  └──────┬───────┘ │ GET /compare│ │ Password reset    │
         │         │ GET /stats  │ └────────┬──────────┘
         │         │ GET /recomm.│           │
         │         └──────┬──────┘           │
         │                │                  │
         └────────────────┼──────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │     FragranceService  │
              │                       │
              │ • seedDataset()       │
              │ • getUserCollection() │
              │ • addToCollection()   │
              │ • removeFromColl.()   │
              │ • getStats()          │
              │ • compareFragrances() │
              └──────────┬────────────┘
                         │
          ┌──────────────┼──────────────┐
          │              │              │
          ▼              ▼              ▼
  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐
  │  Fragrance   │ │  User        │ │  Recommendation      │
  │  Repository  │ │  Repository  │ │  Engine              │
  │  (JPA / H2)  │ │  (JPA / H2)  │ │                      │
  └──────┬───────┘ └──────┬───────┘ │ Scores by:           │
         │                │         │  • Weather (HOT→COLD) │
         │                │         │  • Occasion           │
         │                │         │    (OFFICE/DATE/…)    │
         └────────────────┤         │  • Longevity          │
                          │         │  • Projection         │
                          ▼         └──────────────────────┘
              ┌───────────────────────┐
              │      H2 Database      │
              │   (file: ./data/)     │
              │                       │
              │  ┌─────────────────┐  │
              │  │   fragrance     │  │
              │  │─────────────────│  │
              │  │ id, brand, name │  │
              │  │ projection      │  │
              │  │ longevity       │  │
              │  │ hot/cold score  │  │
              │  │ officeAppropriate│  │
              │  └─────────────────┘  │
              │                       │
              │  ┌─────────────────┐  │
              │  │   app_user      │  │
              │  │─────────────────│  │
              │  │ id, username    │  │
              │  │ password (hash) │  │
              │  │ role            │  │
              │  └─────────────────┘  │
              │                       │
              │  ┌─────────────────┐  │
              │  │ user_collection │  │
              │  │─────────────────│  │
              │  │ user_id (FK)    │  │
              │  │ fragrance_id    │  │
              │  └─────────────────┘  │
              └───────────────────────┘

  Seed data on first startup:
  ┌─────────────────────────────┐
  │  fragrances.json  ──────────┼──► FragranceService.seedDataset()
  │  (resource bundle)          │        │
  └─────────────────────────────┘        └──► Fragrance table (if empty)

  ┌─────────────────────────────────────────────────────────────────────────┐
  │                       Thymeleaf Templates                               │
  │                                                                         │
  │  login.html       register.html    collection.html   add.html           │
  │  recommend.html   compare.html     stats.html                           │
  │  admin.html       admin_fragrances.html  admin_fragrance_form.html      │
  │  fragrance_new.html                                                     │
  └─────────────────────────────────────────────────────────────────────────┘

  Models / DTOs
  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
  │  AppUser     │  │  Fragrance   │  │  Occasion    │  │  Weather     │
  │  (Entity)    │  │  (Entity)    │  │  (Enum)      │  │  (Enum)      │
  │              │  │              │  │              │  │              │
  │ username     │  │ brand        │  │ CASUAL       │  │ HOT          │
  │ password     │  │ name         │  │ OFFICE       │  │ WARM         │
  │ role         │  │ projection   │  │ DATE         │  │ MILD         │
  │ collection   │  │ longevity    │  │ SOCIAL       │  │ COOL         │
  │              │  │ hotScore     │  │ FORMAL       │  │ COLD         │
  └──────────────┘  │ coldScore    │  └──────────────┘  └──────────────┘
                    │ officeOk     │
                    └──────────────┘

  ┌──────────────┐  ┌──────────────────────────────────────────────────┐
  │  UserStats   │  │  RecommendationResult                            │
  │  (DTO)       │  │  (DTO)                                           │
  │              │  │                                                  │
  │ totalOwned   │  │  fragrance     — the scored Fragrance object     │
  │ totalRated   │  │  score         — computed float                  │
  │ topBrands    │  │  weatherScore  — partial score component         │
  │ brandCounts  │  │  occasionScore — partial score component         │
  └──────────────┘  └──────────────────────────────────────────────────┘
```
