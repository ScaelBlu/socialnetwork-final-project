# Social Network API

## Leírás

Ebben a projektben egy egyszerű háromrétegű Spring Boot alapú RESTful webszolgáltatás került megvalósításra, amely
egy - az Instagramhoz hasonló - képmegosztó közösségi médiát reprezentál. Ehhez az alapvető CRUD műveletek
mellett szükség volt a felhasználókat egymással összekapcsoló belső "több a többhöz" kapcsolódást is
implementálni, valamint a felhasználók bejegyzéseihez fűzött képfájlok feltöltését, tárolását, és lekérését
megvalósítani relációs adatbázisban Spring Data JPA használatával.

---

## Felépítés

### User

Az alkalmazás alapvető egységei a `User` entitáspéldányok, amelyek a következő attribútumokkal rendelkeznek:

* `Long` id - a felhasználó egyedi azonosítója, amely az adatbázis szintjén kerül kiosztásra
* `String` username - min. 5, max. 31 karakterből álló egyedi felhasználónév
* `String` password - min. 8 karakterből álló tetszőleges jelszó
* `String` email - megfelelő formátumú egyedi e-mail cím (egy account/e-mail cím)
* `PersonalData` personalData - a felhasználó személyes adatai
* `LocalDateTime` registrationTime - a regisztráció pontos ideje, amely automatikusan mentésre kerül a regisztráció során


Végpontok:

| HTTP metódus | Végpont                         | Leírás                                               |
|--------------|---------------------------------|------------------------------------------------------|
| POST         | `/api/users`                    | Új felhasználó létrehozása                           |
| GET          | `/api/users`                    | Felhasználók keresése opcionális paraméterek alapján |
| GET          | `/api/users/{userId}`           | Felhasználó lekérése ID alapján                      |
| PUT          | `/api/users/{userId}`           | E-mail cím és jelszó módosítása                      |
| DELETE       | `/api/users/{userId}`           | Felhasználó törlése                                  |
| PUT          | `/api/users/{userId}/personal`  | Személyes adatok módosítása                          |

A regisztráció során a felhasználó jelszava SHA3-256 hash algoritmussal emésztett értéke kerül az adatbázisba.
Fontos, hogy a felhasználónév, a jelszó, és az e-mail cím validálása mellett a felhasználónévnek és e-mail címnek
egyedinek kell lennie az adatbázisban. A három közül egyik értéke sem lehet `NULL`, üres, vagy kizárólag whitespace
karakter.

#### PersonalData

A User entitás beágyazottan, de adatbázisban másodlagos táblába kiszervezve tartalmazza a felhasználók személyes adatait,
amelyek már regisztráció során létrejönnek kezdetni `NULL` értékekkel. Ezeket később módosítani lehet.

Attribútumai a következők:

* `String` realName - a felhasználó valódi neve
* `LocalDate` dateOfBirth - a felhasználó születési dátuma
* `String` city - tartózkodási hely (a város neve)

A születési dátumnak kizárólag múltbéli dátum adható meg.


#### Kapcsolatok

A kapcsolatok nem különálló entitások, még csak nem is objektumok, hanem a felhasználók közötti "több a többhöz" reláció.
A felhasználók képesek kapcsolatokat kialakítani egymással, ami megjelenik mindkét félnél. Mivel a kezelésük logikailag
elkülönül a felhasználókon végzett műveletektől, ezért a végpontok külön kontrollerbe lettek kiszervezve. Az idempotenciát
halmazok biztosítják a `User` entitásban, tehát egy kapcsolat két felhasználó között kizárólag egyszer hozható létre.
Ehhez csak az azonosítójukra van szükség.

Végpontok:

| HTTP metódus | Végpont                          | Leírás                                     |
|--------------|----------------------------------|--------------------------------------------|
| PUT          | `/api/users/{userId}/{friendId}` | Új kapcsolat kialakítása                   |
| DELETE       | `/api/users/{userId}/{friendId}` | Meglévő kapcsolat törlése                  |
| GET          | `/api/users/{userId}/friends`    | Adott felhasználó kapcsolatainak listázása |

----

### Post

A felhasználók bejegyzéseit a `Post` entitás reprezentálja, ami kétirányú "több az egyhez" kapcsolatban áll a `User`
entitással. A bejegyzések a következő attribútumokat hordozzák:

* `Long` id - a bejegyzés azonosítója, ami az adatbázisban kap értéket
* `String` title - a bejegyzés címe
* `String` description - a bejegyzéshez fűzhető hosszabb leírás (opcionális)
* `PostFile` postFile - a feltöltött fájl és annak adatai
* `LocalDateTime` postedOn - a bejegyzés közzétételének ideje
* `User` user - a bejegyzés birtokosa (inverse oldal)

Végpontok:

| HTTP metódus | Végpont                       | Leírás                                                               |
|--------------|-------------------------------|----------------------------------------------------------------------|
| POST         | `/api/posts`                  | Új bejegyzés létrehozása                                             |
| GET          | `/api/posts/{postId}`         | Egy bejegyzés adatainak lekérése ID alapján                          |
| DELETE       | `/api/posts/{postId}`         | Adott bejegyzés törlése                                              |
| GET          | `/api/posts/{postId}/content` | A feltöltött fájl betöltése 'inline' elrendezésben                   |
| GET          | `/api/posts/userId=1`         | Az ismerősök posztjainak betöltése időrendben a legújabbaktól kezdve |

A cím nem lehet `NULL`, üres, vagy kizárólag whitespace karakter, azonban a leírás megadása nem kötelező. A közzététel során
validáció történik a tartalom típusára és a fájl kiterjesztésére is. Kizárólag .jpeg/.jpg és .png formátumok engedélyezettek.
Mentéskor a közzététel időpontja automatikusan értéket kap, és az új objektum hozzárendelődik egy létező felhasználóhoz.

#### PostFile

A képtartalom szintén beágyazott objektumként jelenik meg külön másodlagos táblába szervezve. A feltöltött bináris adat mellett
eltárolásra kerül a fájl eredeti neve, és a tartalom MIME típusa is az alábbi attribútumokban:

* `String` filename - az eredeti fájlnév
* `String` mimeType - a kérés MIME típusa
* `byte[]` content - a fájl bináris tartalma

---

## Technológiai részletek

Az alkalmazás a háromrétegű Spring Boot RESTful webszolgáltatások architektúráját követi: 3 controller osztályra, 3 service
osztályra, és 2 repository interfészte tagolódik. Utóbbiak a Spring Data JPA JpaRepository interfészét terjesztik ki, és
a perzisztenciát biztosító MariaDB relációs adatbázisban végzett műveletekért felelősek. Az adatbázistáblák létrehozásáért
Flyway migrációs eszköz a felelős. Az alkalmazásból Docker image készíthető a projekt gyökerében lévő Dockerfile alapján,
így lehetséges önálló konténerben is futtatni.

Az alkalmazás OpenAPI v3.0.1 dokumentációval rendelkezik. A futásidejű manuális tesztelésre a Swagger felület ad lehetőséget a
`/swagger-ui/index.html` útvonalon.

---