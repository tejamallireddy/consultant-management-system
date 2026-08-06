# 👥 Consultant Management System

A web app for keeping track of consultants — the people a company can assign to projects.

You can add a consultant, see the full list, change their details, remove them, and search for someone by name or by the technology they work with. There's also a dashboard that shows totals at a glance. 📊

---

## 🧰 What It's Built With

| Thing | What it does |
|---|---|
| ☕ Java 21 | The programming language |
| 🍃 Spring Boot 4.1 | The framework that runs the app and the web server |
| 🐬 MySQL | The database where consultant records are stored |
| 🔗 Spring Data JPA | Talks to the database so we don't write raw SQL |
| 🍂 Thymeleaf | Turns data into HTML pages |
| 🎨 Bootstrap 5 | Makes the pages look good and work on phones |
| ✅ JUnit + Mockito | Runs automated tests |
| 📦 Maven | Downloads libraries and builds the project |

---

## 📋 Before You Start

You need two things installed:

- ☕ **Java 21 or newer** — check with `java -version`
- 🐬 **MySQL 8 or newer** — make sure it's running

You do *not* need Maven. The project includes a wrapper (`mvnw`) that handles it. 👍

---

## 🚀 How To Run It

### 1️⃣ Create the database

This one command builds the database, creates the table, and adds 12 sample consultants:

```bash
mysql -u root -p < sql/schema.sql
```

It'll ask for your MySQL password. 🔑

Check it worked:

```bash
mysql -u root -p -e "USE consultant_db; SELECT COUNT(*) FROM consultants;"
```

You should see **12** ✅

### 2️⃣ Tell the app your MySQL password

The password isn't written into any file (that would be unsafe to put on GitHub 🙈). Instead the app reads it from your terminal:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
```

If you skip this, the app assumes both are `root`.

> 💡 *Using IntelliJ instead?* Go to **Run → Edit Configurations → Environment variables** and add them there.

### 3️⃣ Start the app

```bash
./mvnw spring-boot:run
```

Wait for this line:

```
Started ConsultantManagementSystemApplication
```

### 4️⃣ Open it

Go to 🌐 **http://localhost:8080** in your browser.

### 5️⃣ Stop it

Press **Ctrl+C** in the terminal. 🛑

---

## 📄 The Pages

| Page | Address | What you can do |
|---|---|---|
| 📊 Dashboard | `/` | See totals: how many consultants, how many active, how many joined this month |
| 📋 All Consultants | `/consultants` | See the full list, search, move between pages |
| ➕ Add Consultant | `/consultants/new` | Fill in a form to add someone new |
| ✏️ Edit | click **Edit** on any row | Change someone's details |
| 🗑️ Delete | click **Delete** on any row | Confirms first, then removes them |

---

## 🔍 Searching

Type into the search box on the Consultants page and press **Search**.

It looks in **two** places at once — the name and the technology. So typing `Java` finds people called Java (unlikely! 😄) *and* everyone who works with Java. Capital letters don't matter: `java`, `Java`, and `JAVA` all give the same results.

Press **Clear** to see everyone again. 🔄

---

## ✅ The Rules For Each Field

When you submit the form, the app checks everything **on the server** before saving. If something's wrong, the field turns red 🔴 and tells you why.

| Field | Rule |
|---|---|
| 👤 Name | Required, between 2 and 100 letters |
| 📧 Email | Required, must look like an email, and no two consultants can share one |
| 📱 Phone | Required, 10 to 15 digits |
| 💻 Technology | Required, up to 150 characters |
| 📅 Experience | Required, a number from 0 to 50 |
| 🟢 Status | Required, either Active or Inactive |

Try submitting the empty form — every field will complain. Try using an email that already exists — it'll tell you it's taken. 🚫

---

## 🔌 The API (For Other Programs)

As well as web pages, the app offers a **REST API**. That means other programs — a phone app 📱, a script, another website — can read and change the data without using the web pages.

Open these in your browser to try them:

| Address | What you get back |
|---|---|
| `localhost:8080/api/consultants` | The full list as data |
| `localhost:8080/api/consultants?keyword=Java` | Only ones matching "Java" |
| `localhost:8080/api/consultants/1` | Just consultant number 1 |
| `localhost:8080/api/consultants/stats` | The dashboard numbers |

The stats one returns something like:

```json
{"total":12,"active":10,"inactive":2,"newThisMonth":3}
```

There are also endpoints for adding, updating, and deleting. Browsers can only *read* 👀, so those need a tool like Postman or the IntelliJ HTTP Client.

| Action | Method | Address |
|---|---|---|
| ➕ Add | POST | `/api/consultants` |
| ✏️ Update | PUT | `/api/consultants/{id}` |
| 🗑️ Delete | DELETE | `/api/consultants/{id}` |

When something goes wrong, the API explains it clearly:

```json
{
  "status": 400,
  "message": "Validation failed",
  "fieldErrors": {
    "email": "Please enter a valid email address",
    "phone": "Phone must be 10 to 15 digits"
  }
}
```

---

## 🛟 When Things Go Wrong

The app never shows a scary wall of red error text. 😌 Instead:

- 🔎 Ask for a consultant that doesn't exist → a friendly "Not Found" page
- 🧭 Type a nonsense address → a friendly "Page Not Found" page
- ⚠️ Something unexpected breaks → a polite "Something Went Wrong" page

Behind the scenes the full technical details still get written to the log 📝, so a developer can fix it. The visitor just sees something readable.

---

## 🧪 Running The Tests

```bash
./mvnw test
```

You should see:

```
Tests run: 29, Failures: 0, Errors: 0
```

These 29 tests check the app automatically. They use a temporary in-memory database, so **MySQL doesn't even need to be running** ⚡

| Test file | What it checks |
|---|---|
| `ConsultantServiceTest` | The rules — like refusing duplicate emails |
| `ConsultantRepositoryTest` | The database queries actually return the right rows |
| `ConsultantApiControllerTest` | The API returns the right data and the right status codes |

---

## 🏗️ How The Code Is Organised

The code is split into layers, so each part has one job:

```
🌐 Browser
      ↓
🎮 Controller  ← receives the request, decides which page to show
      ↓
🧠 Service     ← the rules: check for duplicates, decide what to save
      ↓
🗄️ Repository  ← talks to the database
      ↓
🐬 MySQL
```

In the folders:

```
src/main/java/com/cms/consultant_management_system/
├── controller/    🎮 the web pages and the API
├── service/       🧠 the business rules
├── repository/    🗄️ database access
├── entity/        📦 what a Consultant looks like
├── dto/           📤 data shapes for the API
└── exception/     🛟 error handling

src/main/resources/
├── application.yaml   ⚙️ settings (database address, port)
└── templates/         🎨 the HTML pages

sql/schema.sql         🐬 creates the database
```

---

## 🗄️ What's In The Database

One table, called `consultants`:

| Column | What it holds |
|---|---|
| id | A unique number for each consultant |
| name | Their name |
| email | Their email — no duplicates allowed |
| phone | Their phone number |
| technology | What they work with, e.g. "Java, Spring Boot" |
| experience | Years of experience |
| status | Active or Inactive |
| created_at | When the record was added |
| updated_at | When it was last changed |

The last two are filled in automatically 🤖 — nobody types them.

---

## 🎁 Extra Features

The assignment asked for any **two** optional extras. This project has **four**:

1. 🔌 **REST API** — other programs can use the data
2. 🧪 **Automated tests** — 29 of them
3. 📑 **Pagination** — the list splits into pages instead of one giant table
4. ↕️ **Sorting** — results can be ordered by any field

